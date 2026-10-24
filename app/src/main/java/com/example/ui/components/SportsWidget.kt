package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsBasketball
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SportsEvent
import com.example.data.repository.NbaTeam
import com.example.data.repository.SportsRepository
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TvBorderFocused
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

/**
 * Ficha de deportes: dos pestañas (NBA y Tenis) y hasta tres partidos próximos.
 * En NBA, la lupa abre la lista de equipos para elegir el tuyo.
 */
@Composable
fun SportsWidget(
  sportsEvents: List<SportsEvent>,
  selectedFilter: String,
  nbaTeamName: String?,
  isLoading: Boolean,
  errorMessage: String?,
  onFilterChange: (String) -> Unit,
  onPickNbaTeam: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showTeamPicker by remember { mutableStateOf(false) }
  val isNba = selectedFilter.equals(SportsRepository.NBA, ignoreCase = true)

  // Tarjeta informativa: sin foco propio, el control remoto salta directo a los botones de adentro
  TvCard(
    modifier = modifier,
    backgroundColor = TvSurface.copy(alpha = 0.82f),
    focusedBorderColor = NeonGreen
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)
    ) {
      // Encabezado (+ lupa para elegir equipo cuando estás en NBA)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            imageVector = Icons.Default.SportsBasketball,
            contentDescription = null,
            tint = NeonGreen,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isNba && nbaTeamName != null) "NBA · $nbaTeamName" else "Deportes",
            color = TvTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        if (isNba) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(NeonGreen.copy(alpha = 0.25f))
              .tvClickable(shape = CircleShape, focusColor = NeonGreen) { showTeamPicker = true },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.Search,
              contentDescription = "Elegir equipo de la NBA",
              tint = NeonGreen,
              modifier = Modifier.size(15.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Selector: NBA o Tenis
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        SportsRepository.featuredSports.forEach { cat ->
          val isSelected = selectedFilter.equals(cat, ignoreCase = true)
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) NeonGreen.copy(alpha = 0.30f) else Color.Black.copy(alpha = 0.40f))
              .tvClickable(shape = RoundedCornerShape(8.dp), focusColor = NeonGreen) { onFilterChange(cat) }
              .padding(vertical = 5.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = cat,
              color = if (isSelected) NeonGreen else Color.White,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              maxLines = 1
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Tres partidos, cada uno con el mismo espacio
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        val message = when {
          isLoading -> "Cargando partidos…"
          isNba && nbaTeamName == null -> "Presiona la lupa y elige tu equipo de la NBA"
          errorMessage != null -> errorMessage
          sportsEvents.isEmpty() -> "No hay partidos próximos"
          else -> null
        }

        if (message != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = message,
              color = TvTextSecondary,
              fontSize = 12.sp,
              textAlign = TextAlign.Center
            )
          }
        } else {
          val visibleEvents = sportsEvents.take(3)
          visibleEvents.forEach { event ->
            SportsEventRow(
              event = event,
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            )
          }
          // Si hay menos de 3 partidos, el espacio sobrante queda vacío (sin estirar las filas)
          repeat(3 - visibleEvents.size) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }

  if (showTeamPicker) {
    NbaTeamPickerDialog(
      onDismiss = { showTeamPicker = false },
      onTeamSelected = { team ->
        onPickNbaTeam(team.abbr)
        showTeamPicker = false
      }
    )
  }
}

@Composable
fun SportsEventRow(event: SportsEvent, modifier: Modifier = Modifier) {
  val accent = if (event.sport.equals("Tenis", ignoreCase = true)) NeonBlue else NeonAmber

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color.Black.copy(alpha = 0.30f))
      .padding(horizontal = 8.dp, vertical = 3.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Barra de color según el deporte
    Box(
      modifier = Modifier
        .width(3.dp)
        .fillMaxHeight(0.75f)
        .clip(RoundedCornerShape(2.dp))
        .background(accent)
    )

    Spacer(modifier = Modifier.width(8.dp))

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center
    ) {
      // Quién juega contra quién (o el torneo, si todavía no hay partidos definidos)
      Text(
        text = if (event.teamAway.isBlank()) event.teamHome else "${event.teamHome} vs ${event.teamAway}",
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      // Cuándo juegan
      Text(
        text = event.airDateTime,
        color = NeonAmber,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun NbaTeamPickerDialog(
  onDismiss: () -> Unit,
  onTeamSelected: (NbaTeam) -> Unit
) {
  TvModalOverlay(onDismiss = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .height(320.dp),
      shape = RoundedCornerShape(16.dp),
      color = TvSurfaceElevated,
      border = androidx.compose.foundation.BorderStroke(1.dp, TvBorderFocused)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 18.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Elige tu equipo de la NBA",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Verás sus próximos partidos con fecha y hora",
              color = TvTextSecondary,
              fontSize = 11.sp
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TvTextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
          columns = GridCells.Fixed(5),
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(SportsRepository.nbaTeams, key = { it.abbr }) { team ->
            Box(
              modifier = Modifier
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.40f))
                .tvClickable(shape = RoundedCornerShape(8.dp), focusColor = NeonGreen) { onTeamSelected(team) },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = team.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp)
              )
            }
          }
        }
      }
    }
  }
}
