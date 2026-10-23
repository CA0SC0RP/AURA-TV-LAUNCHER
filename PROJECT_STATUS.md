# AURA TV Launcher — Project Status

## Working branch
`feature/weather-location-and-widget-fixes`

All ongoing work is being committed to this feature branch. Do not merge into `main` until the grouped changes are reviewed and the build is verified.

## Current UI and behavior changes
- Weather setup no longer silently defaults to Madrid; location is configured by country and postal code.
- Weather data failures do not fall back to invented weather values.
- Weather detail includes daily conditions and precipitation probability; Back returns to the launcher.
- The compact moon widget shows the next lunar phase and estimated date. Its detail view lists all four phases and Back returns to the launcher.
- The application dock is now icon-only, with larger rounded app icons and a plus button.
- The dock shows at most six favorite apps. The manager searches installed apps and allows adding/removing favorites.
- App discovery checks both Android's standard launcher and Android TV's Leanback launcher categories.
- Dock and manager use actual installed-app icons where available. Placeholder preset entries are no longer shown as though installed.
- The default favorites are capped at six.
- The sports widget is being changed to show only NBA, football/soccer, Formula 1, and NFL schedules.
- The compact sports widget shows upcoming events and opens a larger calendar with date/time, event status, tournament, and venue when supplied by the data source.
- Sports schedules are fetched from ESPN scoreboard endpoints for NBA, Liga MX, Premier League, LaLiga, F1, and NFL; unavailable feeds are skipped instead of using placeholder fixtures. Feed coverage and Android TV display still require verification.
- The gaming-news widget has a larger presentation area, a rotating visual card, previous/next controls, and a detail dialog with a larger image and readable summary.
- Selecting a gaming-news item opens its detail view; Back closes it and returns to the launcher.
- Gaming news now loads from RSS feeds configured for PlayStation Blog, Xbox Wire, Nintendo Life, IGN, and GameSpot.
- RSS articles carry their own title, summary, publication date, publisher, article URL, and image when the feed supplies one. The widget no longer maps article IDs to illustrative Unsplash images.
- Selecting a story opens its detail dialog and provides a link to the original article. Missing feed images are explicitly shown as unavailable rather than replaced with unrelated artwork.
- RSS requests run in the background so a slow feed does not hold up the initial launcher screen. News refreshes on startup/manual launcher refresh and every 30 minutes while the ViewModel remains active.
- RSS availability and image coverage depend on each publisher's feed and network access; feeds that fail are skipped. Live feed parsing and Android TV rendering still need build/device verification.

## Verification status
- Changes are committed to this feature branch.
- No APK has been generated for these grouped changes.
- Compilation and testing on a physical Android TV device are still pending.
- Continue grouping a couple more requested changes before building one APK for testing.
