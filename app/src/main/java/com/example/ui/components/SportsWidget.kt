package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsMotorsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SportCategory
import com.example.data.model.SportsEvent
import com.example.data.repository.SportsRepository
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRose
import com.example.ui.theme.TvBorder
import com.example.ui.theme.TvBorderFocused
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun SportsWidget(
  sportsEvents: List<SportsEvent>,
  selectedFilter: String,
  onFilterChange: (String) -> Unit,
  onAddCustomSport: (String, String, String, String, String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showSearchDialog by remember { mutableStateOf(false) }

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
      // Encabezado + botón para buscar / programar un partido
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
            text = "Deportes",
            color = TvTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
        }

        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(NeonGreen.copy(alpha = 0.25f))
            .tvClickable(shape = CircleShape, focusColor = NeonGreen) { showSearchDialog = true },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Default.Search,
            contentDescription = "Buscar o añadir partido",
            tint = NeonGreen,
            modifier = Modifier.size(15.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Selector: solo unos pocos deportes, todos caben a la vista
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
              fontSize = 11.sp,
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
        if (sportsEvents.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No hay partidos próximos",
              color = TvTextSecondary,
              fontSize = 12.sp
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

  // Dialog to enter custom sport or team
  if (showSearchDialog) {
    SearchSportDialog(
      onDismiss = { showSearchDialog = false },
      onFilterSelected = { sportName ->
        onFilterChange(sportName)
        showSearchDialog = false
      },
      onAddCustom = { sport, tourney, home, away, time, channel ->
        onAddCustomSport(sport, tourney, home, away, time, channel)
        showSearchDialog = false
      }
    )
  }
}

private fun sportAccentColor(sport: String): Color = when {
  sport.contains("Fútbol", ignoreCase = true) -> NeonGreen
  sport.contains("Baloncesto", ignoreCase = true) || sport.contains("NBA", ignoreCase = true) -> NeonAmber
  sport.contains("Fórmula", ignoreCase = true) -> NeonRose
  else -> NeonBlue
}

@Composable
fun SportsEventRow(event: SportsEvent, modifier: Modifier = Modifier) {
  val accent = sportAccentColor(event.sport)

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
      // Quién juega contra quién
      Text(
        text = "${event.teamHome} vs ${event.teamAway}",
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      // Cuándo juegan, y en qué torneo
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = event.airDateTime,
          color = NeonAmber,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1
        )
        Text(
          text = "  ·  ${event.tournament}",
          color = TvTextSecondary,
          fontSize = 10.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f, fill = false)
        )
      }
    }
  }
}

@Composable
fun SearchSportDialog(
  onDismiss: () -> Unit,
  onFilterSelected: (String) -> Unit,
  onAddCustom: (String, String, String, String, String, String) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var isAddingMode by remember { mutableStateOf(false) }

  // Custom fields
  var customSport by remember { mutableStateOf("Fútbol") }
  var customTourney by remember { mutableStateOf("") }
  var customHome by remember { mutableStateOf("") }
  var customAway by remember { mutableStateOf("") }
  var customTime by remember { mutableStateOf("") }
  var customChannel by remember { mutableStateOf("ESPN") }

  TvModalOverlay(onDismiss = onDismiss) {
    Surface(
      modifier = Modifier
        .width(420.dp),
      shape = RoundedCornerShape(16.dp),
      color = TvSurfaceElevated,
      border = androidx.compose.foundation.BorderStroke(1.dp, TvBorderFocused)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isAddingMode) "Programar Deporte en TV" else "Buscar Deporte o Equipo",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TvTextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!isAddingMode) {
          Text(
            text = "Escribe el deporte o equipo que deseas consultar (ej. 'Fórmula 1', 'Lakers', 'Real Madrid', 'Béisbol'):",
            color = TvTextSecondary,
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Ej. F1, NBA, UFC, Boca...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonGreen,
              unfocusedBorderColor = TvBorder,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Button(
              onClick = { isAddingMode = true },
              colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
              border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
            ) {
              Text("+ Programar Nuevo", color = NeonGreen)
            }

            Button(
              onClick = {
                if (searchQuery.isNotBlank()) onFilterSelected(searchQuery)
              },
              colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
              Text("Filtrar", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
        } else {
          // Add custom schedule mode
          OutlinedTextField(
            value = customSport,
            onValueChange = { customSport = it },
            label = { Text("Deporte (Fútbol, F1, Boxeo...)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = customTourney,
            onValueChange = { customTourney = it },
            label = { Text("Torneo o Liga") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = customHome,
              onValueChange = { customHome = it },
              label = { Text("Equipo / Jugador 1") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedTextField(
              value = customAway,
              onValueChange = { customAway = it },
              label = { Text("Equipo / Jugador 2") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = customTime,
              onValueChange = { customTime = it },
              label = { Text("Día y Hora (ej. Sábado 20:00)") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedTextField(
              value = customChannel,
              onValueChange = { customChannel = it },
              label = { Text("Canal de TV (ej. DAZN)") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            Button(
              onClick = {
                if (customHome.isNotBlank()) {
                  onAddCustom(
                    customSport,
                    customTourney.ifBlank { "Torneo" },
                    customHome,
                    customAway.ifBlank { "Rival" },
                    customTime.ifBlank { "Próximamente" },
                    customChannel.ifBlank { "TV" }
                  )
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
              Text("Guardar Evento", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
