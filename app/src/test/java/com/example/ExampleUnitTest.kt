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
  fun testSportsRepositoryFiltering() {
    val repo = SportsRepository()
    val allEvents = repo.getAllEvents()
    assertTrue("Should have initial sports events", allEvents.isNotEmpty())

    val soccerEvents = repo.getEventsBySport("Fútbol")
    assertTrue("Should have soccer events", soccerEvents.isNotEmpty())
    assertTrue("Filtered events should match sport", soccerEvents.all { it.sport.contains("Fútbol", true) || it.tournament.contains("Fútbol", true) })

    val initialCount = allEvents.size
    repo.addCustomEvent("Boxeo", "Mundial Pesado", "Canelo", "Benavidez", "Sábado 22:00", "DAZN")
    assertEquals(initialCount + 1, repo.getAllEvents().size)
  }

  @Test
  fun testWeatherDescriptions() {
    val repo = WeatherRepository()
    assertEquals("Cielo despejado", repo.getWeatherDescription(0))
    assertEquals("Tormenta eléctrica", repo.getWeatherDescription(95))
  }
}
