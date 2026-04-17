package com.example.myapplication

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val TRANSFER_FOOD_PREFS = "food_prefs"
private const val TRANSFER_FOOD_LIST_KEY = "food_list"
private const val TRANSFER_CATEGORIES_LIST_KEY = "categories_list"

private val pantryTransferGson = Gson()
private val pantryTransferExpiryFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d/M/yyyy", Locale.US)

private data class PantryExportFile(
    val version: Int = 1,
    val exportedAtMs: Long = System.currentTimeMillis(),
    val foods: List<FoodItem> = emptyList(),
    val categories: List<String> = emptyList()
)

@Composable
fun PantryTransferCard() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()
    var statusMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var busyAction by rememberSaveable { mutableStateOf<String?>(null) }

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri == null) {
                statusMessage = "Export was cancelled."
            } else {
                busyAction = "export"
                scope.launch {
                    statusMessage = runCatching {
                        exportPantryToUri(appContext, uri)
                    }.getOrElse { error ->
                        error.message ?: "Export failed."
                    }
                    busyAction = null
                }
            }
        }

    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                statusMessage = "Import was cancelled."
            } else {
                busyAction = "import"
                scope.launch {
                    statusMessage = runCatching {
                        importPantryFromUri(appContext, uri)
                    }.getOrElse { error ->
                        error.message ?: "Import failed."
                    }
                    busyAction = null
                }
            }
        }

    MatchingPillCard(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Pantry transfer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Export your Home pantry items to a file, then import them later with their saved expiry dates and categories.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        exportLauncher.launch(defaultExportFileName())
                    },
                    enabled = busyAction == null,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null
                    )
                    Spacer(Modifier.weight(0.1f))
                    Text(if (busyAction == "export") "Exporting..." else "Export")
                }
                Button(
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    },
                    enabled = busyAction == null,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Upload,
                        contentDescription = null
                    )
                    Spacer(Modifier.weight(0.1f))
                    Text(if (busyAction == "import") "Importing..." else "Import")
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Import merges into the current pantry and skips exact duplicates.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            statusMessage?.takeIf { it.isNotBlank() }?.let { message ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (
                        message.contains("failed", ignoreCase = true) ||
                        message.contains("invalid", ignoreCase = true) ||
                        message.contains("couldn't", ignoreCase = true)
                    ) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

private fun defaultExportFileName(): String {
    val now = java.time.LocalDate.now()
    return "food-expiry-pantry-${now}.json"
}

private suspend fun exportPantryToUri(
    context: Context,
    uri: Uri
): String = withContext(Dispatchers.IO) {
    val prefs = context.getSharedPreferences(TRANSFER_FOOD_PREFS, Context.MODE_PRIVATE)
    val foods = loadTransferFoodList(prefs)
    val categories = loadTransferCategories(prefs)
    val payload = PantryExportFile(
        foods = foods,
        categories = categories
    )
    val json = pantryTransferGson.toJson(payload)

    context.contentResolver.openOutputStream(uri)?.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
        if (writer == null) {
            throw IllegalStateException("Couldn't open the export file.")
        }
        writer.write(json)
    }

    if (foods.isEmpty()) {
        "Exported an empty pantry file."
    } else {
        "Exported ${foods.size} pantry item${if (foods.size == 1) "" else "s"}."
    }
}

private suspend fun importPantryFromUri(
    context: Context,
    uri: Uri
): String = withContext(Dispatchers.IO) {
    val rawJson =
        context.contentResolver.openInputStream(uri)?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }
            ?: throw IllegalStateException("Couldn't read that file.")

    val importedFile = parsePantryImport(rawJson)
    val importedFoods = sanitizeImportedFoods(importedFile.foods)
    val importedCategories = sanitizeImportedCategories(importedFile.categories, importedFoods)

    if (importedFoods.isEmpty()) {
        throw IllegalStateException("That file doesn't contain any valid pantry items to import.")
    }

    val prefs = context.getSharedPreferences(TRANSFER_FOOD_PREFS, Context.MODE_PRIVATE)
    val existingFoods = loadTransferFoodList(prefs)
    val existingCategories = loadTransferCategories(prefs)

    val existingKeys = existingFoods.mapTo(mutableSetOf()) { transferFoodKey(it) }
    val newFoods = importedFoods.filter { existingKeys.add(transferFoodKey(it)) }

    if (newFoods.isNotEmpty()) {
        val mergedFoods = (existingFoods + newFoods)
            .sortedWith(compareBy<FoodItem>({ transferExpirySortValue(it.expiry) }, { normalizeTransferName(it.name) }))
        saveTransferFoodList(prefs, mergedFoods)
    }

    val mergedCategories = (existingCategories + importedCategories)
        .map { it.trim().replace(Regex("\\s+"), " ") }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(Locale.ROOT) }

    if (mergedCategories != existingCategories) {
        saveTransferCategories(prefs, mergedCategories)
    }

    val skipped = importedFoods.size - newFoods.size
    buildString {
        append("Imported ${newFoods.size} pantry item")
        if (newFoods.size != 1) append("s")
        append(".")
        if (skipped > 0) {
            append(" Skipped $skipped exact duplicate")
            if (skipped != 1) append("s")
            append(".")
        }
    }
}

private fun parsePantryImport(rawJson: String): PantryExportFile {
    runCatching {
        pantryTransferGson.fromJson(rawJson, PantryExportFile::class.java)
    }.getOrNull()?.let { parsed ->
        if (parsed.foods.isNotEmpty() || parsed.categories.isNotEmpty()) return parsed
    }

    val listType = object : TypeToken<List<FoodItem>>() {}.type
    val legacyFoods = runCatching {
        pantryTransferGson.fromJson<List<FoodItem>>(rawJson, listType)
    }.getOrNull().orEmpty()

    return PantryExportFile(foods = legacyFoods)
}

private fun sanitizeImportedFoods(foods: List<FoodItem>): List<FoodItem> {
    return foods.mapNotNull { item ->
        val cleanedName = item.name.trim().replace(Regex("\\s+"), " ")
        val cleanedExpiry = item.expiry.trim()
        val cleanedCategory = item.category?.trim()?.replace(Regex("\\s+"), " ")?.takeIf { it.isNotBlank() }

        if (cleanedName.isBlank() || !isValidTransferExpiry(cleanedExpiry)) {
            null
        } else {
            FoodItem(
                name = cleanedName,
                expiry = cleanedExpiry,
                category = cleanedCategory
            )
        }
    }
}

private fun sanitizeImportedCategories(
    categories: List<String>,
    foods: List<FoodItem>
): List<String> {
    return (categories + foods.mapNotNull { it.category })
        .map { it.trim().replace(Regex("\\s+"), " ") }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(Locale.ROOT) }
}

private fun isValidTransferExpiry(value: String): Boolean {
    return runCatching {
        LocalDate.parse(value, pantryTransferExpiryFormatter)
    }.isSuccess
}

private fun loadTransferFoodList(prefs: android.content.SharedPreferences): List<FoodItem> {
    val json = prefs.getString(TRANSFER_FOOD_LIST_KEY, null) ?: return emptyList()
    val type = object : TypeToken<List<FoodItem>>() {}.type
    return runCatching { pantryTransferGson.fromJson<List<FoodItem>>(json, type) }.getOrElse { emptyList() }
}

private fun saveTransferFoodList(
    prefs: android.content.SharedPreferences,
    list: List<FoodItem>
) {
    prefs.edit {
        putString(TRANSFER_FOOD_LIST_KEY, pantryTransferGson.toJson(list))
    }
}

private fun loadTransferCategories(prefs: android.content.SharedPreferences): List<String> {
    val json = prefs.getString(TRANSFER_CATEGORIES_LIST_KEY, null) ?: return emptyList()
    val type = object : TypeToken<List<String>>() {}.type
    return runCatching { pantryTransferGson.fromJson<List<String>>(json, type) }.getOrElse { emptyList() }
}

private fun saveTransferCategories(
    prefs: android.content.SharedPreferences,
    list: List<String>
) {
    prefs.edit {
        putString(TRANSFER_CATEGORIES_LIST_KEY, pantryTransferGson.toJson(list))
    }
}

private fun transferFoodKey(item: FoodItem): String {
    val categoryKey = item.category?.trim()?.lowercase(Locale.ROOT).orEmpty()
    return "${normalizeTransferName(item.name)}|${item.expiry.trim()}|$categoryKey"
}

private fun normalizeTransferName(raw: String): String {
    return raw.trim()
        .lowercase(Locale.ROOT)
        .replace(Regex("\\s+"), " ")
}

private fun transferExpirySortValue(raw: String): Long {
    return runCatching {
        LocalDate.parse(raw, pantryTransferExpiryFormatter).toEpochDay()
    }.getOrDefault(Long.MAX_VALUE)
}
