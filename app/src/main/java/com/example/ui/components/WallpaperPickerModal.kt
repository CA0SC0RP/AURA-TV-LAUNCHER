package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WallpaperItem
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TvBorder
import com.example.ui.theme.TvBorderFocused
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun WallpaperPickerModal(
  currentWallpaperId: String,
  onDismiss: () -> Unit,
  onSelectWallpaper: (WallpaperItem) -> Unit,
  onSetCustomUrl: (String) -> Unit
) {
  val wallpapers = listOf(
    WallpaperItem(
      id = "cosmic_nebula",
      name = "Nebulosa Cósmica 4K",
      subtitle = "Generado por IA Studio",
      drawableResName = "wallpaper_cosmic_4k_1792767721304"
    ),
    WallpaperItem(
      id = "aurora_borealis",
      name = "Aurora Boreal Nórdica",
      subtitle = "Tonos Esmeralda y Cian",
      gradientColors = listOf(0xFF022C22, 0xFF064E3B, 0xFF082F49, 0xFF020617)
    ),
    WallpaperItem(
      id = "cyberpunk_neon",
      name = "Cyberpunk Neo-Tokyo",
      subtitle = "Púrpura y Neón Profundo",
      gradientColors = listOf(0xFF3B0764, 0xFF581C87, 0xFF1E1B4B, 0xFF090D16)
    ),
    WallpaperItem(
      id = "deep_abyss",
      name = "Océano Abisal",
      subtitle = "Azul Zafiro Marino",
      gradientColors = listOf(0xFF082F49, 0xFF0C4A6E, 0xFF031D38, 0xFF020617)
    ),
    WallpaperItem(
      id = "sunset_minimal",
      name = "Atardecer Crepuscular",
      subtitle = "Ámbar y Escarlata Suave",
      gradientColors = listOf(0xFF7C2D12, 0xFF431407, 0xFF180A0A, 0xFF050505)
    ),
    WallpaperItem(
      id = "pure_oled_black",
      name = "Negro Puro OLED",
      subtitle = "Máximo Contraste y Ahorro",
      gradientColors = listOf(0xFF000000, 0xFF050505, 0xFF000000)
    )
  )

  var customUrlInput by remember { mutableStateOf("") }

  // Photo Picker launcher for TV/Storage/USB
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      onSetCustomUrl(uri.toString())
    }
  }

  TvModalOverlay(onDismiss = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .height(330.dp),
      shape = RoundedCornerShape(16.dp),
      color = TvSurfaceElevated,
      border = androidx.compose.foundation.BorderStroke(1.dp, TvBorderFocused)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 18.dp, vertical = 12.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Wallpaper, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Fondo de Pantalla 4K",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Elige un fondo preset, sube tu foto o introduce una URL",
                color = TvTextSecondary,
                fontSize = 10.sp
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            // Button to pick photo from local storage/USB
            Button(
              onClick = {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
              modifier = Modifier.height(32.dp),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Elegir Foto / USB", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TvTextSecondary)
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid of Presets (2 rows of 3)
        LazyVerticalGrid(
          columns = GridCells.Fixed(3),
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(wallpapers) { wp ->
            val isSelected = wp.id == currentWallpaperId
            Box(
              modifier = Modifier
                .height(72.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                  width = if (isSelected) 2.dp else 1.dp,
                  color = if (isSelected) NeonCyan else TvBorder,
                  shape = RoundedCornerShape(10.dp)
                )
                .background(
                  if (wp.gradientColors != null) {
                    Brush.verticalGradient(wp.gradientColors.map { Color(it) })
                  } else {
                    Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFFA855F7), Color(0xFF0F172A)))
                  }
                )
                .clickable { onSelectWallpaper(wp) }
                .padding(8.dp)
            ) {
              if (isSelected) {
                Box(
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(NeonCyan),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                }
              }

              Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                  text = wp.name,
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1
                )
                Text(
                  text = wp.subtitle,
                  color = Color.White.copy(alpha = 0.7f),
                  fontSize = 9.sp,
                  maxLines = 1
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Custom URL input
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = customUrlInput,
            onValueChange = { customUrlInput = it },
            placeholder = { Text("O introduce URL de foto 4K (https://...)", fontSize = 11.sp) },
            modifier = Modifier.weight(1f).height(44.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonBlue,
              unfocusedBorderColor = TvBorder,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = {
              if (customUrlInput.isNotBlank()) {
                onSetCustomUrl(customUrlInput)
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
            modifier = Modifier.height(44.dp),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Aplicar", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
