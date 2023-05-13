package com.ammar.havenwalls.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkResponse<T>(
    val data: T,
    val meta: NetworkMeta? = null,
)
