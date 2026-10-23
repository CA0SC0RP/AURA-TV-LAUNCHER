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

  fun getFallback(): WeatherData? = null

  suspend fun fetchWeather(queryLocation: String, isFahrenheit: Boolean = false): WeatherData? = withContext(Dispatchers.IO) {
    try {
      // 1. Resolve a postal code within the selected country, or accept a legacy city query.
      val cleanQuery = queryLocation.trim()
      val locationParts = cleanQuery.split("|", limit = 2)
      val geoUrl: String
      if (locationParts.size == 2) {
        val postalCode = locationParts[0].trim()
        val countryInput = locationParts[1].trim()
        val countryCode = if (countryInput.length == 2) {
          countryInput.uppercase(Locale.ROOT)
        } else {
          val countryUrl = "https://geocoding-api.open-meteo.com/v1/search?name=${java.net.URLEncoder.encode(countryInput, "UTF-8")}&count=1&language=es&format=json"
          val countryResponse = client.newCall(Request.Builder().url(countryUrl).build()).execute()
          val countryBody = countryResponse.body?.string() ?: return@withContext null
          val countryResults = JSONObject(countryBody).optJSONArray("results") ?: return@withContext null
          if (countryResults.length() == 0) return@withContext null
          countryResults.getJSONObject(0).optString("country_code", "").uppercase(Locale.ROOT)
        }
        if (countryCode.length != 2) return@withContext null
        geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=${java.net.URLEncoder.encode(postalCode, "UTF-8")}&countryCode=$countryCode&count=1&language=es&format=json"
      } else {
        // Compatibility for previously saved city names.
        geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=${java.net.URLEncoder.encode(cleanQuery, "UTF-8")}&count=1&language=es&format=json"
      }
      val geoReq = Request.Builder().url(geoUrl).build()
      val geoResp = client.newCall(geoReq).execute()
      val geoBody = geoResp.body?.string() ?: return@withContext null

      val geoJson = JSONObject(geoBody)
      val results = geoJson.optJSONArray("results")
      if (results == null || results.length() == 0) return@withContext null

      val firstResult = results.getJSONObject(0)
      if (!firstResult.has("latitude") || !firstResult.has("longitude")) return@withContext null
      val lat = firstResult.optDouble("latitude")
      val lon = firstResult.optDouble("longitude")
      val cityName = firstResult.optString("name", cleanQuery)
      val country = firstResult.optString("country", "")
      // 2. Fetch Forecast from Open-Meteo
      val unitParam = if (isFahrenheit) "&temperature_unit=fahrenheit" else ""
      val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&hourly=temperature_2m,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,uv_index_max&timezone=auto$unitParam"

      val weatherReq = Request.Builder().url(weatherUrl).build()
      val weatherResp = client.newCall(weatherReq).execute()
      val weatherBody = weatherResp.body?.string() ?: return@withContext null

      val weatherJson = JSONObject(weatherBody)
      val current = weatherJson.optJSONObject("current") ?: return@withContext null
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

      WeatherData(
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
        hourlyForecast = hourlyList,
        dailyForecast = dailyList
      )
    } catch (e: Exception) {
      null
    }
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
