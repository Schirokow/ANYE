package org.example.anye.data.di

import org.example.anye.data.EventsRepository
import org.example.anye.data.EventsRepositoryImpl
import org.example.anye.data.FavoriteRepository
import org.example.anye.data.FavoriteRepositoryImpl
import org.example.anye.data.FirestoreService
import org.example.anye.data.TicketmasterApiService

import org.example.anye.httpClient
import org.example.anye.usecases.GetEventByIdUseCase
import org.example.anye.usecases.GetEventsUseCase
import org.example.anye.usecases.GetFavoriteUseCase
import org.example.anye.usecases.GetLoginServiceUseCase

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

// Dies ist die initKoin-Funktion, die von den nativen Plattformen aufgerufen wird.
fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            commonModule,
            platformModule()
        )
    }

// Diese Funktion ist nur ein "Eintrittspunkt" für Swift/ObjC
fun doInitKoin() = initKoin()
/*
@main
struct iOSApp: App{
  init(){
  initKoinKT.doInitKoin()
  }

  var body: some Scene{
    WindowGroup{
      ContentView()
    }
  }
}
 */

// Definiert die gemeinsamen (common) Abhängigkeiten für alle Plattformen
val commonModule = module {

    // Ktor HttpClient
    single { httpClient }

    // Services
    single { TicketmasterApiService(get()) }


    single<EventsRepository> { EventsRepositoryImpl(get()) }
    single { GetEventsUseCase() }
    single { GetEventByIdUseCase(get()) }

    single <FavoriteRepository>{ FavoriteRepositoryImpl(get()) }
    single { GetFavoriteUseCase() }


    single { GetLoginServiceUseCase(get()) }



}

// Erwartete (expect) Funktion für plattformspezifische Module
expect fun platformModule(): org.koin.core.module.Module

