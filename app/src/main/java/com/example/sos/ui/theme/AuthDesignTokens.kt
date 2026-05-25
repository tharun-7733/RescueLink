package com.example.sos.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Single source of truth for every design value used in the Auth screen.
 * Matches the RescueLink HTML reference exactly.
 */
object AuthDesignTokens {

    // ── Brand Palette ─────────────────────────────────────────────────────
    val RedHot         = Color(0xFFE8001D)
    val RedDeep        = Color(0xFF9B0013)
    val RedGlow        = Color(0xFFFF1A35)

    // ── Background ────────────────────────────────────────────────────────
    val BackgroundCore = Color(0xFF0A0A0A)
    val BackgroundStart= Color(0xFF0A0A0A)
    val BackgroundEnd  = Color(0xFF0A0A0A)

    // ── Card / Surface ────────────────────────────────────────────────────
    val CardSurface     = Color(0xFF111111)
    val CardCorner      = 24.dp
    val CardBorderStart = Color(0xFF242424)
    val CardBorderEnd   = Color(0xFF242424)
    val CardBorderWidth = 1.dp
    val CardShadowElevation = 32f

    // ── Mid / Rim ──────────────────────────────────────────────────────────
    val BlackMid       = Color(0xFF1A1A1A)
    val BlackRim       = Color(0xFF242424)

    // ── Accent (legacy – now mapped to red) ───────────────────────────────
    val AccentStart    = Color(0xFFE8001D)
    val AccentEnd      = Color(0xFF9B0013)

    // ── Radial Glow ───────────────────────────────────────────────────────
    val GlowAccent     = Color(0xFFE8001D).copy(alpha = 0.18f)
    val GlowAccent2    = Color(0xFF9B0013).copy(alpha = 0.18f)

    // ── Grid overlay ──────────────────────────────────────────────────────
    val GridLineColor  = Color(0xFFE8001D).copy(alpha = 0.04f)
    val GridCellSize   = 44.dp

    // ── Typography ────────────────────────────────────────────────────────
    val TextPrimary    = Color(0xFFFFFFFF)
    val TextSecondary  = Color(0x99FFFFFF)   // 60% white
    val TextHint       = Color(0x33FFFFFF)   // 20% white
    val TextLabel      = Color(0x73FFFFFF)   // 45% white

    val BrandNameSize   = 46.sp
    val BrandTagSize    = 11.sp
    val AppNameSize     = 24.sp
    val SubtitleSize    = 12.sp
    val CardTitleSize   = 22.sp
    val CardSubSize     = 13.sp
    val TabLabelSize    = 13.sp
    val ErrorTextSize   = 11.sp
    val ErrorBannerTextSize = 13.sp
    val PillTextSize    = 11.sp
    val SosTextSize     = 13.sp
    val SosSubTextSize  = 11.sp

    // ── Input fields ──────────────────────────────────────────────────────
    val FieldFill      = Color(0xFF1A1A1A)
    val FieldBorder    = Color(0xFF242424)
    val FieldFocused   = Color(0xFFE8001D)
    val FieldFocusBg   = Color(0xFF1C0004)
    val FieldError     = Color(0xFFFF6B6B)
    val FieldCorner    = 12.dp

    // ── Tab row ───────────────────────────────────────────────────────────
    val TabBackground  = Color(0xFF111111)
    val TabCorner      = 50.dp

    // ── Error banner ──────────────────────────────────────────────────────
    val ErrorBannerBg     = Color(0xFF2D1515)
    val ErrorBannerBorder = Color(0xFFFF6B6B).copy(alpha = 0.4f)
    val ErrorBannerCorner = 10.dp

    // ── Hero icon ─────────────────────────────────────────────────────────
    val HeroRingSize     = 88.dp
    val HeroRingCorner   = 26.dp
    val HeroRingBg1      = Color(0xFF1E0007)
    val HeroRingBg2      = Color(0xFF2D0009)
    val HeroRingBorder   = Color(0xFFE8001D).copy(alpha = 0.35f)
    val HeroIconSize     = 44.dp
    val PulseDotSize     = 18.dp
    val PulseDotColor    = Color(0xFFE8001D)

    // ── Logo (legacy circular rings) ──────────────────────────────────────
    val LogoOuterSize   = 72.dp
    val LogoOuterAlpha  = 0.12f
    val LogoInnerSize   = 56.dp
    val LogoInnerAlpha  = 0.20f
    val LogoIconSize    = 32.dp

    // ── Submit button ─────────────────────────────────────────────────────
    val ButtonHeight    = 56.dp
    val ButtonCorner    = 12.dp
    val ButtonLoadingIndicatorSize   = 22.dp
    val ButtonLoadingIndicatorStroke = 2.dp

    // ── SOS Strip ─────────────────────────────────────────────────────────
    val SosBadgeSize   = 36.dp
    val SosBadgeCorner = 10.dp
    val SosStripBg     = Color(0xFFE8001D).copy(alpha = 0.07f)
    val SosStripBgHot  = Color(0xFFE8001D).copy(alpha = 0.25f)
    val SosStripBorder = Color(0xFFE8001D).copy(alpha = 0.18f)
    val SosStripBorderHot = Color(0xFFE8001D).copy(alpha = 0.7f)
    val SosStripCorner = 14.dp

    // ── Feature Pills ─────────────────────────────────────────────────────
    val PillBg         = Color(0xFFFFFFFF).copy(alpha = 0.08f)
    val PillBorder     = Color(0xFFFFFFFF).copy(alpha = 0.07f)
    val PillCorner     = 100.dp
    val PillIconColor  = Color(0xFFE8001D)
    val PillTextColor  = Color(0xFFFFFFFF).copy(alpha = 0.5f)

    // ── Animation durations (millis) ──────────────────────────────────────
    val ShakeAmplitude         = 14.dp
    val ShakeDurationMs        = 400
    val TabSlideDurationMs     = 300
    val FormTransitionMs       = 300
    val ColorAnimDurationMs    = 200
    val PulseDurationMs        = 1400
    val GlowPulseDurationMs    = 5000
    val SpinRingDurationMs     = 3000
    val StripFlashDurationMs   = 600
}
