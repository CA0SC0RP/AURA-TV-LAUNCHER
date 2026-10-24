package com.example.data.model

data class WeatherData(
  val cityName: String,
  val country: String,
  val currentTemp: Double,
  val tempUnit: String = "°C",
  val conditionDescription: String,
  val weatherCode: Int,
  val highTemp: Double,
  val lowTemp: Double,
  val humidity: Int,
  val windSpeedKmH: Double,
  val uvIndex: Double,
  val hourlyForecast: List<HourlyWeather>,
  val dailyForecast: List<DailyWeather>,
  // Ubicación del clima (también se usa para calcular la salida y puesta de la luna)
  val latitude: Double = 19.4326,
  val longitude: Double = -99.1332
)

data class HourlyWeather(
  val timeLabel: String,
  val temp: Double,
  val weatherCode: Int,
  val isNow: Boolean = false
)

data class DailyWeather(
  val dayLabel: String,
  val dateLabel: String,
  val minTemp: Double,
  val maxTemp: Double,
  val weatherCode: Int,
  val condition: String
)

data class MoonEvent(
  val name: String,
  val dateLabel: String
)

data class MoonPhaseInfo(
  val phaseName: String,
  val illuminationPercent: Int,
  val ageDays: Double,
  val isWaxing: Boolean,
  val nextFullMoonDate: String,
  val nextNewMoonDate: String,
  val constellation: String, // signo zodiacal en el que está la luna
  val moonrise: String, // "HH:mm" o "—" si hoy no sale
  val moonset: String,
  val phase: Double = 0.0, // 0 = nueva, 0.5 = llena
  val distanceKm: Int = 384_400,
  val upcomingPhases: List<MoonEvent> = emptyList()
)
