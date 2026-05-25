package com.example.sos.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

// ── Domain ────────────────────────────────────────────────────────────────────

/**
 * Contract for all authentication operations.
 */
interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        phone: String
    ): Result<String>
}

// ── Firebase implementation ───────────────────────────────────────────────────

class FirebaseAuthRepository(
    private val authProvider: () -> FirebaseAuth = { FirebaseAuth.getInstance() },
    private val databaseProvider: () -> FirebaseDatabase? = {
        try { FirebaseDatabase.getInstance() } catch (e: Exception) { null }
    }
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<String> = runCatching {
        authProvider().signInWithEmailAndPassword(email.trim(), password).await()
        DESTINATION_DASHBOARD
    }.mapFailureMessage()

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        phone: String
    ): Result<String> = runCatching {
        val result = authProvider().createUserWithEmailAndPassword(email.trim(), password).await()
        val uid = result.user?.uid ?: error("UID missing after registration")

        databaseProvider()?.reference
            ?.child("users")
            ?.child(uid)
            ?.setValue(
                mapOf(
                    "displayName" to displayName.trim(),
                    "email" to email.trim(),
                    "phone" to phone.trim(),
                    "createdAt" to System.currentTimeMillis()
                )
            )?.await()

        DESTINATION_DASHBOARD
    }.mapFailureMessage()

    companion object {
        const val DESTINATION_DASHBOARD = "dashboard"
    }
}

// ── Extension helper ──────────────────────────────────────────────────────────

private fun <T> Result<T>.mapFailureMessage(): Result<T> =
    fold(
        onSuccess = { Result.success(it) },
        onFailure = { e ->
            val message = if (e.message.isNullOrBlank()) {
                "An unexpected error occurred. Please try again."
            } else {
                e.message!!
            }
            Result.failure(Exception(message))
        }
    )
