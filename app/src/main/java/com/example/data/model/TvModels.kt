package com.example.data.model

data class TvSeries(
  val id: Long,
  val name: String,
  val posterUrl: String?,
  val currentSeason: Int = 1,
  val currentEpisode: Int = 1,
  val totalSeasons: Int = 1,
  val nextAirDate: String? = null,
  val airDays: List<String> = listOf("Domingo"),
  val airTime: String = "21:00",
  val status: String = "En emisión",
  val network: String = "TV",
  val rating: Double = 8.5,
  val summary: String = ""
)

data class TvMazeSearchResult(
  val showId: Long,
  val name: String,
  val premiered: String?,
  val posterUrl: String?,
  val status: String?,
  val network: String?,
  val rating: Double?,
  val summary: String?,
  val airDays: List<String> = listOf("Domingo"),
  val airTime: String = "21:00"
)
