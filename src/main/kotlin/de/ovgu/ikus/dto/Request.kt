package de.ovgu.ikus.dto

object Request {

    data class Id(val id: Int)
    data class Login(val name: String, val password: String)

    data class AddUser(val name: String, val password: String)
    data class UpdateName(val id: Int, val name: String)
    data class UpdatePassword(val id: Int, val password: String)

    data class CreateChannel(val name: LocalizedString)
    data class RenameChannel(val id: Int, val name: LocalizedString)

    data class CreatePost(val channelId: Int, val title: LocalizedString, val content: LocalizedString)
    data class UpdatePost(val id: Int, val channelId: Int, val title: LocalizedString, val content: LocalizedString)
}