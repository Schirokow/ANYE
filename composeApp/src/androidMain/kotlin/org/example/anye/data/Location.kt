package org.example.anye.data

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class Location(
    var longitude: String? = null,
    var latitude: String? = null
)

