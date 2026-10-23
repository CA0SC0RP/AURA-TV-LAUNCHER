package com.example.data.repository

import com.example.data.model.SportCategory
import com.example.data.model.SportsEvent

class SportsRepository {

  private val eventsList = mutableListOf(
    // Soccer
    SportsEvent(
      id = "s1",
      sport = "Fútbol",
      tournament = "UEFA Champions League",
      teamHome = "Real Madrid",
      teamAway = "Manchester City",
      airDateTime = "Hoy, 21:00 CET",
      broadcaster = "Movistar+ / ESPN",
      status = "Hoy",
      isLive = false
    ),
    SportsEvent(
      id = "s2",
      sport = "Fútbol",
      tournament = "LaLiga EA Sports",
      teamHome = "FC Barcelona",
      teamAway = "Atlético de Madrid",
      airDateTime = "Sábado, 16:15 CET",
      broadcaster = "DAZN LaLiga",
      status = "Próximamente",
      isLive = false
    ),
    SportsEvent(
      id = "s3",
      sport = "Fútbol",
      tournament = "Premier League",
      teamHome = "Arsenal",
      teamAway = "Liverpool",
      airDateTime = "Domingo, 17:30 GMT",
      broadcaster = "Sky Sports / Peacock",
      status = "Próximamente",
      isLive = false
    ),
    // Basketball
    SportsEvent(
      id = "b1",
      sport = "Baloncesto",
      tournament = "NBA Season Regular",
      teamHome = "Los Angeles Lakers",
      teamAway = "Golden State Warriors",
      airDateTime = "Mañana, 04:00 CET",
      broadcaster = "NBA TV / ESPN",
      status = "En emisión hoy",
      isLive = false
    ),
    SportsEvent(
      id = "b2",
      sport = "Baloncesto",
      tournament = "EuroLeague",
      teamHome = "Panathinaikos",
      teamAway = "Real Madrid Baloncesto",
      airDateTime = "Viernes, 20:15 CET",
      broadcaster = "Movistar Deportes",
      status = "Próximamente",
      isLive = false
    ),
    // Formula 1
    SportsEvent(
      id = "f1",
      sport = "Fórmula 1",
      tournament = "Gran Premio de México",
      teamHome = "Carrera Principal",
      teamAway = "Autódromo Hermanos Rodríguez",
      airDateTime = "Domingo, 20:00 CET",
      broadcaster = "F1 TV Pro / DAZN F1",
      status = "Fin de semana",
      isLive = false
    ),
    // Baseball
    SportsEvent(
      id = "bb1",
      sport = "Béisbol",
      tournament = "MLB World Series",
      teamHome = "New York Yankees",
      teamAway = "Los Angeles Dodgers",
      airDateTime = "Viernes, 02:08 CET",
      broadcaster = "FOX Sports / ESPN",
      status = "Serie Mundial",
      isLive = false
    ),
    // NFL
    SportsEvent(
      id = "nfl1",
      sport = "NFL",
      tournament = "Sunday Night Football",
      teamHome = "Kansas City Chiefs",
      teamAway = "San Francisco 49ers",
      airDateTime = "Domingo, 02:20 CET",
      broadcaster = "NBC / ESPN",
      status = "Próximamente",
      isLive = false
    ),
    // Tennis
    SportsEvent(
      id = "t1",
      sport = "Tenis",
      tournament = "ATP Masters 1000",
      teamHome = "Carlos Alcaraz",
      teamAway = "Jannik Sinner",
      airDateTime = "Sábado, 15:00 CET",
      broadcaster = "Tennis Channel / Eurosport",
      status = "Semifinal",
      isLive = false
    )
  )

  fun getAllEvents(): List<SportsEvent> = eventsList.toList()

  fun getEventsBySport(sport: String): List<SportsEvent> {
    if (sport.isBlank() || sport.equals("Todos", ignoreCase = true)) {
      return getAllEvents()
    }
    if (sport.equals("NBA", ignoreCase = true)) {
      return eventsList.filter {
        it.tournament.contains("NBA", true) ||
        it.sport.contains("Baloncesto", true) ||
        it.tournament.contains("EuroLeague", true)
      }
    }
    return eventsList.filter {
      it.sport.contains(sport, ignoreCase = true) ||
      it.tournament.contains(sport, ignoreCase = true) ||
      it.teamHome.contains(sport, ignoreCase = true) ||
      it.teamAway.contains(sport, ignoreCase = true)
    }
  }

  fun addCustomEvent(
    sport: String,
    tournament: String,
    teamHome: String,
    teamAway: String,
    airDateTime: String,
    broadcaster: String
  ) {
    eventsList.add(
      0,
      SportsEvent(
        id = "custom_${System.currentTimeMillis()}",
        sport = sport,
        tournament = tournament,
        teamHome = teamHome,
        teamAway = teamAway,
        airDateTime = airDateTime,
        broadcaster = broadcaster,
        status = "Programado"
      )
    )
  }
}
