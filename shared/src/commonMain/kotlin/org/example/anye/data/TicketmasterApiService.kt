package org.example.anye.data


import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.example.anye.data.ticketmaster_data_classes.TicketmasterEvent
import org.example.anye.data.ticketmaster_data_classes.TicketmasterSearchResponse
import org.example.anye.logMessage

private val BASE_URL = "https://app.ticketmaster.com/discovery/v2/"

private val API_KEY: String = "X0B57u3BuSKfCFLvWjCPRoFMJtA5xiVQ"

class TicketmasterApiService(private val client: HttpClient) {
    suspend fun loadEvents(
        city: String, // Hinzufügen einer Standard-Stadt
        countryCode: String = "DE"
    ): List<TicketmasterEvent> {
        return try {
            withContext(Dispatchers.IO) {
                val response: TicketmasterSearchResponse = client.get("${BASE_URL}events.json") {
                    parameter("apikey", API_KEY)
                    parameter("city", city) // Default-Stadt-Parameter hinzufügen
                    parameter("countryCode", countryCode) // Default-Länder-Code hinzufügen
                    logMessage("suspend fun loadEvents in TicketmasterApiService used")
                }.body()
                // Hier wird die Liste aus dem verschachtelten Objekt extrahiert
                response._embedded?.events ?: emptyList()
            }
        } catch (e: Exception) {
            logMessage("Fehler beim laden von Events: ${e.message}")
            e.printStackTrace() // Wichtig, um den genauen Fehler im Logcat zu sehen
            emptyList()
        }
    }

    suspend fun getEventById(eventId: String): TicketmasterEvent? {
        return try {
            withContext(Dispatchers.IO) {
                val response: TicketmasterEvent = client.get("${BASE_URL}events/$eventId.json") {
                    parameter("apikey", API_KEY)
                }.body()
                response
            }
        } catch (e: Exception) {
            logMessage("TicketmasterApiService, Error loading event with ID $eventId: ${e.message}")
            null
        }
    }

}