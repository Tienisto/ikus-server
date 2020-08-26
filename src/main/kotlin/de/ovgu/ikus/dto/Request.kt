package de.ovgu.ikus.dto

object Request {

    data class Id(val id: Int)
    data class Login(val name: String, val password: String)
    data class CreateChannel(val name: LocalizedString)
    data class RenameChannel(val id: Int, val name: LocalizedString)
}