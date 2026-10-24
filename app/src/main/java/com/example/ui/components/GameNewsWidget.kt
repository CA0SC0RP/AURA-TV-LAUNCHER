package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.GameNewsItem
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRose
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay

@Composable
fun GameNewsWidget(
  newsList: List<GameNewsItem>,
  modifier: Modifier = Modifier
) {
  var currentIndex by remember { mutableIntStateOf(0) }

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
    modifier = modifier,
    backgroundColor = TvSurface.copy(alpha = 0.82f),
    focusedBorderColor = NeonPurple
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
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = game.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(modifier = Modifier.height(2.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                game.highlightBadge?.let { badge ->
                  Text(
                    text = badge,
                    color = NeonPurple,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                  )
                  Text(
                    text = "  ·  ",
                    color = TvTextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1
                  )
                }
                Text(
                  text = "Estreno: ${game.releaseDate}",
                  color = NeonRose,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f, fill = false)
                )
              }

              Spacer(modifier = Modifier.height(3.dp))

              Text(
                text = game.summary,
                color = TvTextSecondary,
                fontSize = 10.sp,
                maxLines = 2,
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
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
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
              .size(26.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.15f))
              .tvClickable(shape = CircleShape, focusColor = NeonPurple) {
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
              .size(26.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.15f))
              .tvClickable(shape = CircleShape, focusColor = NeonPurple) {
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
}
