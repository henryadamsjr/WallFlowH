package com.ammar.havenwalls.data.network.model

import com.ammar.havenwalls.data.common.Purity
import com.ammar.havenwalls.data.db.entity.TagEntity
import com.ammar.havenwalls.data.network.model.util.InstantSerializer
import com.ammar.havenwalls.extensions.trimAll
import com.ammar.havenwalls.model.Tag
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class NetworkTag(
    val id: Long,
    val name: String,
    val alias: String,
    val category_id: Long,
    val category: String,
    val purity: String,
    @Serializable(InstantSerializer::class)
    val created_at: Instant,
)

fun NetworkTag.asTagEntity(id: Long = 0) = TagEntity(
    id = id,
    wallhavenId = this.id,
    name = name,
    alias = alias,
    categoryId = category_id,
    category = category,
    purity = Purity.fromName(purity),
    createdAt = created_at,
)

fun NetworkTag.toTag() = Tag(
    id = id,
    name = name,
    alias = alias.split(",").map { it.trimAll() },
    categoryId = category_id,
    category = category,
    purity = Purity.fromName(purity),
    createdAt = created_at,
)
