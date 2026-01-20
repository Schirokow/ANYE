package org.example.anye.data

import androidx.annotation.Keep

@Keep
data class Event(
    val userId: String? = null,
    val imageUrl: String? = null,
    val title: String? = null,
    val description: String? = null,
    val startData: String? = null,
    val city: String? = null,
    val location: Location? = null
){
    // Manueller leerer Konstruktor für Firebase
    constructor() : this(null, null, null, null, null, null, null)
}
