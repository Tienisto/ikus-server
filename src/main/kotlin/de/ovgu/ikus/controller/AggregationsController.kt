package de.ovgu.ikus.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/aggregations")
class AggregationsController {

    @GetMapping
    fun test(): String {
        return "Hello"
    }
}