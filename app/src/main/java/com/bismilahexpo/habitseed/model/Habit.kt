package com.bismilahexpo.habitseed.model

data class Habit(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val goal: String,
    var isCompleted: Boolean = false
)
