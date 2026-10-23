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
  val dailyForecast: List<DailyWeather>
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

data class MoonPhaseInfo(
  val phaseName: String,
  val illuminationPercent: Int,
  val ageDays: Double,
  val isWaxing: Boolean,
  val nextFullMoonDate: String,
  val nextNewMoonDate: String,
  val constellation: String,
  val moonrise: String,
  val moonset: String
)
