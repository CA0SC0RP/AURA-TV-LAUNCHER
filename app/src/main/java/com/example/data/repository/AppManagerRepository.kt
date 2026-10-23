package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.data.model.AppItem

class AppManagerRepository(private val context: Context) {

  // Preset TV apps that are recognizable on 4K TVs
  private val presetApps = listOf(
    AppItem("com.google.android.youtube.tv", "YouTube TV", isPinned = true, category = "Streaming", defaultIconType = "youtube"),
    AppItem("com.netflix.ninja", "Netflix", isPinned = true, category = "Streaming", defaultIconType = "netflix"),
    AppItem("com.amazon.amazonvideo.livingroom", "Prime Video", isPinned = true, category = "Streaming", defaultIconType = "prime"),
    AppItem("com.disney.disneyplus", "Disney+", isPinned = true, category = "Streaming", defaultIconType = "disney"),
    AppItem("com.hbo.hbonow", "Max / HBO", isPinned = true, category = "Streaming", defaultIconType = "max"),
    AppItem("com.spotify.tv.android", "Spotify", isPinned = true, category = "Música", defaultIconType = "spotify"),
    AppItem("com.plexapp.android", "Plex Media", isPinned = false, category = "Multimedia", defaultIconType = "plex"),
    AppItem("tv.twitch.android.app", "Twitch", isPinned = false, category = "En Vivo", defaultIconType = "twitch"),
    AppItem("com.retroarch", "RetroArch", isPinned = false, category = "Juegos", defaultIconType = "games"),
    AppItem("com.android.vending", "Play Store", isPinned = true, isSystemApp = true, category = "Sistema", defaultIconType = "store"),
    AppItem("com.android.settings", "Ajustes TV", isPinned = true, isSystemApp = true, category = "Sistema", defaultIconType = "settings"),
    AppItem("com.android.chrome", "Navegador", isPinned = false, isSystemApp = true, category = "Utilidades", defaultIconType = "browser")
  )

  fun getInstalledApps(pinnedPackageNames: Set<String>): List<AppItem> {
    val pm = context.packageManager
    val installedList = mutableListOf<AppItem>()

    try {
      val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
      }
      val activities = pm.queryIntentActivities(intent, 0)
      for (ri in activities) {
        val pkg = ri.activityInfo.packageName
        if (pkg == context.packageName) continue // don't list ourselves
        val label = ri.loadLabel(pm).toString()
        val icon = ri.loadIcon(pm)
        val isPinned = pinnedPackageNames.contains(pkg)

        installedList.add(
          AppItem(
            packageName = pkg,
            label = label,
            icon = icon,
            isPinned = isPinned,
            isSystemApp = false,
            category = "Instalada"
          )
        )
      }
    } catch (e: Exception) {
      // ignore
    }

    // Merge preset apps if not already in installedList so user can pin/test them
    val existingPkgs = installedList.map { it.packageName }.toSet()
    val combined = installedList.toMutableList()

    for (preset in presetApps) {
      if (!existingPkgs.contains(preset.packageName)) {
        val isPinned = pinnedPackageNames.contains(preset.packageName) || (pinnedPackageNames.isEmpty() && preset.isPinned)
        combined.add(preset.copy(isPinned = isPinned))
      }
    }

    return combined
  }

  fun launchApp(packageName: String): Boolean {
    val pm = context.packageManager
    val launchIntent = pm.getLaunchIntentForPackage(packageName)
    return if (launchIntent != null) {
      launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(launchIntent)
      true
    } else {
      false
    }
  }
}
