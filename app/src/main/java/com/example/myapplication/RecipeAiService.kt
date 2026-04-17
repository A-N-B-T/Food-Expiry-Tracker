package com.example.myapplication

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale

private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
private const val GEMINI_MODEL = "gemini-2.5-flash-lite"
private const val GEMINI_TIMEOUT_MS = 9000
private const val DEFAULT_RECIPE_LIMIT = 3
private const val MAX_RECIPE_LIMIT = 3
private const val MAX_RECIPE_STEPS = 5

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

    suspend fun generateRecipeSuggestions(
        request: String,
        pantryIngredients: List<String>,
        previousIngredients: List<String>,
        contextId: String,
        limit: Int = DEFAULT_RECIPE_LIMIT
    ): RecipeSuggestionBatch = withContext(Dispatchers.IO) {
        val trimmedRequest = request.trim()
        if (trimmedRequest.isBlank()) {
            throw RecipeAiException("Ask for a recipe first.")
        }

        val cleanedPantryIngredients = sanitizeIngredients(pantryIngredients).take(12)
        val cleanedPreviousIngredients = sanitizeIngredients(previousIngredients).take(8)
        val explicitRequestIngredients = extractExplicitRequestIngredients(trimmedRequest).take(8)
        val effectivePantryIngredients =
            explicitRequestIngredients.ifEmpty { cleanedPantryIngredients }
        val effectivePreviousIngredients =
            if (explicitRequestIngredients.isNotEmpty()) {
                emptyList()
            } else {
                cleanedPreviousIngredients
            }

        val responseBody = generateContent(
            prompt = buildRecipeRequestPrompt(
                request = trimmedRequest,
                pantryIngredients = effectivePantryIngredients,
                previousIngredients = effectivePreviousIngredients,
                explicitRequestIngredients = explicitRequestIngredients,
                contextId = contextId,
                limit = limit.coerceIn(1, MAX_RECIPE_LIMIT)
            ),
            responseMimeType = "application/json"
        )

        parseRecipeSuggestionBatch(
            responseBody = responseBody,
            fallbackIngredients = explicitRequestIngredients.ifEmpty {
                effectivePreviousIngredients.ifEmpty { effectivePantryIngredients }
            }
        )
    }

    suspend fun findRecipesByIngredients(
        ingredients: List<String>,
        limit: Int = DEFAULT_RECIPE_LIMIT
    ): List<RecipeSuggestion> = withContext(Dispatchers.IO) {
        val cleanedIngredients = ingredients
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.US) }
            .take(8)

        if (cleanedIngredients.isEmpty()) return@withContext emptyList()

        val responseBody = generateContent(
            prompt = buildRecipeIdeasPrompt(
                ingredients = cleanedIngredients,
                limit = limit.coerceIn(1, MAX_RECIPE_LIMIT)
            ),
            responseMimeType = "application/json"
        )

        parseRecipeSuggestions(responseBody)
    }

    private fun generateContent(
        prompt: String,
        responseMimeType: String? = null
    ): String {
        ensureApiKeyConfigured()

        val connection = (
            URL("$GEMINI_BASE_URL/$GEMINI_MODEL:generateContent").openConnection()
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

        val requestBody = buildRequestBody(
            prompt = prompt,
            responseMimeType = responseMimeType
        )

        try {
            connection.outputStream.use { stream ->
                stream.write(requestBody.toByteArray(StandardCharsets.UTF_8))
            }

            val statusCode = connection.responseCode
            val body = readResponseBody(connection, statusCode)

            if (statusCode !in 200..299) {
                throw explainError(statusCode, body)
            }

            if (body.isBlank()) {
                throw RecipeAiException(
                    "The Gemini food assistant returned an empty response. Please try again."
                )
            }

            return body
        } catch (error: RecipeAiException) {
            throw error
        } catch (_: Exception) {
            throw RecipeAiException(
                "I couldn't reach the food assistant right now. Check your internet connection and try again."
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun buildRequestBody(
        prompt: String,
        responseMimeType: String?
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
            }
        )

        return gson.toJson(root)
    }

    private fun buildRecipeRequestPrompt(
        request: String,
        pantryIngredients: List<String>,
        previousIngredients: List<String>,
        explicitRequestIngredients: List<String>,
        contextId: String,
        limit: Int
    ): String {
        val pantryText = pantryIngredients.joinToString(", ").ifBlank { "none" }
        val previousText = previousIngredients.joinToString(", ").ifBlank { "none" }
        val explicitText = explicitRequestIngredients.joinToString(", ").ifBlank { "none" }

        return """
            You are FoodExpiryTracker's recipe-only assistant.
            Conversation id: $contextId
            
            Rules:
            - Every reply must be recipe suggestions only.
            - Never ask follow-up questions.
            - Never offer storage tips, nutrition advice, pantry tips, or anything outside recipe suggestions.
            - If the current request ingredients are not "none", use only those current request ingredients as the main ingredient set.
            - If the current request ingredients are not "none", ignore previous recipe ingredients and ignore unrelated pantry ingredients.
            - If the user says "more", "another", "same ingredients", "those ingredients", or "previous ingredients" and the current request ingredients are "none", reuse the previous recipe ingredients.
            - If the user gives only a meal style like dinner, breakfast, snack, quick, or easy and the current request ingredients are "none", combine that preference with the previous recipe ingredients when available.
            - If no ingredients are named in the request and there are no previous recipe ingredients, use the pantry ingredients.
            - Keep resolvedIngredients to 8 or fewer unique items.
            - Prefer exactly $limit recipe ideas when possible.
            - Recipes should be easy, quick, simple, and practical, usually about 30 minutes or less.
            - Prefer low-effort meals with common pantry-style prep, fewer steps, and minimal extra ingredients.
            - Avoid complicated cooking methods, fancy techniques, or long cooking unless absolutely necessary.
            - Each recipe should use the resolved ingredients as much as possible.
            - missedIngredients must be short pantry staples only, with at most 3 items.
            - If perfect matches are limited, still return up to $limit of the easiest closest recipe ideas.
            - Return only valid JSON and nothing else.

            Current request ingredients:
            $explicitText
            
            Pantry ingredients:
            $pantryText

            Previous recipe ingredients:
            $previousText

	            Return JSON with this exact shape:
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

            quickGuide rules:
            - Include 3 to 5 short steps when possible.
            - Never exceed 5 short steps.
            - Keep every step simple, direct, and easy to understand.
            - Focus only on practical home cooking steps.
            - Keep each step to one short sentence.

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

    private fun buildRecipeIdeasPrompt(
        ingredients: List<String>,
        limit: Int
    ): String {
        val ingredientList = ingredients.joinToString(", ")
        return """
            You are FoodExpiryTracker's recipe generator.
            
            Use these pantry ingredients that are expiring soon:
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
            - Prefer exactly $limit recipe ideas when possible.
            - Recipes should be quick, easy, simple, and low effort, usually about 30 minutes or less.
            - Prefer the easiest closest matches instead of fancy or time-consuming ideas.
            - Use the listed ingredients as much as possible.
            - Keep recipe titles short and natural.
            - `missedIngredients` can only include a few simple extras or pantry staples, with a maximum of 3 items.
            - Keep prep practical and minimal, with no complicated cooking unless necessary.
            - Include a `quickGuide` with 3 to 5 short, simple steps when possible, and never more than 5.
            - Do not include instructions, notes, markdown, or any text outside the JSON.
            - If there are not enough strong matches, return up to $limit of the easiest closest ideas.
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
    ): RecipeAiException {
        val apiError = runCatching {
            gson.fromJson(body, GeminiErrorEnvelope::class.java)?.error
        }.getOrNull()

        val apiMessage = apiError?.message?.trim().orEmpty()
        val isQuotaError = statusCode == 429 || apiError?.status == "RESOURCE_EXHAUSTED"

        if (isQuotaError) {
            return RecipeAiException(
                "You have reached the AI recipe limit for now. Please try again later.",
                limitReached = true
            )
        }

        val message = when (statusCode) {
            400 -> apiMessage.ifBlank {
                "The Gemini request was rejected. Please try again with a simpler food question."
            }

            401, 403 -> "The Gemini API key is missing, invalid, or not enabled for this project. Add a valid GEMINI_API_KEY in local.properties and rebuild."
            else -> apiMessage.ifBlank { "The Gemini food assistant returned an error ($statusCode)." }
        }

        return RecipeAiException(message)
    }
}

internal class RecipeAiException(
    override val message: String,
    val limitReached: Boolean = false
) : IllegalStateException(message)

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
