package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppItem
import com.example.data.model.GameNewsItem
import com.example.data.model.MoonPhaseInfo
import com.example.data.model.SportsEvent
import com.example.data.model.TvSeries
import com.example.data.model.WallpaperItem
import com.example.data.model.WeatherData
import com.example.data.repository.AppManagerRepository
import com.example.data.repository.GameNewsRepository
import com.example.data.repository.LauncherPreferences
import com.example.data.repository.MoonCalculator
import com.example.data.repository.SportsRepository
import com.example.data.repository.TvMazeRepository
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TvLauncherUiState(
  val seriesList: List<TvSeries> = emptyList(),
  val weatherData: WeatherData? = null,
  val moonPhaseInfo: MoonPhaseInfo = MoonCalculator.calculate(),
  val sportsEvents: List<SportsEvent> = emptyList(),
  val selectedSportFilter: String = "NBA",
  val nbaTeamAbbr: String = "",
  val sportsLoading: Boolean = true,
  val sportsError: String? = null,
  val gameNews: List<GameNewsItem> = emptyList(),
  val pinnedApps: List<AppItem> = emptyList(),
  val allApps: List<AppItem> = emptyList(),
  val currentWallpaperId: String = "cosmic_nebula",
  val customWallpaperUrl: String? = null,
  val isFahrenheit: Boolean = false,
  val weatherLocation: String = "Ciudad de México",
  val isLoading: Boolean = true
)

class TvLauncherViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = LauncherPreferences(application)
  private val tvMazeRepo = TvMazeRepository()
  private val weatherRepo = WeatherRepository()
  private val sportsRepo = SportsRepository()
  private val gameNewsRepo = GameNewsRepository()
  private val appManagerRepo = AppManagerRepository(application)

  private val _uiState = MutableStateFlow(
    TvLauncherUiState(
      currentWallpaperId = prefs.getWallpaperId(),
      customWallpaperUrl = prefs.getCustomWallpaperUrl(),
      isFahrenheit = prefs.isFahrenheit(),
      weatherLocation = prefs.getWeatherLocation(),
      selectedSportFilter = validSportFilter(prefs.getSportFilter()),
      nbaTeamAbbr = prefs.getNbaTeam()
    )
  )
  val uiState: StateFlow<TvLauncherUiState> = _uiState.asStateFlow()

  // Si el filtro guardado ya no está entre los deportes destacados (por ejemplo "Todos"), usa el primero
  private fun validSportFilter(saved: String): String =
    SportsRepository.featuredSports.firstOrNull { it.equals(saved, ignoreCase = true) }
      ?: SportsRepository.featuredSports.first()

  val tvMazeRepository: TvMazeRepository get() = tvMazeRepo
  val appManagerRepository: AppManagerRepository get() = appManagerRepo

  init {
    loadAllData()
  }

  fun loadAllData() {
    viewModelScope.launch {
      // 1. Series
      val saved = prefs.getSavedSeries()
      val initialSeries = if (!saved.isNullOrEmpty()) saved else tvMazeRepo.getInitialSeries()
      if (saved.isNullOrEmpty()) {
        prefs.saveSeries(initialSeries)
      }

      // 2. Apps
      val pinnedSet = prefs.getPinnedApps()
      val installed = appManagerRepo.getInstalledApps(pinnedSet)
      val pinned = installed.filter { it.isPinned }

      // 3. Games (los deportes se cargan aparte, desde internet)
      val games = gameNewsRepo.getGameNews()

      // 4. Moon
      val moon = MoonCalculator.calculate()

      // 5. Initial Weather fallback
      val weather = weatherRepo.getFallback()

      _uiState.update {
        it.copy(
          seriesList = initialSeries,
          allApps = installed,
          pinnedApps = pinned,
          gameNews = games,
          moonPhaseInfo = moon,
          weatherData = weather,
          isLoading = false
        )
      }

      // Fetch live weather and sports in background
      refreshWeather()
      loadSports()
      refreshSeriesDates()
    }
  }

  fun refreshWeather() {
    viewModelScope.launch {
      val fahrenheit = _uiState.value.isFahrenheit

      // 1. Ubicación automática (según la conexión a internet de la TV)
      if (prefs.isAutoLocation()) {
        val automatic = weatherRepo.fetchWeatherAutomatic(fahrenheit)
        if (automatic != null) {
          applyWeather(automatic)
          return@launch
        }
      }

      // 2. Respaldo: la última ciudad elegida a mano (o Ciudad de México)
      val location = _uiState.value.weatherLocation
      val data = weatherRepo.fetchWeather(location, fahrenheit)
      applyWeather(data)
    }
  }

  // Guarda el clima y recalcula la luna para esa ubicación (la salida y puesta dependen del lugar)
  private fun applyWeather(data: WeatherData) {
    val moon = MoonCalculator.calculate(latitude = data.latitude, longitude = data.longitude)
    _uiState.update { it.copy(weatherData = data, moonPhaseInfo = moon) }
  }

  fun updateWeatherLocation(newLocation: String) {
    prefs.setWeatherLocation(newLocation)
    prefs.setAutoLocation(false)
    _uiState.update { it.copy(weatherLocation = newLocation) }
    refreshWeather()
  }

  fun useAutomaticLocation() {
    prefs.setAutoLocation(true)
    refreshWeather()
  }

  fun toggleWeatherUnit() {
    val newUnit = !_uiState.value.isFahrenheit
    prefs.setFahrenheit(newUnit)
    _uiState.update { it.copy(isFahrenheit = newUnit) }
    refreshWeather()
  }

  // Pone al día la fecha del próximo episodio de cada serie guardada (TVMaze, gratis y sin clave)
  fun refreshSeriesDates() {
    viewModelScope.launch {
      val labels = mutableMapOf<Long, String>()
      for (series in _uiState.value.seriesList) {
        tvMazeRepo.fetchNextAirDate(series.id)?.let { labels[series.id] = it }
      }
      if (labels.isEmpty()) return@launch

      // Se aplica sobre la lista más reciente por si el usuario agregó o quitó series mientras tanto
      val latest = _uiState.value.seriesList
      val merged = latest.map { current ->
        labels[current.id]?.let { current.copy(nextAirDate = it) } ?: current
      }
      if (merged != latest) {
        prefs.saveSeries(merged)
        _uiState.update { it.copy(seriesList = merged) }
      }
    }
  }

  fun addSeries(series: TvSeries) {
    val current = _uiState.value.seriesList.toMutableList()
    if (current.none { it.id == series.id }) {
      current.add(0, series)
      prefs.saveSeries(current)
      _uiState.update { it.copy(seriesList = current) }
    }
  }

  fun updateSeries(updated: TvSeries) {
    val current = _uiState.value.seriesList.map {
      if (it.id == updated.id) updated else it
    }
    prefs.saveSeries(current)
    _uiState.update { it.copy(seriesList = current) }
  }

  fun removeSeries(seriesId: Long) {
    val current = _uiState.value.seriesList.filter { it.id != seriesId }
    prefs.saveSeries(current)
    _uiState.update { it.copy(seriesList = current) }
  }

  private var sportsJob: Job? = null

  fun loadSports() {
    sportsJob?.cancel()
    val filter = _uiState.value.selectedSportFilter
    val team = _uiState.value.nbaTeamAbbr
    sportsJob = viewModelScope.launch {
      _uiState.update { it.copy(sportsLoading = true, sportsError = null, sportsEvents = emptyList()) }

      val result = if (filter.equals(SportsRepository.NBA, ignoreCase = true)) {
        if (team.isBlank()) Result.success(emptyList<SportsEvent>()) else sportsRepo.fetchNbaGames(team)
      } else {
        sportsRepo.fetchTennisMatches()
      }

      _uiState.update {
        it.copy(
          sportsLoading = false,
          sportsEvents = result.getOrDefault(emptyList()),
          sportsError = if (result.isFailure) "No se pudieron cargar los partidos.\nRevisa tu internet y presiona actualizar." else null
        )
      }
    }
  }

  fun setSportFilter(filter: String) {
    val valid = validSportFilter(filter)
    prefs.setSportFilter(valid)
    _uiState.update { it.copy(selectedSportFilter = valid) }
    loadSports()
  }

  fun setNbaTeam(abbr: String) {
    prefs.setNbaTeam(abbr)
    _uiState.update { it.copy(nbaTeamAbbr = abbr) }
    loadSports()
  }

  fun togglePinApp(packageName: String, shouldPin: Boolean) {
    val currentPinned = prefs.getPinnedApps().toMutableSet()
    if (shouldPin) {
      currentPinned.add(packageName)
    } else {
      currentPinned.remove(packageName)
    }
    prefs.setPinnedApps(currentPinned)

    val updatedAll = _uiState.value.allApps.map {
      if (it.packageName == packageName) it.copy(isPinned = shouldPin) else it
    }
    val updatedPinned = updatedAll.filter { it.isPinned }
    _uiState.update { it.copy(allApps = updatedAll, pinnedApps = updatedPinned) }
  }

  fun selectWallpaper(wallpaper: WallpaperItem) {
    prefs.setWallpaperId(wallpaper.id)
    prefs.setCustomWallpaperUrl(null)
    _uiState.update { it.copy(currentWallpaperId = wallpaper.id, customWallpaperUrl = null) }
  }

  fun setCustomWallpaperUrl(url: String) {
    prefs.setWallpaperId("custom_url")
    prefs.setCustomWallpaperUrl(url)
    _uiState.update { it.copy(currentWallpaperId = "custom_url", customWallpaperUrl = url) }
  }
}
