package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// DESIGN SYSTEM — Solo Leveling · RANGO S · Modo Oscuro Premium
// =========================================================================

// COLORES OFICIALES
val SoloBg = Color(0xFF050508)                  // Fondo principal #050508
val SoloCardBg = Color(0xFF0F0F14)              // Fondo tarjetas #0F0F14
val SoloCardBorder = Color(0xFF1A1A24)          // Borde sutil tarjetas #1A1A24
val SoloNeonCyan = Color(0xFF00D9FF)            // Neón activo cyan #00D9FF
val SoloNeonPurple = Color(0xFF8A2BE2)          // Neón activo morado #8A2BE2
val SoloTextPrimary = Color(0xFFFFFFFF)         // Texto principal #FFFFFF
val SoloTextSecondary = Color(0xFF8B8B9B)       // Texto secundario #8B8B9B
val SoloGold = Color(0xFFFFC700)                // Acento dorado (XP, niveles, rangos) #FFC700
val SoloTrack = Color(0xFF1A1A24)               // Track de barras #1A1A24
val SoloGlow = Color(0xFF00D9FF).copy(alpha = 0.4f) // Glow: 0 0 15px rgba(0,217,255,0.4)
val SoloBorderGlow = Color(0xFF00D9FF).copy(alpha = 0.35f)

// DEGRADADOS NEÓN OFICIALES
val SoloNeonGradient = Brush.horizontalGradient(listOf(SoloNeonCyan, SoloNeonPurple))
val SoloVerticalNeonGradient = Brush.verticalGradient(listOf(SoloNeonCyan, SoloNeonPurple))
val SoloCardBorderBrush = Brush.linearGradient(
    listOf(
        SoloNeonCyan.copy(alpha = 0.40f),
        SoloCardBorder,
        SoloNeonPurple.copy(alpha = 0.30f)
    )
)

// =========================================================================
// COMPATIBILIDAD Y TOKENS LIFEOS MAPEADOS AL MODO RANGO S
// =========================================================================
val ExecutiveDarkBg = SoloBg
val ExecutiveDarkSurface = SoloCardBg
val ExecutiveDarkSurfaceVariant = Color(0xFF12121A)
val ExecutiveDarkSurfaceElevated = Color(0xFF161622)
val ExecutiveBorderSubtle = SoloCardBorder
val ExecutiveBorder = Color(0xFF222232)
val ExecutiveBorderGlow = SoloBorderGlow

// Identidad Visual Primaria: Neón Cyan + Morado
val ExecutiveElectricViolet = SoloNeonPurple
val ExecutiveVioletGlow = Color(0xFFA855F7)
val ExecutiveDeepViolet = Color(0xFF7928CA)
val ExecutiveLuminousBlue = SoloNeonCyan
val ExecutiveNeonCyan = SoloNeonCyan

// Status Tokens
val ExecutivePrimaryTeal = SoloNeonCyan
val ExecutivePrimaryTealContainer = Color(0xFF0A1E30)
val ExecutiveCyan = SoloNeonCyan
val ExecutiveSecondaryAmber = SoloGold
val ExecutiveAccentBlue = SoloNeonCyan
val ExecutiveDangerRed = Color(0xFFFF3366)
val ExecutiveSuccessGreen = Color(0xFF00FF9D)
val ExecutivePurple = SoloNeonPurple

// Typography & Contrast Tokens
val ExecutiveTextWhite = SoloTextPrimary
val ExecutiveTextMuted = SoloTextSecondary
val ExecutiveTextDim = Color(0xFF6B6B7B)

// Tactical Aliases
val TacticalNeonCyan = SoloNeonCyan
val TacticalLuminousBlue = SoloNeonCyan
val TacticalVioletIdentity = SoloNeonPurple
val TacticalSuccessGreen = Color(0xFF00FF9D)
val TacticalWarningAmber = SoloGold
val TacticalCriticalCrimson = Color(0xFFFF3366)
val ExecutiveTextPrimary = SoloTextPrimary
val ExecutiveTextSecondary = SoloTextSecondary
