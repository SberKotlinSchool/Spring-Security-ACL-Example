package com.example.demo.repository

import com.example.demo.entity.Phone
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PhoneRepository: JpaRepository<Phone, Long> {

   @Query("SELECT p FROM Phone p WHERE p.owner = ?1 or p.owner = 0")
   fun findByOwner(owner: Long): Iterable<Phone>

}