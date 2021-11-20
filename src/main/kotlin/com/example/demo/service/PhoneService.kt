package com.example.demo.service

import com.example.demo.entity.Phone
import com.example.demo.repository.PhoneRepository
import javassist.NotFoundException
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.domain.GrantedAuthoritySid
import org.springframework.security.acls.domain.ObjectIdentityImpl
import org.springframework.security.acls.domain.PrincipalSid
import org.springframework.security.acls.jdbc.JdbcMutableAclService
import org.springframework.stereotype.Service

@Service
class PhoneService(val repo: PhoneRepository, val aclService: JdbcMutableAclService) {

    /**
     * Выбираем все записи и возвращаем только доступные клиенту
     */
    @PostFilter("hasRole('ADMIN') or hasPermission(filterObject, 'READ') or hasPermission(filterObject, 'DELETE')")
    fun getPhones(): Iterable<Phone> = repo.findAll()

    /**
     * Лучше ограничивать выборку по доступным элементам чем фильтровать уже полученные
     */
    fun getByUserId(id: Long): Iterable<Phone> = repo.findByOwner(id)

    fun getById(id: Long): Phone = repo.findById(id).get()

    fun remove(id: Long) {
        repo.deleteById(id)
    }

    /**
     * Во время создания так же не забываем создавать сущности ACL
     */
    fun add(draft: Phone, username: String): Phone {
        val phone = repo.save(draft)
        createAclForOwner(phone, username)
        return phone
    }

    /**
     * Создание ACL_OBJECT_ENTRY и набор необходимых ACL_ENTRY.
     * Для личных телефонов - добавляем DELETE для владельца
     * Для публичных - дополнительно даем право READ для ROLE_USER группы
     */
    private fun createAclForOwner(
        phone: Phone,
        username: String
    ) {
        val identity = ObjectIdentityImpl(phone)

        val acl = aclService.createAcl(identity)
        acl.insertAce(acl.entries.size, BasePermission.DELETE, PrincipalSid(username), true)
        if (phone.owner == 0L) {
            acl.insertAce(acl.entries.size, BasePermission.READ, GrantedAuthoritySid("ROLE_USER"), true)
        }
        acl.isEntriesInheriting = false

        aclService.updateAcl(acl)
    }

    fun update(phone: Phone): Phone {
        val persisted = repo.findById(phone.id).orElseThrow { NotFoundException("not found") }
        persisted.name = phone.name
        persisted.phone = phone.phone
        repo.save(phone)
        return persisted
    }
}