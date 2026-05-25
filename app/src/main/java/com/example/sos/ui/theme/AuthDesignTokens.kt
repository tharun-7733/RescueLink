package com.example.sos.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Single source of truth for every design value used in the Auth screen.
 * No magic numbers should exist outside this object.
 */
object AuthDesignTokens {

    // ── Background ────────────────────────────────────────────────────────
    val BackgroundStart = Color(0xFF070A0F)
    val BackgroundEnd   = Color(0xFF0D1117)

    // ── Card surface ──────────────────────────────────────────────────────
    val CardSurface     = Color(0xFF111827).copy(alpha = 0.85f)
    val CardCorner      = 24.dp
    val CardBorderStart = Color.White.copy(alpha = 0.12f)
    val CardBorderEnd   = Color.White.copy(alpha = 0.03f)
    val CardBorderWidth = 1.dp
    val CardShadowElevation = 32f

    // ── Accent (red gradient) ─────────────────────────────────────────────
    val AccentStart     = Color(0xFFE53935)
    val AccentEnd       = Color(0xFFB71C1C)

    // ── Radial glow ───────────────────────────────────────────────────────
    val GlowAccent      = Color(0xFFE53935).copy(alpha = 0.04f)

    // ── Typography ────────────────────────────────────────────────────────
    val TextPrimary     = Color(0xFFEEF2FF)
    val TextSecondary   = Color(0xFF8A9BB0)
    val TextHint        = Color(0xFF4A5568)
    val AppNameSize     = 24.sp
    val SubtitleSize    = 12.sp
    val TabLabelSize    = 13.sp
    val ErrorTextSize   = 11.sp
    val ErrorBannerTextSize = 13.sp

    // ── Input fields ──────────────────────────────────────────────────────
    val FieldFill       = Color(0xFF1C2333)
    val FieldBorder     = Color(0xFF2D3748)
    val FieldFocused    = Color(0xFFE53935)
    val FieldError      = Color(0xFFFF6B6B)
    val FieldCorner     = 14.dp

    // ── Tab row ───────────────────────────────────────────────────────────
    val TabBackground   = Color(0xFF1C2333)
    val TabCorner       = 50.dp

    // ── Error banner ──────────────────────────────────────────────────────
    val ErrorBannerBg     = Color(0xFF2D1515)
    val ErrorBannerBorder = Color(0xFFFF6B6B).copy(alpha = 0.4f)
    val ErrorBannerCorner = 10.dp

    // ── Logo ──────────────────────────────────────────────────────────────
    val LogoOuterSize   = 72.dp
    val LogoOuterAlpha  = 0.12f
    val LogoInnerSize   = 56.dp
    val LogoInnerAlpha  = 0.20f
    val LogoIconSize    = 32.dp

    // ── Submit button ─────────────────────────────────────────────────────
    val ButtonHeight    = 56.dp
    val ButtonCorner    = 14.dp
    val ButtonLoadingIndicatorSize   = 22.dp
    val ButtonLoadingIndicatorStroke = 2.dp

    // ── Animation durations ───────────────────────────────────────────────
    val ShakeAmplitude      = 14.dp
    val ShakeDurationMs     = 400
    val TabSlideDurationMs  = 300
    val FormTransitionMs    = 300
    val ColorAnimDurationMs = 200
}
