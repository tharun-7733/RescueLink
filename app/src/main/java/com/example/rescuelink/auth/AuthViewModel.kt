package com.example.rescuelink.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class Success(val destination: String) : AuthUiState()
}

// ── Form State ────────────────────────────────────────────────────────────────

data class AuthFormState(
    // Shared fields
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,

    // Register-only fields
    val displayName: String = "",
    val displayNameError: String? = null,
    val phone: String = "",
    val phoneError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isConfirmPasswordVisible: Boolean = false,
    val termsAccepted: Boolean = false,

    // Password strength (0 = empty, 1–4 = weak…very strong)
    val passwordStrength: Int = 0,

    // Only show inline errors after first submit attempt
    val hasAttemptedSubmit: Boolean = false
) {
    val isLoginFormValid: Boolean
        get() = emailError == null && passwordError == null &&
                email.isNotBlank() && password.isNotBlank()

    val isRegisterFormValid: Boolean
        get() = isLoginFormValid &&
                displayNameError == null && phoneError == null &&
                confirmPasswordError == null &&
                displayName.isNotBlank() && phone.isNotBlank() &&
                confirmPassword.isNotBlank() && termsAccepted
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode: StateFlow<Boolean> = _isLoginMode.asStateFlow()

    // ── Tab toggle ────────────────────────────────────────────────────────

    fun onToggleMode() {
        _isLoginMode.update { !it }
        _formState.update { AuthFormState() } // reset form on tab switch
        _uiState.update { AuthUiState.Idle }
    }

    // ── Field handlers ────────────────────────────────────────────────────

    fun onEmailChanged(value: String) {
        _formState.update { it.copy(email = value) }
        if (_formState.value.hasAttemptedSubmit) validateForm()
    }

    fun onPasswordChanged(value: String) {
        _formState.update { it.copy(password = value, passwordStrength = computeStrength(value)) }
        if (_formState.value.hasAttemptedSubmit) validateForm()
    }

    fun onDisplayNameChanged(value: String) {
        _formState.update { it.copy(displayName = value) }
        if (_formState.value.hasAttemptedSubmit) validateForm()
    }

    fun onPhoneChanged(value: String) {
        _formState.update { it.copy(phone = value) }
        if (_formState.value.hasAttemptedSubmit) validateForm()
    }

    fun onConfirmPasswordChanged(value: String) {
        _formState.update { it.copy(confirmPassword = value) }
        if (_formState.value.hasAttemptedSubmit) validateForm()
    }

    fun onTogglePasswordVisibility() {
        _formState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onToggleConfirmPasswordVisibility() {
        _formState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onTermsToggle() {
        _formState.update { it.copy(termsAccepted = !it.termsAccepted) }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) _uiState.update { AuthUiState.Idle }
    }

    // ── Auth actions ──────────────────────────────────────────────────────

    fun signIn() {
        _formState.update { it.copy(hasAttemptedSubmit = true) }
        validateForm()
        val form = _formState.value
        if (!form.isLoginFormValid) return

        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }
            repository.signIn(form.email, form.password)
                .onSuccess { dest -> _uiState.update { AuthUiState.Success(dest) } }
                .onFailure { e -> _uiState.update { AuthUiState.Error(e.message ?: "Sign in failed") } }
        }
    }

    fun register() {
        _formState.update { it.copy(hasAttemptedSubmit = true) }
        validateForm()
        val form = _formState.value
        if (!form.isRegisterFormValid) return

        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }
            repository.register(form.email, form.password, form.displayName, form.phone)
                .onSuccess { dest -> _uiState.update { AuthUiState.Success(dest) } }
                .onFailure { e -> _uiState.update { AuthUiState.Error(e.message ?: "Registration failed") } }
        }
    }

    // ── Validation ────────────────────────────────────────────────────────

    fun validateForm() {
        val form = _formState.value
        val showErrors = form.hasAttemptedSubmit
        val isRegister = !_isLoginMode.value

        _formState.update {
            it.copy(
                emailError = if (showErrors) validateEmail(form.email) else null,
                passwordError = if (showErrors) validatePassword(form.password) else null,
                displayNameError = if (showErrors && isRegister) validateDisplayName(form.displayName) else null,
                phoneError = if (showErrors && isRegister) validatePhone(form.phone) else null,
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

    private fun validatePhone(phone: String): String? = when {
        phone.isBlank() -> "Phone number is required"
        phone.length < 6 -> "Enter a valid phone number"
        else -> null
    }

    private fun validateConfirmPassword(password: String, confirm: String): String? = when {
        confirm.isBlank() -> "Please confirm your password"
        confirm != password -> "Passwords do not match"
        else -> null
    }

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

    companion object {
        private val EMAIL_REGEX = Regex(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
        )

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(FirebaseAuthRepository()) as T
            }
        }
    }
}
