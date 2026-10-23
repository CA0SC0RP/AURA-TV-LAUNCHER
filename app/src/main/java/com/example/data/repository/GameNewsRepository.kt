package com.example.data.repository

import com.example.data.model.GameNewsItem

class GameNewsRepository {

  private val gamesNews = listOf(
    GameNewsItem(
      id = "gta6",
      title = "Grand Theft Auto VI",
      releaseDate = "Otoño 2025",
      platforms = listOf("PS5", "Xbox Series X|S"),
      genre = "Acción / Mundo Abierto",
      publisher = "Rockstar Games",
      summary = "Regreso al estado de Leonida y las calles iluminadas por neón de Vice City con los protagonistas Lucía y Jason.",
      highlightBadge = "Más Esperado"
    ),
    GameNewsItem(
      id = "mh_wilds",
      title = "Monster Hunter Wilds",
      releaseDate = "28 Feb 2025",
      platforms = listOf("PS5", "Xbox Series", "PC"),
      genre = "Action RPG",
      publisher = "Capcom",
      summary = "Ecosistemas dinámicos con manadas feroces y clima extremo en las Tierras Prohibidas. Soporte cross-play total.",
      highlightBadge = "Próximo Lanzamiento"
    ),
    GameNewsItem(
      id = "metroid_prime_4",
      title = "Metroid Prime 4: Beyond",
      releaseDate = "2025",
      platforms = listOf("Nintendo Switch"),
      genre = "Aventura en 1ª Persona",
      publisher = "Nintendo / Retro Studios",
      summary = "Samus Aran se embarca en una nueva misión estelar enfrentándose al misterioso cazarrecompensas Sylux.",
      highlightBadge = "Exclusivo"
    ),
    GameNewsItem(
      id = "death_stranding_2",
      title = "Death Stranding 2: On the Beach",
      releaseDate = "2025",
      platforms = listOf("PS5"),
      genre = "Aventura Cinematográfica",
      publisher = "Kojima Productions / Sony",
      summary = "Sam Porter Bridges emprende un viaje más allá de la UCA a bordo del Magellan con tecnología háptica de última generación.",
      highlightBadge = "Cinemático"
    ),
    GameNewsItem(
      id = "witcher_4",
      title = "The Witcher: Polaris",
      releaseDate = "En desarrollo (Unreal 5)",
      platforms = listOf("PC", "PS5", "Xbox"),
      genre = "RPG Fantasía Oscura",
      publisher = "CD Projekt RED",
      summary = "Comienzo de una nueva saga en el universo de Geralt de Rivia, explorando la Escuela del Lince con gráficos fotorealistas.",
      highlightBadge = "En Desarrollo"
    ),
    GameNewsItem(
      id = "kcd2",
      title = "Kingdom Come: Deliverance II",
      releaseDate = "Febrero 2025",
      platforms = listOf("PC", "PS5", "Xbox"),
      genre = "RPG Histórico",
      publisher = "Warhorse Studios",
      summary = "Henry de Skalitz regresa en la Bohemia del siglo XV con un mapa el doble de grande y combate medieval realista.",
      highlightBadge = "Próximo"
    ),
    GameNewsItem(
      id = "tes6",
      title = "The Elder Scrolls VI",
      releaseDate = "En producción",
      platforms = listOf("PC", "Xbox Series X|S"),
      genre = "RPG Épico",
      publisher = "Bethesda Game Studios",
      summary = "La esperada continuación de Skyrim situada en Hammerfell y High Rock con nueva generación del motor Creation Engine 2.",
      highlightBadge = "Alta Expectativa"
    )
  )

  fun getGameNews(): List<GameNewsItem> = gamesNews
}
