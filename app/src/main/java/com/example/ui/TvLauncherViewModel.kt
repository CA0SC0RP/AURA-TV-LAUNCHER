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
  val selectedSportFilter: String = "Todos",
  val gameNews: List<GameNewsItem> = emptyList(),
  val pinnedApps: List<AppItem> = emptyList(),
  val allApps: List<AppItem> = emptyList(),
  val currentWallpaperId: String = "cosmic_nebula",
  val customWallpaperUrl: String? = null,
  val isFahrenheit: Boolean = false,
  val weatherLocation: String = "",
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
      selectedSportFilter = prefs.getSportFilter()
    )
  )
  val uiState: StateFlow<TvLauncherUiState> = _uiState.asStateFlow()

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

      // 3. Sports & Games
      val sports = sportsRepo.getEventsBySport(prefs.getSportFilter())
      val games = gameNewsRepo.getGameNews()

      // 4. Moon
      val moon = MoonCalculator.calculate()

      // Weather remains empty until the user configures a location.
      val weather: WeatherData? = null

      _uiState.update {
        it.copy(
          seriesList = initialSeries,
          allApps = installed,
          pinnedApps = pinned,
          sportsEvents = sports,
          gameNews = games,
          moonPhaseInfo = moon,
          weatherData = weather,
          isLoading = false
        )
      }

      // Fetch live weather in background
      val savedLocation = prefs.getWeatherLocation().trim()
      if (savedLocation.isNotEmpty()) refreshWeather(savedLocation, prefs.isFahrenheit())
    }
  }

  fun refreshWeather(location: String, isFahrenheit: Boolean) {
    val cleanLocation = location.trim()
    if (cleanLocation.isEmpty()) {
      _uiState.update { it.copy(weatherData = null, weatherLocation = "") }
      return
    }
    viewModelScope.launch {
      val data = weatherRepo.fetchWeather(cleanLocation, isFahrenheit)
      _uiState.update { it.copy(weatherData = data, weatherLocation = cleanLocation) }
    }
  }

  fun updateWeatherLocation(newLocation: String) {
    prefs.setWeatherLocation(newLocation)
    _uiState.update { it.copy(weatherLocation = newLocation) }
    refreshWeather(newLocation, _uiState.value.isFahrenheit)
  }

  fun toggleWeatherUnit() {
    val newUnit = !_uiState.value.isFahrenheit
    prefs.setFahrenheit(newUnit)
    _uiState.update { it.copy(isFahrenheit = newUnit) }
    refreshWeather(_uiState.value.weatherLocation, newUnit)
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

  fun setSportFilter(filter: String) {
    prefs.setSportFilter(filter)
    val filteredEvents = sportsRepo.getEventsBySport(filter)
    _uiState.update { it.copy(selectedSportFilter = filter, sportsEvents = filteredEvents) }
  }

  fun addCustomSportEvent(
    sport: String,
    tournament: String,
    home: String,
    away: String,
    time: String,
    channel: String
  ) {
    sportsRepo.addCustomEvent(sport, tournament, home, away, time, channel)
    val filteredEvents = sportsRepo.getEventsBySport(_uiState.value.selectedSportFilter)
    _uiState.update { it.copy(sportsEvents = filteredEvents) }
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
