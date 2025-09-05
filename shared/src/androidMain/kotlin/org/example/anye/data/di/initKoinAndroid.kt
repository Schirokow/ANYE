package org.example.anye.data.di

import org.example.anye.data.FavoriteDatabase
import org.example.anye.data.FavoriteRepository
import org.example.anye.data.FavoriteRepositoryImpl
import org.example.anye.data.GetAndroidFavoriteDatabase
import org.example.anye.data.dao.FavoriteDao
import org.example.anye.viewmodels.ContentDetailViewModel
import org.example.anye.viewmodels.FavoriteViewModel
import org.example.anye.viewmodels.HomeViewModel
import org.example.anye.viewmodels.LoginViewModel
import org.example.anye.viewmodels.Profile1ViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

actual fun platformModule() = module {
    // Hier wird die plattformspezifische Datenbank bereitgestellt
    single { GetAndroidFavoriteDatabase.getDatabase(androidContext()) }

    // FavoriteDao aus der Datenbank bereitstellen
    single<FavoriteDao> { get<FavoriteDatabase>().favoriteDao() }

    single<FavoriteRepository> { FavoriteRepositoryImpl(get()) }


    viewModel { Profile1ViewModel(getUsersUseCase = get()) }
    viewModel { FavoriteViewModel(favoriteRepository = get()) }
    viewModel { LoginViewModel(getUsersUseCase = get()) }
    viewModel { HomeViewModel(favoriteRepository = get(), getEventsUseCase = get()) }
    viewModel {  ContentDetailViewModel(
        favoriteRepository = get(),
        eventsUseCase = get(),
        getEventByIdUseCase = get()
    ) }


}