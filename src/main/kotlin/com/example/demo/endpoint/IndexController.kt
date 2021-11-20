package com.example.demo.endpoint

import com.example.demo.service.PhoneService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class IndexController(val service: PhoneService) {

    @GetMapping("/")
    fun index(): String = "redirect:/app/list"

}