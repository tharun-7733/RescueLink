package com.example.sos.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── UI State (sealed) ─────────────────────────────────────────────────────────

/**
 * Exhaustive set of states the Auth screen can be in.
 * Collected in Compose via collectAsStateWithLifecycle().
 */
sealed class AuthUiState {
    /** No pending operation — initial state and state after clearing an error. */
    object Idle : AuthUiState()

    /** A sign-in or register coroutine is in-flight. */
    object Loading : AuthUiState()

    /** The operation failed; [message] is a user-readable explanation. */
    data class Error(val message: String) : AuthUiState()

    /** The operation succeeded; [destination] is the route to navigate to. */
    data class Success(val destination: String) : AuthUiState()
}

// ── Form State ────────────────────────────────────────────────────────────────

/**
 * All mutable data that belongs to the auth form.
 * Stored as a single data class so it can be copied atomically.
 */
data class AuthFormState(
    // Login fields
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,

    // Register-only fields
    val displayName: String = "",
    val displayNameError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isConfirmPasswordVisible: Boolean = false,

    // Password strength (0 = empty, 1 = weak … 4 = very strong)
    val passwordStrength: Int = 0,

    // Becomes true only after first submit attempt — before that we don't
    // show inline errors to avoid overwhelming a fresh form.
    val hasAttemptedSubmit: Boolean = false
) {
    /** True when all visible fields pass validation. */
    val isFormValid: Boolean
        get() = emailError == null && passwordError == null &&
                displayNameError == null && confirmPasswordError == null &&
                email.isNotBlank() && password.isNotBlank()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    // ── Field update handlers ─────────────────────────────────────────────

    fun onEmailChanged(value: String) {
        _formState.update { it.copy(email = value) }
        validateForm()
    }

    fun onPasswordChanged(value: String) {
        _formState.update { it.copy(password = value, passwordStrength = computeStrength(value)) }
        validateForm()
    }

    fun onDisplayNameChanged(value: String) {
        _formState.update { it.copy(displayName = value) }
        validateForm()
    }

    fun onConfirmPasswordChanged(value: String) {
        _formState.update { it.copy(confirmPassword = value) }
        validateForm()
    }

    fun onTogglePasswordVisibility() {
        _formState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onToggleConfirmPasswordVisibility() {
        _formState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.update { AuthUiState.Idle }
        }
    }

    // ── Auth actions ──────────────────────────────────────────────────────

    fun signIn() {
        _formState.update { it.copy(hasAttemptedSubmit = true) }
        validateForm()
        val form = _formState.value
        if (!form.isFormValid) return

        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }
            repository.signIn(form.email, form.password)
                .onSuccess { dest -> _uiState.update { AuthUiState.Success(dest) } }
                .onFailure { e -> _uiState.update { AuthUiState.Error(e.message ?: "Sign in failed") } }
        }
    }

    fun register() {
        _formState.update { it.copy(hasAttemptedSubmit = true) }
        validateForm(isRegister = true)
        val form = _formState.value
        if (!form.isFormValid || form.confirmPasswordError != null) return

        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }
            repository.register(form.email, form.password, form.displayName)
                .onSuccess { dest -> _uiState.update { AuthUiState.Success(dest) } }
                .onFailure { e -> _uiState.update { AuthUiState.Error(e.message ?: "Registration failed") } }
        }
    }

    // ── Validation ────────────────────────────────────────────────────────

    /**
     * Validates the current form state and writes inline error messages back to [_formState].
     * Called on every keystroke so errors are shown eagerly after the first submit attempt.
     */
    fun validateForm(isRegister: Boolean = false) {
        val form = _formState.value
        // Only expose inline errors after the user has tried to submit at least once.
        val showErrors = form.hasAttemptedSubmit

        _formState.update {
            it.copy(
                emailError = if (showErrors) validateEmail(form.email) else null,
                passwordError = if (showErrors) validatePassword(form.password) else null,
                displayNameError = if (showErrors && isRegister) validateDisplayName(form.displayName) else null,
                confirmPasswordError = if (showErrors && isRegister)
                    validateConfirmPassword(form.password, form.confirmPassword) else null
            )
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !EMAIL_REGEX.matches(email) -> "Enter a valid email address"
        else -> null
    }

    private fun validatePassword(password: String): String? = when {
        password.isBlank() -> "Password is required"
        password.length < 8 -> "Must be at least 8 characters"
        else -> null
    }

    private fun validateDisplayName(name: String): String? = when {
        name.isBlank() -> "Full name is required"
        name.trim().length < 2 -> "Name must be at least 2 characters"
        else -> null
    }

    private fun validateConfirmPassword(password: String, confirm: String): String? = when {
        confirm.isBlank() -> "Please confirm your password"
        confirm != password -> "Passwords do not match"
        else -> null
    }

    /**
     * Returns a score 0–4 for password strength:
     * 0 = empty, 1 = weak, 2 = fair, 3 = strong, 4 = very strong.
     */
    private fun computeStrength(password: String): Int {
        if (password.isEmpty()) return 0
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return score.coerceIn(1, 4)
    }

    // ── Factory ───────────────────────────────────────────────────────────

    /**
     * Provides AuthViewModel with a real [FirebaseAuthRepository].
     * Use this in production; inject a fake in tests.
     */
    companion object {
        /**
         * RFC-5321 compatible email pattern — pure JVM regex, safe in unit tests.
         * (android.util.Patterns is null outside the Android runtime.)
         */
        private val EMAIL_REGEX = Regex(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
        )

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(
                    FirebaseAuthRepository(
                        auth = FirebaseAuth.getInstance(),
                        database = FirebaseDatabase.getInstance()
                    )
                ) as T
            }
        }
    }
}
