package org.example.anye.data

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class Event(
    var id: String? = null,
    var userId: String? = null,
    var imageUrl: String? = null,
    var title: String? = null,
    var description: String? = null,
    var startData: String? = null,
    var city: String? = null,
    var location: Location? = null,

)
