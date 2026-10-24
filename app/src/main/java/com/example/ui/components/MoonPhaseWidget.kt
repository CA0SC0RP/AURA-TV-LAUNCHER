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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MoonPhaseInfo
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TvBorderFocused
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

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
      // Header
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
            fontWeight = FontWeight.Bold
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NeonAmber.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "${moonInfo.illuminationPercent}% Luz",
            color = NeonAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Moon graphic and phase name
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Custom Graphical Moon
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFF0F172A)),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.size(44.dp)) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Outer subtle lunar aura
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(Color(0x44FDE047), Color(0x10FDE047), Color.Transparent),
                center = center,
                radius = radius * 1.35f
              ),
              radius = radius * 1.25f,
              center = center
            )

            // Dark moon base (unlit side)
            drawCircle(
              color = Color(0xFF1E293B),
              radius = radius,
              center = center
            )

            // Moon illuminated portion
            val illumFraction = moonInfo.illuminationPercent / 100f
            if (illumFraction > 0.05f) {
              drawCircle(
                brush = Brush.radialGradient(
                  colors = listOf(Color(0xFFFEF08A), Color(0xFFFDE047), Color(0xFFCA8A04)),
                  center = Offset(center.x - radius * 0.2f, center.y - radius * 0.2f),
                  radius = radius
                ),
                radius = radius * (0.4f + (illumFraction * 0.6f)),
                center = center
              )

              // Craters details
              drawCircle(
                color = Color(0x33CA8A04),
                radius = radius * 0.16f,
                center = Offset(center.x - radius * 0.3f, center.y + radius * 0.1f)
              )
              drawCircle(
                color = Color(0x22CA8A04),
                radius = radius * 0.22f,
                center = Offset(center.x + radius * 0.25f, center.y - radius * 0.2f)
              )
            }

            // Outer ring
            drawCircle(
              color = Color(0x44FEF08A),
              radius = radius,
              center = center,
              style = Stroke(width = 1.5f)
            )
          }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = moonInfo.phaseName,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
          Text(
            text = "Día ${moonInfo.ageDays} de 29.5",
            color = TvTextSecondary,
            fontSize = 11.sp,
            maxLines = 1
          )
          Text(
            text = "Signo: ${moonInfo.constellation}",
            color = NeonPurple,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
          )
        }
      }

      // Astronomical Calendar details
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color.Black.copy(alpha = 0.3f))
          .padding(horizontal = 8.dp, vertical = 5.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Luna llena:", color = TvTextSecondary, fontSize = 11.sp, maxLines = 1)
          Text(text = moonInfo.nextFullMoonDate, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Luna nueva:", color = TvTextSecondary, fontSize = 11.sp, maxLines = 1)
          Text(text = moonInfo.nextNewMoonDate, color = NeonAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
      }
    }
  }

  // Astronomical modal when user presses OK / selects the Moon Widget
  if (showDetailsDialog) {
    TvModalOverlay(onDismiss = { showDetailsDialog = false }) {
      Surface(
        modifier = Modifier
          .width(380.dp),
        shape = RoundedCornerShape(18.dp),
        color = TvSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonAmber)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.NightlightRound, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = "Detalles de la Fase Lunar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { showDetailsDialog = false }) {
              Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TvTextSecondary)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Fase Actual: ${moonInfo.phaseName} (${moonInfo.illuminationPercent}% Iluminación)",
            color = NeonAmber,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "La luna se encuentra en el día ${moonInfo.ageDays} de su ciclo sinódico de 29.5 días en la constelación de ${moonInfo.constellation}.",
            color = TvTextSecondary,
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(Color.Black.copy(alpha = 0.35f))
              .padding(12.dp)
          ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "Próxima Luna Llena:", color = TvTextSecondary, fontSize = 11.sp)
              Text(text = moonInfo.nextFullMoonDate, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "Próxima Luna Nueva:", color = TvTextSecondary, fontSize = 11.sp)
              Text(text = moonInfo.nextNewMoonDate, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "Distancia media orbital:", color = TvTextSecondary, fontSize = 11.sp)
              Text(text = "384,400 km", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
