package com.example.sos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.sos.R

// ── Google Fonts provider ──────────────────────────────────────────────────────

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

// ── Font families ──────────────────────────────────────────────────────────────

val MontserratFamily: FontFamily = FontFamily(
    Font(googleFont = GoogleFont("Montserrat"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Montserrat"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Montserrat"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Montserrat"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Montserrat"), fontProvider = provider, weight = FontWeight.Black),
)

val BebasNeueFontFamily: FontFamily = FontFamily(
    Font(googleFont = GoogleFont("Bebas Neue"), fontProvider = provider, weight = FontWeight.Normal),
)

val OutfitFontFamily: FontFamily = FontFamily(
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Bold),
)

// ── Material typography ───────────────────────────────────────────────────────

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily   = OutfitFontFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily   = OutfitFontFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 11.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.5.sp
    )
)