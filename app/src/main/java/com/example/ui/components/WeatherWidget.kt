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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WeatherData
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TvBorder
import com.example.ui.theme.TvBorderFocused
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun WeatherWidget(
  weather: WeatherData,
  onLocationChange: (String) -> Unit,
  onToggleUnit: () -> Unit,
  savedLocation: String = "",
  modifier: Modifier = Modifier
) {
  var isHourlyView by remember { mutableStateOf(false) } // false = Día a día, true = Por hora
  var showLocationDialog by remember { mutableStateOf(false) }
  var showExpandedDialog by remember { mutableStateOf(false) }

  TvCard(
    onClick = { showExpandedDialog = true },
    modifier = modifier,
    backgroundColor = TvSurface.copy(alpha = 0.82f),
    focusedBorderColor = NeonBlue,
    focusedScale = 1.04f
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)
    ) {
      // Header: Location & search button
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
            imageVector = Icons.Default.EditLocationAlt,
            contentDescription = "Cambiar ubicación",
            tint = NeonBlue,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "${weather.cityName}${if (weather.country.isNotBlank()) ", " + weather.country else ""}",
            color = TvTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Unit toggle pill (°C / °F)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(NeonBlue.copy(alpha = 0.2f))
              .clickable { onToggleUnit() }
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = weather.tempUnit,
              color = NeonBlue,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          Icon(
            imageVector = Icons.Default.OpenInFull,
            contentDescription = "Ampliar",
            tint = TvTextSecondary,
            modifier = Modifier.size(13.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Current Weather Highlight
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = "${weather.currentTemp.toInt()}",
              color = Color.White,
              fontSize = 32.sp,
              fontWeight = FontWeight.ExtraBold
            )
            Text(
              text = weather.tempUnit,
              color = NeonBlue,
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(bottom = 4.dp)
            )
          }
          Text(
            text = weather.conditionDescription,
            color = NeonAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        // Weather Stats (Humidity & Wind)
        Column(horizontalAlignment = Alignment.End) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = "${weather.humidity}%", color = TvTextSecondary, fontSize = 10.sp)
          }
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Air, contentDescription = null, tint = TvTextSecondary, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = "${weather.windSpeedKmH} km/h", color = TvTextSecondary, fontSize = 10.sp)
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "H:${weather.highTemp.toInt()}° L:${weather.lowTemp.toInt()}°",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 10.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Toggle Tabs: Por Hora vs Día a Día
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color.Black.copy(alpha = 0.35f))
          .padding(2.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(if (!isHourlyView) NeonBlue.copy(alpha = 0.25f) else Color.Transparent)
            .clickable { isHourlyView = false }
            .padding(vertical = 3.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "7 Días",
            color = if (!isHourlyView) NeonBlue else TvTextSecondary,
            fontSize = 10.sp,
            fontWeight = if (!isHourlyView) FontWeight.Bold else FontWeight.Normal
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isHourlyView) NeonBlue.copy(alpha = 0.25f) else Color.Transparent)
            .clickable { isHourlyView = true }
            .padding(vertical = 3.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Por hora",
            color = if (isHourlyView) NeonBlue else TvTextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isHourlyView) FontWeight.Bold else FontWeight.Normal
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Forecast list (Hourly or Daily)
      if (isHourlyView) {
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(weather.hourlyForecast) { item ->
            Column(
              modifier = Modifier
                .width(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (item.isNow) NeonBlue.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.25f))
                .padding(vertical = 4.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = item.timeLabel,
                color = if (item.isNow) NeonBlue else TvTextSecondary,
                fontSize = 9.sp,
                fontWeight = if (item.isNow) FontWeight.Bold else FontWeight.Normal
              )
              Spacer(modifier = Modifier.height(2.dp))
              Icon(
                imageVector = if (item.weatherCode <= 1) Icons.Default.WbSunny else Icons.Default.WbCloudy,
                contentDescription = null,
                tint = if (item.weatherCode <= 1) NeonAmber else NeonBlue,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${item.temp.toInt()}°",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          items(weather.dailyForecast.take(4)) { item ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = item.dayLabel,
                color = TvTextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(32.dp)
              )

              Icon(
                imageVector = if (item.weatherCode <= 1) Icons.Default.WbSunny else Icons.Default.WbCloudy,
                contentDescription = null,
                tint = if (item.weatherCode <= 1) NeonAmber else NeonBlue,
                modifier = Modifier.size(13.dp)
              )

              Text(
                text = item.condition + (item.precipitationProbabilityPercent?.let { " · lluvia $it%" } ?: ""),
                color = TvTextSecondary,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                  .weight(1f)
                  .padding(horizontal = 4.dp)
              )

              Text(
                text = "${item.maxTemp.toInt()}° / ${item.minTemp.toInt()}°",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }

  // Expanded Weather Window
  if (showExpandedDialog) {
    BackHandler(enabled = true) { showExpandedDialog = false }
    ExpandedWeatherDialog(
      weather = weather,
      onDismiss = { showExpandedDialog = false },
      onOpenLocationSearch = {
        showExpandedDialog = false
        showLocationDialog = true
      },
      onToggleUnit = onToggleUnit
    )
  }

  // Location Search Dialog
  if (showLocationDialog) {
    LocationInputDialog(
      currentLocation = savedLocation,
      onDismiss = { showLocationDialog = false },
      onConfirm = { newLoc ->
        onLocationChange(newLoc)
        showLocationDialog = false
      }
    )
  }
}

@Composable
fun ExpandedWeatherDialog(
  weather: WeatherData,
  onDismiss: () -> Unit,
  onOpenLocationSearch: () -> Unit,
  onToggleUnit: () -> Unit
) {
  TvModalOverlay(onDismiss = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.96f)
        .height(420.dp),
      shape = RoundedCornerShape(18.dp),
      color = TvSurfaceElevated,
      border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonBlue)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.WbSunny, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Pronóstico de 7 días",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${weather.cityName}, ${weather.country} · Datos meteorológicos",
                color = TvTextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
              onClick = onOpenLocationSearch,
              colors = ButtonDefaults.buttonColors(containerColor = NeonBlue.copy(alpha = 0.2f)),
              modifier = Modifier.height(32.dp),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.EditLocationAlt, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Ubicación", color = NeonBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Button(
              onClick = onToggleUnit,
              colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
              modifier = Modifier.height(32.dp),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(weather.tempUnit, color = Color.White, fontSize = 11.sp)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Cerrar pronóstico", tint = TvTextSecondary)
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth().weight(1f),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Column(
            modifier = Modifier
              .width(210.dp)
              .fillMaxSize()
              .clip(RoundedCornerShape(12.dp))
              .background(Color.Black.copy(alpha = 0.30f))
              .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Row(verticalAlignment = Alignment.Bottom) {
                Text(
                  text = "${weather.currentTemp.toInt()}",
                  color = Color.White,
                  fontSize = 42.sp,
                  fontWeight = FontWeight.ExtraBold
                )
                Text(
                  text = weather.tempUnit,
                  color = NeonBlue,
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(bottom = 6.dp)
                )
              }
              Text(
                text = weather.conditionDescription,
                color = NeonAmber,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
            }

            Icon(
              imageVector = when {
                weather.weatherCode in 51..67 || weather.weatherCode in 80..82 || weather.weatherCode >= 95 -> Icons.Default.WaterDrop
                weather.weatherCode <= 1 -> Icons.Default.WbSunny
                else -> Icons.Default.WbCloudy
              },
              contentDescription = weather.conditionDescription,
              tint = when {
                weather.weatherCode in 51..67 || weather.weatherCode in 80..82 || weather.weatherCode >= 95 -> NeonCyan
                weather.weatherCode <= 1 -> NeonAmber
                else -> NeonBlue
              },
              modifier = Modifier.size(54.dp).align(Alignment.CenterHorizontally)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text("Humedad", color = TvTextSecondary, fontSize = 10.sp)
                  Text("${weather.humidity}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                  Text("Viento", color = TvTextSecondary, fontSize = 10.sp)
                  Text("${weather.windSpeedKmH} km/h", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text("Índice UV", color = TvTextSecondary, fontSize = 10.sp)
                  Text("${weather.uvIndex}", color = NeonAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                  Text("Máx. / mín.", color = TvTextSecondary, fontSize = 10.sp)
                  Text("${weather.highTemp.toInt()}° / ${weather.lowTemp.toInt()}°", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          Column(
            modifier = Modifier.weight(1f).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "Esta semana",
              color = NeonCyan,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
            LazyRow(
              modifier = Modifier.fillMaxWidth().weight(1f),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(weather.dailyForecast.take(7)) { item ->
                val isToday = item.dayLabel.equals("Hoy", ignoreCase = true)
                Column(
                  modifier = Modifier
                    .width(88.dp)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isToday) NeonBlue.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.25f))
                    .padding(horizontal = 6.dp, vertical = 10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                  Text(
                    text = item.dayLabel,
                    color = if (isToday) NeonCyan else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = item.dateLabel,
                    color = TvTextSecondary,
                    fontSize = 9.sp,
                    maxLines = 1
                  )
                  Icon(
                    imageVector = when {
                      item.weatherCode in 51..67 || item.weatherCode in 80..82 || item.weatherCode >= 95 -> Icons.Default.WaterDrop
                      item.weatherCode <= 1 -> Icons.Default.WbSunny
                      else -> Icons.Default.WbCloudy
                    },
                    contentDescription = item.condition,
                    tint = when {
                      item.weatherCode in 51..67 || item.weatherCode in 80..82 || item.weatherCode >= 95 -> NeonCyan
                      item.weatherCode <= 1 -> NeonAmber
                      else -> NeonBlue
                    },
                    modifier = Modifier.size(28.dp)
                  )
                  Text(
                    text = item.condition,
                    color = TvTextSecondary,
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                  )
                  Spacer(modifier = Modifier.weight(1f))
                  Text(
                    text = "${item.maxTemp.toInt()}°",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "${item.minTemp.toInt()}°",
                    color = TvTextSecondary,
                    fontSize = 13.sp
                  )
                  item.precipitationProbabilityPercent?.let { probability ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.WaterDrop, contentDescription = "Probabilidad de lluvia", tint = NeonCyan, modifier = Modifier.size(11.dp))
                      Spacer(modifier = Modifier.width(2.dp))
                      Text("${probability}%", color = NeonCyan, fontSize = 10.sp)
                    }
                  }
                }
              }
            }

            Text(
              text = "Pronóstico por hora",
              color = NeonCyan,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
            LazyRow(
              modifier = Modifier.fillMaxWidth().height(76.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              items(weather.hourlyForecast.take(12)) { item ->
                Column(
                  modifier = Modifier
                    .width(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (item.isNow) NeonBlue.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.25f))
                    .padding(vertical = 5.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = item.timeLabel,
                    color = if (item.isNow) NeonCyan else TvTextSecondary,
                    fontSize = 9.sp,
                    fontWeight = if (item.isNow) FontWeight.Bold else FontWeight.Normal
                  )
                  Icon(
                    imageVector = when {
                      item.weatherCode in 51..67 || item.weatherCode in 80..82 || item.weatherCode >= 95 -> Icons.Default.WaterDrop
                      item.weatherCode <= 1 -> Icons.Default.WbSunny
                      else -> Icons.Default.WbCloudy
                    },
                    contentDescription = null,
                    tint = when {
                      item.weatherCode in 51..67 || item.weatherCode in 80..82 || item.weatherCode >= 95 -> NeonCyan
                      item.weatherCode <= 1 -> NeonAmber
                      else -> NeonBlue
                    },
                    modifier = Modifier.size(16.dp)
                  )
                  Text("${item.temp.toInt()}°", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
fun LocationInputDialog(
  currentLocation: String,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  val savedParts = remember(currentLocation) { currentLocation.split("|", limit = 2) }
  var postalCode by remember(currentLocation) { mutableStateOf(savedParts.getOrNull(0)?.takeIf { savedParts.size == 2 } ?: "") }
  var country by remember(currentLocation) { mutableStateOf(savedParts.getOrNull(1)?.takeIf { savedParts.size == 2 } ?: "") }

  TvModalOverlay(onDismiss = onDismiss) {
    Surface(
      modifier = Modifier
        .width(480.dp)
        .clip(RoundedCornerShape(16.dp)),
      shape = RoundedCornerShape(16.dp),
      color = TvSurfaceElevated,
      border = androidx.compose.foundation.BorderStroke(1.5.dp, TvBorderFocused)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Configurar Ubicación de Clima",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TvTextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Introduce el país y el código postal para consultar el pronóstico de esa zona.",
          color = TvTextSecondary,
          fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = country,
          onValueChange = { country = it },
          placeholder = { Text("País (ej. México o MX)", fontSize = 11.sp) },
          modifier = Modifier.fillMaxWidth().height(52.dp),
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonBlue,
            unfocusedBorderColor = TvBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = postalCode,
          onValueChange = { postalCode = it },
          placeholder = { Text("Código postal", fontSize = 11.sp) },
          modifier = Modifier.fillMaxWidth().height(52.dp),
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonBlue,
            unfocusedBorderColor = TvBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          Button(
            onClick = {
              if (country.isNotBlank() && postalCode.isNotBlank()) {
                onConfirm("${postalCode.trim()}|${country.trim()}")
              }
            },
            enabled = country.isNotBlank() && postalCode.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
            modifier = Modifier.height(34.dp),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Actualizar Clima", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun WeatherSetupWidget(
  onLocationChange: (String) -> Unit,
  savedLocation: String = "",
  modifier: Modifier = Modifier
) {
  var showLocationDialog by remember { mutableStateOf(false) }

  TvCard(
    onClick = { showLocationDialog = true },
    modifier = modifier,
    backgroundColor = TvSurface.copy(alpha = 0.82f),
    focusedBorderColor = NeonBlue,
    focusedScale = 1.03f
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = Icons.Default.EditLocationAlt,
        contentDescription = "Configurar ubicación del clima",
        tint = NeonBlue,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = if (savedLocation.isBlank()) "Configurar clima" else "Clima no disponible",
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = if (savedLocation.isBlank()) "Introduce país y código postal" else "Revisar ubicación o reintentar",
        color = TvTextSecondary,
        fontSize = 10.sp
      )
    }
  }

  if (showLocationDialog) {
    LocationInputDialog(
      currentLocation = savedLocation,
      onDismiss = { showLocationDialog = false },
      onConfirm = { location ->
        onLocationChange(location)
        showLocationDialog = false
      }
    )
  }
}
