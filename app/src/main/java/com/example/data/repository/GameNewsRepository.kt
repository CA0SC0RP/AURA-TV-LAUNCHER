package com.example.data.repository

import android.util.Xml
import com.example.data.model.GameNewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Aggregates real articles from official platform blogs and established gaming outlets.
 * No hard-coded article or placeholder image is returned when feeds are unavailable.
 */
class GameNewsRepository {

  private data class FeedSource(val name: String, val url: String, val platform: String)

  private val sources = listOf(
    FeedSource("PlayStation Blog", "https://blog.playstation.com/feed/", "PlayStation"),
    FeedSource("Xbox Wire", "https://news.xbox.com/en-us/feed/", "Xbox"),
    FeedSource("Nintendo Life", "https://www.nintendolife.com/feeds/latest", "Nintendo"),
    FeedSource("IGN", "https://www.ign.com/rss/v2/articles/feed", "Videojuegos"),
    FeedSource("GameSpot", "https://www.gamespot.com/feeds/news/", "Videojuegos")
  )

  private val client = OkHttpClient.Builder()
    .connectTimeout(8, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .callTimeout(12, TimeUnit.SECONDS)
    .build()

  suspend fun getGameNews(): List<GameNewsItem> = withContext(Dispatchers.IO) {
    val articles = mutableListOf<GameNewsItem>()
    for (source in sources) {
      try {
        val request = Request.Builder()
          .url(source.url)
          .header("User-Agent", "AURA-TV-Launcher/1.0 (RSS reader)")
          .header("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml")
          .build()
        client.newCall(request).execute().use { response ->
          if (response.isSuccessful) {
            val xml = response.body?.string().orEmpty()
            if (xml.isNotBlank()) articles += parseFeed(xml, source)
          }
        }
      } catch (_: Exception) {
        // A failed source is skipped; other feeds remain usable.
      }
    }

    articles
      .filter { it.title.isNotBlank() && !it.articleUrl.isNullOrBlank() }
      .distinctBy { it.articleUrl!!.substringBefore("#").trimEnd('/') }
      .sortedByDescending { it.publishedAt?.let(::parseDateMillis) ?: 0L }
      .take(30)
  }

  private fun parseFeed(xml: String, source: FeedSource): List<GameNewsItem> {
    val parser = Xml.newPullParser()
    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
    parser.setInput(StringReader(xml))
    val results = mutableListOf<GameNewsItem>()

    var event = parser.eventType
    var inEntry = false
    var depth = 0
    var title = ""
    var link = ""
    var description = ""
    var image = ""
    var published = ""
    var currentTag = ""

    fun resetEntry() {
      title = ""
      link = ""
      description = ""
      image = ""
      published = ""
    }

    while (event != XmlPullParser.END_DOCUMENT) {
      when (event) {
        XmlPullParser.START_TAG -> {
          val tag = parser.name.substringAfter(':').lowercase(Locale.ROOT)
          if (tag == "item" || tag == "entry") {
            inEntry = true
            depth = 1
            resetEntry()
          } else if (inEntry) {
            depth++
            currentTag = tag
            when (tag) {
              "link" -> {
                val href = parser.getAttributeValue(null, "href")
                  ?: parser.getAttributeValue("http://www.w3.org/1999/xlink", "href")
                if (!href.isNullOrBlank()) link = href
                val rel = parser.getAttributeValue(null, "rel")
                if (rel == "enclosure") {
                  val type = parser.getAttributeValue(null, "type").orEmpty()
                  val url = parser.getAttributeValue(null, "href").orEmpty()
                  if (type.startsWith("image/")) image = url
                }
              }
              "enclosure", "thumbnail", "content" -> {
                val url = parser.getAttributeValue(null, "url")
                  ?: parser.getAttributeValue(null, "href")
                  ?: parser.getAttributeValue("http://search.yahoo.com/mrss/", "url")
                val type = parser.getAttributeValue(null, "type").orEmpty()
                if (!url.isNullOrBlank() && (tag != "enclosure" || type.startsWith("image/") || type.isBlank())) image = url
              }
            }
          }
        }
        XmlPullParser.TEXT, XmlPullParser.CDSECT -> {
          if (inEntry) {
            val value = parser.text.orEmpty().trim()
            when (currentTag) {
              "title" -> if (title.isBlank()) title = value
              "description", "summary", "encoded", "content" -> if (value.length > description.length) description = value
              "pubdate", "published", "updated", "date" -> if (published.isBlank()) published = value
              "link" -> if (link.isBlank() && value.startsWith("http")) link = value
            }
          }
        }
        XmlPullParser.END_TAG -> {
          val tag = parser.name.substringAfter(':').lowercase(Locale.ROOT)
          if (inEntry && (tag == "item" || tag == "entry")) {
            if (title.isNotBlank() && link.startsWith("http")) {
              val cleanSummary = cleanHtml(description).take(360)
              val discoveredImage = image.ifBlank { extractImage(description) }
              val date = published.ifBlank { "Fecha no disponible" }
              results += GameNewsItem(
                id = link.hashCode().toString(),
                title = cleanHtml(title),
                releaseDate = date,
                platforms = listOf(source.platform),
                genre = "Noticia",
                summary = cleanSummary.ifBlank { "Abre el artículo original para consultar todos los detalles." },
                publisher = source.name,
                highlightBadge = source.platform,
                articleUrl = link,
                imageUrl = discoveredImage.takeIf { it.startsWith("http") },
                publishedAt = date
              )
            }
            inEntry = false
            depth = 0
            currentTag = ""
          } else if (inEntry) {
            depth = (depth - 1).coerceAtLeast(1)
            currentTag = ""
          }
        }
      }
      event = parser.nextTokenSafely()
    }
    return results
  }

  private fun cleanHtml(value: String): String =
    value.replace(Regex("(?is)<script[^>]*>.*?</script>"), " ")
      .replace(Regex("(?is)<style[^>]*>.*?</style>"), " ")
      .replace(Regex("(?i)<br\\s*/?>|</p>|</div>"), " ")
      .replace(Regex("<[^>]+>"), " ")
      .replace("&nbsp;", " ")
      .replace("&amp;", "&")
      .replace("&quot;", "\"")
      .replace("&#39;", "'")
      .replace("&lt;", "<")
      .replace("&gt;", ">")
      .replace(Regex("\\s+"), " ")
      .trim()

  private fun extractImage(html: String): String =
    Regex("""(?is)<img[^>]+src=["']([^"']+)["']""").find(html)?.groupValues?.getOrNull(1).orEmpty()

  private fun parseDateMillis(value: String): Long {
    val patterns = listOf(
      "EEE, dd MMM yyyy HH:mm:ss Z",
      "EEE, d MMM yyyy HH:mm:ss Z",
      "yyyy-MM-dd'T'HH:mm:ssXXX",
      "yyyy-MM-dd'T'HH:mm:ss'Z'",
      "yyyy-MM-dd"
    )
    for (pattern in patterns) {
      try {
        val formatter = SimpleDateFormat(pattern, Locale.US)
        formatter.isLenient = true
        val date: Date? = formatter.parse(value)
        if (date != null) return date.time
      } catch (_: Exception) {
        // Try the next common RSS date format.
      }
    }
    return 0L
  }

  private fun XmlPullParser.nextTokenSafely(): Int =
    try { nextToken() } catch (_: Exception) { next() }
}
