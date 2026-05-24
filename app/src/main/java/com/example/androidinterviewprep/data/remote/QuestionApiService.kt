package com.example.androidinterviewprep.data.remote

import retrofit2.http.GET

interface QuestionApiService {
    @GET("Abhijeet41/AndroidInterviewJson/refs/heads/main/interview_questions.json")
    suspend fun fetchQuestionsJson(): String
}
