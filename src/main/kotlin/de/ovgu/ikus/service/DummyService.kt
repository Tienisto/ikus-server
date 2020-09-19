package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.*
import de.ovgu.ikus.utils.toPostType
import org.springframework.data.geo.Point
import org.springframework.stereotype.Service
import java.time.*
import kotlin.random.Random

@Service
class DummyService (
        private val userService: UserService,
        private val logService: LogService,
        private val channelService: ChannelService,
        private val postService: PostService,
        private val postFileService: PostFileService,
        private val eventService: EventService
) {

    suspend fun clear() {
        logService.deleteAll()
        channelService.deleteAll()
        postFileService.cleanup(Duration.ZERO) // delete files
        userService.deleteAll()
        userService.repairAdminAccount() // create admin account
    }

    suspend fun create() {
        createUsers()
        val users = userService.findAllOrdered()
        createLogs(users)
        createChannels()

        val channelsNews = channelService.findByType(ChannelType.NEWS)
        createPosts(channelsNews, Constants.postTitles, Constants.postCount)

        val channelsCalendar = channelService.findByType(ChannelType.CALENDAR)
        createEvents(channelsCalendar)

        val channelsFAQ = channelService.findByType(ChannelType.FAQ)
        createPosts(channelsFAQ, Constants.faqTitles, Constants.faqCount)
    }

    suspend fun createUsers(): List<User> {
        return Constants.userNames.map { name -> userService.addUser(name, "123") }
    }

    suspend fun createLogs(users: List<User>) {
        Constants.logs.forEach { log ->
            logService.log(log.first, users.random(), log.second)
        }
    }

    suspend fun createChannels() {

        val news = Constants.channelsNews.map { channel -> Channel(type = ChannelType.NEWS, name = channel, nameDe = channel) }
        val calendar = Constants.channelsCalendar.map { channel -> Channel(type = ChannelType.CALENDAR, name = channel, nameDe = channel) }
        val faq = Constants.channelsFAQ.map { channel -> Channel(type = ChannelType.FAQ, name = channel.first, nameDe = channel.second) }

        channelService.saveAll(news + calendar + faq)
    }

    suspend fun createPosts(channels: List<Channel>, titles: List<Pair<String, String>>, count: Int) {
        val posts = List(count) {
            val channel = channels.random()
            val date = LocalDate.now().minusDays(Random.nextInt(30).toLong())
            val title = titles.random()
            Post(type = channel.type.toPostType() ?: throw ErrorCode(500, "Forbidden Channel Type"), channelId = channel.id, date = date,
                    title = title.first, titleDe = title.second,
                    content = Constants.content, contentDe = Constants.content)
        }

        postService.saveAll(posts)
    }

    suspend fun createEvents(channels: List<Channel>) {
        val events = List(Constants.eventCount) {
            val channel = channels.random()
            val location = when (Random.nextInt(6) != 0) {
                true -> Constants.eventCoords.random()
                false -> null
            }
            val date = LocalDate.now().plusDays(Random.nextInt(30).toLong() - 15)

            val offset = Constants.zoneId.rules.getOffset(date.atStartOfDay())
            val start = OffsetDateTime.of(date.year, date.monthValue, date.dayOfMonth, Constants.eventHours.random(), Constants.eventMinutes.random(), 0, 0, offset)
            val end = when (Random.nextInt(3) != 0) {
                true -> start.plusHours(2)
                false -> null
            }

            val name = Constants.eventNames.random()
            val info = when (Random.nextInt(6) != 0) {
                true -> Constants.eventInfo
                else -> null
            }

            Event(channelId = channel.id, place = location?.second, coords = location?.first, startTime = start, endTime = end, name = name.first, nameDe = name.second, info = info?.first, infoDe = info?.second)
        }

        eventService.saveAll(events)
    }
}

private object Constants {

    val userNames = listOf("max", "anna")

    val logs = listOf(
            LogType.CREATE_CHANNEL to "IKUS",
            LogType.CREATE_CHANNEL to "AKAA",
            LogType.UPDATE_CHANNEL to "AKAA -> AKA",
            LogType.UPDATE_CHANNEL to "AKA -> AKAA",
            LogType.CREATE_POST to "Weekly Game Night (Wöchentlicher Spieleabend)",
            LogType.CREATE_POST to "Weekly Game Night (Wöchentlicher Spieleabend)"
    )

    val channelsNews = listOf("IKUS", "AKAA", "Wohnheim")
    val channelsCalendar = listOf("Allgemein", "Wohnheim")
    val channelsFAQ = listOf(
            "General" to "Allgemeines",
            "Examination" to "Prüfung",
            "Financing" to "Finanzierung"
    )

    const val postCount = 20
    const val faqCount = 10
    val postTitles = listOf(
            "Information about the corona virus and the behavior of the university" to "Information zum Coronavirus und das Verhalten der Universität",
            "International youth exchange" to "Internationales Begegnungsprojekt",
            "Travelling to Magdeburg" to "Anreise nach Magdeburg"
    )
    val faqTitles = listOf(
            "Until when can I register for the university?" to "Bis wann kann ich mich immatrikulieren?",
            "How can I activate my student card?" to "Wie aktiviere ich meine Studentenkarte?",
            "How can I add money to my student card?" to "Wie lade ich Geld auf meine Studentenkarte auf?",
            "Where can I print?" to "Wo kann ich ausdrucken?"
    )

    const val content = "At <b>vero</b> eos et <span style=\"color: red\">accusamus</span> et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga.<br><br>Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet ut et voluptates repudiandae sint et molestiae non recusandae.<br><table><tr><th>Firstname</th><th>Lastname</th><th>Age</th></tr><tr><td>Jill</td><td>Smith</td><td>50</td></tr><tr><td>Eve</td><td>Jackson</td><td>94</td></tr></table><br>Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat."

    const val eventCount = 30
    val eventNames = listOf(
            "Welcome Day" to "Immatrikulationsfeier",
            "Dorm Game Night" to "Wohnheim-Spieleabend",
            "Barbecue" to "Grillen"
    )

    val eventInfo = Pair(
            "I am delighted to be able to welcome you as members of Otto von Guericke University Magdeburg and to invite you and your families to our Welcome Day",
            "Zum Willkommenstag an der Otto-von-Guericke-Universität Magdeburg erwartet Studienanfängerinnen und Studienanfänger jährlich ein umfangreiches Programm auf dem Campus."
    )
    val eventCoords = listOf(
            Point(52.137813, 11.646508) to "Festung Mark"
    )
    val eventHours = listOf(16, 18, 20)
    val eventMinutes = listOf(0, 30)
    val zoneId = ZoneId.of("Europe/Berlin")

}