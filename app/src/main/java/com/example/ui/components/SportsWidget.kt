package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SportsEvent
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

private val sportFilters = listOf("Todos", "NBA", "Fútbol", "Fórmula 1", "NFL")

@Composable
fun SportsWidget(
  sportsEvents: List<SportsEvent>,
  selectedFilter: String,
  onFilterChange: (String) -> Unit,
  favoriteTeams: Set<String>,
  onToggleFavoriteTeam: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showDetails by remember { mutableStateOf(false) }
  val favoriteFiltered = if (favoriteTeams.isEmpty()) sportsEvents else sportsEvents.filter {
    it.sport == "Fórmula 1" || it.teamHome in favoriteTeams || it.teamAway in favoriteTeams
  }
  val visibleEvents = if (selectedFilter.equals("Todos", true)) favoriteFiltered
    else favoriteFiltered.filter { it.sport.equals(selectedFilter, true) }

  Surface(
    modifier = modifier.clickable { showDetails = true },
    shape = RoundedCornerShape(14.dp),
    color = TvSurface.copy(alpha = 0.84f),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.SportsBasketball, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(17.dp))
          Spacer(Modifier.width(6.dp))
          Text("DEPORTES", color = TvTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Icon(Icons.Default.CalendarMonth, contentDescription = "Abrir calendario deportivo", tint = NeonGreen, modifier = Modifier.size(17.dp))
      }

      Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        sportFilters.drop(1).forEach { sport ->
          Box(
            modifier = Modifier.clip(RoundedCornerShape(5.dp))
              .background(if (selectedFilter.equals(sport, true)) NeonGreen.copy(alpha = 0.24f) else Color.Black.copy(alpha = 0.25f))
              .clickable { onFilterChange(if (selectedFilter.equals(sport, true)) "Todos" else sport) }
              .padding(horizontal = 5.dp, vertical = 4.dp)
          ) {
            Text(sport, color = if (selectedFilter.equals(sport, true)) NeonGreen else TvTextSecondary, fontSize = 8.sp, maxLines = 1)
          }
        }
      }

      if (visibleEvents.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("Sin eventos disponibles\nPulsa para consultar la agenda", color = TvTextSecondary, fontSize = 10.sp)
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          visibleEvents.take(3).forEach { event ->
            Column(
              modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(7.dp))
                .background(Color.Black.copy(alpha = 0.28f)).padding(7.dp)
            ) {
              Text(event.sport.uppercase(), color = NeonGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
              Text(
                text = if (event.sport == "Fórmula 1") event.teamHome else "${event.teamHome} vs ${event.teamAway}",
                color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis
              )
              Text(event.airDateTime, color = NeonAmber, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
          }
          Text("Abrir calendario completo", color = TvTextSecondary, fontSize = 9.sp)
        }
      }
    }
  }

  if (showDetails) {
    BackHandler { showDetails = false }
    Dialog(onDismissRequest = { showDetails = false }) {
      Surface(
        modifier = Modifier.fillMaxWidth(0.94f).height(500.dp),
        shape = RoundedCornerShape(18.dp),
        color = TvSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.65f))
      ) {
        Column(Modifier.fillMaxSize().padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("AGENDA DEPORTIVA", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
              Text(
            if (favoriteTeams.isEmpty()) "Próximos eventos · hora local · elige favoritos con ★"
            else "Favoritos: ${favoriteTeams.size} · hora local",
            color = TvTextSecondary, fontSize = 11.sp
          )
            }
            IconButton(onClick = { showDetails = false }) {
              Icon(Icons.Default.Close, contentDescription = "Cerrar calendario", tint = Color.White)
            }
          }

          Spacer(Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            sportFilters.forEach { sport ->
              Box(
                modifier = Modifier.clip(RoundedCornerShape(7.dp))
                  .background(if (selectedFilter.equals(sport, true)) NeonGreen.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.30f))
                  .clickable { onFilterChange(sport) }
                  .padding(horizontal = 10.dp, vertical = 7.dp)
              ) {
                Text(sport, color = if (selectedFilter.equals(sport, true)) NeonGreen else TvTextSecondary, fontSize = 10.sp)
              }
            }
          }
          Spacer(Modifier.height(10.dp))
          val detailEvents = if (selectedFilter.equals("Todos", true)) sportsEvents
            else sportsEvents.filter { it.sport.equals(selectedFilter, true) }
          if (detailEvents.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text("No hay próximos eventos disponibles.\nComprueba la conexión más tarde.", color = TvTextSecondary, fontSize = 13.sp)
            }
          } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              items(detailEvents) { event ->
                SportsDetailRow(event, favoriteTeams, onToggleFavoriteTeam)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SportsDetailRow(
  event: SportsEvent,
  favoriteTeams: Set<String>,
  onToggleFavoriteTeam: (String) -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
      .background(Color.Black.copy(alpha = 0.28f)).padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
      Text(event.sport.uppercase(), color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
      Text(event.status, color = if (event.isLive) Color(0xFFFF6565) else NeonAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
    if (event.sport == "Fórmula 1") {
      Text(event.teamHome, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    } else {
      listOf(event.teamHome, event.teamAway).forEach { team ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(team, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
          IconButton(onClick = { onToggleFavoriteTeam(team) }) {
            Icon(
              imageVector = if (team in favoriteTeams) Icons.Default.Star else Icons.Default.StarBorder,
              contentDescription = if (team in favoriteTeams) "Quitar favorito" else "Añadir favorito",
              tint = if (team in favoriteTeams) NeonAmber else TvTextSecondary
            )
          }
        }
      }
    }
    Text(event.airDateTime, color = NeonAmber, fontSize = 12.sp)
    Text(event.tournament, color = TvTextSecondary, fontSize = 11.sp)
    event.venue?.let { Text("Sede: $it", color = TvTextSecondary, fontSize = 11.sp) }
  }
}
