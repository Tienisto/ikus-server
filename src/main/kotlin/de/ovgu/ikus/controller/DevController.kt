package de.ovgu.ikus.controller

import de.ovgu.ikus.service.DummyService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dev")
class DevController (
        private val dummyService: DummyService
) {

    @PostMapping("/create")
    suspend fun create() {
        dummyService.create()
    }

    @PostMapping("/clear")
    suspend fun clear() {
        dummyService.clear()
    }
}