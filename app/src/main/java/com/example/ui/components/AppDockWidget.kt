package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppItem
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TvBorder
import com.example.ui.theme.TvBorderFocused
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun AppDockWidget(
  pinnedApps: List<AppItem>,
  allInstalledApps: List<AppItem>,
  onLaunchApp: (String) -> Unit,
  onTogglePinApp: (String, Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  var showManagerDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Minimal icon-only dock: up to six favorite apps plus the add button.
    LazyRow(
      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      items(pinnedApps.take(6), key = { it.packageName }) { app ->
        AppDockItem(
          app = app,
          onClick = { onLaunchApp(app.packageName) }
        )
      }

      // Add / Edit Button
      item {
        TvCard(
          onClick = { showManagerDialog = true },
          modifier = Modifier
            .width(56.dp)
            .height(56.dp),
          shape = RoundedCornerShape(10.dp),
          backgroundColor = TvSurface.copy(alpha = 0.6f),
          focusedBorderColor = NeonCyan,
          focusedScale = 1.08f
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(8.dp)
              .clip(CircleShape)
              .background(NeonCyan.copy(alpha = 0.12f))
              .border(1.dp, NeonCyan.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Agregar aplicaciones",
              tint = NeonCyan,
              modifier = Modifier.size(26.dp)
            )
          }
        }
      }
    }
  }

  // App Manager Modal
  if (showManagerDialog) {
    AppManagerDialog(
      allApps = allInstalledApps,
      onDismiss = { showManagerDialog = false },
      onTogglePin = onTogglePinApp,
      onLaunch = { pkg ->
        onLaunchApp(pkg)
        showManagerDialog = false
      }
    )
  }
}

@Composable
fun AppDockItem(
  app: AppItem,
  onClick: () -> Unit
) {
  TvCard(
    onClick = onClick,
    modifier = Modifier
      .width(56.dp)
      .height(56.dp),
    focusedBorderColor = NeonCyan,
    focusedScale = 1.10f,
    shape = RoundedCornerShape(14.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(5.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(getAppGradient(app.defaultIconType)),
      contentAlignment = Alignment.Center
    ) {
      AppIcon(app = app, modifier = Modifier.fillMaxSize())
    }
  }
}

@Composable
private fun AppIcon(
  app: AppItem,
  modifier: Modifier = Modifier
) {
  val iconBitmap = remember(app.icon) {
    app.icon?.let { drawable ->
      runCatching {
        val width = drawable.intrinsicWidth.coerceIn(1, 256)
        val height = drawable.intrinsicHeight.coerceIn(1, 256)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        bitmap.asImageBitmap()
      }.getOrNull()
    }
  }

  if (iconBitmap != null) {
    Image(
      bitmap = iconBitmap,
      contentDescription = app.label,
      modifier = modifier,
      contentScale = androidx.compose.ui.layout.ContentScale.Fit
    )
  } else {
    Icon(
      imageVector = getAppIconVector(app.defaultIconType),
      contentDescription = app.label,
      tint = Color.White,
      modifier = modifier.padding(8.dp)
    )
  }
}

private fun getAppGradient(iconType: String): Brush {
  return when (iconType) {
    "youtube" -> Brush.linearGradient(listOf(Color(0xFFFF0000), Color(0xFFCC0000)))
    "netflix" -> Brush.linearGradient(listOf(Color(0xFFE50914), Color(0xFF8B0000)))
    "prime" -> Brush.linearGradient(listOf(Color(0xFF00A8E1), Color(0xFF007399)))
    "disney" -> Brush.linearGradient(listOf(Color(0xFF113CCF), Color(0xFF0B227B)))
    "max" -> Brush.linearGradient(listOf(Color(0xFF002BE7), Color(0xFF5600E8)))
    "plex" -> Brush.linearGradient(listOf(Color(0xFFE5A00D), Color(0xFFCC7A00)))
    "spotify" -> Brush.linearGradient(listOf(Color(0xFF1DB954), Color(0xFF14833B)))
    "twitch" -> Brush.linearGradient(listOf(Color(0xFF9146FF), Color(0xFF6441A5)))
    "games" -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
    "store" -> Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF0284C7)))
    "settings" -> Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF475569)))
    "browser" -> Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
    else -> Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF3730A3)))
  }
}

private fun getAppIconVector(iconType: String): ImageVector {
  return when (iconType) {
    "youtube" -> Icons.Default.PlayCircle
    "netflix", "prime", "disney", "max", "plex" -> Icons.Default.Tv
    "spotify" -> Icons.Default.MusicNote
    "twitch" -> Icons.Default.PlayArrow
    "games" -> Icons.Default.SportsEsports
    "store" -> Icons.Default.ShoppingBag
    "settings" -> Icons.Default.Settings
    "browser" -> Icons.Default.Language
    else -> Icons.Default.Apps
  }
}

@Composable
fun AppManagerDialog(
  allApps: List<AppItem>,
  onDismiss: () -> Unit,
  onTogglePin: (String, Boolean) -> Unit,
  onLaunch: (String) -> Unit
) {
  var filterQuery by remember { mutableStateOf("") }
  var limitMessage by remember { mutableStateOf<String?>(null) }
  val filtered = allApps.filter { it.label.contains(filterQuery, ignoreCase = true) }

  TvModalOverlay(onDismiss = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .height(320.dp),
      shape = RoundedCornerShape(16.dp),
      color = TvSurfaceElevated,
      border = androidx.compose.foundation.BorderStroke(1.dp, TvBorderFocused)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 18.dp, vertical = 12.dp)
      ) {
        // Title & Close
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Gestor de Aplicaciones del Launcher",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Marca las casillas para agregar o quitar aplicaciones de la barra",
              color = TvTextSecondary,
              fontSize = 10.sp
            )
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TvTextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = filterQuery,
          onValueChange = { filterQuery = it },
          placeholder = { Text("Filtrar aplicaciones...", fontSize = 11.sp) },
          modifier = Modifier.fillMaxWidth().height(44.dp),
          singleLine = true,
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TvTextSecondary, modifier = Modifier.size(16.dp)) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = TvBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )

        limitMessage?.let { message ->
          Text(
            text = message,
            color = NeonCyan,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          items(filtered, key = { it.packageName }) { app ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.25f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(getAppGradient(app.defaultIconType)),
                  contentAlignment = Alignment.Center
                ) {
                  AppIcon(app = app, modifier = Modifier.fillMaxSize().padding(2.dp))
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                  Text(
                    text = app.label,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                  )
                  Text(
                    text = app.packageName,
                    color = TvTextSecondary,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                  onClick = { onLaunch(app.packageName) },
                  colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                  modifier = Modifier.height(26.dp),
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text("Abrir", color = NeonCyan, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Checkbox(
                  checked = app.isPinned,
                  onCheckedChange = { checked ->
                    if (checked && allApps.count { it.isPinned } >= 6) {
                      limitMessage = "Puedes mostrar hasta 6 aplicaciones. Quita una para agregar otra."
                    } else {
                      limitMessage = null
                      onTogglePin(app.packageName, checked)
                    }
                  },
                  colors = CheckboxDefaults.colors(
                    checkedColor = NeonCyan,
                    checkmarkColor = Color.Black,
                    uncheckedColor = TvBorder
                  )
                )
              }
            }
          }
        }
      }
    }
  }
}
