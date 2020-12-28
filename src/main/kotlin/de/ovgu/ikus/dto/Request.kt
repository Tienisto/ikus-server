package de.ovgu.ikus.dto

import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.Platform
import java.time.OffsetDateTime

object Request {

    data class Id(val id: Int)
    data class Login(val name: String, val password: String)

    data class AddUser(val name: String, val password: String)
    data class UpdateName(val id: Int, val name: String)
    data class UpdatePassword(val id: Int, val password: String)
    data class UpdateMyPassword(val oldPassword: String, val newPassword: String)

    data class CreateChannel(val name: MultiLocaleString)
    data class RenameChannel(val id: Int, val name: MultiLocaleString)

    data class CreatePost(val channelId: Int, val title: MultiLocaleString, val content: MultiLocaleString, val files: List<Int>)
    data class UpdatePost(val id: Int, val channelId: Int, val title: MultiLocaleString, val content: MultiLocaleString, val files: List<Int>)

    data class CreateEvent(val channelId: Int, val name: MultiLocaleString, val info: MultiLocaleString?, val place: String?, val coords: CoordsDto?, val startTime: OffsetDateTime, val endTime: OffsetDateTime?)
    data class UpdateEvent(val id: Int, val channelId: Int, val name: MultiLocaleString, val info: MultiLocaleString?, val place: String?, val coords: CoordsDto?, val startTime: OffsetDateTime, val endTime: OffsetDateTime?)

    data class CreateLink(val channelId: Int, val url: MultiLocaleString, val info: MultiLocaleString)
    data class UpdateLink(val id: Int, val channelId: Int, val url: MultiLocaleString, val info: MultiLocaleString)

    data class UpdateBookmarks(val locale: IkusLocale, val bookmarks: List<HandbookBookmarkDto>)

    data class CreatePodcast(val name: MultiLocaleString)
    data class UpdatePodcast(val id: Int, val name: MultiLocaleString)
    data class CreatePodcastFile(val podcastId: Int, val name: MultiLocaleString, val text: MultiLocaleString?)
    data class UpdatePodcastFile(val id: Int, val podcastId: Int, val name: MultiLocaleString, val text: MultiLocaleString?)

    data class CreateContact(val name: MultiLocaleString, val email: String?, val phoneNumber: String?, val place: String?, val openingHours: MultiLocaleString?, val links: List<String>)
    data class UpdateContact(val id: Int, val name: MultiLocaleString, val email: String?, val phoneNumber: String?, val place: String?, val openingHours: MultiLocaleString?, val links: List<String>)

    data class CreateFeature(val name: MultiLocaleString, val icon: String, val postId: Int?, val linkId: Int?)
    data class UpdateFeature(val id: Int, val name: MultiLocaleString, val icon: String, val postId: Int?, val linkId: Int?)

    data class AppStartSignal(val token: String?, val platform: Platform?, val deviceId: String?)
}