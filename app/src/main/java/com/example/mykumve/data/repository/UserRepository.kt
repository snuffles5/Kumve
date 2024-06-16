package com.example.mykumve.data.repository

import com.example.mykumve.data.model.User

/**
 * Repository interface for user-related data operations.
 * Abstracts data source for user operations.
 *
 * TODO: Define methods for all required user data operations.
 */
interface UserRepository {
    suspend fun getUserByUsername(username: String): User?
    suspend fun insertUser(user: User): Long
}