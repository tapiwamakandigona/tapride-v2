package io.tapiwa.tapride.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand colors ─────────────────────────────────────────────────────────────
/** Primary blue used for buttons, app bars and highlights. */
val Blue600 = Color(0xFF1A73E8)

/** Darker variant used on pressed/focused states. */
val Blue800 = Color(0xFF1557B0)

/** Green for the secondary/success accent. */
val Green600 = Color(0xFF34A853)

/** Darker green variant. */
val Green800 = Color(0xFF267A3C)

// ── Neutral surfaces ─────────────────────────────────────────────────────────
val Gray50  = Color(0xFFF8F9FA)
val Gray100 = Color(0xFFF1F3F4)
val Gray900 = Color(0xFF202124)

// ── Semantic ─────────────────────────────────────────────────────────────────
val ErrorRed    = Color(0xFFEA4335)
val WarningAmber = Color(0xFFFBBC04)
val White       = Color(0xFFFFFFFF)
val Black       = Color(0xFF000000)

// ── Status badge colors ──────────────────────────────────────────────────────
val StatusRequested  = WarningAmber
val StatusAccepted   = Blue600
val StatusInProgress = Green600
val StatusCompleted  = Color(0xFF5F6368)
val StatusCancelled  = ErrorRed
