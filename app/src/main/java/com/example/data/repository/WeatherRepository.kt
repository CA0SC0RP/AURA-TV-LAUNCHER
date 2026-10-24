package com.example.data.repository

import com.example.data.model.DailyWeather
import com.example.data.model.HourlyWeather
import com.example.data.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class WeatherRepository(private val client: OkHttpClient = OkHttpClient.Builder()
  .connectTimeout(8, TimeUnit.SECONDS)
  .readTimeout(8, TimeUnit.SECONDS)
  .build()) {

  private val fallbackWeather = WeatherData(
    cityName = "Ciudad de México",
    country = "México",
    currentTemp = 22.0,
    tempUnit = "°C",
    conditionDescription = "Despejado",
    weatherCode = 0,
    highTemp = 25.0,
    lowTemp = 14.0,
    humidity = 45,
    windSpeedKmH = 12.5,
    uvIndex = 5.2,
    hourlyForecast = listOf(
      HourlyWeather("Ahora", 22.0, 0, isNow = true),
      HourlyWeather("14:00", 24.0, 1),
      HourlyWeather("16:00", 25.0, 1),
      HourlyWeather("18:00", 23.0, 2),
      HourlyWeather("20:00", 20.0, 2),
      HourlyWeather("22:00", 17.0, 0),
      HourlyWeather("00:00", 15.0, 0)
    ),
    dailyForecast = listOf(
      DailyWeather("Hoy", "23 Oct", 14.0, 25.0, 0, "Despejado"),
      DailyWeather("Vie", "24 Oct", 13.0, 24.0, 1, "Soleado"),
      DailyWeather("Sáb", "25 Oct", 15.0, 26.0, 2, "Parcialmente nublado"),
      DailyWeather("Dom", "26 Oct", 14.0, 22.0, 61, "Lluvia ligera"),
      DailyWeather("Lun", "27 Oct", 12.0, 20.0, 3, "Nublado"),
      DailyWeather("Mar", "28 Oct", 13.0, 23.0, 1, "Despejado"),
      DailyWeather("Mié", "29 Oct", 15.0, 25.0, 0, "Soleado")
    )
  )

  fun getFallback(): WeatherData = fallbackWeather

  suspend fun fetchWeather(queryLocation: String, isFahrenheit: Boolean = false): WeatherData = withContext(Dispatchers.IO) {
    try {
      // 1. Geocode location (city, postal code, or country)
      val cleanQuery = queryLocation.trim()
      val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=${java.net.URLEncoder.encode(cleanQuery, "UTF-8")}&count=1&language=es&format=json"
      val geoReq = Request.Builder().url(geoUrl).build()
      val geoResp = client.newCall(geoReq).execute()
      val geoBody = geoResp.body?.string() ?: return@withContext fallbackWeather

      val geoJson = JSONObject(geoBody)
      val results = geoJson.optJSONArray("results")
      if (results == null || results.length() == 0) {
        return@withContext fallbackWeather.copy(cityName = queryLocation)
      }

      val firstResult = results.getJSONObject(0)
      val lat = firstResult.optDouble("latitude", 40.4168)
      val lon = firstResult.optDouble("longitude", -3.7038)
      val cityName = firstResult.optString("name", queryLocation)
      val country = firstResult.optString("country", "")

      fetchForecast(lat, lon, cityName, country, isFahrenheit)
    } catch (e: Exception) {
      fallbackWeather.copy(cityName = queryLocation)
    }
  }

  /**
   * Detecta dónde está la TV a partir de su conexión a internet (por IP) y trae el clima de ahí.
   * Devuelve null si no se pudo detectar la ubicación o no hay internet.
   */
  suspend fun fetchWeatherAutomatic(isFahrenheit: Boolean = false): WeatherData? = withContext(Dispatchers.IO) {
    val location = detectLocationByIp() ?: return@withContext null
    try {
      fetchForecast(location.latitude, location.longitude, location.city, location.country, isFahrenheit)
    } catch (e: Exception) {
      null
    }
  }

  private class DetectedLocation(
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val country: String
  )

  private fun detectLocationByIp(): DetectedLocation? {
    // Servicio 1: ipwho.is (HTTPS, sin clave)
    try {
      val request = Request.Builder()
        .url("https://ipwho.is/?lang=es&fields=success,city,country,latitude,longitude")
        .build()
      val body = client.newCall(request).execute().use { it.body?.string() }
      if (body != null) {
        val json = JSONObject(body)
        if (json.optBoolean("success", false) && json.has("latitude") && json.has("longitude")) {
          return DetectedLocation(
            latitude = json.getDouble("latitude"),
            longitude = json.getDouble("longitude"),
            city = json.optString("city", "").ifBlank { "Mi ubicación" },
            country = json.optString("country", "")
          )
        }
      }
    } catch (e: Exception) {
      // probar el siguiente servicio
    }

    // Servicio 2 (respaldo): ipapi.co
    try {
      val request = Request.Builder().url("https://ipapi.co/json/").build()
      val body = client.newCall(request).execute().use { it.body?.string() }
      if (body != null) {
        val json = JSONObject(body)
        if (!json.isNull("latitude") && !json.isNull("longitude") && json.has("latitude")) {
          return DetectedLocation(
            latitude = json.getDouble("latitude"),
            longitude = json.getDouble("longitude"),
            city = json.optString("city", "").ifBlank { "Mi ubicación" },
            country = json.optString("country_name", "")
          )
        }
      }
    } catch (e: Exception) {
      // sin ubicación
    }
    return null
  }

  private fun fetchForecast(
    lat: Double,
    lon: Double,
    cityName: String,
    country: String,
    isFahrenheit: Boolean
  ): WeatherData {
    // 2. Fetch Forecast from Open-Meteo
    val unitParam = if (isFahrenheit) "&temperature_unit=fahrenheit" else ""
    val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&hourly=temperature_2m,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,uv_index_max&timezone=auto$unitParam"

    val weatherReq = Request.Builder().url(weatherUrl).build()
    val weatherBody = client.newCall(weatherReq).execute().use { it.body?.string() }
      ?: error("Respuesta vacía del clima")

    val weatherJson = JSONObject(weatherBody)
    val current = weatherJson.optJSONObject("current") ?: error("Sin datos actuales")
    val hourly = weatherJson.optJSONObject("hourly")
    val daily = weatherJson.optJSONObject("daily")

    val currentTemp = current.optDouble("temperature_2m", 20.0)
    val weatherCode = current.optInt("weather_code", 0)
    val humidity = current.optInt("relative_humidity_2m", 50)
    val windSpeed = current.optDouble("wind_speed_10m", 10.0)

    val hourlyList = mutableListOf<HourlyWeather>()
    if (hourly != null) {
      val times = hourly.optJSONArray("time")
      val temps = hourly.optJSONArray("temperature_2m")
      val codes = hourly.optJSONArray("weather_code")

      val nowCal = Calendar.getInstance()
      val currentHour = nowCal.get(Calendar.HOUR_OF_DAY)

      if (times != null && temps != null && codes != null) {
        // Grab up to next 12 hours starting near current hour
        var count = 0
        for (i in 0 until times.length()) {
          val tStr = times.optString(i, "")
          val hourPart = if (tStr.contains("T")) tStr.substringAfter("T") else tStr
          val hVal = hourPart.take(2).toIntOrNull() ?: 0

          if (hVal >= currentHour && count < 8) {
            val isNow = count == 0
            val timeLabel = if (isNow) "Ahora" else hourPart
            val tempVal = temps.optDouble(i, currentTemp)
            val codeVal = codes.optInt(i, weatherCode)
            hourlyList.add(HourlyWeather(timeLabel, (tempVal * 10).roundToInt() / 10.0, codeVal, isNow))
            count++
          }
        }
      }
    }

    val dailyList = mutableListOf<DailyWeather>()
    var maxToday = currentTemp + 3
    var minToday = currentTemp - 4
    var uvToday = 4.0

    if (daily != null) {
      val dailyTimes = daily.optJSONArray("time")
      val maxTemps = daily.optJSONArray("temperature_2m_max")
      val minTemps = daily.optJSONArray("temperature_2m_min")
      val dailyCodes = daily.optJSONArray("weather_code")
      val uvMax = daily.optJSONArray("uv_index_max")

      val sdfParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
      val sdfDayName = SimpleDateFormat("EEE", Locale("es", "ES"))
      val sdfDayNum = SimpleDateFormat("dd MMM", Locale("es", "ES"))

      if (dailyTimes != null && maxTemps != null && minTemps != null) {
        for (i in 0 until minOf(dailyTimes.length(), 7)) {
          val dStr = dailyTimes.optString(i, "")
          val parsedDate = try { sdfParser.parse(dStr) } catch (e: Exception) { Date() } ?: Date()
          val dayLabel = if (i == 0) "Hoy" else sdfDayName.format(parsedDate).replaceFirstChar { it.uppercase() }
          val dateLabel = sdfDayNum.format(parsedDate)
          val maxT = (maxTemps.optDouble(i, 20.0) * 10).roundToInt() / 10.0
          val minT = (minTemps.optDouble(i, 10.0) * 10).roundToInt() / 10.0
          val dCode = dailyCodes?.optInt(i, 0) ?: 0

          if (i == 0) {
            maxToday = maxT
            minToday = minT
            uvToday = uvMax?.optDouble(0, 4.0) ?: 4.0
          }

          dailyList.add(
            DailyWeather(
              dayLabel = dayLabel,
              dateLabel = dateLabel,
              minTemp = minT,
              maxTemp = maxT,
              weatherCode = dCode,
              condition = getWeatherDescription(dCode)
            )
          )
        }
      }
    }

    return WeatherData(
      cityName = cityName,
      country = country,
      currentTemp = (currentTemp * 10).roundToInt() / 10.0,
      tempUnit = if (isFahrenheit) "°F" else "°C",
      conditionDescription = getWeatherDescription(weatherCode),
      weatherCode = weatherCode,
      highTemp = maxToday,
      lowTemp = minToday,
      humidity = humidity,
      windSpeedKmH = (windSpeed * 10).roundToInt() / 10.0,
      uvIndex = uvToday,
      hourlyForecast = if (hourlyList.isNotEmpty()) hourlyList else fallbackWeather.hourlyForecast,
      dailyForecast = if (dailyList.isNotEmpty()) dailyList else fallbackWeather.dailyForecast,
      latitude = lat,
      longitude = lon
    )
  }

  fun getWeatherDescription(code: Int): String {
    return when (code) {
      0 -> "Cielo despejado"
      1 -> "Mayormente despejado"
      2 -> "Parcialmente nublado"
      3 -> "Nublado"
      45, 48 -> "Niebla y bruma"
      51, 53, 55 -> "Llovizna"
      61, 63, 65 -> "Lluvia"
      71, 73, 75, 77 -> "Nevada"
      80, 81, 82 -> "Chubascos intensos"
      85, 86 -> "Chubascos de nieve"
      95, 96, 99 -> "Tormenta eléctrica"
      else -> "Tiempo variable"
    }
  }
}
