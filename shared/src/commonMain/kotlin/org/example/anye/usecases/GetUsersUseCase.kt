package org.example.anye.usecases

import kotlinx.coroutines.flow.Flow
import org.example.anye.data.User
import org.example.anye.data.UsersRepository

class GetUsersUseCase(private val users: UsersRepository) {
//    private val users = UsersRepositoryImpl()

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