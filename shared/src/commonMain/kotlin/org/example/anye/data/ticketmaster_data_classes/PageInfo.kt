package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class PageInfo(
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)
