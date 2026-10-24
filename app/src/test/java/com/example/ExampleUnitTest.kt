package com.example

import com.example.data.repository.MoonCalculator
import com.example.data.repository.SportsRepository
import com.example.data.repository.WeatherRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class ExampleUnitTest {
  @Test
  fun testMoonCalculator() {
    val info = MoonCalculator.calculate(Date())
    assertNotNull(info.phaseName)
    assertTrue("Illumination should be between 0 and 100", info.illuminationPercent in 0..100)
    assertTrue("Age should be between 0 and 30 days", info.ageDays in 0.0..30.0)
    assertNotNull(info.nextFullMoonDate)
    assertNotNull(info.nextNewMoonDate)
  }

  @Test
  fun testMoonKnownPhases() {
    // Luna nueva del 11 de enero de 2024 (11:57 UTC): casi nada de luz
    val newMoon = MoonCalculator.calculate(Date(1704974220000L))
    assertTrue("New moon should be almost dark", newMoon.illuminationPercent <= 3)

    // Luna llena del 25 de enero de 2024 (17:54 UTC): casi toda iluminada
    val fullMoon = MoonCalculator.calculate(Date(1706205240000L))
    assertTrue("Full moon should be almost fully lit", fullMoon.illuminationPercent >= 97)

    // Siempre hay 4 próximas fases, en orden de fecha
    assertEquals(4, fullMoon.upcomingPhases.size)
  }

  @Test
  fun testSportsRepositoryTeamsAndSports() {
    assertEquals(listOf("NBA", "Tenis"), SportsRepository.featuredSports)
    assertEquals(30, SportsRepository.nbaTeams.size)
    assertEquals("Lakers", SportsRepository.nbaTeamName("LAL"))
    assertEquals(null, SportsRepository.nbaTeamName("xyz"))
  }

  @Test
  fun testWeatherDescriptions() {
    val repo = WeatherRepository()
    assertEquals("Cielo despejado", repo.getWeatherDescription(0))
    assertEquals("Tormenta eléctrica", repo.getWeatherDescription(95))
  }
}
