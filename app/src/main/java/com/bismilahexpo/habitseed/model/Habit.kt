package com.bismilahexpo.habitseed.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Serializable
data class Habit(
    val id: String? = null,
    
    @SerialName("user_id")
    val userId: String = "",
    
    val name: String = "",
    val goal: String = "",
    
    @SerialName("is_completed")
    var isCompleted: Boolean = false,
    
    @SerialName("evidence_uri")
    var evidenceUri: String? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null
)
