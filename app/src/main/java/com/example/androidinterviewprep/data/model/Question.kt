package com.example.androidinterviewprep.data.model

import com.google.gson.annotations.SerializedName

data class Question(
    @SerializedName("id")
    val id: Int,
    @SerializedName("category")
    val category: String,
    @SerializedName("question")
    val question: String,
    @SerializedName("answer")
    val answer: String
)
