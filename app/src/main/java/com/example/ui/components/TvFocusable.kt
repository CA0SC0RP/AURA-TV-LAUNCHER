package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TvBorder
import com.example.ui.theme.TvSurface

@Composable
fun TvModalOverlay(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  BackHandler(onBack = onDismiss)
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.85f))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onDismiss
      ),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = modifier.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = { /* consume clicks inside dialog */ }
      ),
      content = content
    )
  }
}

@Composable
fun TvCard(
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(16.dp),
  backgroundColor: Color = TvSurface,
  borderColor: Color = TvBorder,
  focusedBorderColor: Color = NeonCyan,
  focusedScale: Float = 1.05f,
  showFocusGlow: Boolean = true,
  // Una tarjeta sin acción (solo informativa) no necesita foco: así el control remoto la salta
  // y no aparecen resaltados raros en widgets que no se pueden "abrir".
  isFocusable: Boolean = onClick != null,
  elevation: Dp = 6.dp,
  content: @Composable BoxScope.() -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()
  val isHovered by interactionSource.collectIsHoveredAsState()
  val isActive = isFocused || isHovered

  // Smooth bounce scale animation when TV remote moves focus
  val scale by animateFloatAsState(
    targetValue = if (isActive) focusedScale else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "tv_card_scale"
  )

  // Animated border color for glowing neon effect
  val currentBorderColor by animateColorAsState(
    targetValue = if (isActive) focusedBorderColor else borderColor,
    animationSpec = tween(durationMillis = 200),
    label = "tv_border_color"
  )

  val borderWidth = if (isActive) 2.5.dp else 1.dp

  Box(
    modifier = modifier
      .scale(scale)
      .shadow(
        elevation = if (isActive) elevation * 2.5f else elevation,
        shape = shape,
        spotColor = if (isActive) focusedBorderColor else Color.Black
      )
      .clip(shape)
      .background(backgroundColor)
      .border(BorderStroke(borderWidth, currentBorderColor), shape)
      .then(
        when {
          // clickable ya es enfocable por sí mismo: un solo objetivo de foco por tarjeta
          onClick != null -> Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
          )
          isFocusable -> Modifier.focusable(interactionSource = interactionSource)
          else -> Modifier
        }
      )
  ) {
    // Glowing ambient background overlay when in TV remote focus
    if (isActive && showFocusGlow) {
      // matchParentSize: el brillo se ajusta al tamaño de la tarjeta. Con fillMaxSize, una tarjeta
      // sin tamaño fijo (como los botones de arriba) crecía hasta ocupar toda la pantalla al enfocarla.
      Box(
        modifier = Modifier
          .matchParentSize()
          .background(
            Brush.radialGradient(
              colors = listOf(
                focusedBorderColor.copy(alpha = 0.22f),
                focusedBorderColor.copy(alpha = 0.05f),
                Color.Transparent
              ),
              radius = 500f
            )
          )
      )
    }

    content()
  }
}

/**
 * Botón pequeño (chip, flecha, pestaña) que se puede seleccionar con el control remoto.
 * Al enfocarlo se ilumina con un borde de color; no usa la indicación blanca por defecto.
 */
fun Modifier.tvClickable(
  shape: Shape = RoundedCornerShape(8.dp),
  focusColor: Color = NeonCyan,
  onClick: () -> Unit
): Modifier = composed {
  val source = remember { MutableInteractionSource() }
  val focused by source.collectIsFocusedAsState()
  this
    .background(if (focused) focusColor.copy(alpha = 0.28f) else Color.Transparent, shape)
    .border(
      width = if (focused) 2.dp else 0.dp,
      color = if (focused) focusColor else Color.Transparent,
      shape = shape
    )
    .clickable(interactionSource = source, indication = null, onClick = onClick)
}
