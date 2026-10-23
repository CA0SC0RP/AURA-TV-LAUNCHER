package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.TvSeries
import org.json.JSONArray
import org.json.JSONObject

class LauncherPreferences(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("aura_tv_launcher_prefs", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_PINNED_APPS = "pinned_apps"
    private const val KEY_LOCATION = "weather_location"
    private const val KEY_FAHRENHEIT = "is_fahrenheit"
    private const val KEY_WALLPAPER_ID = "wallpaper_id"
    private const val KEY_CUSTOM_WALLPAPER_URL = "custom_wallpaper_url"
    private const val KEY_SERIES_JSON = "saved_series_json"
    private const val KEY_SPORT_FILTER = "sport_filter"
  }

  fun getPinnedApps(): Set<String> {
    val saved = prefs.getStringSet(KEY_PINNED_APPS, null)
    return saved ?: setOf(
      "com.google.android.youtube.tv",
      "com.netflix.ninja",
      "com.amazon.amazonvideo.livingroom",
      "com.disney.disneyplus",
      "com.spotify.tv.android",
      "com.android.vending"
    )
  }

  fun setPinnedApps(packages: Set<String>) {
    prefs.edit().putStringSet(KEY_PINNED_APPS, packages).apply()
  }

  fun getWeatherLocation(): String {
    val saved = prefs.getString(KEY_LOCATION, "") ?: ""
    // Older builds silently defaulted to Madrid; treat that legacy value as unconfigured.
    return if (saved.equals("Madrid, España", ignoreCase = true)) "" else saved
  }

  fun setWeatherLocation(location: String) {
    prefs.edit().putString(KEY_LOCATION, location).apply()
  }

  fun isFahrenheit(): Boolean = prefs.getBoolean(KEY_FAHRENHEIT, false)

  fun setFahrenheit(fahrenheit: Boolean) {
    prefs.edit().putBoolean(KEY_FAHRENHEIT, fahrenheit).apply()
  }

  fun getWallpaperId(): String = prefs.getString(KEY_WALLPAPER_ID, "cosmic_nebula") ?: "cosmic_nebula"

  fun setWallpaperId(id: String) {
    prefs.edit().putString(KEY_WALLPAPER_ID, id).apply()
  }

  fun getCustomWallpaperUrl(): String? = prefs.getString(KEY_CUSTOM_WALLPAPER_URL, null)

  fun setCustomWallpaperUrl(url: String?) {
    prefs.edit().putString(KEY_CUSTOM_WALLPAPER_URL, url).apply()
  }

  fun getSportFilter(): String = prefs.getString(KEY_SPORT_FILTER, "Todos") ?: "Todos"

  fun setSportFilter(filter: String) {
    prefs.edit().putString(KEY_SPORT_FILTER, filter).apply()
  }

  fun getSavedSeries(): List<TvSeries>? {
    val jsonStr = prefs.getString(KEY_SERIES_JSON, null) ?: return null
    return try {
      val array = JSONArray(jsonStr)
      val list = mutableListOf<TvSeries>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val daysJson = obj.optJSONArray("airDays")
        val days = mutableListOf<String>()
        if (daysJson != null) {
          for (d in 0 until daysJson.length()) {
            days.add(daysJson.getString(d))
          }
        }
        list.add(
          TvSeries(
            id = obj.getLong("id"),
            name = obj.getString("name"),
            posterUrl = obj.optString("posterUrl", null),
            currentSeason = obj.optInt("currentSeason", 1),
            currentEpisode = obj.optInt("currentEpisode", 1),
            totalSeasons = obj.optInt("totalSeasons", 1),
            nextAirDate = obj.optString("nextAirDate", null),
            airDays = if (days.isNotEmpty()) days else listOf("Domingo"),
            airTime = obj.optString("airTime", "21:00"),
            status = obj.optString("status", "En emisión"),
            network = obj.optString("network", "TV"),
            rating = obj.optDouble("rating", 8.0),
            summary = obj.optString("summary", "")
          )
        )
      }
      list
    } catch (e: Exception) {
      null
    }
  }

  fun saveSeries(seriesList: List<TvSeries>) {
    try {
      val array = JSONArray()
      for (s in seriesList) {
        val daysArr = JSONArray()
        s.airDays.forEach { daysArr.put(it) }

        val obj = JSONObject().apply {
          put("id", s.id)
          put("name", s.name)
          put("posterUrl", s.posterUrl ?: "")
          put("currentSeason", s.currentSeason)
          put("currentEpisode", s.currentEpisode)
          put("totalSeasons", s.totalSeasons)
          put("nextAirDate", s.nextAirDate ?: "")
          put("airDays", daysArr)
          put("airTime", s.airTime)
          put("status", s.status)
          put("network", s.network)
          put("rating", s.rating)
          put("summary", s.summary)
        }
        array.put(obj)
      }
      prefs.edit().putString(KEY_SERIES_JSON, array.toString()).apply()
    } catch (e: Exception) {
      // ignore
    }
  }
}
