package com.example.demo.config

import com.example.demo.service.CustomUserDetailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.web.servlet.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder


@EnableWebSecurity
class SecurityConfigure : WebSecurityConfigurerAdapter() {

    @Autowired
    lateinit var myProvider: AuthenticationProvider

    override fun configure(http: HttpSecurity?) {
        http {
            authorizeRequests {
                authorize("/app/**", authenticated)
                authorize(anyRequest, permitAll)
            }
            formLogin { }


            csrf {
                //отключаем csrf для h2-console
                ignoringAntMatchers("/h2-console/*")
            }
            headers {
                frameOptions {
                    //разрешаем работу в iframe внутри домена для h2-console
                    sameOrigin = true
                }
            }

        }
    }

    /**
     * Подключаем наш настроенный AuthenticationProvider
     */
    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth!!.authenticationProvider(myProvider)
    }

    /**
     * Настраиваем DelegatingPasswordEncoder для поддержки нескольких типов хранимых паролей
     */
    @Bean
    fun delegatingPasswordEncoder(): DelegatingPasswordEncoder = DelegatingPasswordEncoder(
        "bcrypt", mapOf(
            "bcrypt" to BCryptPasswordEncoder() // для примера только один encoder
        )
    )

    /**
     * Настраиваем наш DaoAuthenticationProvider
     */
    @Bean
    fun authProvider(
        customUDS: CustomUserDetailService,
        delegatingPasswordEncoder: DelegatingPasswordEncoder
    ): AuthenticationProvider {
        val manager = DaoAuthenticationProvider()
        manager.setUserDetailsService(customUDS) //подключаем нашу реализацию UserDetailsService
        manager.setPasswordEncoder(delegatingPasswordEncoder) //подключаем наш настроенный DelegatingPasswordEncoder
        return manager
    }


}