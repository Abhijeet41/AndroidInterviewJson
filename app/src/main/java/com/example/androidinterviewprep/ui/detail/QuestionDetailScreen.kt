package com.example.androidinterviewprep.ui.detail

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidinterviewprep.data.model.Question
import com.example.androidinterviewprep.viewmodel.QuestionViewModel

@Composable
fun QuestionDetailScreen(
    categoryName: String,
    questionId: Int,
    viewModel: QuestionViewModel,
    onBackClick: () -> Unit,
    onNavigateToQuestion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val reviewedIds by viewModel.reviewedQuestionIds.collectAsState()
    val questions = remember(categoryName, viewModel.questions.collectAsState().value) {
        viewModel.getByCategory(categoryName)
    }
    
    val currentIndex = remember(questions, questionId) {
        questions.indexOfFirst { it.id == questionId }
    }
    
    val currentQuestion = remember(questions, currentIndex) {
        if (currentIndex != -1) questions[currentIndex] else null
    }

    val hasPrevious = currentIndex > 0
    val hasNext = currentIndex != -1 && currentIndex < questions.size - 1

    QuestionDetailContent(
        categoryName = categoryName,
        currentQuestion = currentQuestion,
        isReviewed = currentQuestion?.let { it.id in reviewedIds } ?: false,
        hasPrevious = hasPrevious,
        hasNext = hasNext,
        onBackClick = onBackClick,
        onToggleReviewed = { currentQuestion?.let { viewModel.toggleReviewed(it.id) } },
        onNavigateToPrevious = { 
            if (hasPrevious) onNavigateToQuestion(questions[currentIndex - 1].id) 
        },
        onNavigateToNext = { 
            if (hasNext) onNavigateToQuestion(questions[currentIndex + 1].id) 
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDetailContent(
    categoryName: String,
    currentQuestion: Question?,
    isReviewed: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onBackClick: () -> Unit,
    onToggleReviewed: () -> Unit,
    onNavigateToPrevious: () -> Unit,
    onNavigateToNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Question Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (currentQuestion == null) {
            EmptyQuestionState(Modifier.padding(paddingValues))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                QuestionMainContent(
                    modifier = Modifier.weight(1f),
                    categoryName = categoryName,
                    questionText = currentQuestion.question,
                    isReviewed = isReviewed,
                    onToggleReviewed = onToggleReviewed,
                    answerText = currentQuestion.answer,
                    questionId = currentQuestion.id
                )

                Spacer(modifier = Modifier.height(16.dp))

                DetailNavigationRow(
                    hasPrevious = hasPrevious,
                    hasNext = hasNext,
                    onNavigateToPrevious = onNavigateToPrevious,
                    onNavigateToNext = onNavigateToNext
                )
            }
        }
    }
}

@Composable
private fun EmptyQuestionState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Question not found.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuestionMainContent(
    categoryName: String,
    questionText: String,
    isReviewed: Boolean,
    onToggleReviewed: () -> Unit,
    answerText: String,
    questionId: Int,
    modifier: Modifier = Modifier
) {
    var showAnswer by remember(questionId) { mutableStateOf(false) }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        // Category Tag Badge
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Question Text Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = questionText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reviewed toggle row
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Checkbox(
                    checked = isReviewed,
                    onCheckedChange = { onToggleReviewed() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mark as Reviewed",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show/Hide Answer Button
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
            Text(
                text = if (showAnswer) "Hide Answer" else "Show Answer",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Answer Card with AnimatedVisibility
        AnimatedVisibility(
            visible = showAnswer,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
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
                        text = answerText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailNavigationRow(
    hasPrevious: Boolean,
    hasNext: Boolean,
    onNavigateToPrevious: () -> Unit,
    onNavigateToNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onNavigateToPrevious,
            enabled = hasPrevious,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                contentDescription = "Previous Question"
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Previous")
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = onNavigateToNext,
            enabled = hasNext,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
        ) {
            Text("Next")
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                contentDescription = "Next Question"
            )
        }
    }
}

private const val PREVIEW_CATEGORY = "Android Basics"

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewQuestionDetailContent() {
    val mockQuestion = Question(
        id = 1,
        category = PREVIEW_CATEGORY,
        question = "What is the life cycle of an activity in Android?",
        answer = "The lifecycle of an activity includes onCreate(), onStart(), onResume(), onPause(), onStop(), onDestroy(), and onRestart()."
    )
    MaterialTheme {
        Surface {
            QuestionDetailContent(
                categoryName = PREVIEW_CATEGORY,
                currentQuestion = mockQuestion,
                isReviewed = false,
                hasPrevious = true,
                hasNext = true,
                onBackClick = {},
                onToggleReviewed = {},
                onNavigateToPrevious = {},
                onNavigateToNext = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewQuestionDetailNotFound() {
    MaterialTheme {
        Surface {
            QuestionDetailContent(
                categoryName = PREVIEW_CATEGORY,
                currentQuestion = null,
                isReviewed = false,
                hasPrevious = false,
                hasNext = false,
                onBackClick = {},
                onToggleReviewed = {},
                onNavigateToPrevious = {},
                onNavigateToNext = {}
            )
        }
    }
}
