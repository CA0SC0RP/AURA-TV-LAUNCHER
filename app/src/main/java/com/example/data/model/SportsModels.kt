package com.example.data.model

data class SportsEvent(
  val id: String,
  val sport: String,
  val tournament: String,
  val teamHome: String,
  val teamAway: String,
  val airDateTime: String,
  val broadcaster: String,
  val status: String,
  val isLive: Boolean = false,
  val venue: String? = null
)

enum class SportCategory(val displayName: String, val iconName: String) {
  ALL("Todos", "sports"),
  SOCCER("Fútbol", "sports_soccer"),
  BASKETBALL("NBA", "sports_basketball"),
  F1("Fórmula 1", "sports_motorsports"),
  NFL("NFL", "sports_football")
}
