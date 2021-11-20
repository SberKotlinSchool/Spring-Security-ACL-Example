package com.example.demo.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.ehcache.EhCacheFactoryBean
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.acls.AclPermissionCacheOptimizer
import org.springframework.security.acls.AclPermissionEvaluator
import org.springframework.security.acls.domain.*
import org.springframework.security.acls.jdbc.BasicLookupStrategy
import org.springframework.security.acls.jdbc.JdbcMutableAclService
import org.springframework.security.acls.jdbc.LookupStrategy
import org.springframework.security.acls.model.PermissionGrantingStrategy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import javax.sql.DataSource

/**
 * Настраиваем бины ACL
 */
@Configuration
class AclConfigure {

    @Autowired
    lateinit var dataSource: DataSource

    /**
     * Настраиваем стратегию управления ACL для записей
     */
    @Bean
    fun aclAuthorizationStrategy(): AclAuthorizationStrategy {
        //Для редактирования ACL объекта требуется быть владельцем, иметь право ADMINISTRATE или быть соответствующей роли
        return AclAuthorizationStrategyImpl(
            SimpleGrantedAuthority("ROLE_ADMIN") //Указываем роль(SID), с которой можно редактировать все ACL
        )
    }

    /**
     * Настраиваем стратегию определения доступа.
     * DefaultPermissionGrantingStrategy требует полного соответствия маски ACL_ENTRY.mask и проверяемого permission
     * Если требуется например проверять наличие бита в маске - требуется переопределить метод .isGranted()
     */
    @Bean
    fun permissionGrantingStrategy(): PermissionGrantingStrategy {
        return DefaultPermissionGrantingStrategy(
            ConsoleAuditLogger()
        )
    }

    /**
     * Настройка кэша для ACL
     */
    @Bean
    fun aclCache(
        cacheFactory: EhCacheFactoryBean,
        permissionGrantingStrategy: PermissionGrantingStrategy,
        authorizationStrategy: AclAuthorizationStrategy
    ): EhCacheBasedAclCache {
        return EhCacheBasedAclCache(
            cacheFactory.getObject(),
            permissionGrantingStrategy,
            authorizationStrategy
        )
    }

    /**
     * Настройка фабрики кэш менеджера
     */
    @Bean
    fun aclCacheManager(): EhCacheManagerFactoryBean {
        return EhCacheManagerFactoryBean()
    }

    /**
     * Настройка фабрики кэша
     */
    @Bean
    fun ehCacheFactoryBean(cacheManagerFactory: EhCacheManagerFactoryBean): EhCacheFactoryBean {
        val ehCacheFactoryBean = EhCacheFactoryBean()
        ehCacheFactoryBean.setCacheManager(cacheManagerFactory.getObject()!!)
        ehCacheFactoryBean.setCacheName("aclCache")
        return ehCacheFactoryBean
    }

    /**
     * Настраиваем стратегию поиска записей ACL. Используем базовый вариант
     */
    @Bean
    fun lookupStrategy(
        aclCache: EhCacheBasedAclCache,
        authorizationStrategy: AclAuthorizationStrategy
    ): LookupStrategy {
        return BasicLookupStrategy(
            dataSource,
            aclCache,
            authorizationStrategy,
            ConsoleAuditLogger()
        )
    }

    /**
     * Настраиваем сервис для управления ACL
     */
    @Bean
    fun aclService(lookupStrategy: LookupStrategy, aclCache: EhCacheBasedAclCache): JdbcMutableAclService {
        return JdbcMutableAclService(
            dataSource, lookupStrategy, aclCache
        )
    }

    /**
     * Настраиваем AclPermissionEvaluator для работы ACL в hasPermission() в выражениях
     * При отсутствии реализации PermissionEvaluator в контексте, будет использоваться DenyAllPermissionEvaluator который запрещает доступ во всех случаях
     */
    @Bean
    fun permissionEvaluator(aclService: JdbcMutableAclService): PermissionEvaluator {
        return AclPermissionEvaluator(aclService)
    }

    /**
     * Настраиваем DefaultMethodSecurityExpressionHandler для PrePost защиты методов
     */
    @Bean
    fun expressionHandlerAcl(
        aclService: JdbcMutableAclService,
        aclPermissionEvaluator: PermissionEvaluator
    ): MethodSecurityExpressionHandler {
        val expressionHandler = DefaultMethodSecurityExpressionHandler()
        expressionHandler.setPermissionEvaluator(aclPermissionEvaluator)
        expressionHandler.setPermissionCacheOptimizer(AclPermissionCacheOptimizer(aclService))
        return expressionHandler
    }

}