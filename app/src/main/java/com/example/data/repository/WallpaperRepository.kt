package com.example.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Downloads real, high-resolution space imagery from NASA's public Image and Video Library.
 * The last successful image is stored locally so the launcher can keep showing it offline.
 */
class WallpaperRepository(context: Context) {
  data class CachedWallpaper(val nasaId: String, val uri: String)

  private val appContext = context.applicationContext
  private val prefs = appContext.getSharedPreferences("aura_space_wallpaper", Context.MODE_PRIVATE)
  private val cacheFile = File(appContext.filesDir, "space_wallpaper.jpg")
  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .callTimeout(45, TimeUnit.SECONDS)
    .build()

  fun cachedWallpaperUri(): String? =
    if (cacheFile.exists() && cacheFile.length() > 100_000L) Uri.fromFile(cacheFile).toString() else null

  fun lastUpdatedAt(): Long = prefs.getLong(KEY_UPDATED_AT, 0L)
  fun lastNasaId(): String = prefs.getString(KEY_NASA_ID, "") ?: ""

  suspend fun downloadNewWallpaper(): CachedWallpaper? = withContext(Dispatchers.IO) {
    val queries = listOf("nebula", "galaxy", "planet", "deep space", "star cluster", "supernova")
    val candidates = mutableListOf<Pair<String, String>>()

    for (query in queries.shuffled()) {
      val searchUrl = "https://images-api.nasa.gov/search?q=" +
        java.net.URLEncoder.encode(query, "UTF-8") + "&media_type=image&page_size=50"
      val searchJson = getJson(searchUrl) ?: continue
      val items = searchJson.optJSONObject("collection")?.optJSONArray("items") ?: continue
      for (i in 0 until items.length()) {
        val item = items.optJSONObject(i) ?: continue
        val data = item.optJSONArray("data")?.optJSONObject(0) ?: continue
        val id = data.optString("nasa_id", "")
        val title = data.optString("title", "").lowercase()
        val assetUrl = item.optString("href", "")
        if (id.isBlank() || assetUrl.isBlank() || id == lastNasaId()) continue
        if (data.optString("media_type") != "image") continue
        // Keep the collection focused on astronomical subjects, not general NASA photography.
        if (queries.none { title.contains(it) } &&
          listOf("space", "nebula", "galaxy", "planet", "star", "cosmic", "universe", "supernova", "cluster").none { title.contains(it) }) continue
        candidates.add(id to assetUrl)
      }
      if (candidates.size >= 12) break
    }

    if (candidates.isEmpty()) return@withContext null

    for ((id, assetUrl) in candidates.shuffled().take(12)) {
      val assets = getJson(assetUrl) ?: continue
      val files = assets.optJSONArray("collection")?.optJSONArray("items") ?: continue
      val imageUrls = (0 until files.length()).mapNotNull { index ->
        files.optJSONObject(index)?.optString("href")?.takeIf {
          it.startsWith("https://") &&
            (it.substringBefore('?').endsWith(".jpg", true) ||
              it.substringBefore('?').endsWith(".jpeg", true) ||
              it.substringBefore('?').endsWith(".png", true))
        }
      }
      // Prefer NASA's original-size asset and avoid thumbnails/preview files.
      val chosen = imageUrls.firstOrNull { it.contains("~orig", true) }
        ?: imageUrls.filterNot { it.contains("thumb", true) || it.contains("small", true) }
          .maxByOrNull { resolutionRank(it) }
        ?: continue

      val request = Request.Builder().url(chosen).header("User-Agent", "AURA-TV-Launcher/1.0").build()
      try {
        client.newCall(request).execute().use { response ->
          if (!response.isSuccessful) return@use
          val body = response.body ?: return@use
          if (body.contentLength() > 0 && body.contentLength() < MIN_IMAGE_BYTES) return@use
          val tempFile = File(appContext.filesDir, "space_wallpaper.tmp")
          body.byteStream().use { input ->
            FileOutputStream(tempFile).use { output -> input.copyTo(output) }
          }
          if (tempFile.length() < MIN_IMAGE_BYTES) {
            tempFile.delete()
            return@use
          }
          if (tempFile.renameTo(cacheFile) || tempFile.copyTo(cacheFile, overwrite = true).exists()) {
            tempFile.delete()
            prefs.edit()
              .putString(KEY_NASA_ID, id)
              .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
              .apply()
            return@withContext CachedWallpaper(id, Uri.fromFile(cacheFile).toString())
          }
          tempFile.delete()
        }
      } catch (_: Exception) {
        // Try the next NASA asset; keep the previous cached wallpaper untouched.
      }
    }
    null
  }

  private fun getJson(url: String): JSONObject? {
    return try {
      val request = Request.Builder().url(url).header("User-Agent", "AURA-TV-Launcher/1.0").build()
      client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) null else response.body?.string()?.let(::JSONObject)
      }
    } catch (_: Exception) {
      null
    }
  }

  private fun resolutionRank(url: String): Int {
    val name = url.substringAfterLast('/').lowercase()
    return when {
      name.contains("orig") -> 100
      name.contains("large") -> 80
      name.contains("medium") -> 40
      name.contains("small") || name.contains("thumb") -> 1
      else -> 60
    }
  }

  companion object {
    const val ROTATION_INTERVAL_MS: Long = 12 * 60 * 60 * 1000L
    private const val KEY_UPDATED_AT = "last_successful_download_at"
    private const val KEY_NASA_ID = "last_nasa_asset_id"
    private const val MIN_IMAGE_BYTES = 100_000L
  }
}