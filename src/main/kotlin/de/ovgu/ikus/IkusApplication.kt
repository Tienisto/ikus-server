package de.ovgu.ikus

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class IkusApplication

fun main(args: Array<String>) {
	runApplication<IkusApplication>(*args)
}
