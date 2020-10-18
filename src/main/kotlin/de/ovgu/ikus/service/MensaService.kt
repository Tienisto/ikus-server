package de.ovgu.ikus.service

import de.ovgu.ikus.model.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.springframework.data.geo.Point
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class MensaService (
        private val cacheService: CacheService
) {

    private val client = WebClient.create()
    private val datePattern = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    private var menuCache = listOf<MensaInfo>()

    fun getMenu(): List<MensaInfo> {
        return menuCache
    }

    /**
     * updates the mensa cache
     */
    suspend fun updateMenu() {
        menuCache = Mensa.values()
                .mapNotNull { location ->
                    try {
                        when (location) {
                            Mensa.UNI_CAMPUS_DOWN -> scrapeUniCampusDown()
                            Mensa.UNI_CAMPUS_UP -> scrapeUniCampusUp()
                            Mensa.ZSCHOKKE -> scrapeZschokke()
                            Mensa.HERRENKRUG -> scrapeHerrenkrug()
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
        cacheService.triggerUpdateFlag(CacheKey.MENSA)
    }

    private suspend fun scrapeUniCampusDown(): MensaInfo {
        val menus = scrapeStudentenwerk("https://www.studentenwerk-magdeburg.de/mensen-cafeterien/mensa-unicampus/speiseplan-unten/")
        return MensaInfo(
                name = Mensa.UNI_CAMPUS_DOWN,
                openingHours = "Mon - Fri, 10:45 AM - 2:00 PM",
                openingHoursDe = "Mo - Fr, 10:45 - 14:00",
                coords = Point(52.13944, 11.64725),
                menus = menus
        )
    }

    private suspend fun scrapeUniCampusUp(): MensaInfo {
        val menus = scrapeStudentenwerk("https://www.studentenwerk-magdeburg.de/mensen-cafeterien/mensa-unicampus/speiseplan-oben/")
        return MensaInfo(
                name = Mensa.UNI_CAMPUS_UP,
                openingHours = "closed",
                openingHoursDe = "geschlossen",
                coords = Point(52.13944, 11.64725),
                menus = menus
        )
    }

    private suspend fun scrapeZschokke(): MensaInfo {
        val menus = scrapeStudentenwerk("https://www.studentenwerk-magdeburg.de/mensen-cafeterien/mensa-kellercafe/speiseplan/")
        return MensaInfo(
                name = Mensa.ZSCHOKKE,
                openingHours = "Mon - Fri, 10:45 AM - 2:00 PM",
                openingHoursDe = "Mo - Fr, 10:45 - 14:00",
                coords = Point(52.13799, 11.63387),
                menus = menus
        )
    }

    private suspend fun scrapeHerrenkrug(): MensaInfo {
        val menus = scrapeStudentenwerk("https://www.studentenwerk-magdeburg.de/mensen-cafeterien/mensa-herrenkrug/speiseplan/")
        return MensaInfo(
                name = Mensa.HERRENKRUG,
                openingHours = "Mon - Fri, 11:00 AM - 2:15 PM",
                openingHoursDe = "Mo - Fr, 11:00 - 14:15",
                coords = Point(52.13975, 11.67615),
                menus = menus
        )
    }

    /**
     * common method which can be used for all studentenwerk websites because they share the same structure
     * @param url the address of the website to be scraped
     * @return the menu
     */
    private suspend fun scrapeStudentenwerk(url: String): List<Menu> {
        val doc = getDocument(url) ?: return emptyList()
        return doc.select(".mensa table")
                .map { table ->
                    val date = getDateFromTable(table)
                    val food = getFoodFromTable(table)
                    Menu(date, food)
                }
    }

    private fun getDateFromTable(table: Element): LocalDate {
        return try {
            val text = table.select("thead").text()
            val dateString = text.split(",")[1].trim()
            LocalDate.parse(dateString, datePattern)
        } catch (e: Exception) {
            LocalDate.of(2000, 1, 1) // fallback to 2000-01-01
        }
    }

    private fun getFoodFromTable(table: Element): List<Food> {
        return try {
            table.select("tbody tr").mapNotNull { tr ->

                if (tr.children().size != 2)
                    return@mapNotNull null

                try {
                    // name
                    val name = tr.select("strong").first()
                    val nameEn = name.select(".grau").text()
                    val nameDe = when (val nameDeNode = name.childNodes()[0]) {
                        is Element -> nameDeNode.text()
                        is TextNode -> nameDeNode.wholeText
                        else -> "Error"
                    }

                    // price
                    val tds = tr.children()
                    val price = tds[0].childNodes().last().outerHtml() // take last row of the left column
                            .split("|").first().trim() // 1,45 | 2,50 | 3,20 -> 1,45
                            .replace(",", ".").toDouble() // 1,45 -> 1.45

                    // tags
                    val tags = tds[1].select("img")
                            .mapNotNull { img ->
                                val tagTitle = img.attr("title").toLowerCase()
                                when {
                                    tagTitle.contains("vegan") -> FoodTag.VEGAN
                                    tagTitle.contains("vegetarisch") -> FoodTag.VEGETARIAN
                                    tagTitle.contains("knoblauch") -> FoodTag.GARLIC
                                    tagTitle.contains("fisch") -> FoodTag.FISH
                                    tagTitle.contains("geflügel") -> FoodTag.CHICKEN
                                    tagTitle.contains("rind") -> FoodTag.BEEF
                                    tagTitle.contains("schwein") -> FoodTag.PIG
                                    tagTitle.contains("suppe") -> FoodTag.SOUP
                                    tagTitle.contains("alkohol") -> FoodTag.ALCOHOL
                                    else -> null
                                }
                            }

                    Food(nameEn, nameDe, price, tags)
                } catch (e: Exception) {
                    // fallback for specific food
                    Food("Error", "Error", 0.0, emptyList())
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * fetches the website in [url]
     * @return the parsed document
     */
    private suspend fun getDocument(url: String): Document? {
        val response = client.get()
                .uri(url)
                .accept(MediaType.ALL)
                .awaitExchange { response -> response }

        if(response.statusCode() != HttpStatus.OK)
            return null

        val body = response.awaitBody<String>()
        return Jsoup.parse(body)
    }
}