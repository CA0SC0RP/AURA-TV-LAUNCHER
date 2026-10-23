package com.example.data.model

data class SportsEvent(
  val id: String,
  val sport: String,
  val tournament: String,
  val teamHome: String,
  val teamAway: String,
  val airDateTime: String,
  val broadcaster: String,
  val status: String, // "Próximamente", "En Vivo", "Hoy"
  val isLive: Boolean = false
)

enum class SportCategory(val displayName: String, val iconName: String) {
  ALL("Todos", "sports"),
  SOCCER("Fútbol", "sports_soccer"),
  BASKETBALL("Baloncesto", "sports_basketball"),
  F1("Fórmula 1", "sports_motorsports"),
  BASEBALL("Béisbol", "sports_baseball"),
  NFL("NFL", "sports_football"),
  TENNIS("Tenis", "sports_tennis")
}
