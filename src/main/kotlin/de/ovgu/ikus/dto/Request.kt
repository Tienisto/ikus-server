package de.ovgu.ikus.dto

object Request {

    data class Login(val name: String, val password: String)
}