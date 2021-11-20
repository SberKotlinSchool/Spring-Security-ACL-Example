package com.example.demo.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration

/**
 * Для работы Pre/Post Authorize/Filter требуется настройка MethodSecurityExpressionHandler
 */
@EnableGlobalMethodSecurity(prePostEnabled = true)
class GlobalMethodSecurityConfigure : GlobalMethodSecurityConfiguration() {

    @Autowired
    lateinit var aclExprHandl: MethodSecurityExpressionHandler

    /**
     * Подключаем наш настроенный MethodSecurityExpressionHandler
     */
    override fun createExpressionHandler(): MethodSecurityExpressionHandler = aclExprHandl

}