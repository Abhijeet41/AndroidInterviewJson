@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.androidinterviewprep.ui.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidinterviewprep.data.model.Question
import com.example.androidinterviewprep.viewmodel.QuestionViewModel

sealed interface QuizState {
    object Setup : QuizState
    data class InProgress(
        val quizQuestions: List<Question>,
        val currentIndex: Int,
        val correctCount: Int,
        val incorrectCount: Int
    ) : QuizState
    data class Finished(
        val totalCount: Int,
        val correctCount: Int,
        val incorrectCount: Int
    ) : QuizState
}

@Composable
fun QuizScreen(
    viewModel: QuestionViewModel,
    modifier: Modifier = Modifier
) {
    val questions by viewModel.questions.collectAsState()
    var quizState by remember { mutableStateOf<QuizState>(QuizState.Setup) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quiz Mode", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = quizState) {
                is QuizState.Setup -> {
                    QuizSetupScreen(
                        questions = questions,
                        onStartQuiz = { selectedCategory, quizLength ->
                            val filtered = if (selectedCategory == "All Categories") {
                                questions
                            } else {
                                questions.filter { it.category == selectedCategory }
                            }
                            val shuffled = filtered.shuffled().take(quizLength)
                            if (shuffled.isNotEmpty()) {
                                quizState = QuizState.InProgress(
                                    quizQuestions = shuffled,
                                    currentIndex = 0,
                                    correctCount = 0,
                                    incorrectCount = 0
                                )
                            }
                        }
                    )
                }
                is QuizState.InProgress -> {
                    QuizInProgressScreen(
                        state = state,
                        onAnswerLogged = { isCorrect ->
                            val nextIndex = state.currentIndex + 1
                            val newCorrect = if (isCorrect) state.correctCount + 1 else state.correctCount
                            val newIncorrect = if (!isCorrect) state.incorrectCount + 1 else state.incorrectCount
                            
                            quizState = if (nextIndex < state.quizQuestions.size) {
                                state.copy(
                                    currentIndex = nextIndex,
                                    correctCount = newCorrect,
                                    incorrectCount = newIncorrect
                                )
                            } else {
                                QuizState.Finished(
                                    totalCount = state.quizQuestions.size,
                                    correctCount = newCorrect,
                                    incorrectCount = newIncorrect
                                )
                            }
                        },
                        onQuit = { quizState = QuizState.Setup }
                    )
                }
                is QuizState.Finished -> {
                    QuizFinishedScreen(
                        state = state,
                        onRetry = { quizState = QuizState.Setup }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSetupScreen(
    questions: List<Question>,
    onStartQuiz: (String, Int) -> Unit
) {
    val categories = remember(questions) {
        listOf("All Categories") + questions.map { it.category }.distinct().sorted()
    }

    var selectedCategory by remember { mutableStateOf("All Categories") }
    var quizLength by remember { mutableStateOf(5) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Quiz,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Test Your Knowledge",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Configure your quiz parameters to start testing yourself.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        // Category selection dropdown
        ExposedDropdownMenuBox(
            expanded = expandedCategoryDropdown,
            onExpandedChange = { expandedCategoryDropdown = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expandedCategoryDropdown,
                onDismissRequest = { expandedCategoryDropdown = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expandedCategoryDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quiz length slider
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Number of Questions: $quizLength",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = quizLength.toFloat(),
                    onValueChange = { quizLength = it.toInt() },
                    valueRange = 5f..20f,
                    steps = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val filteredCount = remember(questions, selectedCategory) {
            if (selectedCategory == "All Categories") {
                questions.size
            } else {
                questions.count { it.category == selectedCategory }
            }
        }

        Button(
            onClick = { onStartQuiz(selectedCategory, quizLength) },
            enabled = filteredCount > 0,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Start Quiz", fontWeight = FontWeight.Bold)
        }
        
        if (filteredCount == 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please load questions first before starting the quiz.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun QuizInProgressScreen(
    state: QuizState.InProgress,
    onAnswerLogged: (Boolean) -> Unit,
    onQuit: () -> Unit
) {
    val currentQuestion = state.quizQuestions[state.currentIndex]
    var showAnswer by remember(state.currentIndex) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question ${state.currentIndex + 1} of ${state.quizQuestions.size}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onQuit) {
                Text("Quit Quiz", color = MaterialTheme.colorScheme.error)
            }
        }

        val progress = (state.currentIndex).toFloat() / state.quizQuestions.size
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Category tag
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = currentQuestion.category,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Question Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentQuestion.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Show Answer Trigger
            Button(
                onClick = { showAnswer = !showAnswer },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showAnswer) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (showAnswer) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (showAnswer) "Hide Answer" else "Show Answer")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Answer Content with AnimatedVisibility
            AnimatedVisibility(
                visible = showAnswer,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Answer",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = currentQuestion.answer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Correct / Incorrect Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { onAnswerLogged(false) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Incorrect", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { onAnswerLogged(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981), // Emerald Green
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Correct", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuizFinishedScreen(
    state: QuizState.Finished,
    onRetry: () -> Unit
) {
    val scorePercentage = if (state.totalCount > 0) {
        (state.correctCount.toFloat() / state.totalCount * 100).toInt()
    } else 0

    val (ratingText, ratingColor) = when {
        scorePercentage >= 80 -> "Excellent! You're ready!" to Color(0xFF10B981)
        scorePercentage >= 50 -> "Good Job! Keep practicing." to MaterialTheme.colorScheme.primary
        else -> "Needs Practice. Try again!" to MaterialTheme.colorScheme.error
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Quiz Completed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Score Circular display
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(150.dp)
                ) {
                    CircularProgressIndicator(
                        progress = scorePercentage / 100f,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 12.dp,
                        color = ratingColor,
                        trackColor = ratingColor.copy(alpha = 0.1f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$scorePercentage%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ratingColor
                        )
                        Text(
                            text = "${state.correctCount} / ${state.totalCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = ratingText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ratingColor,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Stats breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatDisplay(label = "Correct", value = "${state.correctCount}", color = Color(0xFF10B981))
                    StatDisplay(label = "Incorrect", value = "${state.incorrectCount}", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Retry Quiz", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatDisplay(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
