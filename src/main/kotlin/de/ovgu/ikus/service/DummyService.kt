package de.ovgu.ikus.service

import de.ovgu.ikus.model.*
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.random.Random

@Service
class DummyService (
        private val userService: UserService,
        private val logService: LogService,
        private val channelService: ChannelService,
        private val postService: PostService
) {

    suspend fun clear() {
        logService.deleteAll()
        channelService.deleteAll()
        userService.deleteAll()
        userService.repairAdminAccount() // create admin account
    }

    suspend fun create() {
        createUsers()
        val users = userService.findAllOrdered()
        createLogs(users)
        createChannels()

        val channels = channelService.findByTypeOrdered(ChannelType.NEWS)
        createPosts(channels)
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
        Constants.channelsNews.forEach { channel ->
            channelService.save(Channel(type = ChannelType.NEWS, name = channel, nameDe = channel))
        }

        Constants.channelsCalendar.forEach { channel ->
            channelService.save(Channel(type = ChannelType.CALENDAR, name = channel, nameDe = channel))
        }
    }

    suspend fun createPosts(channels: List<Channel>) {
        val posts = List(Constants.postCount) {
            val date = LocalDate.now().minusDays(Random.nextInt(30).toLong() + 1)
            val title = Constants.titles.random()
            Post(type = PostType.NEWS, channelId = channels.random().id, date = date,
                    title = title.first, titleDe = title.second,
                    content = Constants.content, contentDe = Constants.content)
        }

        postService.saveAll(posts)
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

    const val postCount = 20
    val titles = listOf(
            "Information about the corona virus and the behavior of the university" to "Information zum Coronavirus und das Verhalten der Universität",
            "International youth exchange" to "Internationales Begegnungsprojekt",
            "Travelling to Magdeburg" to "Anreise nach Magdeburg"
    )

    const val content = "At <b>vero</b> eos et <span style=\"color: red\">accusamus</span> et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga.<br><br>Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet ut et voluptates repudiandae sint et molestiae non recusandae.<br><table><tr><th>Firstname</th><th>Lastname</th><th>Age</th></tr><tr><td>Jill</td><td>Smith</td><td>50</td></tr><tr><td>Eve</td><td>Jackson</td><td>94</td></tr></table><br>Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat."
}