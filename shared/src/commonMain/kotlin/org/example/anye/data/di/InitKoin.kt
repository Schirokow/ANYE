package org.example.anye.data.di

import org.example.anye.data.EventByIdData
import org.example.anye.data.EventByIdImplFlow
import org.example.anye.data.EventsRepository
import org.example.anye.data.EventsRepositoryImpl
import org.example.anye.data.UsersRepository
import org.example.anye.data.UsersRepositoryImpl
import org.example.anye.usecases.GetEventByIdUseCase
import org.example.anye.usecases.GetEventsUseCase
import org.example.anye.usecases.GetUsersUseCase
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

// Definiert die gemeinsamen (common) Abhängigkeiten für alle Plattformen
val commonModule = module {

    single<EventByIdData> { EventByIdImplFlow() }
    single { GetEventByIdUseCase(get()) }

    single<EventsRepository> { EventsRepositoryImpl() }
    single { GetEventsUseCase(get()) }

    // Usecases sind plattformunabhängig und verwenden das Repository
    single<UsersRepository> { UsersRepositoryImpl() }
    single { GetUsersUseCase(get()) }



}

// Erwartete (expect) Funktion für plattformspezifische Module
expect fun platformModule(): org.koin.core.module.Module