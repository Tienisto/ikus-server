package de.ovgu.ikus.service

import de.ovgu.ikus.model.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.slf4j.LoggerFactory
import org.springframework.data.geo.Point
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class MensaService(
    private val cacheService: CacheService,
) {

    private val logger = LoggerFactory.getLogger(MensaService::class.java)
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
                        Mensa.PIER_16 -> scrapePier16()
                    }
                } catch (e: Exception) {
                    null
                }
            }
        cacheService.triggerUpdateFlag(CacheKey.MENSA)
    }

    private suspend fun scrapeUniCampusDown(): MensaInfo {
        val menus = scrapeStudentenwerk("https://www.studentenwerk-magdeburg.de/mensen-cafeterien/mensa-unicampus-speiseplan-unten/")
        return MensaInfo(
            name = Mensa.UNI_CAMPUS_DOWN,
            openingHours = "Mon - Fri, 10:45 AM - 2:00 PM",
            openingHoursDe = "Mo - Fr, 10:45 - 14:00",
            coords = Point(52.13944, 11.64725),
            menus = menus
        )
    }

    private suspend fun scrapeUniCampusUp(): MensaInfo {
        val menus = scrapeStudentenwerk("https://www.studentenwerk-magdeburg.de/mensen-cafeterien/mensa-unicampus-speiseplan-oben/")
        return MensaInfo(
            name = Mensa.UNI_CAMPUS_UP,
            openingHours = "Mon - Fri, 10:45 AM - 2:00 PM & 4:00 PM - 7:00 PM",
            openingHoursDe = "Mo - Fr, 10:45 - 14:00 & 16:00 - 19:00",
            coords = Point(52.13944, 11.64725),
            menus = menus
        )
    }

    private suspend fun scrapeZschokke(): MensaInfo {
        val menus = scrapeStudentenwerk("https://www.studentenwerk-magdeburg.de/mensen-cafeterien/mensa-kellercafe-speiseplan/")
        return MensaInfo(
            name = Mensa.ZSCHOKKE,
            openingHours = "Mon - Fri, 8:00 AM - 3:00 PM",
            openingHoursDe = "Mo - Fr, 8:00 - 15:00",
            coords = Point(52.13799, 11.63387),
            menus = menus
        )
    }

    private suspend fun scrapeHerrenkrug(): MensaInfo {
        val menus = scrapeStudentenwerk("https://www.studentenwerk-magdeburg.de/mensen-cafeterien/mensa-herrenkrug-speiseplan/")
        return MensaInfo(
            name = Mensa.HERRENKRUG,
            openingHours = "Mon - Fri, 8:00 AM - 2:00 PM",
            openingHoursDe = "Mo - Fr, 8:00 - 14:00",
            coords = Point(52.13975, 11.67615),
            menus = menus
        )
    }

    private suspend fun scrapePier16(): MensaInfo {
        val menus = scrapePier16("https://www.studentenwerk-magdeburg.de/mensen-cafeterien/pier-16-speiseplan/")
        return MensaInfo(
            name = Mensa.PIER_16,
            openingHours = "Mon - Fri, 7:30 AM - 3:00 PM",
            openingHoursDe = "Mo - Fr, 7:30 - 15:00",
            coords = Point(52.1402869,11.6435952),
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
                try {
                    if (tr.children().size == 1) {
                        // Sides
                        val td = tr.children().first()!!
                        val nameEn = td
                            .select(".grau")
                            .text()
                            .replace("Sides: ", "")
                        val nameDe = td
                            .childNodes()
                            .mapNotNull { node ->
                                when (node) {
                                    is TextNode -> node.text()
                                    else -> null
                                }
                            }
                            .first()
                            .replace("Beilagen: ", "")

                        return@mapNotNull Food(nameEn, nameDe, null, listOf(FoodTag.SIDES))
                    }

                    val tds = tr.children()

                    // Extract German name
                    val nameDe = tds[0].selectFirst("span.gruen")
                        ?.text()
                        ?.trim()
                        ?.ifBlank { null }
                        ?: tds[0].childNodes()
                            .firstOrNull { it is TextNode }
                            ?.let { (it as TextNode).text().trim() }
                            ?.ifBlank { null }
                        ?: "Error"

                    // Extract English name
                    val nameEn = tds[0].selectFirst("span.grau")
                        ?.ownText()
                        ?.trim()
                        ?.ifBlank { null }
                        ?: "Error"

                    // Extract price
                    val price = tds[0].childNodes()
                        .lastOrNull { it is TextNode && it.text().contains("(") }
                        ?.let {
                            val priceText = (it as TextNode).text().trim()
                            val prices = priceText.removeSurrounding("(", ")").split("|")
                            prices.firstOrNull()?.trim()?.replace(",", ".")?.toDouble()
                        }
                        ?: 0.0

                    // Extract tags
                    val tags = try {
                        tds[1].select("img")
                            .mapNotNull { img ->
                                val tagTitle = img.attr("title").lowercase()
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
                            .distinct()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    Food(nameEn, nameDe, price, tags)
                } catch (e: Exception) {
                    // fallback for specific food
                    Food("Error", "Error", null, emptyList())
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun scrapePier16(url: String): List<Menu> {
        val doc = getDocument(url) ?: return emptyList()
        return doc.select("#content table")
            .map { table ->
                val food = getFoodFromNewTable(table)
                Menu(LocalDate.now(), food)
            }
    }

    private fun getFoodFromNewTable(table: Element): List<Food> {
        return table.select("tbody tr").mapNotNull { tr ->
            try {
                val td = tr.selectFirst("td") ?: return@mapNotNull null

                // Get the German name from the <strong> tag
                val strong = td.selectFirst("strong")
                val nameDe = strong?.text()?.trim() ?: "Error"

                val childNodes = td.childNodes()
                val strongIndex = childNodes.indexOf(strong)
                if (strongIndex == -1) return@mapNotNull null

                // Initialize variables
                var price: Double? = null

                var i = strongIndex + 1

                // Skip any whitespace text nodes
                while (i < childNodes.size && childNodes[i] is TextNode && childNodes[i].toString().trim().isEmpty()) {
                    i++
                }

                // Skip allergen information (text in parentheses)
                if (i < childNodes.size && childNodes[i] is TextNode) {
                    val textNode = childNodes[i] as TextNode
                    val text = textNode.text().trim()
                    // If text starts with '(', assume it's allergen info and skip
                    if (text.startsWith("(") && text.endsWith(")")) {
                        i++
                    }
                }

                // Skip any whitespace text nodes
                while (i < childNodes.size && childNodes[i] is TextNode && childNodes[i].toString().trim().isEmpty()) {
                    i++
                }

                // Expect a <br> element
                if (i < childNodes.size && childNodes[i] is Element && (childNodes[i] as Element).tagName() == "br") {
                    i++
                }

                // Skip any whitespace text nodes
                while (i < childNodes.size && childNodes[i] is TextNode && childNodes[i].toString().trim().isEmpty()) {
                    i++
                }

                // Check if next node is TextNode with English name
                if (i < childNodes.size && childNodes[i] is TextNode) {
                    val textNode = childNodes[i] as TextNode
                    val text = textNode.text().trim()

                    // Check if the text is not a price (does not start with a digit)
                    if (text.isNotEmpty() && !text[0].isDigit()) {
                        i++

                        // Skip any whitespace text nodes
                        while (i < childNodes.size && childNodes[i] is TextNode && childNodes[i].toString().trim().isEmpty()) {
                            i++
                        }

                        // Expect a <br> element
                        if (i < childNodes.size && childNodes[i] is Element && (childNodes[i] as Element).tagName() == "br") {
                            i++
                        }
                    }
                }

                // Skip any whitespace text nodes
                while (i < childNodes.size && childNodes[i] is TextNode && childNodes[i].toString().trim().isEmpty()) {
                    i++
                }

                // Now, expect the price text
                if (i < childNodes.size && childNodes[i] is TextNode) {
                    val textNode = childNodes[i] as TextNode
                    val priceText = textNode.text().trim()
                    // Process the priceText to get the price
                    // E.g., "4,20 l 5,90 l 7,30"
                    val prices = priceText.split("l")
                        .map { it.trim().replace(",", ".") }
                        .mapNotNull { it.toDoubleOrNull() }

                    price = prices.firstOrNull()
                }

                // Create the Food object
                Food(nameDe, nameDe, price, emptyList())
            } catch (e: Exception) {
                // Fallback in case of errors
                Food("Error", "Error", null, emptyList())
            }
        }
    }

    /**
     * fetches the website in [url]
     * @return the parsed document
     */
    private suspend fun getDocument(url: String): Document? {
        try {
            val response = client.get()
                .uri(url)
                .accept(MediaType.ALL)
                .retrieve()

            val body = response.awaitBody<String>()
            return Jsoup.parse(body)
        } catch (e: Exception) {
            // e.g. 404
            logger.warn(e.message)
            return null
        }
    }
}