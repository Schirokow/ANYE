package org.example.anye.data.di

import org.example.anye.data.FavoriteDatabase
import org.example.anye.data.FavoriteRepository
import org.example.anye.data.FavoriteRepositoryImpl
import org.example.anye.data.GetIOSFavoriteDatabase
import org.example.anye.data.LoginService
import org.example.anye.data.LoginServiceiOS
import org.example.anye.data.dao.FavoriteDao
import org.example.anye.viewmodels.ContentDetailViewModel
import org.example.anye.viewmodels.FavoriteViewModel
import org.example.anye.viewmodels.HomeViewModel
import org.example.anye.viewmodels.LoginViewModel
import org.example.anye.viewmodels.Profile1ViewModel
import org.koin.dsl.module

actual fun platformModule() = module {
    // iOS-spezifische Abhängigkeiten.

    // iOS DB bereitstellen
    single { GetIOSFavoriteDatabase.getDatabase() }

    // Dao bereitstellen
    single<FavoriteDao> { get<FavoriteDatabase>().favoriteDao() }

    // Repo
    single<FavoriteRepository> { FavoriteRepositoryImpl(get()) }

    single<LoginService> { LoginServiceiOS() }

    // ViewModels registrieren:
    single { HomeViewModel(getFavoriteUseCase = get(), getEventsUseCase = get()) }
    single { FavoriteViewModel(getFavoriteUseCase = get()) }
    single { LoginViewModel(getUsersUseCase = get(), getLoginServiceUseCase = get()) }
    single { Profile1ViewModel(getUsersUseCase = get()) }
    single {  ContentDetailViewModel(
        getFavoriteUseCase = get(),
        eventsUseCase = get(),
        getEventByIdUseCase = get()
    ) }

}

/*

Swift: Beispiel Zugriff auf Shared-ViewModel (FavoriteViewModel)

import shared
import Combine

class IOSFavoriteViewModel: ObservableObject {
    private let viewModel: FavoriteViewModel

    @Published var favorites: [Favorite] = []

    private var cancellable: AnyCancellable?

    init() {
        // Hole Shared-ViewModel aus Koin
        self.viewModel = KoinKt.koin.get(objCClass: FavoriteViewModel.self) as! FavoriteViewModel

        // StateFlow -> Combine Publisher konvertieren
        viewModel.favoriteEvents.watch { favorites in
                if let favs = favorites as? [Favorite] {
                    self.favorites = favs
                }
        }
    }

    func toggleFavorite(favorite: Favorite) {
        viewModel.toggleFavorite(favoriteEvent: favorite)
    }

    func deleteAllFavorites() {
        viewModel.deleteAllFavorites()
    }
}

------------------------------------------------------------------------------------------------
SwiftUI View mit Shared-ViewModel(FavoriteViewModel)

import SwiftUI
import shared

struct FavoritesView: View {
    @StateObject private var viewModel = IOSFavoriteViewModel()

    var body: some View {
        NavigationView {
            List(viewModel.favorites, id: \.eventId) { favorite in
                VStack(alignment: .leading) {
                    Text(favorite.name)
                        .font(.headline)
                    Text(favorite.venueName)
                        .font(.subheadline)
                }
                .onTapGesture {
                    viewModel.toggleFavorite(favorite: favorite)
                }
            }
            .navigationTitle("Favorites")
            .toolbar {
                Button("Delete All") {
                    viewModel.deleteAllFavorites()
                }
            }
        }
    }
}

 */