package com.example.rescuelink

import com.example.rescuelink.auth.AuthRepository
import com.example.rescuelink.auth.AuthUiState
import com.example.rescuelink.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// ── Fake repository ───────────────────────────────────────────────────────────

/**
 * In-memory fake that lets each test control success/failure independently.
 */
class FakeAuthRepository(
    private val signInResult: Result<String> = Result.success("dashboard"),
    private val registerResult: Result<String> = Result.success("dashboard")
) : AuthRepository {
    override suspend fun signIn(email: String, password: String): Result<String> = signInResult
    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        phone: String
    ): Result<String> = registerResult
}


// ── Tests ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ─────────────────────────────────────────────────────

    @Test
    fun `initial uiState is Idle`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository())
        assertTrue(viewModel.uiState.value is AuthUiState.Idle)
    }

    @Test
    fun `initial formState has empty fields`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository())
        val form = viewModel.formState.value
        assertEquals("", form.email)
        assertEquals("", form.password)
        assertEquals("", form.displayName)
        assertEquals("", form.confirmPassword)
    }

    // ── Sign-in success ───────────────────────────────────────────────────

    @Test
    fun `signIn with valid credentials transitions to Success`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository(signInResult = Result.success("dashboard")))
        // Populate valid fields
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("Password1!")
        viewModel.signIn()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Success but was $state", state is AuthUiState.Success)
        assertEquals("dashboard", (state as AuthUiState.Success).destination)
    }

    // ── Sign-in failure ───────────────────────────────────────────────────

    @Test
    fun `signIn failure sets Error state`() = runTest {
        val viewModel = AuthViewModel(
            FakeAuthRepository(signInResult = Result.failure(Exception("Wrong password")))
        )
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("Password1!")
        viewModel.signIn()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Error but was $state", state is AuthUiState.Error)
        assertEquals("Wrong password", (state as AuthUiState.Error).message)
    }

    // ── Validation guards ─────────────────────────────────────────────────

    @Test
    fun `signIn with blank email does not reach repository`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository())
        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("Password1!")
        viewModel.signIn()
        advanceUntilIdle()

        // Should stay Idle because validation blocked the call
        assertTrue(viewModel.uiState.value is AuthUiState.Idle)
    }

    @Test
    fun `signIn with short password does not reach repository`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository())
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("short")
        viewModel.signIn()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AuthUiState.Idle)
        assertEquals(
            "Must be at least 8 characters",
            viewModel.formState.value.passwordError
        )
    }

    @Test
    fun `signIn with invalid email format shows email error`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository())
        viewModel.onEmailChanged("not-an-email")
        viewModel.onPasswordChanged("Password1!")
        viewModel.signIn()
        advanceUntilIdle()

        assertEquals("Enter a valid email address", viewModel.formState.value.emailError)
    }

    // ── Register success ──────────────────────────────────────────────────

    @Test
    fun `register with valid fields transitions to Success`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository(registerResult = Result.success("dashboard")))
        viewModel.onToggleMode() // switch to register mode
        viewModel.onDisplayNameChanged("Jane Doe")
        viewModel.onEmailChanged("jane@example.com")
        viewModel.onPhoneChanged("+1234567890")
        viewModel.onPasswordChanged("Password1!")
        viewModel.onConfirmPasswordChanged("Password1!")
        viewModel.onTermsToggle() // accept terms
        viewModel.register()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AuthUiState.Success)
    }

    // ── Password mismatch ─────────────────────────────────────────────────

    @Test
    fun `register with mismatched passwords shows confirmPassword error`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository())
        viewModel.onToggleMode() // switch to register mode
        viewModel.onDisplayNameChanged("Jane Doe")
        viewModel.onEmailChanged("jane@example.com")
        viewModel.onPhoneChanged("+1234567890")
        viewModel.onPasswordChanged("Password1!")
        viewModel.onConfirmPasswordChanged("Different!")
        viewModel.onTermsToggle()
        viewModel.register()
        advanceUntilIdle()

        // Should not reach Success
        assertTrue(viewModel.uiState.value !is AuthUiState.Success)
        assertEquals("Passwords do not match", viewModel.formState.value.confirmPasswordError)
    }

    // ── Password strength ─────────────────────────────────────────────────

    @Test
    fun `empty password yields strength 0`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository())
        viewModel.onPasswordChanged("")
        assertEquals(0, viewModel.formState.value.passwordStrength)
    }

    @Test
    fun `short lowercase password yields strength 1`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository())
        viewModel.onPasswordChanged("abc")
        // length < 8 → score 0, but coerced to 1 for non-empty
        assertEquals(1, viewModel.formState.value.passwordStrength)
    }

    @Test
    fun `complex password yields strength 4`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository())
        viewModel.onPasswordChanged("Secure1!")
        assertEquals(4, viewModel.formState.value.passwordStrength)
    }

    // ── clearError ────────────────────────────────────────────────────────

    @Test
    fun `clearError resets Error to Idle`() = runTest {
        val viewModel = AuthViewModel(
            FakeAuthRepository(signInResult = Result.failure(Exception("fail")))
        )
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("Password1!")
        viewModel.signIn()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AuthUiState.Error)
        viewModel.clearError()
        assertTrue(viewModel.uiState.value is AuthUiState.Idle)
    }

    // ── Field toggles ─────────────────────────────────────────────────────

    @Test
    fun `togglePasswordVisibility flips the flag`() = runTest {
        val viewModel = AuthViewModel(FakeAuthRepository())
        assertNull(viewModel.formState.value.isPasswordVisible.takeIf { it })
        viewModel.onTogglePasswordVisibility()
        assertTrue(viewModel.formState.value.isPasswordVisible)
        viewModel.onTogglePasswordVisibility()
        assertTrue(!viewModel.formState.value.isPasswordVisible)
    }
}
