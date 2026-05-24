package com.example.androidinterviewprep.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidinterviewprep.data.model.Question
import com.example.androidinterviewprep.data.repository.DataSource
import com.example.androidinterviewprep.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UiState {
    object Loading : UiState
    data class Success(val questions: List<Question>) : UiState
    data class Error(val message: String) : UiState
}

@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val repository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    val reviewedQuestionIds: StateFlow<Set<Int>> = repository.getReviewedQuestionIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    val dataSource: StateFlow<DataSource> = repository.dataSource

    init {
        loadQuestions()
    }

    fun loadQuestions() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = repository.getQuestions()
                _questions.value = result
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun toggleReviewed(questionId: Int) {
        viewModelScope.launch {
            repository.toggleReviewed(questionId)
        }
    }

    fun getByCategory(category: String): List<Question> {
        return _questions.value.filter { it.category.equals(category, ignoreCase = true) }
    }

    fun searchQuestions(query: String): List<Question> {
        if (query.isBlank()) return emptyList()
        return _questions.value.filter {
            it.question.contains(query, ignoreCase = true) ||
            it.answer.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }
}
