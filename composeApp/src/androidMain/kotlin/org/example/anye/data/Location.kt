package org.example.anye.data

import androidx.annotation.Keep

@Keep
data class Location(
    val longitude: String? = null,
    val latitude: String? = null
){
    // Manueller leerer Konstruktor für Firebase
    constructor() : this(null, null)
}

