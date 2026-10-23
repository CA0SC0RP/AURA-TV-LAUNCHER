package com.example.data.model

data class WallpaperItem(
  val id: String,
  val name: String,
  val subtitle: String,
  val drawableResName: String? = null,
  val imageUrl: String? = null,
  val gradientColors: List<Long>? = null
)
