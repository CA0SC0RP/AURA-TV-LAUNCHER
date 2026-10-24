package com.example.ui

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.AppDockWidget
import com.example.ui.components.GameNewsWidget
import com.example.ui.components.MoonPhaseWidget
import com.example.ui.components.SportsWidget
import com.example.ui.components.TvCard
import com.example.ui.components.TvShowsWidget
import com.example.ui.components.WeatherWidget
import com.example.ui.components.WeatherSetupWidget
import com.example.ui.components.WallpaperPickerModal
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TvLauncherScreen(
  viewModel: TvLauncherViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  var showWallpaperPicker by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize()) {
    // 1. 4K Wallpaper Background
    WallpaperBackground(
      wallpaperId = uiState.currentWallpaperId,
      customUrl = uiState.customWallpaperUrl
    )

    // El fondo espacial permanece visible a pantalla completa; el contraste lo aportan los propios widgets.

    if (uiState.isLoading) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = NeonCyan)
      }
    } else {
      // Fullscreen 4K TV Layout
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        // TOP SECTION: Header with Logo, Clock & Date, Actions + Compact TV Shows Strip (en chiquito)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(vertical = 4.dp)
        ) {
          // Line 1: Status Bar (clock/date omitted; weather widget already provides them)
          TopTvStatusBar(
            onOpenSettings = {
              try {
                val intent = Intent(Settings.ACTION_SETTINGS)
                context.startActivity(intent)
              } catch (e: Exception) {
                Toast.makeText(context, "Ajustes de TV", Toast.LENGTH_SHORT).show()
              }
            },
            onRefresh = { viewModel.loadAllData() },
            onOpenWallpaperPicker = { showWallpaperPicker = true }
          )

          // Line 2: Compact TV series horizontal strip linked with TVMaze
          TvShowsWidget(
            seriesList = uiState.seriesList,
            onAddSeries = { viewModel.addSeries(it) },
            onUpdateSeries = { viewModel.updateSeries(it) },
            onRemoveSeries = { viewModel.removeSeries(it) },
            tvMazeRepository = viewModel.tvMazeRepository,
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // MAIN BODY: Left Widgets (Sports & NBA + Games), Center (Open Wallpaper + App Dock), Right Widgets (Weather + Moon)
        BoxWithConstraints(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
          val isWideScreen = maxWidth >= 680.dp

          if (isWideScreen) {
            // Standard 16:9 4K TV Landscape Layout - more compact side widgets as requested
            val sideColumnWidth = if (maxWidth >= 1000.dp) 185.dp else 170.dp

            Row(
              modifier = Modifier.fillMaxSize(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              // 1. LADO IZQUIERDO: Deportes & NBA (arriba) y Videojuegos (abajo)
              Column(
                modifier = Modifier
                  .width(sideColumnWidth)
                  .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                // Widget 1: Deportes / NBA
                SportsWidget(
                  sportsEvents = uiState.allSportsEvents,
                  selectedFilter = uiState.selectedSportFilter,
                  onFilterChange = { viewModel.setSportFilter(it) },
                  favoriteTeams = uiState.favoriteSportsTeams,
                  onToggleFavoriteTeam = { viewModel.toggleFavoriteSportsTeam(it) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.85f)
                )

                // Widget 2: Videojuegos (Ticker informativo pasando poco a poco)
                GameNewsWidget(
                  newsList = uiState.gameNews,
                  modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.15f)
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              // 2. EN MEDIO: Espacio amplio y despejado para admirar el Wallpaper 4K + Barra de Aplicaciones en el fondo
              Column(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight()
                  .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                // Espacio 100% despejado y no-focusable para ver el fondo sin que el control remoto lo abra por accidente al presionar arriba
                Spacer(modifier = Modifier.weight(1f))

                // Bottom of Center: App Dock Widget (en medio de la pantalla abajo)
                AppDockWidget(
                  pinnedApps = uiState.pinnedApps,
                  allInstalledApps = uiState.allApps,
                  onLaunchApp = { pkg ->
                    val launched = viewModel.appManagerRepository.launchApp(pkg)
                    if (!launched) {
                      Toast.makeText(context, "Iniciando $pkg...", Toast.LENGTH_SHORT).show()
                    }
                  },
                  onTogglePinApp = { pkg, shouldPin ->
                    viewModel.togglePinApp(pkg, shouldPin)
                  },
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(vertical = 4.dp)
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              // 3. LADO DERECHO: Clima / Temperatura (arriba) y Fase Lunar (abajo)
              Column(
                modifier = Modifier
                  .width(sideColumnWidth)
                  .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                // Widget 1: Clima & Temperatura (día a día / por hora, buscador de país y código postal)
                if (uiState.weatherData != null) {
                  WeatherWidget(
                    weather = uiState.weatherData!!,
                    onLocationChange = { viewModel.updateWeatherLocation(it) },
                    onToggleUnit = { viewModel.toggleWeatherUnit() },
                    savedLocation = uiState.weatherLocation,
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(210.dp)
                  )
                } else {
                  WeatherSetupWidget(
                    onLocationChange = { viewModel.updateWeatherLocation(it) },
                    savedLocation = uiState.weatherLocation,
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(210.dp)
                  )
                }

                // Widget 2: Fase Lunar (compacta, debajo del clima)
                MoonPhaseWidget(
                  moonInfo = uiState.moonPhaseInfo,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                )
              }
            }
          } else {
            // Adaptive layout for narrow / portrait window preview: Scrollable so user can see all 4 widgets and dock
            Column(
              modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // App Dock in center
              AppDockWidget(
                pinnedApps = uiState.pinnedApps,
                allInstalledApps = uiState.allApps,
                onLaunchApp = { pkg -> viewModel.appManagerRepository.launchApp(pkg) },
                onTogglePinApp = { pkg, shouldPin -> viewModel.togglePinApp(pkg, shouldPin) },
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.Black.copy(alpha = 0.35f))
                  .padding(vertical = 4.dp)
              )

              // Left Widgets: Sports & Video Games
              Text(text = "Lado Izquierdo (Deportes & Juegos)", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              SportsWidget(
                sportsEvents = uiState.allSportsEvents,
                selectedFilter = uiState.selectedSportFilter,
                onFilterChange = { viewModel.setSportFilter(it) },
                favoriteTeams = uiState.favoriteSportsTeams,
                onToggleFavoriteTeam = { viewModel.toggleFavoriteSportsTeam(it) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(180.dp)
              )
              GameNewsWidget(
                newsList = uiState.gameNews,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(250.dp)
              )

              // Right Widgets: Weather & Moon
              Text(text = "Lado Derecho (Clima & Fase Lunar)", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              if (uiState.weatherData != null) {
                WeatherWidget(
                  weather = uiState.weatherData!!,
                  onLocationChange = { viewModel.updateWeatherLocation(it) },
                  onToggleUnit = { viewModel.toggleWeatherUnit() },
                  savedLocation = uiState.weatherLocation,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                )
              } else {
                WeatherSetupWidget(
                  onLocationChange = { viewModel.updateWeatherLocation(it) },
                  savedLocation = uiState.weatherLocation,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                )
              }
              MoonPhaseWidget(
                moonInfo = uiState.moonPhaseInfo,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(160.dp)
              )
            }
          }
        }
      }
    }

  }

    if (showWallpaperPicker) {
      WallpaperPickerModal(
        currentWallpaperId = uiState.currentWallpaperId,
        onDismiss = { showWallpaperPicker = false },
        onSelectWallpaper = { wallpaper ->
          viewModel.setWallpaper(wallpaper.id)
          showWallpaperPicker = false
        },
        onSetCustomUrl = { url ->
          viewModel.setCustomWallpaper(url)
          showWallpaperPicker = false
        }
      )
    }
}

@Composable
fun TopTvStatusBar(
  onOpenSettings: () -> Unit,
  onRefresh: () -> Unit,
  onOpenWallpaperPicker: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left: Launcher branding
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(Brush.linearGradient(listOf(NeonCyan, NeonBlue))),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.Tv, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
      }
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "AURA TV",
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.width(6.dp))
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(Color.White.copy(alpha = 0.15f))
          .padding(horizontal = 5.dp, vertical = 1.dp)
      ) {
        Text(text = "4K HDR", color = NeonCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
      }
    }

    // Right: Action buttons
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      TvCard(
        onClick = onRefresh,
        shape = CircleShape,
        backgroundColor = Color.White.copy(alpha = 0.1f),
        focusedBorderColor = NeonCyan,
        focusedScale = 1.15f,
        elevation = 2.dp
      ) {
        Box(modifier = Modifier.padding(6.dp)) {
          Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White, modifier = Modifier.size(14.dp))
        }
      }

      TvCard(
        onClick = onOpenWallpaperPicker,
        shape = CircleShape,
        backgroundColor = Color.White.copy(alpha = 0.1f),
        focusedBorderColor = NeonCyan,
        focusedScale = 1.15f,
        elevation = 2.dp
      ) {
        Box(modifier = Modifier.padding(6.dp)) {
          Icon(Icons.Default.Wallpaper, contentDescription = "Fondo de pantalla", tint = Color.White, modifier = Modifier.size(14.dp))
        }
      }

      TvCard(
        onClick = onOpenSettings,
        shape = CircleShape,
        backgroundColor = Color.White.copy(alpha = 0.1f),
        focusedBorderColor = NeonCyan,
        focusedScale = 1.15f,
        elevation = 2.dp
      ) {
        Box(modifier = Modifier.padding(6.dp)) {
          Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = Color.White, modifier = Modifier.size(14.dp))
        }
      }
    }
  }
}

@Composable
fun WallpaperBackground(
  wallpaperId: String,
  customUrl: String?
) {
  Box(modifier = Modifier.fillMaxSize()) {
    if (!customUrl.isNullOrBlank()) {
      AsyncImage(
        model = customUrl,
        contentDescription = "Fondo espacial automático",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )
    } else if (wallpaperId == "cosmic_nebula" || wallpaperId == "space_auto") {
      Image(
        painter = painterResource(id = R.drawable.wallpaper_cosmic_4k_1792767721304),
        contentDescription = "Nebulosa Cósmica 4K",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )
    } else {
      val gradientColors = when (wallpaperId) {
        "aurora_borealis" -> listOf(Color(0xFF022C22), Color(0xFF064E3B), Color(0xFF082F49), Color(0xFF020617))
        "cyberpunk_neon" -> listOf(Color(0xFF3B0764), Color(0xFF581C87), Color(0xFF1E1B4B), Color(0xFF090D16))
        "deep_abyss" -> listOf(Color(0xFF082F49), Color(0xFF0C4A6E), Color(0xFF031D38), Color(0xFF020617))
        "sunset_minimal" -> listOf(Color(0xFF7C2D12), Color(0xFF431407), Color(0xFF180A0A), Color(0xFF050505))
        "pure_oled_black" -> listOf(Color(0xFF000000), Color(0xFF050505), Color(0xFF000000))
        else -> listOf(Color(0xFF090D16), Color(0xFF0F172A), Color(0xFF020617))
      }
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Brush.radialGradient(gradientColors, radius = 1600f))
      )
    }
  }
}
