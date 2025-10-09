package org.example.anye.data.di

import org.example.anye.data.FavoriteDatabase
import org.example.anye.data.FavoriteRepository
import org.example.anye.data.FavoriteRepositoryImpl
import org.example.anye.data.FirestoreService
import org.example.anye.data.FirestoreServiceAndroid
import org.example.anye.data.GetAndroidFavoriteDatabase
import org.example.anye.data.LoginService
import org.example.anye.data.LoginServiceAndroid
import org.example.anye.data.dao.FavoriteDao
import org.example.anye.usecases.GetLoginServiceUseCase
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

    single<LoginService> { LoginServiceAndroid(get()) }
    single<FirestoreService> { FirestoreServiceAndroid() }

    // ViewModels Registrieren:
    viewModel { Profile1ViewModel() }
    viewModel { FavoriteViewModel(getFavoriteUseCase = get()) }
    viewModel { LoginViewModel(getLoginServiceUseCase = get()) }
    viewModel { HomeViewModel(getFavoriteUseCase = get(), getEventsUseCase = get()) }
    viewModel {  ContentDetailViewModel(
        getFavoriteUseCase = get(),
        eventsUseCase = get(),
        getEventByIdUseCase = get()
    ) }


}