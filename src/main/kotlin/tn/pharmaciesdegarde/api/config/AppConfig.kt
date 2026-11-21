package tn.pharmaciesdegarde.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class AppConfig {

    @Bean //Parce que tu pourras ensuite injecter automatiquement cet objet dans les autres classes grâce à l'injection de dépendances
    fun restTemplate(): RestTemplate {
        val factory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(5000)
            setReadTimeout(5000)
        }
        return RestTemplate(factory)
    }
}