package com.example.sos.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

// ── Domain ────────────────────────────────────────────────────────────────────

/**
 * Contract for all authentication operations.
 * Keeping this interface separate allows fake implementations in unit tests.
 */
interface AuthRepository {
    /**
     * Signs in with email + password.
     * @return [Result.success] with destination route, or [Result.failure] with a user-readable message.
     */
    suspend fun signIn(email: String, password: String): Result<String>

    /**
     * Creates a new account and persists the display name to Realtime Database.
     * @return [Result.success] with destination route, or [Result.failure] with a user-readable message.
     */
    suspend fun register(email: String, password: String, displayName: String): Result<String>
}

// ── Firebase implementation ───────────────────────────────────────────────────

/**
 * Production implementation backed by Firebase Auth and Realtime Database.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<String> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        DESTINATION_DASHBOARD
    }.mapFailureMessage()

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<String> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val uid = result.user?.uid ?: error("UID missing after registration")
        database.reference
            .child("users")
            .child(uid)
            .setValue(
                mapOf(
                    "displayName" to displayName.trim(),
                    "email" to email.trim()
                )
            ).await()
        DESTINATION_DASHBOARD
    }.mapFailureMessage()

    companion object {
        const val DESTINATION_DASHBOARD = "dashboard"
    }
}

// ── Extension helper ──────────────────────────────────────────────────────────

/**
 * Maps any exception thrown inside [runCatching] to a concise, user-readable failure message
 * while preserving the [Result.success] value unchanged.
 */
private fun <T> Result<T>.mapFailureMessage(): Result<T> =
    fold(
        onSuccess = { Result.success(it) },
        onFailure = { e ->
            val message = when {
                e.message.isNullOrBlank() -> "An unexpected error occurred. Please try again."
                else -> e.message!!.removePrefix("com.google.firebase.auth.")
                    .replace(Regex("([A-Z])"), " $1")
                    .trim()
                    .replaceFirstChar { it.uppercaseChar() }
            }
            Result.failure(Exception(message))
        }
    )
