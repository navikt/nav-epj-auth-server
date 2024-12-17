package no.nav.helse.navepjauthserver.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity(debug = true)
class SecurityConfig() {

  @Bean
  @Order(1)
  fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
    val authConfig = OAuth2AuthorizationServerConfigurer.authorizationServer()
    http.securityMatcher(authConfig.endpointsMatcher).with(authConfig) { authServer ->
      authServer.oidc(Customizer.withDefaults())
    }
      .headers { headers ->
        headers.frameOptions { frameOptions -> frameOptions.sameOrigin() }
      }
      .exceptionHandling { exHandler ->
        exHandler.defaultAuthenticationEntryPointFor(
          LoginUrlAuthenticationEntryPoint("/login"),
          MediaTypeRequestMatcher(MediaType.TEXT_HTML)
        )
      }
    return http.build()
  }

  @Bean
  @Order(2)
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http {
      authorizeHttpRequests {
        authorize("/login/**", permitAll)
        authorize("/error/**", permitAll)
        authorize("/internal/**", permitAll)
        authorize(anyRequest, authenticated)
      }
      csrf { disable() }
      cors { configurationSource = corsConfigurationSource() }
      formLogin { }
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
