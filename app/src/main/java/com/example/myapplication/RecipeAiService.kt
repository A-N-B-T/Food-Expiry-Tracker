package com.example.myapplication

// Search guide for AI recipe behavior:
// SEARCH: AI_MAIN_REQUEST_FLOW
// SEARCH: AI_PROMPT_RULES
// SEARCH: AI_PANTRY_PROMPT_RULES
// SEARCH: AI_RESPONSE_FILTERS

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.util.Locale

private const val TAG = "RecipeAiService"
private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
private val GEMINI_MODEL_FALLBACKS = listOf(
    "gemini-2.5-flash-lite",
    "gemini-3.1-flash-lite-preview"
)
private const val GEMINI_TIMEOUT_MS = 30000
private const val GEMINI_RETRIES_BEFORE_FALLBACK = 2
private const val GEMINI_INITIAL_BACKOFF_MS = 700L
private const val GEMINI_MAX_BACKOFF_MS = 2800L
private const val DEFAULT_RECIPE_LIMIT = 3
private const val MIN_RECIPE_LIMIT = 1
private const val MAX_RECIPE_LIMIT = 3
private const val MAX_RECIPE_STEPS = 5
private const val MAX_PANTRY_INGREDIENT_CONTEXT = 80
private const val MAX_DIRECT_INGREDIENT_CONTEXT = 14
private val KNOWN_FOOD_HINT_TOKENS = setOf(
    "apple", "avocado", "banana", "bean", "beans", "beef", "bread", "broccoli",
    "butter", "cabbage", "carrot", "cheese", "chicken", "chili", "corn",
    "cucumber", "egg", "eggs", "fish", "flour", "garlic", "grape", "ham",
    "honey", "lettuce", "lemon", "lime", "mango", "meat", "milk", "mushroom",
    "noodle", "noodles", "oat", "oats", "oil", "onion", "orange", "pasta",
    "peanut", "pepper", "potato", "potatoes", "rice", "salmon", "salt",
    "sausage", "shrimp", "spinach", "sugar", "tomato", "tomatoes", "tuna",
    "turkey", "vegetable", "vegetables", "veggie", "veggies", "yogurt"
)
private val KNOWN_FOOD_HINT_PHRASES = listOf(
    "bell pepper",
    "olive oil",
    "peanut butter",
    "soy sauce",
    "green onion",
    "spring onion",
    "coconut milk",
    "tomato sauce"
)

internal fun wantsMoreRecipeIdeas(request: String): Boolean {
    val normalized = request
        .trim()
        .lowercase(Locale.US)
        .replace(Regex("\\s+"), " ")

    if (normalized.isBlank()) return false

    val tokens = normalized
        .split(Regex("[^a-z0-9]+"))
        .filter { it.isNotBlank() }

    val compact = tokens.joinToString("")
    val hasMoreIntent = tokens.any { token ->
        token in setOf("more", "another", "again", "next", "extra", "same", "previous", "those","same","similar")
    } || compact.contains("somemore") ||
            compact.contains("givemore") ||
            compact.contains("needmore") ||
            compact.contains("wantmore") ||
            compact.contains("anotherone")

    if (!hasMoreIntent) return false

    val hasRecipeFollowUpTone = tokens.any { token ->
        token in setOf(
            "recipe", "recipes", "idea", "ideas", "meal", "meals",
            "give", "make", "show", "suggest", "need", "want", "please"
        )
    } || compact.contains("canyou") ||
            compact.contains("couldyou") ||
            compact.contains("wouldyou")

    return hasRecipeFollowUpTone || tokens.size <= 4
}

internal fun looksLikeIngredientEditFollowUp(
    request: String,
    pantryIngredients: List<String>,
    previousIngredients: List<String>
): Boolean {
    val normalized = request
        .trim()
        .lowercase(Locale.US)
        .replace(Regex("\\s+"), " ")

    if (normalized.isBlank() || previousIngredients.isEmpty()) return false

    val tokens = normalized
        .split(Regex("[^a-z0-9]+"))
        .filter { it.isNotBlank() }
    val referenceTokens =
        (pantryIngredients + previousIngredients)
            .flatMap { ingredient ->
                ingredient
                    .lowercase(Locale.US)
                    .split(Regex("[^a-z0-9]+"))
                    .map(::normalizeFollowUpMatchToken)
            }
            .filter { it.length >= 3 }
            .toSet()
    val ingredientMentions = tokens.filter { token ->
        val normalizedToken = normalizeFollowUpMatchToken(token)
        normalizedToken in KNOWN_FOOD_HINT_TOKENS || normalizedToken in referenceTokens
    }

    if (ingredientMentions.isEmpty()) return false

    val hasFollowUpCue = hasReferentialIngredientEditCue(normalized, tokens)
    val isShortIngredientReply = tokens.size <= 4
    val isExplicitFreshRecipeRequest =
        ingredientMentions.isNotEmpty() &&
            looksLikeFreshRecipeIngredientRequest(normalized, tokens)

    if (isExplicitFreshRecipeRequest && !hasFollowUpCue) return false

    return hasFollowUpCue || isShortIngredientReply
}

private fun normalizeFollowUpMatchToken(token: String): String {
    return when {
        token.endsWith("ies") && token.length > 4 -> token.dropLast(3) + "y"
        token.endsWith("oes") && token.length > 4 -> token.dropLast(2)
        token.endsWith("es") && token.length > 5 -> token.dropLast(2)
        token.endsWith("s") && token.length > 4 -> token.dropLast(1)
        else -> token
    }
}

private fun hasReferentialIngredientEditCue(
    normalized: String,
    tokens: List<String> = normalized
        .split(Regex("[^a-z0-9]+"))
        .filter { it.isNotBlank() }
): Boolean {
    val followUpCueTokens = setOf(
        "add", "include", "plus", "also", "without", "remove",
        "drop", "replace", "swap", "instead", "too", "same",
        "previous", "it", "them", "that", "those"
    )

    return tokens.any { it in followUpCueTokens } ||
        normalized.startsWith("add ") ||
        normalized.startsWith("with ") ||
        normalized.startsWith("and ") ||
        normalized.startsWith("without ") ||
        normalized.startsWith("remove ") ||
        normalized.contains(" to that") ||
        normalized.contains(" to it") ||
        normalized.contains(" with that") ||
        normalized.contains(" with it") ||
        normalized.contains(" same ingredients") ||
        normalized.contains(" previous ingredients") ||
        normalized.contains(" those ingredients")
}

private fun looksLikeFreshRecipeIngredientRequest(
    normalized: String,
    tokens: List<String>
): Boolean {
    val freshRecipeCueTokens = setOf(
        "recipe", "recipes", "make", "cook", "meal", "meals",
        "breakfast", "lunch", "dinner", "snack", "using"
    )

    return tokens.any { it in freshRecipeCueTokens } ||
        normalized.startsWith("can you make") ||
        normalized.startsWith("make ") ||
        normalized.startsWith("cook ") ||
        normalized.startsWith("give me ") ||
        normalized.contains(" recipe with ") ||
        normalized.contains(" recipes with ")
}

internal fun requestedRecipeLimit(request: String): Int? {
    val normalized = request
        .trim()
        .lowercase(Locale.US)
        .replace(Regex("\\s+"), " ")

    if (normalized.isBlank()) return null

    val matchedNumber = findRequestedRecipeCountToken(normalized) ?: return null

    return recipeCountValue(matchedNumber)
        ?.takeIf { it in MIN_RECIPE_LIMIT..MAX_RECIPE_LIMIT }
}

internal fun requestedRecipeLimitTooHigh(request: String): Boolean {
    val normalized = request
        .trim()
        .lowercase(Locale.US)
        .replace(Regex("\\s+"), " ")

    if (normalized.isBlank()) return false

    val tokens = normalized
        .split(Regex("[^a-z0-9]+"))
        .filter { it.isNotBlank() }
    val recipeWords = setOf("recipe", "recipes", "idea", "ideas", "meal", "meals")
    val countIndexes = tokens.mapIndexedNotNull { index, token ->
        recipeCountValue(token)?.let { count -> index to count }
    }

    val overLimitPattern = recipeCountTokenPattern()
    val moreThanMatch = Regex("\\b(?:more\\s+than|over|above)\\s+$overLimitPattern\\b")
        .find(normalized)
        ?.groupValues
        ?.getOrNull(1)
        ?.let(::recipeCountValue)

    if (moreThanMatch != null) {
        return moreThanMatch >= MAX_RECIPE_LIMIT
    }

    if (countIndexes.any { (index, count) ->
            count > MAX_RECIPE_LIMIT &&
                    tokens.indices.any { otherIndex ->
                        tokens[otherIndex] in recipeWords &&
                                kotlin.math.abs(otherIndex - index) <= 3
                    }
        }
    ) {
        return true
    }

    if (countIndexes.any { (index, count) ->
            count > MAX_RECIPE_LIMIT &&
                    tokens.getOrNull(index + 1) in setOf("more", "another", "extra")
        }
    ) {
        return true
    }

    val requestedCount = findRequestedRecipeCountToken(normalized)
        ?.let(::recipeCountValue)

    return requestedCount != null && requestedCount > MAX_RECIPE_LIMIT
}

private fun findRequestedRecipeCountToken(normalizedRequest: String): String? {
    val numberPattern = recipeCountTokenPattern()
    val recipeWords = "(recipe|recipes|idea|ideas|meal|meals)"

    val directCountPatterns = listOf(
        Regex("\\b(?:only|just|exactly)?\\s*$numberPattern\\s+(?:more\\s+)?$recipeWords\\b"),
        Regex("\\b(?:give|show|make|suggest|need|want)\\s+(?:me\\s+)?(?:only\\s+|just\\s+|exactly\\s+)?$numberPattern\\s+(?:more\\s+)?$recipeWords\\b"),
        Regex("\\b(?:only|just|exactly)\\s+$numberPattern\\b"),
        Regex("\\b$numberPattern\\s+more\\b")
    )

    return directCountPatterns.firstNotNullOfOrNull { pattern ->
        pattern.find(normalizedRequest)?.groupValues?.getOrNull(1)
    }
}

private fun recipeCountTokenPattern(): String {
    return "([0-9]+|one|two|three|four|five|six|seven|eight|nine|ten|single|couple)"
}

private fun recipeCountValue(token: String): Int? {
    return token.toIntOrNull() ?: when (token) {
        "one", "single" -> 1
        "two", "couple" -> 2
        "three" -> 3
        "four" -> 4
        "five" -> 5
        "six" -> 6
        "seven" -> 7
        "eight" -> 8
        "nine" -> 9
        "ten" -> 10
        else -> null
    }
}

internal data class RecipeSuggestion(
    val title: String,
    val usedIngredientCount: Int,
    val missedIngredientCount: Int,
    val usedIngredients: List<String>,
    val missedIngredients: List<String>,
    val quickGuide: List<String>
)

internal data class RecipeSuggestionBatch(
    val resolvedIngredients: List<String>,
    val recipes: List<RecipeSuggestion>
)

internal object RecipeAiService {
    private val gson = Gson()

    // SEARCH: AI_MAIN_REQUEST_FLOW
    // Main AI request pipeline: request parsing, prompt building, retrying, and result filtering.
    suspend fun generateRecipeSuggestions(
        request: String,
        pantryIngredients: List<String>,
        previousIngredients: List<String>,
        contextId: String,
        limit: Int = DEFAULT_RECIPE_LIMIT,
        avoidRepeatingIngredients: List<String> = emptyList(),
        avoidRecipeTitles: List<String> = emptyList()
    ): RecipeSuggestionBatch = withContext(Dispatchers.IO) {
        val trimmedRequest = request.trim()
        if (trimmedRequest.isBlank()) {
            throw RecipeAiException("Ask for a recipe first.")
        }
        if (requestedRecipeLimitTooHigh(trimmedRequest)) {
            throw RecipeAiException("Sorry, I can only give 1, 2, or 3 recipes at a time.")
        }
        val effectiveLimit = requestedRecipeLimit(trimmedRequest)
            ?: limit.coerceIn(1, MAX_RECIPE_LIMIT)

        val cleanedPantryIngredients = sanitizeIngredients(pantryIngredients)
            .take(MAX_PANTRY_INGREDIENT_CONTEXT)
        val cleanedPreviousIngredients = sanitizeIngredients(previousIngredients).take(8)
        val cleanedAvoidRepeatingIngredients = sanitizeIngredients(avoidRepeatingIngredients).take(35)
        val cleanedAvoidRecipeTitles = avoidRecipeTitles
            .map { it.trim().replace(Regex("\\s+"), " ") }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.US) }
            .take(20)
        val ingredientEditFollowUp =
            looksLikeIngredientEditFollowUp(
                request = trimmedRequest,
                pantryIngredients = cleanedPantryIngredients,
                previousIngredients = cleanedPreviousIngredients
            )
        val wantsMoreFromPrevious =
            cleanedPreviousIngredients.isNotEmpty() &&
                !ingredientEditFollowUp &&
                wantsMoreRecipeIdeas(trimmedRequest)
        val rawExplicitRequestIngredients =
            if (wantsMoreFromPrevious) {
                emptyList()
            } else {
                extractExplicitRequestIngredients(trimmedRequest).take(8)
            }
        val conversationalIngredientUpdate =
            if (
                wantsMoreFromPrevious ||
                rawExplicitRequestIngredients.isEmpty() &&
                    cleanedPreviousIngredients.isEmpty()
            ) {
                null
            } else {
                val shouldUseConversationalUpdate =
                    cleanedPreviousIngredients.isNotEmpty() && ingredientEditFollowUp

                if (shouldUseConversationalUpdate) {
                    resolveConversationalIngredientUpdate(
                        request = trimmedRequest,
                        pantryIngredients = cleanedPantryIngredients,
                        previousIngredients = cleanedPreviousIngredients
                    )?.take(8)
                } else {
                    null
                }
            }
        val explicitRequestIngredients =
            when {
                wantsMoreFromPrevious -> emptyList()
                conversationalIngredientUpdate != null -> conversationalIngredientUpdate
                else -> rawExplicitRequestIngredients
            }
        val addedConversationalIngredients =
            conversationalIngredientUpdate?.filter { updatedIngredient ->
                cleanedPreviousIngredients.none { previousIngredient ->
                    ingredientMatchesEitherWay(updatedIngredient, previousIngredient)
                }
            }.orEmpty()
        val removedConversationalIngredients =
            conversationalIngredientUpdate?.let { updatedIngredients ->
                cleanedPreviousIngredients.filter { previousIngredient ->
                    updatedIngredients.none { updatedIngredient ->
                        ingredientMatchesEitherWay(previousIngredient, updatedIngredient)
                    }
                }
            }.orEmpty()
        val effectivePantryIngredients =
            when {
                wantsMoreFromPrevious -> emptyList()
                conversationalIngredientUpdate != null -> emptyList()
                explicitRequestIngredients.isNotEmpty() -> emptyList()
                else -> cleanedPantryIngredients
            }
        val effectivePreviousIngredients =
            if (explicitRequestIngredients.isNotEmpty()) {
                emptyList()
            } else if (wantsMoreFromPrevious) {
                cleanedPreviousIngredients
            } else {
                emptyList()
            }
        val strictRequestedIngredients =
            when {
                explicitRequestIngredients.isNotEmpty() -> explicitRequestIngredients
                wantsMoreFromPrevious -> cleanedPreviousIngredients
                else -> emptyList()
            }
        val requiredRecipeIngredients =
            when {
                strictRequestedIngredients.size in 1..MAX_RECIPE_LIMIT -> strictRequestedIngredients
                addedConversationalIngredients.isNotEmpty() -> addedConversationalIngredients
                else -> emptyList()
            }
        val effectiveRequest =
            when {
                wantsMoreFromPrevious -> "Please suggest $effectiveLimit more easy and quick ${
                    if (effectiveLimit == 1) "recipe" else "recipes"
                } using the same food products as before."
                conversationalIngredientUpdate != null ->
                    "Please suggest $effectiveLimit easy and quick ${
                        if (effectiveLimit == 1) "recipe" else "recipes"
                    } using these foods: ${conversationalIngredientUpdate.joinToString(", ")}."
                else -> trimmedRequest
            }

        val responseBody = generateContent(
            prompt = buildRecipeRequestPrompt(
                request = effectiveRequest,
                pantryIngredients = effectivePantryIngredients,
                previousIngredients = effectivePreviousIngredients,
                explicitRequestIngredients = explicitRequestIngredients,
                contextId = contextId,
                limit = effectiveLimit,
                strictRequestedIngredients = strictRequestedIngredients,
                requiredRecipeIngredients = requiredRecipeIngredients,
                excludedRecipeIngredients = removedConversationalIngredients,
                avoidRepeatingIngredients = cleanedAvoidRepeatingIngredients,
                avoidRecipeTitles = cleanedAvoidRecipeTitles
            ),
            responseMimeType = "application/json"
        )

        var parsedBatch = parseRecipeSuggestionBatch(
            responseBody = responseBody,
            fallbackIngredients = explicitRequestIngredients.ifEmpty {
                effectivePreviousIngredients.ifEmpty {
                    effectivePantryIngredients.ifEmpty { cleanedPantryIngredients }
                }
            }
        )

        if (
            shouldRetryForStrictIngredients(
                batch = parsedBatch,
                strictRequestedIngredients = strictRequestedIngredients,
                requiredRecipeIngredients = requiredRecipeIngredients,
                excludedRecipeIngredients = removedConversationalIngredients
            )
        ) {
            val strictRetryBody = generateContent(
                prompt = buildRecipeRequestPrompt(
                    request = effectiveRequest,
                    pantryIngredients = effectivePantryIngredients,
                    previousIngredients = effectivePreviousIngredients,
                    explicitRequestIngredients = explicitRequestIngredients,
                    contextId = contextId,
                    limit = effectiveLimit,
                    strictRequestedIngredients = strictRequestedIngredients,
                    requiredRecipeIngredients = requiredRecipeIngredients,
                    excludedRecipeIngredients = removedConversationalIngredients,
                    avoidRepeatingIngredients = cleanedAvoidRepeatingIngredients,
                    avoidRecipeTitles = cleanedAvoidRecipeTitles,
                    strictRetry = true
                ),
                responseMimeType = "application/json"
            )
            parsedBatch = parseRecipeSuggestionBatch(
                responseBody = strictRetryBody,
                fallbackIngredients = explicitRequestIngredients.ifEmpty {
                    effectivePreviousIngredients.ifEmpty {
                        effectivePantryIngredients.ifEmpty { cleanedPantryIngredients }
                    }
                }
            )
        }

        sanitizeBatchForRequestedIngredients(
            batch = parsedBatch.copy(recipes = parsedBatch.recipes.take(effectiveLimit)),
            strictRequestedIngredients = strictRequestedIngredients,
            requiredRecipeIngredients = requiredRecipeIngredients,
            excludedRecipeIngredients = removedConversationalIngredients
        )
    }

    suspend fun findRecipesByIngredients(
        ingredients: List<String>,
        limit: Int = DEFAULT_RECIPE_LIMIT
    ): List<RecipeSuggestion> = withContext(Dispatchers.IO) {
        val effectiveLimit = limit.coerceIn(1, MAX_RECIPE_LIMIT)
        val cleanedIngredients = ingredients
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.US) }
            .take(MAX_DIRECT_INGREDIENT_CONTEXT)

        if (cleanedIngredients.isEmpty()) return@withContext emptyList()

        val responseBody = generateContent(
            prompt = buildRecipeIdeasPrompt(
                ingredients = cleanedIngredients,
                limit = effectiveLimit
            ),
            responseMimeType = "application/json"
        )

        parseRecipeSuggestions(responseBody).take(effectiveLimit)
    }

    private suspend fun generateContent(
        prompt: String,
        responseMimeType: String? = null
    ): String {
        ensureApiKeyConfigured()
        var bestFailure: GeminiServiceFailure? = null

        GEMINI_MODEL_FALLBACKS.forEachIndexed { modelIndex, modelName ->
            when (val result = executeModelWithRetries(modelName, prompt, responseMimeType)) {
                is GeminiTextResult.Success -> {
                    Log.d(TAG, "Gemini success with $modelName")
                    return result.body
                }

                is GeminiTextResult.Failure -> {
                    bestFailure = preferFailure(bestFailure, result.failure)
                    val shouldTryNextModel =
                        result.failure.allowFallback &&
                            modelIndex < GEMINI_MODEL_FALLBACKS.lastIndex

                    if (shouldTryNextModel) {
                        Log.w(
                            TAG,
                            "Falling back from $modelName to next model: ${result.failure.message}"
                        )
                    } else {
                        throw result.failure.toException()
                    }
                }
            }
        }

        throw (bestFailure ?: temporaryBusyFailure()).toException()
    }

    private suspend fun executeModelWithRetries(
        modelName: String,
        prompt: String,
        responseMimeType: String?
    ): GeminiTextResult {
        repeat(GEMINI_RETRIES_BEFORE_FALLBACK + 1) { attempt ->
            Log.d(
                TAG,
                "Trying Gemini model $modelName (attempt ${attempt + 1}/${GEMINI_RETRIES_BEFORE_FALLBACK + 1})"
            )

            when (val result = executeHttpRequest(modelName, prompt, responseMimeType)) {
                is GeminiTextResult.Success -> return result

                is GeminiTextResult.Failure -> {
                    val shouldRetry =
                        result.failure.retryable && attempt < GEMINI_RETRIES_BEFORE_FALLBACK

                    if (!shouldRetry) {
                        return result
                    }

                    val delayMs = retryBackoffMs(attempt)
                    Log.w(
                        TAG,
                        "Retrying $modelName after temporary Gemini issue in ${delayMs}ms: ${result.failure.message}"
                    )
                    delay(delayMs)
                }
            }
        }

        return GeminiTextResult.Failure(temporaryBusyFailure())
    }

    private fun executeHttpRequest(
        modelName: String,
        prompt: String,
        responseMimeType: String?
    ): GeminiTextResult {
        val connection = openGeminiConnection(modelName)
        val requestBody = buildRequestBody(
            prompt = prompt,
            responseMimeType = responseMimeType,
            modelName = modelName
        )

        return try {
            connection.outputStream.use { stream ->
                stream.write(requestBody.toByteArray(StandardCharsets.UTF_8))
            }

            val statusCode = connection.responseCode
            val body = readResponseBody(connection, statusCode)

            if (statusCode !in 200..299) {
                GeminiTextResult.Failure(explainError(statusCode, body))
            } else if (body.isBlank()) {
                GeminiTextResult.Failure(
                    GeminiServiceFailure(
                        kind = GeminiFailureKind.TEMPORARY,
                        message = "The food assistant returned an empty reply. Please try again in a moment.",
                        retryable = true,
                        allowFallback = true
                    )
                )
            } else {
                GeminiTextResult.Success(body)
            }
        } catch (error: SocketTimeoutException) {
            GeminiTextResult.Failure(
                GeminiServiceFailure(
                    kind = GeminiFailureKind.TEMPORARY,
                    message = "The food assistant took too long to answer. Please try again in a moment.",
                    retryable = true,
                    allowFallback = true
                )
            )
        } catch (error: UnknownHostException) {
            GeminiTextResult.Failure(
                GeminiServiceFailure(
                    kind = GeminiFailureKind.NETWORK,
                    message = "I couldn't reach the food assistant. Check your internet connection and try again."
                )
            )
        } catch (error: IOException) {
            GeminiTextResult.Failure(
                GeminiServiceFailure(
                    kind = GeminiFailureKind.TEMPORARY,
                    message = "The food assistant is temporarily unavailable. Please try again in a moment.",
                    retryable = true,
                    allowFallback = true
                )
            )
        } catch (error: RecipeAiException) {
            GeminiTextResult.Failure(
                GeminiServiceFailure(
                    kind = GeminiFailureKind.OTHER,
                    message = error.message
                )
            )
        } catch (error: Exception) {
            GeminiTextResult.Failure(
                GeminiServiceFailure(
                    kind = GeminiFailureKind.OTHER,
                    message = "I couldn't reach the food assistant right now: ${error.message ?: error.javaClass.simpleName}"
                )
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun openGeminiConnection(modelName: String): HttpURLConnection {
        return (
            URL("$GEMINI_BASE_URL/$modelName:generateContent").openConnection()
                as HttpURLConnection
            ).apply {
                requestMethod = "POST"
                connectTimeout = GEMINI_TIMEOUT_MS
                readTimeout = GEMINI_TIMEOUT_MS
                doOutput = true
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("x-goog-api-key", BuildConfig.GEMINI_API_KEY)
            }
    }

    private fun retryBackoffMs(attempt: Int): Long {
        return (GEMINI_INITIAL_BACKOFF_MS * (1L shl attempt))
            .coerceAtMost(GEMINI_MAX_BACKOFF_MS)
    }

    private fun preferFailure(
        current: GeminiServiceFailure?,
        candidate: GeminiServiceFailure
    ): GeminiServiceFailure {
        if (current == null) return candidate
        return if (candidate.kind.priority < current.kind.priority) candidate else current
    }

    private fun buildRequestBody(
        prompt: String,
        responseMimeType: String?,
        modelName: String
    ): String {
        val root = JsonObject()
        val contents = JsonArray()
        val userContent = JsonObject()
        val parts = JsonArray()

        parts.add(
            JsonObject().apply {
                addProperty("text", prompt)
            }
        )

        userContent.addProperty("role", "user")
        userContent.add("parts", parts)
        contents.add(userContent)
        root.add("contents", contents)

        root.add(
            "generationConfig",
            JsonObject().apply {
                addProperty("temperature", 0.4)
                addProperty("topP", 0.9)
                responseMimeType?.let { addProperty("responseMimeType", it) }
                if (supportsThinkingConfig(modelName)) {
                    add(
                        "thinkingConfig",
                        JsonObject().apply {
                            addProperty("thinkingLevel", "minimal")
                        }
                    )
                }
            }
        )

        return gson.toJson(root)
    }

    private fun supportsThinkingConfig(modelName: String): Boolean {
        return modelName == "gemini-3.1-flash-lite-preview"
    }

    // SEARCH: AI_PROMPT_RULES
    // Core recipe assistant prompt. Add or change AI obedience rules here.
    private fun buildRecipeRequestPrompt(
        request: String,
        pantryIngredients: List<String>,
        previousIngredients: List<String>,
        explicitRequestIngredients: List<String>,
        contextId: String,
        limit: Int,
        strictRequestedIngredients: List<String> = emptyList(),
        requiredRecipeIngredients: List<String> = emptyList(),
        excludedRecipeIngredients: List<String> = emptyList(),
        avoidRepeatingIngredients: List<String> = emptyList(),
        avoidRecipeTitles: List<String> = emptyList(),
        strictRetry: Boolean = false
    ): String {
        val pantryText = pantryIngredients.joinToString(", ").ifBlank { "none" }
        val previousText = previousIngredients.joinToString(", ").ifBlank { "none" }
        val explicitText = explicitRequestIngredients.joinToString(", ").ifBlank { "none" }
        val strictText = strictRequestedIngredients.joinToString(", ").ifBlank { "none" }
        val requiredText = requiredRecipeIngredients.joinToString(", ").ifBlank { "none" }
        val excludedText = excludedRecipeIngredients.joinToString(", ").ifBlank { "none" }
        val avoidRepeatText = avoidRepeatingIngredients.joinToString(", ").ifBlank { "none" }
        val avoidTitleText = avoidRecipeTitles.joinToString(", ").ifBlank { "none" }
        val strictRuleBlock =
            if (strictRequestedIngredients.isNotEmpty()) {
                """
                Strict ingredient mode:
                - Treat these as the only user food ingredients for this request: $strictText
                - Do not switch to unrelated pantry or previous foods.
                - usedIngredients must only contain foods from that strict food set.
                - If there are 3 or fewer strict ingredients, every recipe must use all of them.
                - If required ingredients are not "none", every recipe must use them: $requiredText
                - If excluded ingredients are not "none", never use them in usedIngredients: $excludedText
                """.trimIndent()
            } else {
                ""
            }
        val strictRetryBlock =
            if (strictRetry) {
                """
                Important correction:
                - The previous reply ignored the requested foods.
                - Retry now and follow the strict ingredient mode exactly.
                """.trimIndent()
            } else {
                ""
            }

        return """
            You are FoodExpiryTracker's recipe-only assistant.
            Conversation id: $contextId

            Return only valid JSON with this exact shape:
            {
              "resolvedIngredients": ["ingredient 1", "ingredient 2"],
              "recipes": [
                {
                  "title": "Recipe name",
                  "usedIngredients": ["ingredient 1", "ingredient 2"],
                  "missedIngredients": ["optional extra 1", "optional extra 2"],
                  "quickGuide": ["Short step 1", "Short step 2", "Short step 3"]
                }
              ]
            }

            Ingredient priority:
            - If current request ingredients are not "none", use only those as the main set.
            - If the user asks for pantry, saved foods, or available foods, use pantry ingredients.
            - If the user asks for more, another, same ingredients, those ingredients, or previous ingredients and current request ingredients are "none", reuse previous recipe ingredients.
            - If the user says add/include/also use an ingredient with previous foods, combine it with previous recipe ingredients.
            - If the user says remove/without an ingredient, remove it from previous recipe ingredients.
            - If the user says replace one ingredient with another, swap them.
            - If no ingredients are named, use previous recipe ingredients when available, otherwise pantry ingredients.
            - When using pantry ingredients, use these pantry foods as the active pantry set for this request: $pantryText
            - Do not use avoided ingredients unless there are not enough usable foods: $avoidRepeatText
            - Avoid repeating recipe titles from the avoid list: $avoidTitleText
            - Make the next set noticeably different from previous pantry recipe suggestions.
            - Do not keep choosing the same few pantry foods when other pantry foods can make good recipes.

            $strictRuleBlock
            $strictRetryBlock

            Rules:
            - Reply with recipe suggestions only.
            - Never ask follow-up questions.
            - Prefer exactly $limit easy, quick, practical recipes when possible, usually about 30 minutes or less.
            - Use simple home cooking, low effort, minimal extras, and short pantry staples only.
            - Keep resolvedIngredients to 8 or fewer unique items.
            - Keep missedIngredients to 3 or fewer items.
            - If exact matches are limited, still return up to $limit easiest close matches.
            - quickGuide should have 3 to 5 short steps when possible, never more than 5.
            - Keep each step simple and direct.

            Current request ingredients:
            $explicitText
            
            Pantry ingredients:
            $pantryText

            Previous recipe ingredients:
            $previousText

            User request:
            $request
        """.trimIndent()
    }

    private fun extractExplicitRequestIngredients(request: String): List<String> {
        val normalized = request
            .lowercase(Locale.US)
            .replace('\n', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()

        if (normalized.isBlank()) return emptyList()

        val tailSource = extractIngredientTail(normalized)
        val cleanedSource = tailSource
            .replace(Regex("[?!.]"), " ")
            .replace(
                Regex(
                    "\\b(can you|could you|would you|please|give me|give|make|cook|show me|tell me|recipe|recipes|idea|ideas|meal|meals|snack|breakfast|lunch|dinner|dessert|quick|easy|some more|more|another|just|only|something|anything|with|using|use|from|for|kind of|what can i make)\\b"
                ),
                " "
            )
            .replace(
                Regex(
                    "\\b(my|the|a|an|in|inside|saved|available|list|lists|pantry|home|items|item|foods|food|things|stuff|have|has|already)\\b"
                ),
                " "
            )
            .replace(Regex("\\s+"), " ")
            .trim(' ', ',', ';', ':')

        val referentialPhrases = setOf(
            "same ingredients",
            "those ingredients",
            "previous ingredients"
        )

        return cleanedSource
            .split(Regex("\\s*,\\s*|\\s+and\\s+|\\s*&\\s*|\\s*\\+\\s*|\\s*/\\s*"))
            .mapNotNull { segment ->
                val cleaned = segment
                    .trim()
                    .trim(',', '.', '?', '!', ';', ':')
                    .replace(Regex("\\s+"), " ")

                when {
                    cleaned.isBlank() -> null
                    cleaned in referentialPhrases -> null
                    cleaned.length < 2 -> null
                    else -> cleaned
                }
            }
            .let(::sanitizeIngredients)
    }

    private fun extractIngredientTail(normalizedRequest: String): String {
        val markers = listOf(" with ", " using ", ":")
        val markerIndex = markers.maxOfOrNull { normalizedRequest.lastIndexOf(it) } ?: -1
        if (markerIndex < 0) return normalizedRequest

        val marker = markers.firstOrNull { normalizedRequest.lastIndexOf(it) == markerIndex }.orEmpty()
        return normalizedRequest.substring(markerIndex + marker.length).trim()
    }

    private fun resolveConversationalIngredientUpdate(
        request: String,
        pantryIngredients: List<String>,
        previousIngredients: List<String>
    ): List<String>? {
        if (previousIngredients.isEmpty()) return null

        val normalized = request
            .trim()
            .lowercase(Locale.US)
            .replace(Regex("\\s+"), " ")

        if (normalized.isBlank()) return null

        val replaceMatch = Regex("\\breplace\\s+(.+?)\\s+with\\s+(.+)").find(normalized)
        if (replaceMatch != null) {
            val removedIngredients = extractMentionedIngredientCandidates(
                text = replaceMatch.groupValues[1],
                pantryIngredients = pantryIngredients,
                previousIngredients = previousIngredients
            )
            val addedIngredients = extractMentionedIngredientCandidates(
                text = replaceMatch.groupValues[2],
                pantryIngredients = pantryIngredients,
                previousIngredients = previousIngredients
            )

            val replaced = addUniqueIngredients(
                base = removeMatchingIngredients(previousIngredients, removedIngredients),
                additions = addedIngredients
            )
            if (replaced.isNotEmpty() && replaced != previousIngredients) {
                return replaced
            }
        }

        val mentionedIngredients = extractMentionedIngredientCandidates(
            text = normalized,
            pantryIngredients = pantryIngredients,
            previousIngredients = previousIngredients
        )
        if (mentionedIngredients.isEmpty()) return null

        val removeIntent =
            Regex("\\b(remove|without|drop|skip|no)\\b").containsMatchIn(normalized)
        if (removeIntent) {
            val updated = removeMatchingIngredients(previousIngredients, mentionedIngredients)
            if (updated.isNotEmpty() && updated != previousIngredients) {
                return updated
            }
            return null
        }

        val addIntent =
            Regex("\\b(add|include|plus|also|with|too|and)\\b").containsMatchIn(normalized) ||
                normalized.startsWith("with ") ||
                normalized.startsWith("and ")
        val shortIngredientReply = normalized.split(Regex("[^a-z0-9]+")).count { it.isNotBlank() } <= 4

        if (addIntent || shortIngredientReply) {
            val updated = addUniqueIngredients(previousIngredients, mentionedIngredients)
            if (updated.isNotEmpty() && updated != previousIngredients) {
                return updated
            }
        }

        return null
    }

    private fun extractMentionedIngredientCandidates(
        text: String,
        pantryIngredients: List<String>,
        previousIngredients: List<String>
    ): List<String> {
        val normalized = text
            .lowercase(Locale.US)
            .replace('\n', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()

        if (normalized.isBlank()) return emptyList()

        val candidatePhrases =
            (KNOWN_FOOD_HINT_PHRASES + pantryIngredients + previousIngredients)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase(Locale.US) }
                .sortedByDescending { it.length }

        val matchedPhrases = mutableListOf<String>()
        var remainingText = " $normalized "

        candidatePhrases.forEach { phrase ->
            val phrasePattern = Regex("\\b${Regex.escape(phrase.lowercase(Locale.US))}\\b")
            if (phrasePattern.containsMatchIn(remainingText)) {
                matchedPhrases += phrase
                remainingText = phrasePattern.replace(remainingText, " ")
            }
        }

        val referenceTokens =
            ingredientMatchTokensFromList(pantryIngredients + previousIngredients)
        val matchedTokens = remainingText
            .split(Regex("[^a-z0-9]+"))
            .asSequence()
            .map { it.trim() }
            .filter { it.length >= 3 }
            .map { rawToken -> rawToken to normalizeIngredientMatchToken(rawToken) }
            .filter { (_, normalizedToken) ->
                normalizedToken in KNOWN_FOOD_HINT_TOKENS ||
                    normalizedToken in referenceTokens
            }
            .map { (rawToken, _) -> rawToken }
            .toList()

        return sanitizeIngredients(matchedPhrases + matchedTokens)
    }

    private fun addUniqueIngredients(
        base: List<String>,
        additions: List<String>
    ): List<String> {
        if (additions.isEmpty()) return base

        val updated = base.toMutableList()
        additions.forEach { addition ->
            if (updated.none { ingredientMatchesReference(it, addition) }) {
                updated += addition
            }
        }
        return sanitizeIngredients(updated)
    }

    private fun removeMatchingIngredients(
        base: List<String>,
        removals: List<String>
    ): List<String> {
        if (removals.isEmpty()) return base

        return base.filterNot { ingredient ->
            removals.any { removal -> ingredientMatchesReference(ingredient, removal) }
        }
    }

    private fun ingredientMatchesReference(
        ingredient: String,
        reference: String
    ): Boolean {
        if (ingredient.equals(reference, ignoreCase = true)) return true

        val ingredientTokens = ingredientMatchTokens(ingredient)
        val referenceTokens = ingredientMatchTokens(reference)

        return ingredientTokens.isNotEmpty() &&
            referenceTokens.isNotEmpty() &&
            ingredientTokens.intersect(referenceTokens).isNotEmpty()
    }

    private fun ingredientMatchesEitherWay(
        first: String,
        second: String
    ): Boolean {
        return ingredientMatchesReference(first, second) || ingredientMatchesReference(second, first)
    }

    private fun ingredientMatchTokensFromList(items: List<String>): Set<String> {
        return items.flatMapTo(mutableSetOf()) { ingredient ->
            ingredientMatchTokens(ingredient)
        }
    }

    private fun ingredientMatchTokens(text: String): Set<String> {
        return text
            .lowercase(Locale.US)
            .split(Regex("[^a-z0-9]+"))
            .map(::normalizeIngredientMatchToken)
            .filter { it.length >= 3 }
            .toSet()
    }

    private fun normalizeIngredientMatchToken(token: String): String {
        return when {
            token.endsWith("ies") && token.length > 4 -> token.dropLast(3) + "y"
            token.endsWith("oes") && token.length > 4 -> token.dropLast(2)
            token.endsWith("es") && token.length > 5 -> token.dropLast(2)
            token.endsWith("s") && token.length > 4 -> token.dropLast(1)
            else -> token
        }
    }

    // SEARCH: AI_PANTRY_PROMPT_RULES
    // Simpler pantry-only prompt used when the app asks for ideas from a direct ingredient list.
    private fun buildRecipeIdeasPrompt(
        ingredients: List<String>,
        limit: Int
    ): String {
        val ingredientList = ingredients.joinToString(", ")
        return """
            You are FoodExpiryTracker's recipe generator.
            Use these pantry ingredients:
            $ingredientList

            Return only valid JSON with this exact shape:
            {
              "recipes": [
                {
                  "title": "Recipe name",
                  "usedIngredients": ["ingredient 1", "ingredient 2"],
                  "missedIngredients": ["optional extra 1", "optional extra 2"],
                  "quickGuide": ["Short step 1", "Short step 2", "Short step 3"]
                }
              ]
            }

            Rules:
            - Prefer exactly $limit quick, easy, low-effort recipes when possible, usually about 30 minutes or less.
            - Use the listed ingredients as much as possible.
            - Prefer the easiest closest matches instead of fancy or time-consuming ideas.
            - Keep titles short and natural.
            - missedIngredients can only include a few simple extras or pantry staples, with a maximum of 3 items.
            - quickGuide should have 3 to 5 short steps when possible, and never more than 5.
            - Return JSON only, with no markdown or extra text.
            - If strong matches are limited, return up to $limit easiest close ideas.
            - If there are no good recipe ideas, return {"recipes":[]}.
        """.trimIndent()
    }

    private fun extractTextResponse(responseBody: String): String {
        val response = runCatching {
            gson.fromJson(responseBody, GeminiGenerateContentResponse::class.java)
        }.getOrNull()

        return response
            ?.candidates
            .orEmpty()
            .asSequence()
            .flatMap { it.content?.parts.orEmpty().asSequence() }
            .mapNotNull { it.text?.trim() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
    }

    private fun parseRecipeSuggestions(responseBody: String): List<RecipeSuggestion> {
        return parseRecipesOnly(
            cleanJsonResponse(extractTextResponse(responseBody))
        )
    }

    // SEARCH: AI_RESPONSE_FILTERS
    // Parses the model JSON and recovers resolved ingredients used by the UI.
    private fun parseRecipeSuggestionBatch(
        responseBody: String,
        fallbackIngredients: List<String>
    ): RecipeSuggestionBatch {
        val cleanedJson = cleanJsonResponse(extractTextResponse(responseBody))
        val parsed = runCatching {
            gson.fromJson(cleanedJson, GeminiRecipeBatchResponse::class.java)
        }.getOrNull() ?: throw RecipeAiException(
            "I couldn't understand the recipe ideas right now. Please try again."
        )

        val recipes = parseRecipesOnly(cleanedJson)
        val resolvedIngredients = sanitizeIngredients(parsed.resolvedIngredients)
            .ifEmpty {
                recipes.flatMap { it.usedIngredients }.let(::sanitizeIngredients)
            }
            .ifEmpty { fallbackIngredients }

        return RecipeSuggestionBatch(
            resolvedIngredients = resolvedIngredients,
            recipes = recipes
        )
    }

    // SEARCH: AI_RESPONSE_FILTERS
    // Decides whether the model ignored strict requested ingredients and must be retried.
    private fun shouldRetryForStrictIngredients(
        batch: RecipeSuggestionBatch,
        strictRequestedIngredients: List<String>,
        requiredRecipeIngredients: List<String>,
        excludedRecipeIngredients: List<String>
    ): Boolean {
        if (strictRequestedIngredients.isEmpty()) return false
        if (batch.recipes.isEmpty()) return false

        return batch.recipes.any { recipe ->
            !recipeMatchesRequestedIngredients(
                recipe = recipe,
                strictRequestedIngredients = strictRequestedIngredients,
                requiredRecipeIngredients = requiredRecipeIngredients,
                excludedRecipeIngredients = excludedRecipeIngredients
            )
        }
    }

    // SEARCH: AI_RESPONSE_FILTERS
    // Final guardrail that removes recipes not matching required/excluded ingredient rules.
    private fun sanitizeBatchForRequestedIngredients(
        batch: RecipeSuggestionBatch,
        strictRequestedIngredients: List<String>,
        requiredRecipeIngredients: List<String>,
        excludedRecipeIngredients: List<String>
    ): RecipeSuggestionBatch {
        if (strictRequestedIngredients.isEmpty()) return batch

        val filteredRecipes = batch.recipes.filter { recipe ->
            recipeMatchesRequestedIngredients(
                recipe = recipe,
                strictRequestedIngredients = strictRequestedIngredients,
                requiredRecipeIngredients = requiredRecipeIngredients,
                excludedRecipeIngredients = excludedRecipeIngredients
            )
        }

        return batch.copy(
            resolvedIngredients = strictRequestedIngredients,
            recipes = filteredRecipes
        )
    }

    private fun recipeMatchesRequestedIngredients(
        recipe: RecipeSuggestion,
        strictRequestedIngredients: List<String>,
        requiredRecipeIngredients: List<String>,
        excludedRecipeIngredients: List<String>
    ): Boolean {
        if (recipe.usedIngredients.isEmpty()) return false

        val usesOnlyRequestedIngredients = recipe.usedIngredients.all { usedIngredient ->
            strictRequestedIngredients.any { requestedIngredient ->
                ingredientMatchesEitherWay(usedIngredient, requestedIngredient)
            }
        }
        if (!usesOnlyRequestedIngredients) return false

        val includesRequiredIngredients = requiredRecipeIngredients.all { requiredIngredient ->
            recipe.usedIngredients.any { usedIngredient ->
                ingredientMatchesEitherWay(usedIngredient, requiredIngredient)
            }
        }
        if (!includesRequiredIngredients) return false

        val avoidsExcludedIngredients = excludedRecipeIngredients.none { excludedIngredient ->
            recipe.usedIngredients.any { usedIngredient ->
                ingredientMatchesEitherWay(usedIngredient, excludedIngredient)
            }
        }

        return avoidsExcludedIngredients
    }

    private fun parseRecipesOnly(cleanedJson: String): List<RecipeSuggestion> {
        val parsed = runCatching {
            gson.fromJson(cleanedJson, GeminiRecipeResponse::class.java)
        }.getOrNull() ?: throw RecipeAiException(
            "I couldn't understand the recipe ideas right now. Please try again."
        )

        return parsed.recipes.orEmpty()
            .mapNotNull { recipe ->
                val title = recipe.title?.trim().orEmpty()
                if (title.isBlank()) return@mapNotNull null

                val usedIngredients = sanitizeIngredients(recipe.usedIngredients)
                val missedIngredients = sanitizeIngredients(recipe.missedIngredients)
                val quickGuide = sanitizeQuickGuide(recipe.quickGuide)

                RecipeSuggestion(
                    title = title,
                    usedIngredientCount = usedIngredients.size,
                    missedIngredientCount = missedIngredients.size,
                    usedIngredients = usedIngredients,
                    missedIngredients = missedIngredients,
                    quickGuide = quickGuide
                )
            }
            .take(MAX_RECIPE_LIMIT)
    }

    private fun sanitizeIngredients(items: List<String>?): List<String> {
        return items
            .orEmpty()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.US) }
    }

    private fun sanitizeQuickGuide(steps: List<String>?): List<String> {
        return steps
            .orEmpty()
            .map { step ->
                step.trim()
                    .replace(Regex("\\s+"), " ")
                    .trim('-', '*', '•', ' ')
            }
            .filter { it.isNotBlank() }
            .map { step ->
                if (step.length <= 120) {
                    step
                } else {
                    step.take(117).trimEnd() + "..."
                }
            }
            .take(MAX_RECIPE_STEPS)
    }

    private fun cleanJsonResponse(text: String): String {
        return text
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun ensureApiKeyConfigured() {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            throw RecipeAiException(
                "Add GEMINI_API_KEY to local.properties and rebuild the app to enable the AI recipe assistant."
            )
        }
    }

    private fun readResponseBody(
        connection: HttpURLConnection,
        statusCode: Int
    ): String {
        val stream = if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        } ?: return ""

        return stream.bufferedReader().use { it.readText() }
    }

    private fun explainError(
        statusCode: Int,
        body: String
    ): GeminiServiceFailure {
        val apiError = runCatching {
            gson.fromJson(body, GeminiErrorEnvelope::class.java)?.error
        }.getOrNull()

        val apiMessage = apiError?.message?.trim().orEmpty()
        val normalizedMessage = apiMessage.lowercase(Locale.US)
        val statusText = apiError?.status?.trim().orEmpty()
        val isClearQuotaExhaustion =
            normalizedMessage.contains("quota") ||
                normalizedMessage.contains("billing") ||
                normalizedMessage.contains("insufficient_quota") ||
                normalizedMessage.contains("exceeded your current quota")
        val isTemporaryBusy =
            statusCode >= 500 ||
                statusCode == 429 ||
                statusText == "RESOURCE_EXHAUSTED" ||
                statusText == "UNAVAILABLE" ||
                normalizedMessage.contains("high demand") ||
                normalizedMessage.contains("try again later") ||
                normalizedMessage.contains("too many requests") ||
                normalizedMessage.contains("resource exhausted") ||
                normalizedMessage.contains("temporarily unavailable")
        val isConfigurationIssue =
            statusCode == 401 ||
                statusCode == 403 ||
                normalizedMessage.contains("api key not valid") ||
                normalizedMessage.contains("permission denied") ||
                normalizedMessage.contains("service disabled") ||
                normalizedMessage.contains("not enabled")
        val isModelUnavailable =
            statusCode == 404 ||
                (
                    normalizedMessage.contains("model") && (
                        normalizedMessage.contains("not found") ||
                            normalizedMessage.contains("not supported") ||
                            normalizedMessage.contains("not available") ||
                            normalizedMessage.contains("unsupported")
                        )
                    )

        if (isClearQuotaExhaustion) {
            return GeminiServiceFailure(
                kind = GeminiFailureKind.QUOTA,
                message = "The AI recipe quota looks used up right now. Please try again later.",
                limitReached = true
            )
        }

        if (isTemporaryBusy) {
            return temporaryBusyFailure()
        }

        if (isModelUnavailable) {
            return GeminiServiceFailure(
                kind = GeminiFailureKind.MODEL_UNAVAILABLE,
                message = apiMessage.ifBlank {
                    "This Gemini recipe model is not available for this project."
                },
                allowFallback = true
            )
        }

        if (isConfigurationIssue) {
            return GeminiServiceFailure(
                kind = GeminiFailureKind.CONFIGURATION,
                message = "The Gemini recipe service is not configured correctly. Check GEMINI_API_KEY and project permissions, then rebuild the app."
            )
        }

        if (statusCode == 400) {
            return GeminiServiceFailure(
                kind = GeminiFailureKind.REQUEST,
                message = apiMessage.ifBlank {
                    "The recipe request couldn't be processed. Try a simpler food request."
                }
            )
        }

        return GeminiServiceFailure(
            kind = GeminiFailureKind.OTHER,
            message = apiMessage.ifBlank {
                "The Gemini food assistant returned an error ($statusCode)."
            }
        )
    }

    private fun temporaryBusyFailure(): GeminiServiceFailure {
        return GeminiServiceFailure(
            kind = GeminiFailureKind.TEMPORARY,
            message = "The food assistant is busy right now. Please try again in a moment.",
            retryable = true,
            allowFallback = true
        )
    }
}

internal class RecipeAiException(
    override val message: String,
    val limitReached: Boolean = false
) : IllegalStateException(message)

private enum class GeminiFailureKind(
    val priority: Int
) {
    CONFIGURATION(0),
    REQUEST(1),
    QUOTA(2),
    NETWORK(3),
    TEMPORARY(4),
    MODEL_UNAVAILABLE(5),
    OTHER(6)
}

private data class GeminiServiceFailure(
    val kind: GeminiFailureKind,
    val message: String,
    val retryable: Boolean = false,
    val allowFallback: Boolean = false,
    val limitReached: Boolean = false
) {
    fun toException(): RecipeAiException = RecipeAiException(
        message = message,
        limitReached = limitReached
    )
}

private sealed interface GeminiTextResult {
    data class Success(val body: String) : GeminiTextResult
    data class Failure(val failure: GeminiServiceFailure) : GeminiTextResult
}

private data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate>? = null
)

private data class GeminiCandidate(
    val content: GeminiContent? = null
)

private data class GeminiContent(
    val parts: List<GeminiPart>? = null
)

private data class GeminiPart(
    val text: String? = null
)

private data class GeminiErrorEnvelope(
    val error: GeminiError? = null
)

private data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

private data class GeminiRecipeResponse(
    val recipes: List<GeminiRecipe>? = null
)

private data class GeminiRecipeBatchResponse(
    val resolvedIngredients: List<String>? = null,
    val recipes: List<GeminiRecipe>? = null
)

private data class GeminiRecipe(
    val title: String? = null,
    val usedIngredients: List<String>? = null,
    val missedIngredients: List<String>? = null,
    val quickGuide: List<String>? = null
)
