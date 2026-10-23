package com.example.data.repository

import com.example.data.model.SportsEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class SportsRepository {
  private val client = OkHttpClient.Builder()
    .connectTimeout(7, TimeUnit.SECONDS)
    .readTimeout(8, TimeUnit.SECONDS)
    .callTimeout(10, TimeUnit.SECONDS)
    .build()

  suspend fun getUpcomingEvents(): List<SportsEvent> = withContext(Dispatchers.IO) {
    val start = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = Date() }
    val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 21) }
    val range = "\${dateKey(start.time)}-\${dateKey(end.time)}"

    val sources = listOf(
      Source("NBA", "https://site.api.espn.com/apis/site/v2/sports/basketball/nba/scoreboard?dates=$range"),
      Source("Fútbol", "https://site.api.espn.com/apis/site/v2/sports/soccer/mex.1/scoreboard?dates=$range"),
      Source("Fútbol", "https://site.api.espn.com/apis/site/v2/sports/soccer/eng.1/scoreboard?dates=$range"),
      Source("Fútbol", "https://site.api.espn.com/apis/site/v2/sports/soccer/esp.1/scoreboard?dates=$range"),
      Source("Fórmula 1", "https://site.api.espn.com/apis/site/v2/sports/racing/f1/scoreboard?dates=$range"),
      Source("NFL", "https://site.api.espn.com/apis/site/v2/sports/football/nfl/scoreboard?dates=$range")
    )

    sources.flatMap { source ->
      runCatching { parseEvents(source.sport, requestJson(source.url)) }.getOrDefault(emptyList())
    }
      .distinctBy { it.id }
      .sortedBy { parseSortDate(it.id.substringAfter('|', "")) }
      .take(60)
  }

  fun filterEvents(events: List<SportsEvent>, sport: String): List<SportsEvent> {
    if (sport.isBlank() || sport.equals("Todos", ignoreCase = true)) return events
    return events.filter { it.sport.equals(sport, ignoreCase = true) }
  }

  private fun requestJson(url: String): JSONObject {
    val request = Request.Builder()
      .url(url)
      .header("User-Agent", "AURA-TV-Launcher/1.0")
      .header("Accept", "application/json")
      .build()
    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) error("Sports feed HTTP \${response.code}")
      JSONObject(response.body?.string() ?: error("Empty sports response"))
    }
  }

  private fun parseEvents(sport: String, json: JSONObject): List<SportsEvent> {
    val events = json.optJSONArray("events") ?: return emptyList()
    val output = mutableListOf<SportsEvent>()
    for (i in 0 until events.length()) {
      val event = events.optJSONObject(i) ?: continue
      val dateRaw = event.optString("date").takeIf { it.isNotBlank() } ?: continue
      val competition = event.optJSONArray("competitions")?.optJSONObject(0)
      val competitors = competition?.optJSONArray("competitors")
      var home = ""
      var away = ""
      if (competitors != null) {
        for (j in 0 until competitors.length()) {
          val competitor = competitors.optJSONObject(j) ?: continue
          val name = competitor.optJSONObject("team")?.optString("displayName").orEmpty()
          if (name.isBlank()) continue
          if (competitor.optString("homeAway") == "away") away = name else home = name
        }
      }

      val venueObj = competition?.optJSONObject("venue")
      val venueName = venueObj?.optString("fullName")?.takeIf { it.isNotBlank() }
      val city = venueObj?.optJSONObject("address")?.optString("city")?.takeIf { it.isNotBlank() }
      val venue = listOfNotNull(venueName, city).distinct().joinToString(", ").ifBlank { null }
      val statusType = competition?.optJSONObject("status")?.optJSONObject("type")
        ?: event.optJSONObject("status")?.optJSONObject("type")
      val detail = statusType?.optString("shortDetail")?.takeIf { it.isNotBlank() }
      val state = statusType?.optString("state").orEmpty()
      val live = state == "in"
      val title = event.optString("name").takeIf { it.isNotBlank() } ?: event.optString("shortName")
      val tournament = when (sport) {
        "NBA" -> "NBA"
        "NFL" -> "NFL"
        "Fórmula 1" -> "Fórmula 1"
        else -> event.optJSONObject("season")?.optString("name")?.takeIf { it.isNotBlank() } ?: "Fútbol"
      }

      if (sport == "Fórmula 1") {
        if (home.isBlank()) home = title
        if (away.isBlank()) away = venue ?: "Sede por confirmar"
      } else {
        if (home.isBlank() || away.isBlank()) continue
      }

      val formattedDate = formatDate(dateRaw) ?: continue
      output += SportsEvent(
        id = "\${sport}|\${dateRaw}|\${event.optString("id")}",
        sport = sport,
        tournament = tournament,
        teamHome = home,
        teamAway = away,
        airDateTime = formattedDate,
        broadcaster = "",
        status = when {
          live -> "EN VIVO"
          state == "post" -> detail ?: "Finalizado"
          else -> detail ?: "Próximamente"
        },
        isLive = live,
        venue = venue
      )
    }
    return output
  }

  private fun dateKey(date: Date): String =
    SimpleDateFormat("yyyyMMdd", Locale.US).apply {
      timeZone = TimeZone.getTimeZone("UTC")
    }.format(date)

  private fun formatDate(raw: String): String? = runCatching {
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US).apply {
      timeZone = TimeZone.getTimeZone("UTC")
      isLenient = true
    }
    val parsed = parser.parse(raw) ?: return null
    SimpleDateFormat("EEE d MMM · HH:mm", Locale("es", "MX")).format(parsed)
      .replaceFirstChar { it.uppercase(Locale("es", "MX")) }
  }.getOrNull()

  private fun parseSortDate(raw: String): Long = runCatching {
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US).apply {
      timeZone = TimeZone.getTimeZone("UTC")
    }
    parser.parse(raw)?.time ?: Long.MAX_VALUE
  }.getOrDefault(Long.MAX_VALUE)

  private data class Source(val sport: String, val url: String)
}
