package no.nav.helse.navepjauthserver.security

import com.nimbusds.jose.jwk.JWK
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
 @Value("\${helseid.openid.jwk}") private val helseidOpenJwk: String,
) {

  private val helseIdJwk = JWK.parse(helseidOpenJwk)

  @Bean
  @Order(1)
  fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
    val authConfig = OAuth2AuthorizationServerConfigurer()
    http {
      securityMatcher(authConfig.endpointsMatcher)
      with(authConfig) {
        oidc { Customizer.withDefaults<OAuth2AuthorizationServerConfigurer>() }
      }
      headers {
        frameOptions { sameOrigin }
      }

    }
    return http.build()
  }

  @Bean
  @Order(2)
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http {
      securityMatcher("/**")
      authorizeHttpRequests {
        authorize("/login/**", permitAll)
        authorize("/error/**", permitAll)
        authorize("/monitoring/**", permitAll)
        authorize(anyRequest, authenticated)
      }
      csrf { disable() }
      cors { configurationSource = corsConfigurationSource()
      }
    }
    return http.build()
  }

  private fun corsConfigurationSource(): CorsConfigurationSource {
    val config =
      CorsConfiguration().apply {
        allowedOriginPatterns =
          listOf("https://*.dev.nav.no", "http://localhost:[*]", "http://127.0.0.1:[*]")
        allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        allowedHeaders = listOf("Authorization", "Content-Type", "X-Requested-With")
        allowCredentials = true
        maxAge = 3600L
      }

    return UrlBasedCorsConfigurationSource().apply { registerCorsConfiguration("/**", config) }
  }
}
