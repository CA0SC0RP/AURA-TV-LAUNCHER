package com.example.data.repository

import com.example.data.model.SportsEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class NbaTeam(val abbr: String, val name: String)

/**
 * Próximos partidos de NBA (por equipo) y tenis.
 *
 * Los datos salen de los servicios públicos de ESPN. Son gratis y no piden clave, pero no son una
 * API oficial: si ESPN cambia algo, la ficha mostrará un aviso en lugar de cerrarse.
 */
class SportsRepository(
  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(8, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .build()
) {

  companion object {
    const val NBA = "NBA"
    const val TENNIS = "Tenis"

    // Los dos deportes de la ficha (el primero es el inicial)
    val featuredSports = listOf(NBA, TENNIS)

    // Abreviaturas tal como las usa ESPN
    val nbaTeams = listOf(
      NbaTeam("atl", "Hawks"), NbaTeam("bos", "Celtics"), NbaTeam("bkn", "Nets"),
      NbaTeam("cha", "Hornets"), NbaTeam("chi", "Bulls"), NbaTeam("cle", "Cavaliers"),
      NbaTeam("dal", "Mavericks"), NbaTeam("den", "Nuggets"), NbaTeam("det", "Pistons"),
      NbaTeam("gs", "Warriors"), NbaTeam("hou", "Rockets"), NbaTeam("ind", "Pacers"),
      NbaTeam("lac", "Clippers"), NbaTeam("lal", "Lakers"), NbaTeam("mem", "Grizzlies"),
      NbaTeam("mia", "Heat"), NbaTeam("mil", "Bucks"), NbaTeam("min", "Timberwolves"),
      NbaTeam("no", "Pelicans"), NbaTeam("ny", "Knicks"), NbaTeam("okc", "Thunder"),
      NbaTeam("orl", "Magic"), NbaTeam("phi", "76ers"), NbaTeam("phx", "Suns"),
      NbaTeam("por", "Trail Blazers"), NbaTeam("sac", "Kings"), NbaTeam("sa", "Spurs"),
      NbaTeam("tor", "Raptors"), NbaTeam("utah", "Jazz"), NbaTeam("wsh", "Wizards")
    )

    fun nbaTeamName(abbr: String): String? =
      nbaTeams.firstOrNull { it.abbr.equals(abbr, ignoreCase = true) }?.name

    private const val ESPN = "https://site.api.espn.com/apis/site/v2/sports"
    private const val MAX_EVENTS = 3
    private const val LIVE_WINDOW_MS = 3L * 60 * 60 * 1000 // un partido empezado hace <3 h sigue "vigente"
  }

  /** Próximos partidos de un equipo de la NBA. */
  suspend fun fetchNbaGames(teamAbbr: String): Result<List<SportsEvent>> = withContext(Dispatchers.IO) {
    runCatching {
      val body = get("$ESPN/basketball/nba/teams/${teamAbbr.lowercase(Locale.US)}/schedule")
      val events = JSONObject(body).optJSONArray("events") ?: JSONArray()
      val minTime = System.currentTimeMillis() - LIVE_WINDOW_MS

      val games = mutableListOf<Pair<Date, SportsEvent>>()
      for (i in 0 until events.length()) {
        val ev = events.optJSONObject(i) ?: continue
        val date = parseIso(ev.optString("date")) ?: continue
        if (date.time < minTime) continue

        val competitors = ev.optJSONArray("competitions")
          ?.optJSONObject(0)
          ?.optJSONArray("competitors")
        var home = ""
        var away = ""
        if (competitors != null) {
          for (c in 0 until competitors.length()) {
            val comp = competitors.optJSONObject(c) ?: continue
            val label = teamLabel(comp.optJSONObject("team"))
            if (comp.optString("homeAway") == "home") home = label else away = label
          }
        }
        if (home.isBlank() && away.isBlank()) {
          home = ev.optString("shortName", ev.optString("name", "Partido"))
        }

        games.add(
          date to SportsEvent(
            id = "nba_${ev.optString("id", i.toString())}",
            sport = "Baloncesto",
            tournament = "NBA",
            teamHome = home,
            teamAway = away,
            airDateTime = formatLocal(date),
            broadcaster = "",
            status = "Próximo"
          )
        )
      }
      games.sortedBy { it.first }.map { it.second }.take(MAX_EVENTS)
    }
  }

  /** Próximos partidos de tenis (ATP y WTA). Si uno de los dos falla, usa el otro. */
  suspend fun fetchTennisMatches(): Result<List<SportsEvent>> = withContext(Dispatchers.IO) {
    val results = listOf("atp", "wta").map { tour ->
      runCatching { parseTennis(get("$ESPN/tennis/$tour/scoreboard"), tour.uppercase(Locale.US)) }
    }
    val ok = results.mapNotNull { it.getOrNull() }
    if (ok.isEmpty()) {
      Result.failure(results.firstNotNullOfOrNull { it.exceptionOrNull() } ?: IllegalStateException("Sin datos"))
    } else {
      Result.success(
        ok.flatten().sortedBy { it.first }.map { it.second }.take(MAX_EVENTS)
      )
    }
  }

  private fun parseTennis(body: String, tour: String): List<Pair<Date, SportsEvent>> {
    val events = JSONObject(body).optJSONArray("events") ?: JSONArray()
    val minTime = System.currentTimeMillis() - LIVE_WINDOW_MS
    val matches = mutableListOf<Pair<Date, SportsEvent>>()
    val tournaments = mutableListOf<Pair<Date, SportsEvent>>()

    for (i in 0 until events.length()) {
      val ev = events.optJSONObject(i) ?: continue
      val tournamentName = ev.optString("name", tour)

      // Partidos individuales (si ESPN los incluye)
      val groupings = ev.optJSONArray("groupings") ?: JSONArray()
      for (g in 0 until groupings.length()) {
        val competitions = groupings.optJSONObject(g)?.optJSONArray("competitions") ?: continue
        for (c in 0 until competitions.length()) {
          val comp = competitions.optJSONObject(c) ?: continue
          val date = parseIso(comp.optString("date")) ?: continue
          val state = comp.optJSONObject("status")?.optJSONObject("type")?.optString("state")
          if (state == "post" || date.time < minTime) continue

          val players = comp.optJSONArray("competitors") ?: continue
          val names = (0 until players.length()).mapNotNull { p ->
            val player = players.optJSONObject(p) ?: return@mapNotNull null
            val athlete = player.optJSONObject("athlete")
            athlete?.optString("shortName")?.takeIf { it.isNotBlank() }
              ?: athlete?.optString("displayName")?.takeIf { it.isNotBlank() }
              ?: player.optString("displayName").takeIf { it.isNotBlank() }
          }
          if (names.size < 2) continue

          matches.add(
            date to SportsEvent(
              id = "tennis_${comp.optString("id", "$i-$g-$c")}",
              sport = "Tenis",
              tournament = "$tour · $tournamentName",
              teamHome = names[0],
              teamAway = names[1],
              airDateTime = formatLocal(date),
              broadcaster = "",
              status = "Próximo"
            )
          )
        }
      }

      // Respaldo: si no hay partidos sueltos, al menos el torneo y cuándo empieza
      val start = parseIso(ev.optString("date"))
      val end = parseIso(ev.optString("endDate")) ?: start
      if (start != null && end != null && end.time >= minTime) {
        tournaments.add(
          start to SportsEvent(
            id = "tennis_t_${ev.optString("id", i.toString())}",
            sport = "Tenis",
            tournament = tour,
            teamHome = tournamentName,
            teamAway = "",
            airDateTime = formatLocal(start),
            broadcaster = "",
            status = "Torneo"
          )
        )
      }
    }
    return if (matches.isNotEmpty()) matches else tournaments
  }

  private fun teamLabel(team: JSONObject?): String {
    if (team == null) return ""
    for (key in listOf("shortDisplayName", "nickname", "displayName", "name", "abbreviation")) {
      val value = team.optString(key, "")
      if (value.isNotBlank()) return value
    }
    return ""
  }

  private fun get(url: String): String {
    val request = Request.Builder().url(url).build()
    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) error("HTTP ${response.code}")
      return response.body?.string() ?: error("Respuesta vacía")
    }
  }

  private fun parseIso(value: String?): Date? {
    if (value.isNullOrBlank()) return null
    for (pattern in listOf("yyyy-MM-dd'T'HH:mm'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")) {
      try {
        val format = SimpleDateFormat(pattern, Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.parse(value)
      } catch (e: Exception) {
        // probar el siguiente formato
      }
    }
    return null
  }

  // Hora local de la TV (si la TV está en México, sale en hora de México)
  private fun formatLocal(date: Date): String {
    val format = SimpleDateFormat("EEE d MMM, HH:mm", Locale("es", "MX"))
    format.timeZone = TimeZone.getDefault()
    return format.format(date).replaceFirstChar { it.uppercase() }
  }
}
