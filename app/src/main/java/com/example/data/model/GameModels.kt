package com.example.data.model

data class GameNewsItem(
  val id: String,
  val title: String,
  val releaseDate: String,
  val platforms: List<String>,
  val genre: String,
  val summary: String,
  val publisher: String,
  val highlightBadge: String? = null
)
