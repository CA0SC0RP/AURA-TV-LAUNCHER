package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppItem
import com.example.data.model.GameNewsItem
import com.example.data.model.MoonPhaseInfo
import com.example.data.model.SportsEvent
import com.example.data.model.TvSeries
import com.example.data.model.WeatherData
import com.example.data.repository.AppManagerRepository
import com.example.data.repository.GameNewsRepository
import com.example.data.repository.LauncherPreferences
import com.example.data.repository.MoonCalculator
import com.example.data.repository.SportsRepository
import com.example.data.repository.TvMazeRepository
import com.example.data.repository.WeatherRepository
import com.example.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

data class TvLauncherUiState(
  val seriesList: List<TvSeries> = emptyList(),
  val weatherData: WeatherData? = null,
  val moonPhaseInfo: MoonPhaseInfo = MoonCalculator.calculate(),
  val sportsEvents: List<SportsEvent> = emptyList(),
  val allSportsEvents: List<SportsEvent> = emptyList(),
  val favoriteSportsTeams: Set<String> = emptySet(),
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
  private val wallpaperRepo = WallpaperRepository(application)

  private val _uiState = MutableStateFlow(
    TvLauncherUiState(
      currentWallpaperId = "space_auto",
      customWallpaperUrl = wallpaperRepo.cachedWallpaperUri(),
      isFahrenheit = prefs.isFahrenheit(),
      weatherLocation = prefs.getWeatherLocation(),
      selectedSportFilter = prefs.getSportFilter(),
      favoriteSportsTeams = prefs.getFavoriteSportsTeams()
    )
  )
  val uiState: StateFlow<TvLauncherUiState> = _uiState.asStateFlow()

  val tvMazeRepository: TvMazeRepository get() = tvMazeRepo
  val appManagerRepository: AppManagerRepository get() = appManagerRepo

  init {
    loadAllData()
    viewModelScope.launch {
      while (true) {
        delay(TimeUnit.MINUTES.toMillis(30))
        refreshGameNews()
        refreshSports()
      }
    }
    viewModelScope.launch {
      while (true) {
        delay(WallpaperRepository.ROTATION_INTERVAL_MS)
        refreshSpaceWallpaper(force = true)
      }
    }
  }

  private fun refreshGameNews() {
    viewModelScope.launch {
      val articles = gameNewsRepo.getGameNews()
      _uiState.update { it.copy(gameNews = articles) }
    }
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
      val discoveredApps = appManagerRepo.getInstalledApps(pinnedSet)
      // Keep only favorites that are actually installed and normalize the dock to six items.
      val pinnedPackages = discoveredApps.filter { it.isPinned }.take(6).map { it.packageName }.toSet()
      prefs.setPinnedApps(pinnedPackages)
      val installed = discoveredApps.map { it.copy(isPinned = it.packageName in pinnedPackages) }
      val pinned = installed.filter { it.isPinned }

      // Sports calendars are fetched after the first screen state to avoid delaying launcher startup.

      // 4. Moon
      val moon = MoonCalculator.calculate()

      // Weather remains empty until the user configures a location.
      val weather: WeatherData? = null

      _uiState.update {
        it.copy(
          seriesList = initialSeries,
          allApps = installed,
          pinnedApps = pinned,
          sportsEvents = emptyList(),
          allSportsEvents = emptyList(),
          gameNews = emptyList(),
          moonPhaseInfo = moon,
          weatherData = weather,
          isLoading = false
        )
      }

      // Fetch RSS, sports, and the automatic space wallpaper in the background.
      refreshGameNews()
      refreshSports()
      refreshSpaceWallpaper(force = false)

      // Fetch live weather in background
      val savedLocation = prefs.getWeatherLocation().trim()
      if (savedLocation.isNotEmpty()) refreshWeather(savedLocation, prefs.isFahrenheit())
    }
  }

  private fun refreshSpaceWallpaper(force: Boolean) {
    val cachedUri = wallpaperRepo.cachedWallpaperUri()
    val elapsed = System.currentTimeMillis() - wallpaperRepo.lastUpdatedAt()
    val isDue = cachedUri == null || wallpaperRepo.lastUpdatedAt() == 0L ||
      elapsed >= WallpaperRepository.ROTATION_INTERVAL_MS
    if (!force && !isDue) {
      if (_uiState.value.customWallpaperUrl != cachedUri) {
        _uiState.update { it.copy(currentWallpaperId = "space_auto", customWallpaperUrl = cachedUri) }
      }
      return
    }
    viewModelScope.launch {
      val downloaded = wallpaperRepo.downloadNewWallpaper()
      // A failed request leaves the previous cached image visible.
      val uriToShow = downloaded?.uri ?: wallpaperRepo.cachedWallpaperUri()
      if (uriToShow != null) {
        _uiState.update {
          it.copy(currentWallpaperId = "space_auto", customWallpaperUrl = uriToShow)
        }
      }
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

  fun setWallpaper(wallpaperId: String) {
    _uiState.update { it.copy(currentWallpaperId = wallpaperId, customWallpaperUrl = null) }
  }

  fun setCustomWallpaper(url: String) {
    val cleanUrl = url.trim()
    if (cleanUrl.isNotEmpty()) {
      _uiState.update { it.copy(currentWallpaperId = "custom", customWallpaperUrl = cleanUrl) }
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

  private fun refreshSports() {
    viewModelScope.launch {
      val events = sportsRepo.getUpcomingEvents()
      val filter = _uiState.value.selectedSportFilter
      _uiState.update {
        it.copy(
          allSportsEvents = events,
          sportsEvents = sportsRepo.filterEvents(events, filter)
        )
      }
    }
  }

  fun setSportFilter(filter: String) {
    prefs.setSportFilter(filter)
    val filteredEvents = sportsRepo.filterEvents(_uiState.value.allSportsEvents, filter)
    _uiState.update { it.copy(selectedSportFilter = filter, sportsEvents = filteredEvents) }
  }

  fun toggleFavoriteSportsTeam(team: String) {
    val updated = _uiState.value.favoriteSportsTeams.toMutableSet()
    if (team in updated) updated.remove(team) else updated.add(team)
    prefs.setFavoriteSportsTeams(updated)
    _uiState.update { it.copy(favoriteSportsTeams = updated) }
  }

  fun togglePinApp(packageName: String, shouldPin: Boolean) {
    val currentPinned = prefs.getPinnedApps().toMutableSet()
    if (shouldPin) {
      if (packageName !in currentPinned && _uiState.value.allApps.count { it.isPinned } >= 6) return
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

}
