package de.ovgu.ikus.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class HtmlController {

    @GetMapping("/dashboard", "/posts", "/calendar", "/channels", "/links", "/handbook", "/faq", "/contact", "/statistics", "/logs")
    @ResponseBody
    fun routes(): Resource {
        return ClassPathResource("static/index.html")
    }
}