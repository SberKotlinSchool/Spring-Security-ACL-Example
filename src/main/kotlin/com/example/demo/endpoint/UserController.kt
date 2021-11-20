package com.example.demo.endpoint

import com.example.demo.endpoint.dto.RequestData
import com.example.demo.entity.Phone
import com.example.demo.entity.UserDetailsAdapter
import com.example.demo.service.PhoneService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/app")
class UserController(val service: PhoneService) {

    @GetMapping("/")
    fun index(): String = "redirect:/app/list"

    @GetMapping("/list")
    fun list(model: Model): String {
        model.addAttribute("phones", service.getPhones())
        return "list"
    }

    @GetMapping("/add")
    fun add(): String {
        return "add"
    }

    @PostMapping("/add")
    fun add(requestData: RequestData, auth: Authentication): String {
        val user = (auth.principal as UserDetailsAdapter).user
        var owner = user.id
        if (requestData.public == true) {
            owner = 0
        }
        service.add(Phone(0, requestData.name, requestData.phone, owner), user.username)
        return "redirect:/app/list"
    }

    @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'com.example.demo.entity.Phone','DELETE') or hasPermission(#id, 'com.example.demo.entity.Phone','READ')")
    @GetMapping("/view/{id}")
    fun view(model: Model, @PathVariable("id") id: Long): String {
        model.addAttribute("phone", service.getById(id))
        return "view"
    }

    @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'com.example.demo.entity.Phone','DELETE')")
    @PostMapping("/view/{id}")
    fun update(requestData: RequestData, @PathVariable("id") id: Long): String {
        service.update(Phone(id, requestData.name, requestData.phone, 0))
        return "redirect:/app/view/$id"
    }

    @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'com.example.demo.entity.Phone','DELETE')")
    @GetMapping("/remove/{id}")
    fun remove(model: Model, @PathVariable("id") id: Long): String {
        model.addAttribute("phone", service.remove(id))
        return "redirect:/app/list"
    }
}