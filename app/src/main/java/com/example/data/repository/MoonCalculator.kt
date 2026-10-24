package com.example.data.repository

import com.example.data.model.MoonEvent
import com.example.data.model.MoonPhaseInfo
import com.example.data.model.MoonPhasePoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Cálculo astronómico de la luna hecho dentro de la app (no necesita internet).
 *
 * Usa las fórmulas aproximadas del algoritmo SunCalc (Vladimir Agafonkin, licencia BSD): la fase y
 * la iluminación salen de la posición del sol y de la luna, y la salida / puesta dependen de la
 * latitud y longitud. La precisión es de pocos minutos, suficiente para una pantalla de TV.
 */
object MoonCalculator {
  const val DEFAULT_LATITUDE = 19.4326 // Ciudad de México
  const val DEFAULT_LONGITUDE = -99.1332

  private const val SYNODIC_MONTH = 29.530588853
  private const val RAD = Math.PI / 180.0
  private const val DAY_MS = 86_400_000.0
  private const val HOUR_MS = 3_600_000L
  private const val J1970 = 2440588.0
  private const val J2000 = 2451545.0
  private const val OBLIQUITY = RAD * 23.4397

  private val zodiac = listOf(
    "Aries", "Tauro", "Géminis", "Cáncer", "Leo", "Virgo",
    "Libra", "Escorpio", "Sagitario", "Capricornio", "Acuario", "Piscis"
  )

  private class Coords(val ra: Double, val dec: Double, val dist: Double, val lon: Double)

  fun calculate(
    currentDate: Date = Date(),
    latitude: Double = DEFAULT_LATITUDE,
    longitude: Double = DEFAULT_LONGITUDE
  ): MoonPhaseInfo {
    val now = currentDate.time
    val phase = phaseAt(now)
    val illumination = ((1.0 - cos(2.0 * Math.PI * phase)) / 2.0 * 100.0).roundToInt().coerceIn(0, 100)

    val coords = moonCoords(toDays(now))
    val sign = zodiac[(floor(normalizeDegrees(coords.lon / RAD) / 30.0).toInt()).coerceIn(0, 11)]

    // Salida y puesta de hoy (hora local de la TV)
    val dayStart = Calendar.getInstance().apply {
      time = currentDate
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val (rise, set) = moonTimes(dayStart, latitude, longitude)

    // Próximas fases principales, ordenadas por fecha
    val upcoming = listOf(0.25 to "Cuarto creciente", 0.5 to "Luna llena", 0.75 to "Cuarto menguante", 0.0 to "Luna nueva")
      .map { (target, name) -> Triple(findPhaseTime(now, target), name, target) }
      .sortedBy { it.first }

    // Las 8 formas del ciclo (nueva, creciente, cuarto, gibosa, llena...) con el día en que llegan
    val strip = listOf(0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875)
      .map { target -> findPhaseTime(now, target) to target }
      .sortedBy { it.first }
      .map { MoonPhasePoint(it.second, shortDate(it.first, "d MMM")) }

    val nextFull = upcoming.first { it.third == 0.5 }.first
    val nextNew = upcoming.first { it.third == 0.0 }.first

    return MoonPhaseInfo(
      phaseName = phaseName(phase),
      illuminationPercent = illumination,
      ageDays = ((phase * SYNODIC_MONTH) * 10.0).roundToInt() / 10.0,
      isWaxing = phase < 0.5,
      nextFullMoonDate = formatDay(nextFull),
      nextNewMoonDate = formatDay(nextNew),
      constellation = sign,
      moonrise = formatClock(rise),
      moonset = formatClock(set),
      phase = phase,
      distanceKm = coords.dist.roundToInt(),
      upcomingPhases = upcoming.map { MoonEvent(it.second, formatDayAndTime(it.first)) },
      phaseStrip = strip
    )
  }

  // ---------------------------------------------------------------- fases

  private fun phaseName(p: Double): String = when {
    p < 0.03 || p >= 0.97 -> "Luna nueva"
    p < 0.22 -> "Luna creciente"
    p < 0.28 -> "Cuarto creciente"
    p < 0.47 -> "Gibosa creciente"
    p < 0.53 -> "Luna llena"
    p < 0.72 -> "Gibosa menguante"
    p < 0.78 -> "Cuarto menguante"
    else -> "Luna menguante"
  }

  /** Fase de 0 a 1: 0 = nueva, 0.25 = cuarto creciente, 0.5 = llena, 0.75 = cuarto menguante. */
  private fun phaseAt(ms: Long): Double {
    val d = toDays(ms)
    val s = sunCoords(d)
    val m = moonCoords(d)
    val phi = acos(sin(s.dec) * sin(m.dec) + cos(s.dec) * cos(m.dec) * cos(s.ra - m.ra))
    val inc = atan2(s.dist * sin(phi), m.dist - s.dist * cos(phi))
    val angle = atan2(
      cos(s.dec) * sin(s.ra - m.ra),
      sin(s.dec) * cos(m.dec) - cos(s.dec) * sin(m.dec) * cos(s.ra - m.ra)
    )
    return 0.5 + 0.5 * inc * (if (angle < 0) -1.0 else 1.0) / Math.PI
  }

  /** Momento en que la luna llega a la fase [target], a partir de [nowMs]. */
  private fun findPhaseTime(nowMs: Long, target: Double): Long {
    var delta = target - phaseAt(nowMs)
    delta -= floor(delta)
    if (delta < 0.002) delta += 1.0
    var t = nowMs + (delta * SYNODIC_MONTH * DAY_MS).toLong()
    repeat(6) {
      var diff = target - phaseAt(t)
      diff -= Math.round(diff).toDouble() // a [-0.5, 0.5]
      t += (diff * SYNODIC_MONTH * DAY_MS).toLong()
    }
    return t
  }

  // ---------------------------------------------------------------- salida y puesta

  private fun moonTimes(dayStart: Long, lat: Double, lng: Double): Pair<Long?, Long?> {
    val hc = 0.133 * RAD
    var h0 = moonAltitude(dayStart, lat, lng) - hc
    var rise: Double? = null
    var set: Double? = null

    var i = 1
    while (i <= 24) {
      val h1 = moonAltitude(dayStart + i * HOUR_MS, lat, lng) - hc
      val h2 = moonAltitude(dayStart + (i + 1) * HOUR_MS, lat, lng) - hc

      val a = (h0 + h2) / 2 - h1
      val b = (h2 - h0) / 2
      val xe = -b / (2 * a)
      val ye = (a * xe + b) * xe + h1
      val disc = b * b - 4 * a * h1
      var roots = 0
      var x1 = 0.0
      var x2 = 0.0

      if (disc >= 0) {
        val dx = sqrt(disc) / (abs(a) * 2)
        x1 = xe - dx
        x2 = xe + dx
        if (abs(x1) <= 1) roots++
        if (abs(x2) <= 1) roots++
        if (x1 < -1) x1 = x2
      }

      if (roots == 1) {
        if (h0 < 0) rise = i + x1 else set = i + x1
      } else if (roots == 2) {
        rise = i + (if (ye < 0) x2 else x1)
        set = i + (if (ye < 0) x1 else x2)
      }

      if (rise != null && set != null) break
      h0 = h2
      i += 2
    }

    return Pair(
      rise?.let { dayStart + (it * HOUR_MS).toLong() },
      set?.let { dayStart + (it * HOUR_MS).toLong() }
    )
  }

  private fun moonAltitude(ms: Long, lat: Double, lng: Double): Double {
    val lw = RAD * -lng
    val phi = RAD * lat
    val d = toDays(ms)
    val c = moonCoords(d)
    val h = siderealTime(d, lw) - c.ra
    val altitude = asin(sin(phi) * sin(c.dec) + cos(phi) * cos(c.dec) * cos(h))
    return altitude + astroRefraction(altitude)
  }

  // ---------------------------------------------------------------- astronomía básica

  private fun toDays(ms: Long): Double = ms / DAY_MS - 0.5 + J1970 - J2000

  private fun rightAscension(l: Double, b: Double): Double =
    atan2(sin(l) * cos(OBLIQUITY) - tan(b) * sin(OBLIQUITY), cos(l))

  private fun declination(l: Double, b: Double): Double =
    asin(sin(b) * cos(OBLIQUITY) + cos(b) * sin(OBLIQUITY) * sin(l))

  private fun siderealTime(d: Double, lw: Double): Double =
    RAD * (280.16 + 360.9856235 * d) - lw

  private fun astroRefraction(altitude: Double): Double {
    val h = if (altitude < 0) 0.0 else altitude
    return 0.0002967 / tan(h + 0.00312536 / (h + 0.08901179))
  }

  private fun sunCoords(d: Double): Coords {
    val m = RAD * (357.5291 + 0.98560028 * d)
    val c = RAD * (1.9148 * sin(m) + 0.02 * sin(2 * m) + 0.0003 * sin(3 * m))
    val p = RAD * 102.9372
    val l = m + c + p + Math.PI
    return Coords(rightAscension(l, 0.0), declination(l, 0.0), 149_598_000.0, l)
  }

  private fun moonCoords(d: Double): Coords {
    val l0 = RAD * (218.316 + 13.176396 * d)
    val m = RAD * (134.963 + 13.064993 * d)
    val f = RAD * (93.272 + 13.229350 * d)
    val l = l0 + RAD * 6.289 * sin(m)
    val b = RAD * 5.128 * sin(f)
    val dt = 385_001 - 20_905 * cos(m)
    return Coords(rightAscension(l, b), declination(l, b), dt, l)
  }

  private fun normalizeDegrees(deg: Double): Double = ((deg % 360.0) + 360.0) % 360.0

  // ---------------------------------------------------------------- formato

  private fun formatClock(ms: Long?): String {
    if (ms == null) return "—"
    return SimpleDateFormat("HH:mm", Locale.US).format(Date(ms))
  }

  private fun formatDay(ms: Long): String = shortDate(ms, "EEE d MMM")

  private fun formatDayAndTime(ms: Long): String = shortDate(ms, "EEE d MMM, HH:mm")

  private fun shortDate(ms: Long, pattern: String): String =
    SimpleDateFormat(pattern, Locale("es", "MX")).format(Date(ms))
      .replace(".", "")
      .replaceFirstChar { it.uppercase() }
}
