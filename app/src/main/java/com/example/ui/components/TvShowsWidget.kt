package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.TvMazeSearchResult
import com.example.data.model.TvSeries
import com.example.data.repository.TvMazeRepository
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TvBorder
import com.example.ui.theme.TvBorderFocused
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvTextMuted
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.launch

@Composable
fun TvShowsWidget(
  seriesList: List<TvSeries>,
  onAddSeries: (TvSeries) -> Unit,
  onUpdateSeries: (TvSeries) -> Unit,
  onRemoveSeries: (Long) -> Unit,
  tvMazeRepository: TvMazeRepository,
  modifier: Modifier = Modifier
) {
  var showAddDialog by remember { mutableStateOf(false) }
  var showCalendarDialog by remember { mutableStateOf(false) }
  var selectedSeriesForDetails by remember { mutableStateOf<TvSeries?>(null) }

  // Horizontal strip without label pill, directly showing series, + Serie, and Calendario
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 6.dp, vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    LazyRow(
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Calendario button
      item {
        TvCard(
          onClick = { showCalendarDialog = true },
          modifier = Modifier
            .height(60.dp)
            .width(130.dp),
          shape = RoundedCornerShape(10.dp),
          backgroundColor = Color.Black.copy(alpha = 0.55f),
          focusedBorderColor = NeonCyan,
          focusedScale = 1.07f
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = "Calendario de Series",
              tint = NeonCyan,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Calendario",
              color = NeonCyan,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // 2. Tracked TV series list
      items(seriesList, key = { it.id }) { series ->
        CompactTvSeriesCard(
          series = series,
          onClick = { selectedSeriesForDetails = series },
          onIncrementEpisode = {
            val updated = series.copy(currentEpisode = series.currentEpisode + 1)
            onUpdateSeries(updated)
          }
        )
      }

      // 3. Compact Add Series button (+ Serie)
      item {
        TvCard(
          onClick = { showAddDialog = true },
          modifier = Modifier
            .height(60.dp)
            .width(112.dp),
          shape = RoundedCornerShape(10.dp),
          backgroundColor = Color.Black.copy(alpha = 0.45f),
          focusedBorderColor = NeonBlue,
          focusedScale = 1.07f
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Agregar Serie",
              tint = NeonBlue,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "+ Serie",
              color = NeonBlue,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }

  // Dialog for searching and adding new TV series via TVMaze (Top anchored to avoid keyboard collision)
  if (showAddDialog) {
    AddSeriesDialog(
      onDismiss = { showAddDialog = false },
      tvMazeRepository = tvMazeRepository,
      onSeriesSelected = { newSeries ->
        onAddSeries(newSeries)
        showAddDialog = false
      }
    )
  }

  // Dialog for weekly TV series calendar (Lunes a Domingo)
  if (showCalendarDialog) {
    TvSeriesCalendarDialog(
      seriesList = seriesList,
      onDismiss = { showCalendarDialog = false },
      onSelectSeries = {
        showCalendarDialog = false
        selectedSeriesForDetails = it
      }
    )
  }

  // Dialog for series details, episode stepper & easy deletion
  selectedSeriesForDetails?.let { series ->
    SeriesDetailDialog(
      series = series,
      onDismiss = { selectedSeriesForDetails = null },
      onUpdate = { updated ->
        onUpdateSeries(updated)
        selectedSeriesForDetails = null
      },
      onDelete = {
        onRemoveSeries(series.id)
        selectedSeriesForDetails = null
      }
    )
  }
}

@Composable
fun CompactTvSeriesCard(
  series: TvSeries,
  onClick: () -> Unit,
  onIncrementEpisode: () -> Unit
) {
  TvCard(
    onClick = onClick,
    modifier = Modifier
      .height(64.dp)
      .width(270.dp),
    shape = RoundedCornerShape(10.dp),
    backgroundColor = Color.Black.copy(alpha = 0.55f),
    focusedBorderColor = NeonPurple,
    focusedScale = 1.05f
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 6.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Mini Poster
      Box(
        modifier = Modifier
          .size(42.dp, 56.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF1E293B)),
        contentAlignment = Alignment.Center
      ) {
        if (!series.posterUrl.isNullOrBlank()) {
          AsyncImage(
            model = series.posterUrl,
            contentDescription = series.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        } else {
          Icon(Icons.Default.LiveTv, contentDescription = null, tint = TvTextMuted, modifier = Modifier.size(16.dp))
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Series Info with ample horizontal room
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = series.name,
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Season/Episode pill
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(NeonBlue.copy(alpha = 0.25f))
              .padding(horizontal = 5.dp, vertical = 1.dp)
          ) {
            Text(
              text = "T${series.currentSeason} E${series.currentEpisode}",
              color = NeonBlue,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Next air date or status (won't get cut off now)
          Text(
            text = series.nextAirDate ?: series.status,
            color = NeonAmber,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

@Composable
fun TvSeriesCalendarDialog(
  seriesList: List<TvSeries>,
  onDismiss: () -> Unit,
  onSelectSeries: (TvSeries) -> Unit
) {
  val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
  var selectedDay by remember { mutableStateOf("Domingo") }

  val seriesOnSelectedDay = seriesList.filter { series ->
    series.airDays.any { it.equals(selectedDay, ignoreCase = true) }
  }

  TvModalOverlay(onDismiss = onDismiss) {
    Surface(
      modifier = Modifier
        .width(580.dp)
        .height(340.dp),
      shape = RoundedCornerShape(18.dp),
      color = TvSurfaceElevated,
      border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(14.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Calendario Semanal de Emisión",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TvTextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days of Week Tabs (Lunes a Domingo)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(2.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          daysOfWeek.forEach { day ->
            val isSelected = day == selectedDay
            val count = seriesList.count { s -> s.airDays.any { it.equals(day, ignoreCase = true) } }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) NeonCyan.copy(alpha = 0.25f) else Color.Transparent)
                .clickable { selectedDay = day }
                .padding(vertical = 5.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = day.take(3),
                  color = if (isSelected) NeonCyan else TvTextSecondary,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                if (count > 0) {
                  Text(
                    text = "$count",
                    color = if (isSelected) Color.White else NeonAmber,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Series List for Selected Day
        if (seriesOnSelectedDay.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No tienes series programadas para los días $selectedDay.",
              color = TvTextSecondary,
              fontSize = 12.sp
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            items(seriesOnSelectedDay) { series ->
              TvCard(
                onClick = { onSelectSeries(series) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(56.dp),
                focusedBorderColor = NeonCyan,
                shape = RoundedCornerShape(10.dp)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Poster Thumbnail
                  Box(
                    modifier = Modifier
                      .size(34.dp, 44.dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                  ) {
                    if (!series.posterUrl.isNullOrBlank()) {
                      AsyncImage(
                        model = series.posterUrl,
                        contentDescription = series.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                      )
                    }
                  }

                  Spacer(modifier = Modifier.width(10.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = series.name,
                      color = Color.White,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = "${series.network} • ${series.airTime} • ${series.status}",
                      color = TvTextSecondary,
                      fontSize = 10.sp
                    )
                  }

                  Column(horizontalAlignment = Alignment.End) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonBlue.copy(alpha = 0.25f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = "T${series.currentSeason} E${series.currentEpisode}",
                        color = NeonBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                    Text(
                      text = series.nextAirDate ?: "En emisión",
                      color = NeonAmber,
                      fontSize = 9.sp
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun AddSeriesDialog(
  onDismiss: () -> Unit,
  tvMazeRepository: TvMazeRepository,
  onSeriesSelected: (TvSeries) -> Unit
) {
  var query by remember { mutableStateOf("") }
  var isSearching by remember { mutableStateOf(false) }
  var searchResults by remember { mutableStateOf<List<TvMazeSearchResult>>(emptyList()) }
  val coroutineScope = rememberCoroutineScope()

  val popularPicks = listOf("Chicago Fire", "Stranger Things", "The Last of Us", "Severance", "The Bear", "Fallout")

  // Anchored at top center so TV keyboard below never overlaps
  TvModalOverlay(onDismiss = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = 16.dp),
      contentAlignment = Alignment.TopCenter
    ) {
      Surface(
        modifier = Modifier
          .width(560.dp)
          .heightIn(max = 340.dp),
        shape = RoundedCornerShape(18.dp),
        color = TvSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonBlue)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
        ) {
          // Title & Close
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Buscar Serie en TVMaze",
                color = TvTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Escribe el título o elige una serie recomendada",
                color = TvTextSecondary,
                fontSize = 10.sp
              )
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TvTextSecondary)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Search Input
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = query,
              onValueChange = { query = it },
              placeholder = { Text("Ej. Chicago Fire, The Last of Us...") },
              modifier = Modifier.weight(1f),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonBlue,
                unfocusedBorderColor = TvBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
              onClick = {
                val clean = query.trim()
                if (clean.isNotBlank()) {
                  isSearching = true
                  coroutineScope.launch {
                    searchResults = tvMazeRepository.searchShows(clean)
                    isSearching = false
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
              modifier = Modifier.height(44.dp)
            ) {
              Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Buscar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Popular picks row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            popularPicks.take(4).forEach { pick ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color.White.copy(alpha = 0.1f))
                  .clickable {
                    query = pick
                    isSearching = true
                    coroutineScope.launch {
                      searchResults = tvMazeRepository.searchShows(pick)
                      isSearching = false
                    }
                  }
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(text = pick, color = NeonBlue, fontSize = 9.sp, fontWeight = FontWeight.Medium)
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          if (isSearching) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
              CircularProgressIndicator(color = NeonBlue, modifier = Modifier.size(28.dp))
            }
          } else if (searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
              Text(
                text = if (query.isEmpty()) "Escribe un nombre o presiona una sugerencia arriba" else "No se encontraron resultados para '$query'",
                color = TvTextSecondary,
                fontSize = 11.sp
              )
            }
          } else {
            LazyRow(
              modifier = Modifier.fillMaxWidth().weight(1f),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(searchResults) { result ->
                TvCard(
                  onClick = {
                    coroutineScope.launch {
                      val fullSeries = tvMazeRepository.fetchShowDetails(result.showId)
                        ?: TvSeries(
                          id = result.showId,
                          name = result.name,
                          posterUrl = result.posterUrl,
                          status = result.status ?: "En emisión",
                          network = result.network ?: "TV",
                          rating = result.rating ?: 8.0,
                          summary = result.summary ?: "",
                          airDays = result.airDays,
                          airTime = result.airTime
                        )
                      onSeriesSelected(fullSeries)
                    }
                  },
                  modifier = Modifier
                    .width(135.dp)
                    .fillMaxHeight(),
                  focusedBorderColor = NeonBlue,
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp)
                    ) {
                      if (!result.posterUrl.isNullOrBlank()) {
                        AsyncImage(
                          model = result.posterUrl,
                          contentDescription = result.name,
                          modifier = Modifier.fillMaxSize(),
                          contentScale = ContentScale.Crop
                        )
                      }
                    }

                    Column(modifier = Modifier.padding(6.dp)) {
                      Text(
                        text = result.name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = "${result.network ?: "TV"} • ${result.airDays.firstOrNull() ?: "Semanal"}",
                        color = TvTextSecondary,
                        fontSize = 9.sp,
                        maxLines = 1
                      )
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = "+ Agregar",
                        color = NeonBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SeriesDetailDialog(
  series: TvSeries,
  onDismiss: () -> Unit,
  onUpdate: (TvSeries) -> Unit,
  onDelete: () -> Unit
) {
  TvModalOverlay(onDismiss = onDismiss) {
    Surface(
      modifier = Modifier
        .width(580.dp)
        .height(320.dp),
      shape = RoundedCornerShape(18.dp),
      color = TvSurfaceElevated,
      border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonPurple)
    ) {
      Row(modifier = Modifier.fillMaxSize()) {
        // Poster on left
        Box(
          modifier = Modifier
            .width(135.dp)
            .fillMaxHeight()
        ) {
          if (!series.posterUrl.isNullOrBlank()) {
            AsyncImage(
              model = series.posterUrl,
              contentDescription = series.name,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          } else {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E293B)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.LiveTv, contentDescription = null, tint = TvTextMuted, modifier = Modifier.size(36.dp))
            }
          }
        }

        // Details and actions on right
        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .padding(16.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            // Header with title and close button
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = series.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
              )

              IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TvTextSecondary)
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Network, Status, Rating
            Text(
              text = "${series.network} • Estado: ${series.status} • ★ ${series.rating}",
              color = NeonAmber,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Clean Season & Episode Information Pill
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(NeonBlue.copy(alpha = 0.25f))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "Temporada ${series.currentSeason} • Episodio ${series.currentEpisode}",
                  color = NeonBlue,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              Text(
                text = "Días: ${series.airDays.joinToString().ifBlank { "Por confirmar" }} (${series.airTime})",
                color = TvTextSecondary,
                fontSize = 11.sp
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Next Air Date / Status
            Text(
              text = "Próxima emisión: ${series.nextAirDate ?: "Por confirmar"}",
              color = NeonCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Synopsis
            Text(
              text = series.summary.ifBlank { "Sin sinopsis disponible." },
              color = TvTextSecondary,
              fontSize = 10.sp,
              maxLines = 3,
              overflow = TextOverflow.Ellipsis
            )
          }

          // Bottom Action Row: Big Red Delete Button & Dismiss
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Highly Visible Red Delete Button
            Button(
              onClick = onDelete,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
              modifier = Modifier.height(36.dp),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Eliminar Serie de mi lista", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = onDismiss,
              colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
              modifier = Modifier.height(36.dp),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Cerrar", color = Color.White, fontSize = 11.sp)
            }
          }
        }
      }
    }
  }
}

