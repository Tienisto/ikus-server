package de.ovgu.ikus.service

import de.ovgu.ikus.model.Food
import de.ovgu.ikus.model.FoodTag
import de.ovgu.ikus.model.MensaLocation
import de.ovgu.ikus.model.Menu
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
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

    private var menuCache = listOf<Menu>()

    fun getMenu(): List<Menu> {
        return menuCache
    }

    suspend fun updateMenu() {
        menuCache = MensaLocation.values()
                .mapNotNull { location ->
                    try {
                        when (location) {
                            MensaLocation.UNI_CAMPUS_DOWN -> scrapeUniCampusDown()
                            MensaLocation.UNI_CAMPUS_UP -> scrapeUniCampusUp()
                            MensaLocation.ZSCHOKKE -> scrapeZschokke()
                            MensaLocation.HERRENKRUG -> scrapeHerrenkrug()
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                .flatten()
        cacheService.triggerUpdateFlag(CacheKey.MENSA)
    }

    private suspend fun scrapeUniCampusDown(): List<Menu> {
        return scrapeStudentenwerk(
                location = MensaLocation.UNI_CAMPUS_DOWN,
                url = "https://www.studentenwerk-magdeburg.de/mensen-cafeterien/mensa-unicampus/speiseplan-unten/"
        )
    }

    private suspend fun scrapeUniCampusUp(): List<Menu> {
        return scrapeStudentenwerk(
                location = MensaLocation.UNI_CAMPUS_UP,
                url = "https://www.studentenwerk-magdeburg.de/mensen-cafeterien/mensa-unicampus/speiseplan-oben/"
        )
    }

    private suspend fun scrapeZschokke(): List<Menu> {
        return scrapeStudentenwerk(
                location = MensaLocation.ZSCHOKKE,
                url = "https://www.studentenwerk-magdeburg.de/mensen-cafeterien/mensa-kellercafe/speiseplan/"
        )
    }

    private suspend fun scrapeHerrenkrug(): List<Menu> {
        return scrapeStudentenwerk(
                location = MensaLocation.HERRENKRUG,
                url = "https://www.studentenwerk-magdeburg.de/mensen-cafeterien/mensa-herrenkrug/speiseplan/"
        )
    }

    private suspend fun scrapeStudentenwerk(location: MensaLocation, url: String): List<Menu> {
        val doc = getDocument(url) ?: return emptyList()

        return doc.select(".mensa table")
                .map { table ->
                    val date = getDateFromTable(table)
                    val food = getFoodFromTable(table)
                    Menu(location, date, food)
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
            val trList = table.select("tbody tr")
            trList.mapNotNull { tr ->

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

    private suspend fun getDocument(url: String): Document? {
        val response = client.get()
                .uri(url)
                .accept(MediaType.ALL)
                .awaitExchange()

        if(response.statusCode() != HttpStatus.OK)
            return null

        val body = response.awaitBody<String>()
        return Jsoup.parse(body)
    }
}