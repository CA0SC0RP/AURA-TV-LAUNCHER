package com.example.data.repository

import com.example.data.model.TvMazeSearchResult
import com.example.data.model.TvSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TvMazeRepository(private val client: OkHttpClient = OkHttpClient.Builder()
  .connectTimeout(8, TimeUnit.SECONDS)
  .readTimeout(8, TimeUnit.SECONDS)
  .build()) {

  private val fallbackSeries = listOf(
    TvSeries(
      id = 2993,
      name = "Stranger Things",
      posterUrl = "https://static.tvmaze.com/uploads/images/original_untouched/200/501942.jpg",
      currentSeason = 4,
      currentEpisode = 9,
      totalSeasons = 5,
      nextAirDate = "Próximamente (Temp 5)",
      airDays = listOf("Viernes"),
      airTime = "09:00",
      status = "En producción",
      network = "Netflix",
      rating = 8.6,
      summary = "Cuando un niño desaparece, sus amigos, la familia y la policía se ven envueltos en un misterio que involucra experimentos secretos y fuerzas sobrenaturales."
    ),
    TvSeries(
      id = 44778,
      name = "House of the Dragon",
      posterUrl = "https://static.tvmaze.com/uploads/images/original_untouched/518/1296657.jpg",
      currentSeason = 2,
      currentEpisode = 8,
      totalSeasons = 3,
      nextAirDate = "Verano 2026",
      airDays = listOf("Domingo"),
      airTime = "21:00",
      status = "En emisión",
      network = "HBO Max",
      rating = 8.4,
      summary = "La historia de la dinastía Targaryen 200 años antes de los eventos de Game of Thrones."
    ),
    TvSeries(
      id = 46562,
      name = "The Last of Us",
      posterUrl = "https://static.tvmaze.com/uploads/images/original_untouched/444/1110052.jpg",
      currentSeason = 1,
      currentEpisode = 9,
      totalSeasons = 2,
      nextAirDate = "2025 (Temp 2)",
      airDays = listOf("Domingo"),
      airTime = "21:00",
      status = "En producción",
      network = "HBO",
      rating = 8.8,
      summary = "Veinte años después de que la civilización moderna fuera destruida, Joel es contratado para sacar a Ellie de una zona de cuarentena opresiva."
    ),
    TvSeries(
      id = 44026,
      name = "Severance",
      posterUrl = "https://static.tvmaze.com/uploads/images/original_untouched/443/1109438.jpg",
      currentSeason = 1,
      currentEpisode = 9,
      totalSeasons = 2,
      nextAirDate = "Enero 2025",
      airDays = listOf("Viernes"),
      airTime = "03:00",
      status = "En emisión",
      network = "Apple TV+",
      rating = 8.7,
      summary = "Mark lidera un equipo en Lumon Industries, cuyos empleados se han sometido a un procedimiento quirúrgico que separa sus recuerdos laborales y personales."
    )
  )

  fun getInitialSeries(): List<TvSeries> = fallbackSeries

  suspend fun searchShows(query: String): List<TvMazeSearchResult> = withContext(Dispatchers.IO) {
    if (query.isBlank()) return@withContext emptyList()
    try {
      val url = "https://api.tvmaze.com/search/shows?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
      val request = Request.Builder().url(url).build()
      val response = client.newCall(request).execute()
      val body = response.body?.string() ?: return@withContext emptyList()

      val results = mutableListOf<TvMazeSearchResult>()
      val jsonArray = JSONArray(body)
      for (i in 0 until jsonArray.length()) {
        val item = jsonArray.getJSONObject(i)
        val show = item.optJSONObject("show") ?: continue

        val id = show.optLong("id")
        val name = show.optString("name", "Desconocido")
        val premiered = show.optString("premiered", null)
        val ratingObj = show.optJSONObject("rating")
        val rating = ratingObj?.optDouble("average", 8.0) ?: 8.0

        val imageObj = show.optJSONObject("image")
        val posterUrl = imageObj?.optString("original") ?: imageObj?.optString("medium")

        val status = show.optString("status", "En emisión")
        val networkObj = show.optJSONObject("network") ?: show.optJSONObject("webChannel")
        val networkName = networkObj?.optString("name", "TV") ?: "TV"
        val rawSummary = show.optString("summary", "")
        val cleanSummary = rawSummary.replace(Regex("<[^>]*>"), "")

        // Schedule
        val sched = show.optJSONObject("schedule")
        val time = sched?.optString("time", "21:00")?.ifBlank { "21:00" } ?: "21:00"
        val daysArray = sched?.optJSONArray("days")
        val days = mutableListOf<String>()
        if (daysArray != null && daysArray.length() > 0) {
          for (d in 0 until daysArray.length()) {
            days.add(translateDay(daysArray.optString(d)))
          }
        } else {
          days.add("Domingo")
        }

        results.add(
          TvMazeSearchResult(
            showId = id,
            name = name,
            premiered = premiered,
            posterUrl = posterUrl,
            status = translateStatus(status),
            network = networkName,
            rating = rating,
            summary = cleanSummary,
            airDays = days,
            airTime = time
          )
        )
      }
      results
    } catch (e: Exception) {
      fallbackSeries.filter { it.name.contains(query, ignoreCase = true) }.map {
        TvMazeSearchResult(
          showId = it.id,
          name = it.name,
          premiered = "2022",
          posterUrl = it.posterUrl,
          status = it.status,
          network = it.network,
          rating = it.rating,
          summary = it.summary,
          airDays = it.airDays,
          airTime = it.airTime
        )
      }
    }
  }

  suspend fun fetchShowDetails(showId: Long): TvSeries? = withContext(Dispatchers.IO) {
    try {
      val url = "https://api.tvmaze.com/shows/$showId?embed[]=nextepisode&embed[]=seasons"
      val request = Request.Builder().url(url).build()
      val response = client.newCall(request).execute()
      val body = response.body?.string() ?: return@withContext null
      val show = JSONObject(body)

      val name = show.optString("name", "Serie")
      val imageObj = show.optJSONObject("image")
      val posterUrl = imageObj?.optString("original") ?: imageObj?.optString("medium")
      val ratingObj = show.optJSONObject("rating")
      val rating = ratingObj?.optDouble("average", 8.2) ?: 8.2
      val status = translateStatus(show.optString("status", "En emisión"))
      val networkObj = show.optJSONObject("network") ?: show.optJSONObject("webChannel")
      val network = networkObj?.optString("name", "TV") ?: "TV"
      val cleanSummary = show.optString("summary", "").replace(Regex("<[^>]*>"), "")

      // Schedule days & time
      val sched = show.optJSONObject("schedule")
      val time = sched?.optString("time", "21:00")?.ifBlank { "21:00" } ?: "21:00"
      val daysArray = sched?.optJSONArray("days")
      val days = mutableListOf<String>()
      if (daysArray != null && daysArray.length() > 0) {
        for (d in 0 until daysArray.length()) {
          days.add(translateDay(daysArray.optString(d)))
        }
      } else {
        days.add("Domingo")
      }

      // Check embedded next episode
      val embedded = show.optJSONObject("_embedded")
      val nextEp = embedded?.optJSONObject("nextepisode")
      val nextAirDate = if (nextEp != null) {
        val airdate = nextEp.optString("airdate", "")
        val airtime = nextEp.optString("airtime", "")
        val epSeason = nextEp.optInt("season", 1)
        val epNumber = nextEp.optInt("number", 1)
        "T$epSeason E$epNumber: $airdate $airtime".trim()
      } else {
        if (status.contains("Finalizada", true)) "Serie finalizada" else "Por confirmar"
      }

      val seasonsArray = embedded?.optJSONArray("seasons")
      val totalSeasons = seasonsArray?.length() ?: 1

      TvSeries(
        id = showId,
        name = name,
        posterUrl = posterUrl,
        currentSeason = 1,
        currentEpisode = 1,
        totalSeasons = if (totalSeasons > 0) totalSeasons else 1,
        nextAirDate = nextAirDate,
        airDays = days,
        airTime = time,
        status = status,
        network = network,
        rating = rating,
        summary = cleanSummary
      )
    } catch (e: Exception) {
      fallbackSeries.find { it.id == showId }
    }
  }

  private fun translateDay(day: String): String {
    return when (day.lowercase()) {
      "monday" -> "Lunes"
      "tuesday" -> "Martes"
      "wednesday" -> "Miércoles"
      "thursday" -> "Jueves"
      "friday" -> "Viernes"
      "saturday" -> "Sábado"
      "sunday" -> "Domingo"
      else -> day
    }
  }

  private fun translateStatus(status: String): String {
    return when (status.lowercase()) {
      "running" -> "En emisión"
      "ended" -> "Finalizada"
      "to be determined" -> "Por determinar"
      "in development" -> "En desarrollo"
      else -> status
    }
  }
}
