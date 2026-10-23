package com.example.data.repository

import com.example.data.model.MoonPhaseInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.roundToInt

object MoonCalculator {
  private const val SYNODIC_MONTH = 29.53058867
  // Reference new moon: Jan 11, 2024, 11:57 UTC
  private const val KNOWN_NEW_MOON_MILLIS = 1704974220000L

  fun calculate(currentDate: Date = Date()): MoonPhaseInfo {
    val diffMillis = currentDate.time - KNOWN_NEW_MOON_MILLIS
    val diffDays = diffMillis / (1000.0 * 60 * 60 * 24)
    val cycles = diffDays / SYNODIC_MONTH
    val currentCycleFraction = cycles - cycles.toLong()
    val normalizedFraction = if (currentCycleFraction < 0) currentCycleFraction + 1.0 else currentCycleFraction
    val ageDays = normalizedFraction * SYNODIC_MONTH

    // Illumination fraction approx: (1 - cos(angle)) / 2
    val angleRad = normalizedFraction * 2.0 * Math.PI
    val illumination = ((1.0 - cos(angleRad)) / 2.0 * 100.0).roundToInt().coerceIn(0, 100)
    val isWaxing = normalizedFraction < 0.5

    val phaseName = when {
      ageDays < 1.84 -> "Luna Nueva"
      ageDays < 5.53 -> "Creciente Cóncava"
      ageDays < 9.22 -> "Cuarto Creciente"
      ageDays < 12.91 -> "Gibosa Creciente"
      ageDays < 16.61 -> "Luna Llena"
      ageDays < 20.30 -> "Gibosa Menguante"
      ageDays < 23.99 -> "Cuarto Menguante"
      ageDays < 27.68 -> "Menguante Cóncava"
      else -> "Luna Nueva"
    }

    // Next full moon and new moon
    val daysUntilFull = if (ageDays <= 14.76) {
      14.76 - ageDays
    } else {
      SYNODIC_MONTH + (14.76 - ageDays)
    }
    val daysUntilNew = SYNODIC_MONTH - ageDays

    val cal = Calendar.getInstance()
    cal.time = currentDate

    val nextFullCal = Calendar.getInstance().apply {
      timeInMillis = currentDate.time + (daysUntilFull * 86400000L).toLong()
    }
    val nextNewCal = Calendar.getInstance().apply {
      timeInMillis = currentDate.time + (daysUntilNew * 86400000L).toLong()
    }

    val dateFormat = SimpleDateFormat("dd 'de' MMMM", Locale("es", "ES"))

    val constellations = listOf("Tauro", "Géminis", "Cáncer", "Leo", "Virgo", "Libra", "Escorpio", "Sagitario", "Capricornio", "Acuario", "Piscis", "Aries")
    val zodiacIndex = ((cal.get(Calendar.DAY_OF_YEAR) / 30.4) + (ageDays / 2.5)).toInt() % constellations.size
    val currentConstellation = constellations[zodiacIndex]

    // Moonrise and moonset estimation based on phase
    val riseHour = ((6 + (ageDays * 0.8)) % 24).toInt()
    val setHour = ((riseHour + 12) % 24)

    return MoonPhaseInfo(
      phaseName = phaseName,
      illuminationPercent = illumination,
      ageDays = (ageDays * 10.0).roundToInt() / 10.0,
      isWaxing = isWaxing,
      nextFullMoonDate = dateFormat.format(nextFullCal.time),
      nextNewMoonDate = dateFormat.format(nextNewCal.time),
      constellation = currentConstellation,
      moonrise = String.format(Locale.getDefault(), "%02d:15", riseHour),
      moonset = String.format(Locale.getDefault(), "%02d:40", setHour)
    )
  }
}
