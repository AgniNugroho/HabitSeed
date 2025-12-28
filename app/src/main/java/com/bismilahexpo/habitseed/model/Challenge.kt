package com.bismilahexpo.habitseed.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Challenge(
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    @SerialName("icon_type")
    val iconType: String = "fitness"
)

@Serializable
data class UserChallenge(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("challenge_id")
    val challengeId: String = "",
    @SerialName("completed_at")
    val completedAt: String = "",
    @SerialName("evidence_uri")
    val evidenceUri: String? = null
)
