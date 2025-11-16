package org.example.anye.presentation.map

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.TranslateAnimation
import android.widget.TextView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import org.example.anye.R

class CustomMarkerInfoWindow(mapView: MapView) :
    MarkerInfoWindow(R.layout.custom_marker_bubble, mapView) {

    private var isAnimating = false

    override fun onOpen(item: Any?) {
        val marker = item as? Marker ?: return
        val titleView = mView.findViewById<TextView>(R.id.bubble_title)
        val snippetView = mView.findViewById<TextView>(R.id.bubble_snippet)

        // Hole IMMER aktuelle Werte aus Marker (nicht aus gespeichertem State)
        titleView.text = marker.title ?: "Unbenanntes Event"
        snippetView.text = marker.snippet ?: "Entfernung wird berechnet..."

        // Stelle sicher, dass die View sichtbar ist
        mView.visibility = View.VISIBLE

        // Sanftes Einblenden + Hochgleiten
        mView.clearAnimation()
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 300
            fillAfter = true
        }
        val slideUp = TranslateAnimation(0f, 0f, 40f, 0f).apply {
            duration = 300
            fillAfter = true
        }
        mView.startAnimation(fadeIn)
        mView.startAnimation(slideUp)
    }

    override fun onClose() {
        if (isAnimating) return
        isAnimating = true

        val fadeOut = AlphaAnimation(1f, 0f).apply {
            duration = 200
            fillAfter = false // nicht „stehenlassen“
        }

        fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                // nicht GONE setzen, OSMDroid kümmert sich selbst
                isAnimating = false
            }
        })

        mView.startAnimation(fadeOut)
    }
}