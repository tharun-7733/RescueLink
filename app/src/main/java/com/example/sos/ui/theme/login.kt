@file:Suppress("AnimateAsStateLabel")

package com.example.sos.ui.theme

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sos.auth.AuthFormState
import com.example.sos.auth.AuthUiState
import com.example.sos.auth.AuthViewModel
import com.example.sos.ui.theme.AuthDesignTokens as Tokens

// ═══════════════════════════════════════════════════════════════════════════════
// Root Screen
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Root composable consumed by MainActivity. Collects [AuthViewModel] state and
 * drives navigation via [onLoginSuccess].
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    // Navigate on success
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess()
        }
    }

    AuthScreen(
        uiState = uiState,
        formState = formState,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onDisplayNameChanged = viewModel::onDisplayNameChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onToggleConfirmPasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
        onSignIn = viewModel::signIn,
        onRegister = viewModel::register,
        onClearError = viewModel::clearError
    )
}

/**
 * Stateless presentation-layer composable — all state is passed in via parameters.
 * Makes it trivially previewable and testable.
 */
@Composable
fun AuthScreen(
    uiState: AuthUiState,
    formState: AuthFormState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onSignIn: () -> Unit,
    onRegister: () -> Unit,
    onClearError: () -> Unit
) {
    // ── Shake animation ───────────────────────────────────────────────────
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Error) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = Tokens.ShakeDurationMs
                    0f at 0
                    Tokens.ShakeAmplitude.value at 60
                    -Tokens.ShakeAmplitude.value at 120
                    10f at 180
                    -10f at 240
                    6f at 300
                    0f at Tokens.ShakeDurationMs
                }
            )
        }
    }

    // ── Tab state ─────────────────────────────────────────────────────────
    val selectedTab = remember { mutableIntStateOf(0) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Authentication screen" }
    ) {
        // Layer 1 — Background
        BackgroundLayer()

        // Layer 2 — Glass card (centered, with shake)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .imePaddingCompat(),
            contentAlignment = Alignment.Center
        ) {
            GlassAuthCard(
                modifier = Modifier.offset(x = shakeOffset.value.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = 24.dp,
                            vertical = 28.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo
                    AuthLogoHeader()

                    Spacer(modifier = Modifier.height(24.dp))

                    // Pill tab row
                    AuthTabRow(
                        selectedTab = selectedTab.intValue,
                        onTabSelected = { tab ->
                            if (tab != selectedTab.intValue) {
                                selectedTab.intValue = tab
                                onClearError()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Animated form area
                    val isLoginTab = selectedTab.intValue == 0
                    AnimatedContent(
                        targetState = selectedTab.intValue,
                        transitionSpec = {
                            val direction = if (targetState > initialState) 1 else -1
                            (slideInHorizontally(tween(Tokens.FormTransitionMs)) { it * direction } +
                                    fadeIn(tween(Tokens.FormTransitionMs)))
                                .togetherWith(
                                    slideOutHorizontally(tween(Tokens.FormTransitionMs)) { -it * direction } +
                                            fadeOut(tween(Tokens.FormTransitionMs))
                                )
                                .using(SizeTransform(clip = false))
                        },
                        label = "auth-form-transition"
                    ) { tab ->
                        if (tab == 0) {
                            LoginForm(
                                formState = formState,
                                isLoading = uiState is AuthUiState.Loading,
                                onEmailChanged = onEmailChanged,
                                onPasswordChanged = onPasswordChanged,
                                onTogglePasswordVisibility = onTogglePasswordVisibility,
                                onSignIn = {
                                    focusManager.clearFocus()
                                    onSignIn()
                                }
                            )
                        } else {
                            RegisterForm(
                                formState = formState,
                                isLoading = uiState is AuthUiState.Loading,
                                onDisplayNameChanged = onDisplayNameChanged,
                                onEmailChanged = onEmailChanged,
                                onPasswordChanged = onPasswordChanged,
                                onConfirmPasswordChanged = onConfirmPasswordChanged,
                                onTogglePasswordVisibility = onTogglePasswordVisibility,
                                onToggleConfirmPasswordVisibility = onToggleConfirmPasswordVisibility,
                                onRegister = {
                                    focusManager.clearFocus()
                                    onRegister()
                                }
                            )
                        }
                    }

                    // Error banner
                    AnimatedVisibility(
                        visible = uiState is AuthUiState.Error,
                        enter = fadeIn(tween(200)) + expandVertically(),
                        exit = fadeOut(tween(200))
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AuthErrorBanner(
                            message = (uiState as? AuthUiState.Error)?.message ?: ""
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Background Layer
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun BackgroundLayer() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Vertical gradient base
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Tokens.BackgroundStart, Tokens.BackgroundEnd)
                    )
                )
        )
        // Subtle top-right radial accent glow
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Tokens.GlowAccent, Color.Transparent)
                    )
                )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Glass Card
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun GlassAuthCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val borderBrush = Brush.linearGradient(
        colors = listOf(Tokens.CardBorderStart, Tokens.CardBorderEnd)
    )
    val shape = RoundedCornerShape(Tokens.CardCorner)

    // Blur only on API 31+
    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(radius = 0.5.dp) // very subtle frosted glass
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { shadowElevation = Tokens.CardShadowElevation }
            .clip(shape)
            .then(blurModifier)
            .background(color = Tokens.CardSurface, shape = shape)
            .border(width = Tokens.CardBorderWidth, brush = borderBrush, shape = shape)
    ) {
        content()
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Logo Header
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AuthLogoHeader() {
    AnimatedVisibility(
        visible = true,
        enter = scaleIn(tween(400)) + fadeIn(tween(400))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.semantics { contentDescription = "Emergency Response Platform logo" }
        ) {
            // Outer ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(Tokens.LogoOuterSize)
                    .background(
                        color = Tokens.AccentStart.copy(alpha = Tokens.LogoOuterAlpha),
                        shape = CircleShape
                    )
            ) {
                // Inner ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(Tokens.LogoInnerSize)
                        .background(
                            color = Tokens.AccentStart.copy(alpha = Tokens.LogoInnerAlpha),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null, // described by outer semantics
                        tint = Tokens.AccentStart,
                        modifier = Modifier.size(Tokens.LogoIconSize)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SOS",
                fontSize = Tokens.AppNameSize,
                fontWeight = FontWeight.Bold,
                color = Tokens.TextPrimary
            )
            Text(
                text = "Emergency Response Platform",
                fontSize = Tokens.SubtitleSize,
                color = Tokens.TextSecondary
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Custom Pill Tab Row
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AuthTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Sign In", "Create Account")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Tokens.TabBackground, shape = RoundedCornerShape(Tokens.TabCorner))
            .padding(4.dp)
            .semantics { contentDescription = "Authentication mode selector" }
    ) {
        // Animated active pill indicator
        val indicatorOffsetFraction by animateDpAsState(
            targetValue = if (selectedTab == 0) 0.dp else 1.dp,
            animationSpec = tween(Tokens.TabSlideDurationMs),
            label = "tab-indicator-offset"
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, title ->
                val isActive = selectedTab == index
                val textColor by animateColorAsState(
                    targetValue = if (isActive) Tokens.TextPrimary else Tokens.TextSecondary,
                    animationSpec = tween(Tokens.TabSlideDurationMs),
                    label = "tab-text-color-$index"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Tokens.TabCorner))
                        .background(
                            color = if (isActive) Tokens.AccentStart else Color.Transparent
                        )
                        .then(
                            Modifier.semantics {
                                contentDescription = "$title tab${if (isActive) ", selected" else ""}"
                                role = Role.Tab
                            }
                        )
                        .noRippleClickable { onTabSelected(index) }
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = Tokens.TabLabelSize,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Forms
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun LoginForm(
    formState: AuthFormState,
    isLoading: Boolean,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSignIn: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AuthTextField(
            value = formState.email,
            onChange = onEmailChanged,
            label = "Email",
            placeholder = "you@example.com",
            leadingIcon = Icons.Rounded.MailOutline,
            leadingIconDescription = "Email icon",
            isError = formState.emailError != null,
            errorMessage = formState.emailError,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value = formState.password,
            onChange = onPasswordChanged,
            label = "Password",
            placeholder = "••••••••",
            leadingIcon = Icons.Rounded.Lock,
            leadingIconDescription = "Password icon",
            isError = formState.passwordError != null,
            errorMessage = formState.passwordError,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            visualTransformation = if (formState.isPasswordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(
                    onClick = onTogglePasswordVisibility,
                    modifier = Modifier.semantics {
                        contentDescription = if (formState.isPasswordVisible)
                            "Hide password" else "Show password"
                    }
                ) {
                    Icon(
                        imageVector = if (formState.isPasswordVisible)
                            Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = Tokens.TextSecondary
                    )
                }
            },
            keyboardActions = KeyboardActions(onDone = { onSignIn() })
        )

        // Forgot password
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(
                onClick = { /* TODO: forgot password flow */ },
                modifier = Modifier.semantics { contentDescription = "Forgot password" }
            ) {
                Text(
                    text = "Forgot password?",
                    fontSize = Tokens.ErrorTextSize,
                    color = Tokens.AccentStart,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        AuthSubmitButton(
            label = "Sign In",
            isLoading = isLoading,
            isEnabled = !isLoading,
            onClick = onSignIn
        )
    }
}

@Composable
private fun RegisterForm(
    formState: AuthFormState,
    isLoading: Boolean,
    onDisplayNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onRegister: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AuthTextField(
            value = formState.displayName,
            onChange = onDisplayNameChanged,
            label = "Full Name",
            placeholder = "Jane Doe",
            leadingIcon = Icons.Rounded.Person,
            leadingIconDescription = "Person icon",
            isError = formState.displayNameError != null,
            errorMessage = formState.displayNameError,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value = formState.email,
            onChange = onEmailChanged,
            label = "Email",
            placeholder = "you@example.com",
            leadingIcon = Icons.Rounded.MailOutline,
            leadingIconDescription = "Email icon",
            isError = formState.emailError != null,
            errorMessage = formState.emailError,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value = formState.password,
            onChange = onPasswordChanged,
            label = "Password",
            placeholder = "Min 8 characters",
            leadingIcon = Icons.Rounded.Lock,
            leadingIconDescription = "Password icon",
            isError = formState.passwordError != null,
            errorMessage = formState.passwordError,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            visualTransformation = if (formState.isPasswordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(
                    onClick = onTogglePasswordVisibility,
                    modifier = Modifier.semantics {
                        contentDescription = if (formState.isPasswordVisible)
                            "Hide password" else "Show password"
                    }
                ) {
                    Icon(
                        imageVector = if (formState.isPasswordVisible)
                            Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = Tokens.TextSecondary
                    )
                }
            }
        )

        // Password strength bar
        if (formState.password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            PasswordStrengthBar(strength = formState.passwordStrength)
        }

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value = formState.confirmPassword,
            onChange = onConfirmPasswordChanged,
            label = "Confirm Password",
            placeholder = "Re-enter password",
            leadingIcon = Icons.Rounded.Lock,
            leadingIconDescription = "Confirm password icon",
            isError = formState.confirmPasswordError != null,
            errorMessage = formState.confirmPasswordError,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            visualTransformation = if (formState.isConfirmPasswordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(
                    onClick = onToggleConfirmPasswordVisibility,
                    modifier = Modifier.semantics {
                        contentDescription = if (formState.isConfirmPasswordVisible)
                            "Hide confirm password" else "Show confirm password"
                    }
                ) {
                    Icon(
                        imageVector = if (formState.isConfirmPasswordVisible)
                            Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = Tokens.TextSecondary
                    )
                }
            },
            keyboardActions = KeyboardActions(onDone = { onRegister() })
        )

        Spacer(modifier = Modifier.height(20.dp))

        AuthSubmitButton(
            label = "Create Account",
            isLoading = isLoading,
            isEnabled = !isLoading,
            onClick = onRegister
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Reusable Text Field
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AuthTextField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    leadingIconDescription: String,
    isError: Boolean,
    errorMessage: String?,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> Tokens.FieldError
            isFocused -> Tokens.FieldFocused
            else -> Tokens.FieldBorder
        },
        animationSpec = tween(Tokens.ColorAnimDurationMs),
        label = "field-border-$label"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isFocused) Tokens.FieldFocused else Tokens.TextSecondary,
        animationSpec = tween(Tokens.ColorAnimDurationMs),
        label = "icon-tint-$label"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$label field" }
    ) {
        TextField(
            value = value,
            onValueChange = onChange,
            placeholder = {
                Text(text = placeholder, color = Tokens.TextHint)
            },
            label = {
                Text(text = label, color = if (isError) Tokens.FieldError else Tokens.TextSecondary)
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = leadingIconDescription,
                    tint = if (isError) Tokens.FieldError else iconTint,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = trailingIcon,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            singleLine = true,
            shape = RoundedCornerShape(Tokens.FieldCorner),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Tokens.FieldFill,
                unfocusedContainerColor = Tokens.FieldFill,
                errorContainerColor = Tokens.FieldFill,
                focusedTextColor = Tokens.TextPrimary,
                unfocusedTextColor = Tokens.TextPrimary,
                errorTextColor = Tokens.TextPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = Tokens.FieldFocused,
                errorCursorColor = Tokens.FieldError
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(Tokens.FieldCorner)
                )
        )

        // Inline error message
        AnimatedVisibility(
            visible = isError && errorMessage != null,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
        ) {
            Text(
                text = errorMessage ?: "",
                fontSize = Tokens.ErrorTextSize,
                color = Tokens.FieldError,
                modifier = Modifier
                    .padding(start = 16.dp, top = 4.dp)
                    .semantics { contentDescription = "Error: $errorMessage" }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Password Strength Bar
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PasswordStrengthBar(strength: Int) {
    val labels = listOf("", "Weak", "Fair", "Strong", "Very Strong")
    val colors = listOf(
        Color.Transparent,
        Color(0xFFFF6B6B),   // weak — error-ish red
        Color(0xFFFFB74D),   // fair — amber
        Color(0xFF66BB6A),   // strong — green
        Tokens.AccentStart   // very strong — brand red (max engagement)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Password strength: ${labels.getOrElse(strength) { "" }}" }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { index ->
                val segmentFilled = strength > index
                val segmentColor by animateColorAsState(
                    targetValue = if (segmentFilled) colors.getOrElse(strength) { Color.Transparent }
                    else Tokens.FieldBorder,
                    animationSpec = tween(Tokens.ColorAnimDurationMs),
                    label = "strength-segment-$index"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(segmentColor)
                )
            }
        }
        if (strength > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = labels.getOrElse(strength) { "" },
                fontSize = Tokens.ErrorTextSize,
                color = colors.getOrElse(strength) { Color.Transparent },
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Submit Button
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AuthSubmitButton(
    label: String,
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isLoading) 0.97f else 1f,
        animationSpec = tween(100),
        label = "submit-button-scale"
    )

    val accentGradient = Brush.horizontalGradient(
        colors = listOf(Tokens.AccentStart, Tokens.AccentEnd)
    )

    Button(
        onClick = onClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(Tokens.ButtonCorner),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Tokens.TextPrimary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Tokens.TextPrimary.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(Tokens.ButtonHeight)
            .scale(scale)
            .background(
                brush = if (isEnabled) accentGradient
                else Brush.horizontalGradient(
                    colors = listOf(
                        Tokens.AccentStart.copy(alpha = 0.5f),
                        Tokens.AccentEnd.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(Tokens.ButtonCorner)
            )
            .semantics {
                contentDescription = if (isLoading) "Loading, please wait" else label
            }
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(tween(150)) togetherWith fadeOut(tween(150))
            },
            label = "submit-button-content"
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    color = Tokens.TextPrimary,
                    strokeWidth = Tokens.ButtonLoadingIndicatorStroke,
                    modifier = Modifier.size(Tokens.ButtonLoadingIndicatorSize)
                )
            } else {
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = Tokens.TabLabelSize
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Error Banner
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AuthErrorBanner(message: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Tokens.ErrorBannerBg,
                shape = RoundedCornerShape(Tokens.ErrorBannerCorner)
            )
            .border(
                width = 1.dp,
                color = Tokens.ErrorBannerBorder,
                shape = RoundedCornerShape(Tokens.ErrorBannerCorner)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics { contentDescription = "Error: $message" }
    ) {
        Icon(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = Tokens.FieldError,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = message,
            fontSize = Tokens.ErrorBannerTextSize,
            color = Tokens.FieldError,
            fontWeight = FontWeight.Medium
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Utility Extensions
// ═══════════════════════════════════════════════════════════════════════════════

/** Cross-version IME padding helper. */
@Composable
private fun Modifier.imePaddingCompat(): Modifier = this.imePadding()

/** Ripple-free clickable — must be called inside a @Composable context. */
@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = source,
        indication = null,
        onClick = onClick
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// Previews
// ═══════════════════════════════════════════════════════════════════════════════

@Preview(name = "Auth — Default Dark (Sign In)", showBackground = true, backgroundColor = 0xFF070A0F)
@Composable
private fun PreviewAuthDefault() {
    SOSTheme(darkTheme = true, dynamicColor = false) {
        AuthScreen(
            uiState = AuthUiState.Idle,
            formState = AuthFormState(),
            onEmailChanged = {}, onPasswordChanged = {}, onDisplayNameChanged = {},
            onConfirmPasswordChanged = {}, onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {}, onSignIn = {}, onRegister = {},
            onClearError = {}
        )
    }
}

@Preview(name = "Auth — Loading State", showBackground = true, backgroundColor = 0xFF070A0F)
@Composable
private fun PreviewAuthLoading() {
    SOSTheme(darkTheme = true, dynamicColor = false) {
        AuthScreen(
            uiState = AuthUiState.Loading,
            formState = AuthFormState(email = "user@sos.app", password = "password123"),
            onEmailChanged = {}, onPasswordChanged = {}, onDisplayNameChanged = {},
            onConfirmPasswordChanged = {}, onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {}, onSignIn = {}, onRegister = {},
            onClearError = {}
        )
    }
}

@Preview(name = "Auth — Error State", showBackground = true, backgroundColor = 0xFF070A0F)
@Composable
private fun PreviewAuthError() {
    SOSTheme(darkTheme = true, dynamicColor = false) {
        AuthScreen(
            uiState = AuthUiState.Error("Invalid credentials. Please check your email and password."),
            formState = AuthFormState(
                email = "user@sos.app",
                password = "wrong",
                hasAttemptedSubmit = true,
                passwordError = "Must be at least 8 characters"
            ),
            onEmailChanged = {}, onPasswordChanged = {}, onDisplayNameChanged = {},
            onConfirmPasswordChanged = {}, onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {}, onSignIn = {}, onRegister = {},
            onClearError = {}
        )
    }
}

@Preview(name = "Auth — Register Tab", showBackground = true, backgroundColor = 0xFF070A0F)
@Composable
private fun PreviewAuthRegister() {
    SOSTheme(darkTheme = true, dynamicColor = false) {
        // We preview the RegisterForm directly to pin the tab
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070A0F))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassAuthCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AuthLogoHeader()
                    Spacer(modifier = Modifier.height(24.dp))
                    AuthTabRow(selectedTab = 1, onTabSelected = {})
                    Spacer(modifier = Modifier.height(20.dp))
                    RegisterForm(
                        formState = AuthFormState(password = "Secur3!", passwordStrength = 3),
                        isLoading = false,
                        onDisplayNameChanged = {}, onEmailChanged = {},
                        onPasswordChanged = {}, onConfirmPasswordChanged = {},
                        onTogglePasswordVisibility = {},
                        onToggleConfirmPasswordVisibility = {},
                        onRegister = {}
                    )
                }
            }
        }
    }
}
