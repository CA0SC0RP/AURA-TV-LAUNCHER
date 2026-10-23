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
    val installedByPackage = linkedMapOf<String, AppItem>()

    // Android TV apps may expose LEANBACK_LAUNCHER instead of the phone-style LAUNCHER category.
    val launcherCategories = listOf(
      Intent.CATEGORY_LEANBACK_LAUNCHER,
      Intent.CATEGORY_LAUNCHER
    )

    for (category in launcherCategories) {
      try {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
          addCategory(category)
        }
        val activities = pm.queryIntentActivities(intent, 0)
        for (ri in activities) {
          val pkg = ri.activityInfo.packageName
          if (pkg == context.packageName || installedByPackage.containsKey(pkg)) continue

          val preset = presetApps.firstOrNull { it.packageName == pkg }
          val isSystemApp = (ri.activityInfo.applicationInfo.flags and
            android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0

          installedByPackage[pkg] = AppItem(
            packageName = pkg,
            label = ri.loadLabel(pm).toString(),
            icon = ri.loadIcon(pm),
            isPinned = pinnedPackageNames.contains(pkg),
            isSystemApp = isSystemApp,
            category = preset?.category ?: if (isSystemApp) "Sistema" else "Instalada",
            defaultIconType = preset?.defaultIconType ?: "default"
          )
        }
      } catch (_: Exception) {
        // Continue with the other launcher category if this query is unavailable.
      }
    }

    // Only return apps actually found on the device; do not show fictional preset entries.
    return installedByPackage.values.toList()
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
