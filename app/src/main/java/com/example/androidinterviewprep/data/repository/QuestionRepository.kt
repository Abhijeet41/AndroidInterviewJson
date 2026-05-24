package com.example.androidinterviewprep.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.androidinterviewprep.data.model.Question
import com.example.androidinterviewprep.data.remote.QuestionApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class DataSource {
    REMOTE, CACHE, ASSETS
}

@Singleton
class QuestionRepository @Inject constructor(
    private val apiService: QuestionApiService,
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val _dataSource = MutableStateFlow(DataSource.REMOTE)
    val dataSource: StateFlow<DataSource> = _dataSource.asStateFlow()

    private val questionsJsonKey = stringPreferencesKey("cached_questions_json")
    private val reviewedQuestionsKey = stringPreferencesKey("reviewed_questions_set")

    suspend fun getQuestions(): List<Question> {
        return try {
            val json = apiService.fetchQuestionsJson()
            saveToDataStore(json)
            _dataSource.value = DataSource.REMOTE
            parseJson(json)
        } catch (e: Exception) {
            val cached = loadFromDataStore()
            if (cached != null) {
                _dataSource.value = DataSource.CACHE
                parseJson(cached)
            } else {
                _dataSource.value = DataSource.ASSETS
                loadFromAssets()
            }
        }
    }

    private suspend fun saveToDataStore(json: String) {
        dataStore.edit { preferences ->
            preferences[questionsJsonKey] = json
        }
    }

    private suspend fun loadFromDataStore(): String? {
        return try {
            val preferences = dataStore.data.first()
            preferences[questionsJsonKey]
        } catch (e: Exception) {
            null
        }
    }

    private fun loadFromAssets(): List<Question> {
        return try {
            context.assets.open("interview_questions.json").bufferedReader().use { it.readText() }
                .let { parseJson(it) }
        } catch (e: IOException) {
            emptyList()
        }
    }

    private fun parseJson(json: String): List<Question> {
        return try {
            val type = object : TypeToken<List<Question>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Expose reviewed question IDs as a Flow of Set<Int>
    fun getReviewedQuestionIds(): Flow<Set<Int>> {
        return dataStore.data.map { preferences ->
            val idsString = preferences[reviewedQuestionsKey] ?: ""
            if (idsString.isEmpty()) {
                emptySet()
            } else {
                idsString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
            }
        }
    }

    // Toggle reviewed state
    suspend fun toggleReviewed(questionId: Int) {
        dataStore.edit { preferences ->
            val idsString = preferences[reviewedQuestionsKey] ?: ""
            val currentIds = if (idsString.isEmpty()) {
                mutableSetOf()
            } else {
                idsString.split(",").mapNotNull { it.toIntOrNull() }.toMutableSet()
            }
            if (currentIds.contains(questionId)) {
                currentIds.remove(questionId)
            } else {
                currentIds.add(questionId)
            }
            preferences[reviewedQuestionsKey] = currentIds.joinToString(",")
        }
    }
}
