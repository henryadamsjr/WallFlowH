package com.ammar.havenwalls.model

import com.ammar.havenwalls.data.common.Purity
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: Long,
    val name: String,
    val alias: List<String>,
    val categoryId: Long,
    val category: String,
    val purity: Purity,
    val createdAt: Instant,
)
