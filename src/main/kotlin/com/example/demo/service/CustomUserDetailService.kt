package com.example.demo.service

import com.example.demo.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
/**
 * Реализация интерфейса для получения UserDetails
 */
@Service
class CustomUserDetailService(val repo: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(p0: String?): UserDetails =
        repo.findByUsername(p0!!)?.toUserDetails() ?: throw UsernameNotFoundException("User not found")



}

