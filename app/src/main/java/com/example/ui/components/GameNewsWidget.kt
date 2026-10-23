package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Surface
import coil.compose.AsyncImage
import com.example.data.model.GameNewsItem
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRose
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvBorderFocused
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay

@Composable
fun GameNewsWidget(
  newsList: List<GameNewsItem>,
  modifier: Modifier = Modifier
) {
  var currentIndex by remember { mutableIntStateOf(0) }
  var selectedNews by remember { mutableStateOf<GameNewsItem?>(null) }

  // Auto-pass ticker every 6.5 seconds
  LaunchedEffect(newsList.size) {
    if (newsList.isNotEmpty()) {
      while (true) {
        delay(6500)
        currentIndex = (currentIndex + 1) % newsList.size
      }
    }
  }

  val currentItem = if (newsList.isNotEmpty()) newsList[currentIndex % newsList.size] else null

  TvCard(
    onClick = { currentItem?.let { selectedNews = it } },
    modifier = modifier,
    backgroundColor = TvSurface.copy(alpha = 0.88f),
    focusedBorderColor = NeonPurple,
    focusedScale = 1.04f
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(10.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Gamepad,
            contentDescription = null,
            tint = NeonPurple,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Videojuegos",
            color = TvTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "Sólo Información",
            color = TvTextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // Animated News Content
      if (currentItem != null) {
        AnimatedContent(
          targetState = currentItem,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "game_news_anim",
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) { game ->
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            AsyncImage(
              model = gameNewsImage(game.id),
              contentDescription = game.title,
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp, max = 82.dp)
                .clip(RoundedCornerShape(8.dp)),
              contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.clickable { selectedNews = game }) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = game.title,
                  color = Color.White,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f)
                )

                game.highlightBadge?.let { badge ->
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(NeonPurple.copy(alpha = 0.3f))
                      .padding(horizontal = 5.dp, vertical = 1.dp)
                  ) {
                    Text(
                      text = badge,
                      color = NeonPurple,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(2.dp))

              Text(
                text = "${game.publisher} • Estreno: ${game.releaseDate}",
                color = NeonRose,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
              )

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = game.summary,
                color = TvTextSecondary,
                fontSize = 11.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
              )
            }

            // Platform badges
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              game.platforms.forEach { platform ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = platform,
                    color = NeonBlue,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }

      // Bottom Navigation Dots & Ticker manual controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Ticker Dots
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          repeat(minOf(newsList.size, 6)) { idx ->
            val isActive = (currentIndex % newsList.size) == idx
            Box(
              modifier = Modifier
                .size(if (isActive) 12.dp else 5.dp, 5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (isActive) NeonPurple else Color.White.copy(alpha = 0.2f))
            )
          }
        }

        // Left / Right manual arrows
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Box(
            modifier = Modifier
              .size(22.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.15f))
              .clickable {
                if (newsList.isNotEmpty()) {
                  currentIndex = if (currentIndex > 0) currentIndex - 1 else newsList.size - 1
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior", tint = Color.White, modifier = Modifier.size(16.dp))
          }

          Box(
            modifier = Modifier
              .size(22.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.15f))
              .clickable {
                if (newsList.isNotEmpty()) {
                  currentIndex = (currentIndex + 1) % newsList.size
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente", tint = Color.White, modifier = Modifier.size(16.dp))
          }
        }
      }
    }
  }
  selectedNews?.let { game ->
    BackHandler(enabled = true) { selectedNews = null }
    GameNewsDetailDialog(
      game = game,
      onDismiss = { selectedNews = null }
    )
  }
}

private fun gameNewsImage(id: String): String = when (id) {
  "gta6" -> "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=1200&q=85"
  "mh_wilds" -> "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=1200&q=85"
  "metroid_prime_4" -> "https://images.unsplash.com/photo-1493711662062-fa541372b786?w=1200&q=85"
  "death_stranding_2" -> "https://images.unsplash.com/photo-1547394765-185e1e68f34e?w=1200&q=85"
  "witcher_4" -> "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&q=85"
  "kcd2" -> "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=1200&q=85"
  else -> "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=1200&q=85"
}

@Composable
private fun GameNewsDetailDialog(
  game: GameNewsItem,
  onDismiss: () -> Unit
) {
  TvModalOverlay(onDismiss = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.88f)
        .heightIn(min = 300.dp, max = 420.dp),
      shape = RoundedCornerShape(18.dp),
      color = TvSurfaceElevated,
      border = androidx.compose.foundation.BorderStroke(1.5.dp, TvBorderFocused)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "NOTICIAS DE VIDEOJUEGOS",
            color = NeonPurple,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          androidx.compose.material3.IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar noticia", tint = TvTextSecondary)
          }
        }

        AsyncImage(
          model = gameNewsImage(game.id),
          contentDescription = game.title,
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 110.dp, max = 170.dp)
            .clip(RoundedCornerShape(10.dp)),
          contentScale = ContentScale.Crop
        )

        Text(
          text = game.title,
          color = Color.White,
          fontSize = 21.sp,
          fontWeight = FontWeight.ExtraBold,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Text(
          text = "${game.publisher}  •  ${game.releaseDate}",
          color = NeonRose,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )

        Text(
          text = game.summary,
          color = TvTextPrimary,
          fontSize = 14.sp,
          lineHeight = 20.sp
        )

        Text(
          text = "Plataformas: ${game.platforms.joinToString(" · ")}",
          color = NeonBlue,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
