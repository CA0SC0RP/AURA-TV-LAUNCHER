package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MoonPhaseInfo
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

/** Frase corta que dice hacia dónde va la luna. */
private fun moonTrend(info: MoonPhaseInfo): String = when {
  info.phaseName == "Luna nueva" -> "Cielo oscuro"
  info.phaseName == "Luna llena" -> "Máximo brillo"
  info.isWaxing -> "Va creciendo"
  else -> "Va menguando"
}

/** Dibuja la luna con la forma real de su fase (la parte iluminada cambia cada noche). */
@Composable
fun MoonDisc(phase: Double, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val r = size.minDimension / 2f * 0.82f
    val c = Offset(size.width / 2f, size.height / 2f)

    // Resplandor suave
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color(0x33FDE047), Color.Transparent),
        center = c,
        radius = r * 1.5f
      ),
      radius = r * 1.5f,
      center = c
    )

    // Lado oscuro
    drawCircle(color = Color(0xFF1E293B), radius = r, center = c)

    // Lado iluminado: media luna a un lado y la elipse del borde de sombra
    val waxing = phase < 0.5
    val k = cos(2.0 * PI * phase).toFloat()
    val disc = Rect(c.x - r, c.y - r, c.x + r, c.y + r)
    val half = Path().apply {
      arcTo(disc, if (waxing) -90f else 90f, 180f, true)
      close()
    }
    val ellipse = Path().apply {
      addOval(Rect(c.x - r * abs(k), c.y - r, c.x + r * abs(k), c.y + r))
    }
    val isCrescent = if (waxing) phase < 0.25 else phase > 0.75
    val lit = Path.combine(
      if (isCrescent) PathOperation.Difference else PathOperation.Union,
      half,
      ellipse
    )
    drawPath(
      path = lit,
      brush = Brush.radialGradient(
        colors = listOf(Color(0xFFFEF9C3), Color(0xFFFDE047), Color(0xFFCA8A04)),
        center = Offset(c.x - r * 0.2f, c.y - r * 0.2f),
        radius = r * 1.4f
      )
    )

    drawCircle(color = Color(0x55FEF08A), radius = r, center = c, style = Stroke(width = 1.5f))
  }
}

@Composable
fun MoonPhaseWidget(
  moonInfo: MoonPhaseInfo,
  modifier: Modifier = Modifier
) {
  var showDetailsDialog by remember { mutableStateOf(false) }

  TvCard(
    onClick = { showDetailsDialog = true },
    modifier = modifier,
    backgroundColor = TvSurface.copy(alpha = 0.82f),
    focusedBorderColor = NeonAmber,
    focusedScale = 1.04f
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Encabezado
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.NightlightRound,
            contentDescription = null,
            tint = NeonAmber,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Fase Lunar",
            color = TvTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NeonAmber.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "${moonInfo.illuminationPercent}% luz",
            color = NeonAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
        }
      }

      // La luna y su fase
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        MoonDisc(phase = moonInfo.phase, modifier = Modifier.size(56.dp))

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = moonInfo.phaseName,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = moonTrend(moonInfo),
            color = NeonAmber,
            fontSize = 11.sp,
            maxLines = 1
          )
          Text(
            text = "Signo: ${moonInfo.constellation}",
            color = NeonPurple,
            fontSize = 11.sp,
            maxLines = 1
          )
        }
      }

      // Datos rápidos
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color.Black.copy(alpha = 0.3f))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Sale ${moonInfo.moonrise}", color = Color.White, fontSize = 11.sp, maxLines = 1)
          Text(text = "Se oculta ${moonInfo.moonset}", color = Color.White, fontSize = 11.sp, maxLines = 1)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Luna llena", color = TvTextSecondary, fontSize = 11.sp, maxLines = 1)
          Text(
            text = moonInfo.nextFullMoonDate,
            color = NeonAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
        }
      }
    }
  }

  if (showDetailsDialog) {
    MoonDetailsDialog(moonInfo = moonInfo, onDismiss = { showDetailsDialog = false })
  }
}

@Composable
private fun MoonDataRow(label: String, value: String, valueColor: Color = Color.White) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, color = TvTextSecondary, fontSize = 13.sp, maxLines = 1)
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = value,
      color = valueColor,
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun MoonSection(title: String, content: @Composable () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(Color.Black.copy(alpha = 0.35f))
      .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(5.dp)
  ) {
    Text(text = title, color = NeonAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    content()
  }
}

@Composable
fun MoonDetailsDialog(moonInfo: MoonPhaseInfo, onDismiss: () -> Unit) {
  // La X arranca seleccionada: con el control basta presionar OK para cerrar
  val closeFocus = remember { FocusRequester() }
  LaunchedEffect(Unit) { runCatching { closeFocus.requestFocus() } }

  TvModalOverlay(onDismiss = onDismiss) {
    Surface(
      modifier = Modifier
        .width(680.dp)
        .height(400.dp),
      shape = RoundedCornerShape(18.dp),
      color = TvSurfaceElevated,
      border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonAmber)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 18.dp, vertical = 12.dp)
      ) {
        // Título y cerrar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.NightlightRound, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "La Luna hoy", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
          }
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.12f))
              .focusRequester(closeFocus)
              .tvClickable(shape = CircleShape, focusColor = NeonAmber) { onDismiss() },
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(22.dp))
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Izquierda: la luna grande y su fase
          Column(
            modifier = Modifier.width(150.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            MoonDisc(phase = moonInfo.phase, modifier = Modifier.size(110.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = moonInfo.phaseName,
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1
            )
            Text(
              text = moonTrend(moonInfo),
              color = NeonAmber,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              maxLines = 1
            )
          }

          // Datos de hoy, en dos columnas cortas
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
          ) {
            MoonSection(title = "Hoy") {
              MoonDataRow("Luz", "${moonInfo.illuminationPercent}%")
              MoonDataRow("Día del ciclo", "${moonInfo.ageDays} de 29.5")
              MoonDataRow("Signo", moonInfo.constellation, NeonPurple)
            }
          }
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
          ) {
            MoonSection(title = "En el cielo") {
              MoonDataRow("Sale", moonInfo.moonrise, NeonAmber)
              MoonDataRow("Se oculta", moonInfo.moonset, NeonAmber)
              MoonDataRow("Distancia", String.format(Locale.US, "%,d km", moonInfo.distanceKm))
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // El ciclo completo: cómo va cambiando la luna y qué día llega a cada forma
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
          Text(text = "Cómo cambia la luna", color = NeonAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            moonInfo.phaseStrip.forEach { point ->
              Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                MoonDisc(phase = point.phase, modifier = Modifier.size(46.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = point.dateLabel,
                  color = Color.White,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  maxLines = 1
                )
              }
            }
          }
        }
      }
    }
  }
}
