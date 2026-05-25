@file:Suppress("AnimateAsStateLabel")

package com.example.sos.ui.theme

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sos.auth.AuthFormState
import com.example.sos.auth.AuthUiState
import com.example.sos.auth.AuthViewModel
import com.example.sos.ui.theme.AuthDesignTokens as T

// ══════════════════════════════════════════════════════════════════════════════
// Font aliases (from Type.kt – downloadable Google Fonts)
// ══════════════════════════════════════════════════════════════════════════════

private val BebasNeue = BebasNeueFontFamily
private val Outfit    = OutfitFontFamily

// ══════════════════════════════════════════════════════════════════════════════
// Root Screen (wires ViewModel)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val uiState  by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) onLoginSuccess()
    }

    AuthScreen(
        uiState                        = uiState,
        formState                      = formState,
        onEmailChanged                 = viewModel::onEmailChanged,
        onPasswordChanged              = viewModel::onPasswordChanged,
        onDisplayNameChanged           = viewModel::onDisplayNameChanged,
        onConfirmPasswordChanged       = viewModel::onConfirmPasswordChanged,
        onTogglePasswordVisibility     = viewModel::onTogglePasswordVisibility,
        onToggleConfirmPasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
        onSignIn                       = viewModel::signIn,
        onRegister                     = viewModel::register,
        onClearError                   = viewModel::clearError
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// Stateless Auth Screen
// ══════════════════════════════════════════════════════════════════════════════

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
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Error) {
            shakeOffset.animateTo(0f, keyframes {
                durationMillis = T.ShakeDurationMs
                0f at 0; T.ShakeAmplitude.value at 60; -T.ShakeAmplitude.value at 120
                10f at 180; -10f at 240; 6f at 300; 0f at T.ShakeDurationMs
            })
        }
    }

    val selectedTab = remember { mutableIntStateOf(0) }
    val focusManager = LocalFocusManager.current
    var rememberMe by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(T.BackgroundCore)
            .semantics { contentDescription = "RescueLink authentication screen" }
    ) {
        // ── Layer 0: Atmospheric background ──────────────────────────────
        RescueLinkBackground()

        // ── Layer 1: Scrollable content ───────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 28.dp, bottom = 24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Icon
            HeroIconWrap()
            Spacer(Modifier.height(14.dp))

            // Brand
            BrandBlock()
            Spacer(Modifier.height(20.dp))

            // Auth card with shake
            RescueLinkCard(
                modifier = Modifier.offset(x = shakeOffset.value.dp)
            ) {
                Text(
                    "Welcome back",
                    fontFamily = Outfit,
                    fontSize = T.CardTitleSize,
                    fontWeight = FontWeight.Bold,
                    color = T.TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Sign in to get instant roadside help, 24/7.",
                    fontFamily = Outfit,
                    fontSize = T.CardSubSize,
                    color = T.TextSecondary,
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(18.dp))

                // Tab row
                AuthTabRow(
                    selectedTab = selectedTab.intValue,
                    onTabSelected = { tab ->
                        if (tab != selectedTab.intValue) {
                            selectedTab.intValue = tab
                            onClearError()
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))

                // Forms
                AnimatedContent(
                    targetState = selectedTab.intValue,
                    transitionSpec = {
                        val dir = if (targetState > initialState) 1 else -1
                        (slideInHorizontally(tween(T.FormTransitionMs)) { it * dir } + fadeIn(tween(T.FormTransitionMs)))
                            .togetherWith(slideOutHorizontally(tween(T.FormTransitionMs)) { -it * dir } + fadeOut(tween(T.FormTransitionMs)))
                            .using(SizeTransform(clip = false))
                    },
                    label = "form"
                ) { tab ->
                    if (tab == 0) {
                        LoginForm(
                            formState = formState,
                            isLoading = uiState is AuthUiState.Loading,
                            rememberMe = rememberMe,
                            onRememberMeToggle = { rememberMe = !rememberMe },
                            onEmailChanged = onEmailChanged,
                            onPasswordChanged = onPasswordChanged,
                            onTogglePasswordVisibility = onTogglePasswordVisibility,
                            onSignIn = { focusManager.clearFocus(); onSignIn() }
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
                            onRegister = { focusManager.clearFocus(); onRegister() }
                        )
                    }
                }

                // Error banner
                AnimatedVisibility(
                    visible = uiState is AuthUiState.Error,
                    enter = fadeIn(tween(200)) + expandVertically(),
                    exit = fadeOut(tween(200))
                ) {
                    Spacer(Modifier.height(12.dp))
                    AuthErrorBanner((uiState as? AuthUiState.Error)?.message ?: "")
                }
            }

            // Sign-up row
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    if (selectedTab.intValue == 0) "New to RescueLink? " else "Already have an account? ",
                    fontFamily = Outfit,
                    fontSize = 13.5.sp,
                    color = T.TextSecondary
                )
                Text(
                    if (selectedTab.intValue == 0) "Create free account" else "Sign in",
                    fontFamily = Outfit,
                    fontSize = 13.5.sp,
                    color = T.RedHot,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { selectedTab.intValue = if (selectedTab.intValue == 0) 1 else 0 }
                )
            }

            // SOS Strip
            Spacer(Modifier.height(16.dp))
            SosEmergencyStrip()
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Background Layer
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun RescueLinkBackground() {
    val inf = rememberInfiniteTransition(label = "bgGlow")

    // Blob 1 – top-left red
    val glow1Alpha by inf.animateFloat(
        0.14f, 0.24f,
        infiniteRepeatable(tween(T.GlowPulseDurationMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow1"
    )
    val glow1Scale by inf.animateFloat(
        1f, 1.08f,
        infiniteRepeatable(tween(T.GlowPulseDurationMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow1s"
    )

    // Blob 2 – bottom-right deep red (delayed)
    val glow2Alpha by inf.animateFloat(
        0.14f, 0.24f,
        infiniteRepeatable(tween(T.GlowPulseDurationMs, easing = FastOutSlowInEasing, delayMillis = 2500), RepeatMode.Reverse),
        label = "glow2"
    )
    val glow2Scale by inf.animateFloat(
        1f, 1.08f,
        infiniteRepeatable(tween(T.GlowPulseDurationMs, easing = FastOutSlowInEasing, delayMillis = 2500), RepeatMode.Reverse),
        label = "glow2s"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Dot grid
        Box(modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawGrid(T.GridLineColor, T.GridCellSize.toPx()) }
        )

        // Blob 1
        Box(
            modifier = Modifier
                .size(560.dp)
                .offset(x = (-160).dp, y = (-160).dp)
                .graphicsLayer { alpha = glow1Alpha; scaleX = glow1Scale; scaleY = glow1Scale }
                .background(
                    Brush.radialGradient(listOf(T.RedHot, Color.Transparent)),
                    CircleShape
                )
        )

        // Blob 2
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 100.dp)
                .graphicsLayer { alpha = glow2Alpha; scaleX = glow2Scale; scaleY = glow2Scale }
                .background(
                    Brush.radialGradient(listOf(T.RedDeep, Color.Transparent)),
                    CircleShape
                )
        )

        // Diagonal slash accent
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(340.dp)
                .align(Alignment.Center)
                .graphicsLayer { alpha = 0.12f; rotationZ = -28f }
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, T.RedHot, Color.Transparent))
                )
        )
    }
}

private fun DrawScope.drawGrid(color: Color, cellPx: Float) {
    // Horizontal lines
    var y = 0f
    while (y < size.height) {
        drawLine(color, Offset(0f, y), Offset(size.width, y), 1f)
        y += cellPx
    }
    // Vertical lines
    var x = 0f
    while (x < size.width) {
        drawLine(color, Offset(x, 0f), Offset(x, size.height), 1f)
        x += cellPx
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Hero Icon
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HeroIconWrap() {
    val inf = rememberInfiniteTransition(label = "hero")

    // Spinning conic ring
    val ringRotation by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(T.SpinRingDurationMs, easing = LinearEasing)),
        label = "ring"
    )

    // Pulse dot
    val dotScale by inf.animateFloat(
        1f, 1.1f,
        infiniteRepeatable(tween(T.PulseDurationMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot"
    )
    val dotGlow by inf.animateFloat(
        0f, 6f,
        infiniteRepeatable(tween(T.PulseDurationMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dotG"
    )

    Box(
        modifier = Modifier.size(T.HeroRingSize + 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer spinning gradient ring (conic-ish via rotation)
        Box(
            modifier = Modifier
                .size(T.HeroRingSize + 4.dp)
                .graphicsLayer { rotationZ = ringRotation }
                .background(
                    Brush.sweepGradient(
                        0f to Color.Transparent,
                        0.6f to Color.Transparent,
                        0.85f to T.RedHot.copy(alpha = 0.7f),
                        1f to Color.Transparent
                    ),
                    RoundedCornerShape(28.dp)
                )
        )

        // Inner hero box
        Box(
            modifier = Modifier
                .size(T.HeroRingSize)
                .clip(RoundedCornerShape(T.HeroRingCorner))
                .background(
                    Brush.linearGradient(listOf(T.HeroRingBg1, T.HeroRingBg2))
                )
                .border(1.5.dp, T.HeroRingBorder, RoundedCornerShape(T.HeroRingCorner)),
            contentAlignment = Alignment.Center
        ) {
            // Car / warning icon (using Warning material icon with red tint + glow)
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = "Road emergency",
                tint = T.RedHot,
                modifier = Modifier.size(T.HeroIconSize)
            )
        }

        // Pulse dot
        Box(
            modifier = Modifier
                .size(T.PulseDotSize)
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 4.dp)
                .scale(dotScale)
                .clip(CircleShape)
                .background(T.PulseDotColor)
                .border(2.5.dp, T.BackgroundCore, CircleShape)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Brand Block – gradient text "RescueLink"
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun BrandBlock() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "RescueLink",
            style = TextStyle(
                fontFamily = BebasNeue,
                fontSize    = T.BrandNameSize,
                letterSpacing = 2.sp,
                brush       = Brush.linearGradient(
                    0.3f to Color.White,
                    1.0f to T.RedGlow,
                    start = Offset(0f, 0f),
                    end   = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "EMERGENCY ROAD ASSIST",
            fontFamily = Outfit,
            fontSize = T.BrandTagSize,
            fontWeight = FontWeight.Medium,
            letterSpacing = 3.sp,
            color = T.RedHot
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Card Container
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun RescueLinkCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(T.CardCorner)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { shadowElevation = T.CardShadowElevation }
            .clip(shape)
            .background(T.CardSurface, shape)
            .border(T.CardBorderWidth, T.CardBorderStart, shape)
    ) {
        // Top-edge shimmer line
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.horizontalGradient(listOf(Color.Transparent, T.RedHot.copy(0.6f), Color.Transparent))
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            content = content
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Tab Row
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AuthTabRow(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Sign In", "Create Account")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(T.TabBackground, RoundedCornerShape(T.TabCorner))
            .padding(4.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, title ->
                val isActive = selectedTab == index
                val textColor by animateColorAsState(
                    if (isActive) T.TextPrimary else T.TextSecondary,
                    tween(T.TabSlideDurationMs), label = "tc$index"
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(T.TabCorner))
                        .background(if (isActive) T.RedHot else Color.Transparent)
                        .semantics { contentDescription = "$title tab${if (isActive) ", selected" else ""}"; role = Role.Tab }
                        .noRippleClickable { onTabSelected(index) }
                        .padding(vertical = 8.dp)
                ) {
                    Text(title, fontFamily = Outfit, fontSize = T.TabLabelSize, fontWeight = FontWeight.SemiBold, color = textColor)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Login Form
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun LoginForm(
    formState: AuthFormState,
    isLoading: Boolean,
    rememberMe: Boolean,
    onRememberMeToggle: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSignIn: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        RescueLinkTextField(
            value = formState.email,
            onChange = onEmailChanged,
            label = "Email Address",
            placeholder = "you@example.com",
            leadingIcon = Icons.Rounded.MailOutline,
            leadingIconDescription = "Email",
            isError = formState.emailError != null,
            errorMessage = formState.emailError,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(12.dp))

        RescueLinkTextField(
            value = formState.password,
            onChange = onPasswordChanged,
            label = "Password",
            placeholder = "••••••••",
            leadingIcon = Icons.Rounded.Lock,
            leadingIconDescription = "Password",
            isError = formState.passwordError != null,
            errorMessage = formState.passwordError,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            visualTransformation = if (formState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        if (formState.isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (formState.isPasswordVisible) "Hide password" else "Show password",
                        tint = T.TextSecondary
                    )
                }
            },
            keyboardActions = KeyboardActions(onDone = { onSignIn() })
        )

        // Remember / Forgot row
        Spacer(Modifier.height(2.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RememberMeCheckbox(checked = rememberMe, onToggle = onRememberMeToggle)
            TextButton(onClick = {}) {
                Text("Forgot password?", fontFamily = Outfit, fontSize = 13.sp, color = T.RedHot, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(2.dp))
        AuthSubmitButton("Sign In to RescueLink", isLoading = isLoading, isEnabled = !isLoading, onClick = onSignIn)

        // Divider
        Row(
            Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, T.BlackRim))))
            Text(
                "  or continue with  ",
                fontFamily = Outfit,
                fontSize = 12.sp,
                color = T.TextSecondary.copy(0.4f)
            )
            Box(Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(T.BlackRim, Color.Transparent))))
        }

        // Google Button
        GoogleSignInButton()
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Register Form
// ══════════════════════════════════════════════════════════════════════════════

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
    Column(Modifier.fillMaxWidth()) {
        RescueLinkTextField(
            value = formState.displayName,
            onChange = onDisplayNameChanged,
            label = "Full Name",
            placeholder = "Jane Doe",
            leadingIcon = Icons.Rounded.Person,
            leadingIconDescription = "Name",
            isError = formState.displayNameError != null,
            errorMessage = formState.displayNameError,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(12.dp))

        RescueLinkTextField(
            value = formState.email,
            onChange = onEmailChanged,
            label = "Email Address",
            placeholder = "you@example.com",
            leadingIcon = Icons.Rounded.MailOutline,
            leadingIconDescription = "Email",
            isError = formState.emailError != null,
            errorMessage = formState.emailError,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(12.dp))

        RescueLinkTextField(
            value = formState.password,
            onChange = onPasswordChanged,
            label = "Password",
            placeholder = "Min 8 characters",
            leadingIcon = Icons.Rounded.Lock,
            leadingIconDescription = "Password",
            isError = formState.passwordError != null,
            errorMessage = formState.passwordError,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            visualTransformation = if (formState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        if (formState.isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        null, tint = T.TextSecondary
                    )
                }
            }
        )

        if (formState.password.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            PasswordStrengthBar(strength = formState.passwordStrength)
        }
        Spacer(Modifier.height(10.dp))

        RescueLinkTextField(
            value = formState.confirmPassword,
            onChange = onConfirmPasswordChanged,
            label = "Confirm Password",
            placeholder = "Re-enter password",
            leadingIcon = Icons.Rounded.Lock,
            leadingIconDescription = "Confirm password",
            isError = formState.confirmPasswordError != null,
            errorMessage = formState.confirmPasswordError,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            visualTransformation = if (formState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleConfirmPasswordVisibility) {
                    Icon(
                        if (formState.isConfirmPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        null, tint = T.TextSecondary
                    )
                }
            },
            keyboardActions = KeyboardActions(onDone = { onRegister() })
        )
        Spacer(Modifier.height(18.dp))
        AuthSubmitButton("Create Account", isLoading = isLoading, isEnabled = !isLoading, onClick = onRegister)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Reusable TextField
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun RescueLinkTextField(
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
        when { isError -> T.FieldError; isFocused -> T.FieldFocused; else -> T.FieldBorder },
        tween(T.ColorAnimDurationMs), label = "border-$label"
    )
    val iconTint by animateColorAsState(
        if (isFocused) T.FieldFocused else T.TextSecondary.copy(0.4f),
        tween(T.ColorAnimDurationMs), label = "icon-$label"
    )
    val bgColor by animateColorAsState(
        if (isFocused) T.FieldFocusBg else T.FieldFill,
        tween(T.ColorAnimDurationMs), label = "bg-$label"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Label
        Text(
            label.uppercase(),
            fontFamily = Outfit,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            color = T.TextLabel,
            modifier = Modifier.padding(bottom = 5.dp)
        )

        TextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, fontFamily = Outfit, color = T.TextHint) },
            leadingIcon = {
                Icon(leadingIcon, leadingIconDescription, tint = if (isError) T.FieldError else iconTint, modifier = Modifier.size(18.dp))
            },
            trailingIcon = trailingIcon,
            isError = isError,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            singleLine = true,
            shape = RoundedCornerShape(T.FieldCorner),
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = bgColor,
                unfocusedContainerColor = bgColor,
                errorContainerColor     = bgColor,
                focusedTextColor        = T.TextPrimary,
                unfocusedTextColor      = T.TextPrimary,
                errorTextColor          = T.TextPrimary,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor     = Color.Transparent,
                cursorColor             = T.FieldFocused,
                errorCursorColor        = T.FieldError
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, borderColor, RoundedCornerShape(T.FieldCorner))
        )

        AnimatedVisibility(
            visible = isError && errorMessage != null,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
        ) {
            Text(
                errorMessage ?: "",
                fontSize = T.ErrorTextSize,
                color = T.FieldError,
                fontFamily = Outfit,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Remember Me Checkbox
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun RememberMeCheckbox(checked: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .noRippleClickable { onToggle() }
            .semantics { contentDescription = if (checked) "Remember me, checked" else "Remember me, unchecked" }
    ) {
        val bgColor by animateColorAsState(
            if (checked) T.RedHot else T.BlackMid,
            tween(T.ColorAnimDurationMs), label = "cb-bg"
        )
        val borderCol by animateColorAsState(
            if (checked) T.RedHot else T.BlackRim,
            tween(T.ColorAnimDurationMs), label = "cb-border"
        )
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(bgColor)
                .border(1.5.dp, borderCol, RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
        Spacer(Modifier.width(8.dp))
        Text("Remember me", fontFamily = Outfit, fontSize = 13.sp, color = T.TextSecondary)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Google Sign-In Button
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun GoogleSignInButton() {
    OutlinedButton(
        onClick = { /* TODO: Google sign-in */ },
        modifier = Modifier.fillMaxWidth().height(44.dp),
        shape = RoundedCornerShape(T.FieldCorner),
        border = BorderStroke(1.5.dp, T.BlackRim),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = T.BlackMid, contentColor = T.TextPrimary)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            // Google "G" icon (simplified colored circles)
            Box(
                Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4285F4)),
                contentAlignment = Alignment.Center
            ) {
                Text("G", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Text("Continue with Google", fontFamily = Outfit, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Submit Button
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AuthSubmitButton(label: String, isLoading: Boolean, isEnabled: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isLoading) 0.97f else 1f, tween(100), label = "btn-scale")
    val gradient = Brush.linearGradient(listOf(T.RedHot, T.RedDeep))

    Button(
        onClick = onClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(T.ButtonCorner),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor   = T.TextPrimary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = T.TextPrimary.copy(0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(T.ButtonHeight)
            .scale(scale)
            .background(
                if (isEnabled) gradient else Brush.horizontalGradient(listOf(T.RedHot.copy(0.5f), T.RedDeep.copy(0.5f))),
                RoundedCornerShape(T.ButtonCorner)
            )
            .semantics { contentDescription = if (isLoading) "Loading" else label }
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
            label = "btn-content"
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    color = T.TextPrimary,
                    strokeWidth = T.ButtonLoadingIndicatorStroke,
                    modifier = Modifier.size(T.ButtonLoadingIndicatorSize)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Rounded.Login, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(label, fontFamily = Outfit, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Password Strength Bar (carried over)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PasswordStrengthBar(strength: Int) {
    val labels = listOf("", "Weak", "Fair", "Strong", "Very Strong")
    val colors = listOf(Color.Transparent, Color(0xFFFF6B6B), Color(0xFFFFB74D), Color(0xFF66BB6A), T.RedHot)
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { index ->
                val filled = strength > index
                val segColor by animateColorAsState(
                    if (filled) colors.getOrElse(strength) { Color.Transparent } else T.FieldBorder,
                    tween(T.ColorAnimDurationMs), label = "seg-$index"
                )
                Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(segColor))
            }
        }
        if (strength > 0) {
            Spacer(Modifier.height(4.dp))
            Text(labels.getOrElse(strength) { "" }, fontSize = T.ErrorTextSize, color = colors.getOrElse(strength) { Color.Transparent }, fontWeight = FontWeight.Medium, fontFamily = Outfit)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Error Banner
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AuthErrorBanner(message: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(T.ErrorBannerBg, RoundedCornerShape(T.ErrorBannerCorner))
            .border(1.dp, T.ErrorBannerBorder, RoundedCornerShape(T.ErrorBannerCorner))
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics { contentDescription = "Error: $message" }
    ) {
        Icon(Icons.Rounded.ErrorOutline, null, tint = T.FieldError, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(message, fontSize = T.ErrorBannerTextSize, color = T.FieldError, fontWeight = FontWeight.Medium, fontFamily = Outfit)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SOS Emergency Strip
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SosEmergencyStrip() {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }

    val bgAlpha by animateFloatAsState(if (isPressed) 0.25f else 0.07f, tween(200), label = "sos-bg")
    val borderAlpha by animateFloatAsState(if (isPressed) 0.7f else 0.18f, tween(200), label = "sos-border")

    // Pulse for the badge
    val inf = rememberInfiniteTransition(label = "sos-pulse")
    val badgeScale by inf.animateFloat(
        1f, 1.1f,
        infiniteRepeatable(tween(T.PulseDurationMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "badge-scale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(T.StripFlashDurationMs.toLong())
            isPressed = false
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(T.RedHot.copy(bgAlpha), RoundedCornerShape(T.SosStripCorner))
            .border(1.dp, T.RedHot.copy(borderAlpha), RoundedCornerShape(T.SosStripCorner))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                AlertDialog.Builder(context)
                    .setTitle("Emergency Help")
                    .setMessage("Choose how to get immediate assistance:")
                    .setPositiveButton("Call 112") { _, _ ->
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")))
                    }
                    .setNeutralButton("Call 911") { _, _ ->
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:911")))
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .semantics { contentDescription = "SOS Emergency Help button" }
    ) {
        // Pulsing SOS badge
        Box(
            modifier = Modifier
                .size(T.SosBadgeSize)
                .scale(badgeScale)
                .clip(RoundedCornerShape(T.SosBadgeCorner))
                .background(T.RedHot),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "SOS",
                fontFamily = BebasNeue,
                fontSize = 14.sp,
                letterSpacing = 1.sp,
                color = Color.White
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text("Immediate Emergency Help", fontFamily = Outfit, fontSize = T.SosTextSize, fontWeight = FontWeight.Bold, color = T.TextPrimary)
            Spacer(Modifier.height(1.dp))
            Text("Tap to call without logging in", fontFamily = Outfit, fontSize = T.SosSubTextSize, color = T.TextSecondary)
        }

        Icon(Icons.Rounded.ChevronRight, null, tint = T.TextSecondary.copy(0.4f), modifier = Modifier.size(18.dp))
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Utilities
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    return clickable(interactionSource = source, indication = null, onClick = onClick)
}

// ══════════════════════════════════════════════════════════════════════════════
// Previews
// ══════════════════════════════════════════════════════════════════════════════

@Preview(name = "RescueLink – Sign In", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PreviewSignIn() {
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

@Preview(name = "RescueLink – Error State", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PreviewError() {
    SOSTheme(darkTheme = true, dynamicColor = false) {
        AuthScreen(
            uiState = AuthUiState.Error("Invalid credentials. Please check your email and password."),
            formState = AuthFormState(email = "user@sos.app", password = "wrong", hasAttemptedSubmit = true),
            onEmailChanged = {}, onPasswordChanged = {}, onDisplayNameChanged = {},
            onConfirmPasswordChanged = {}, onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {}, onSignIn = {}, onRegister = {},
            onClearError = {}
        )
    }
}
