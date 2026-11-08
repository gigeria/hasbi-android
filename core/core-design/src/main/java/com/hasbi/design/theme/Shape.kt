package com.hasbi.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Hasbi shape scale.
 *
 * Premium feel: rounded but not pill-shaped for cards.
 * Pills used for chips, badges, snackbar.
 */
val HasbiShapes: Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

// Custom shapes for specific components
val HasbiCardShape = RoundedCornerShape(14.dp)
val HasbiHeroCardShape = RoundedCornerShape(22.dp)
val HasbiBottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
val HasbiPillShape = RoundedCornerShape(100.dp)
val HasbiIconBoxShape = RoundedCornerShape(12.dp)
