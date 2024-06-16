package com.example.mykumve.ui.register

import android.content.Context
import android.util.Base64
import com.example.mykumve.data.model.User
import com.example.mykumve.data.repository.RepositoryProvider
import com.example.mykumve.data.repository.UserRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.SecureRandom

class RegisterManagerHelper(private val context: Context) {

    private val userRepository: UserRepository by lazy {
        RepositoryProvider.getUserRepository(context)
    }

    fun registerUser(username: String, password: String, callback: (Boolean) -> Unit) {
        GlobalScope.launch {
            if (userRepository.getUserByUsername(username) != null) {
                callback(false)
                return@launch
            }
            val salt = generateSalt()
            val passwordHash = hashPassword(password, salt)
            val newUser = User(username = username, passwordHash = passwordHash, salt = salt)
            userRepository.insertUser(newUser)
            callback(true)
        }
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.DEFAULT)
    }

    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(Base64.decode(salt, Base64.DEFAULT))
        val hashedBytes = digest.digest(password.toByteArray())
        return Base64.encodeToString(hashedBytes, Base64.DEFAULT)
    }
}