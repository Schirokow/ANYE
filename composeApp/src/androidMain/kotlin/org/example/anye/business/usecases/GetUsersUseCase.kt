package org.example.anye.business.usecases

import org.example.anye.data.User
import org.example.anye.data.UsersRepositoryImpl
import kotlinx.coroutines.flow.Flow

class GetUsersUseCase {
    private val users = UsersRepositoryImpl()

    fun getUsersFlow(): Flow<List<User>> {
        return users.getUsersFlow()
    }

    fun getUserByIdFlow(id: Int): Flow<User?> {
        return users.getUserByIdFlow(id)
    }

    fun addUser(user: User){
        users.addUser(user)
    }


}