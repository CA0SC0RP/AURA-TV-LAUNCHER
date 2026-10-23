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

## Verification status
- Changes are committed to this feature branch.
- No APK has been generated for these grouped changes.
- Compilation and testing on a physical Android TV device are still pending.
- Continue grouping a couple more requested changes before building one APK for testing.
