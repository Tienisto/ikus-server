package de.ovgu.ikus

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class IkusApplication

fun main(args: Array<String>) {
	runApplication<IkusApplication>(*args)
}
