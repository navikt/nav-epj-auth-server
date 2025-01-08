package no.nav.helse.navepjauthserver.security

import com.nimbusds.jose.jwk.JWK
import no.nav.helse.navepjauthserver.smart.FederatedIdentityIdTokenCustomizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.security.config.Customizer
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.client.endpoint.NimbusJwtClientAuthenticationParametersConverter
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers.withPkce
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
  @Value("\${helseid.openid.jwk}") private val helseIdOpenidJwk: String
) {

  @Bean
  @Order(1)
  fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
    val authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer.authorizationServer()
    http {
      securityMatcher(authorizationServerConfigurer.endpointsMatcher)

      with(authorizationServerConfigurer) {
        oidc { } // Enable OpenID Connect 1.0
      }

      authorizeHttpRequests {
        authorize(anyRequest, authenticated)
      }

      exceptionHandling {
        defaultAuthenticationEntryPointFor(
          LoginUrlAuthenticationEntryPoint("/oauth2/authorization/nav-epj-auth-server"),
          MediaTypeRequestMatcher(MediaType.TEXT_HTML)
        )
      }

      headers {
        frameOptions { sameOrigin }
      }
    }
    return http.build()
  }

  @Bean
  @Order(2)
  fun securityFilterChain(
    http: HttpSecurity,
    repo: ClientRegistrationRepository,
    tokenResponseClient: RestClientAuthorizationCodeTokenResponseClient,
  ): SecurityFilterChain {
    http {
      authorizeHttpRequests {
        authorize("/login/**", permitAll)
        authorize("/error/**", permitAll)
        authorize("/monitoring/**", permitAll)
        authorize(anyRequest, authenticated)
      }

      oauth2Login {

        authorizationEndpoint {
          authorizationRequestResolver = pkceAddingResolver(repo)
        }

        tokenEndpoint {
          accessTokenResponseClient = tokenResponseClient
        }

      }

      csrf { disable() }

      cors {
        configurationSource = corsConfigurationSource()
      }
    }
    return http.build()
  }

  @Bean
  fun oauth2TokenCustomizer(): OAuth2TokenCustomizer<JwtEncodingContext> {
    return FederatedIdentityIdTokenCustomizer()
  }

  @Bean
  fun tokenResponseClient(): RestClientAuthorizationCodeTokenResponseClient {
    return RestClientAuthorizationCodeTokenResponseClient().apply {
      setParametersConverter(NimbusJwtClientAuthenticationParametersConverter {
        when (it.registrationId) {
          "nav-epj-auth-server" -> JWK.parse(helseIdOpenidJwk)
          else -> throw IllegalArgumentException("Unknown client: ${it.registrationId}")
        }
      })
    }
  }

  private fun pkceAddingResolver(repo: ClientRegistrationRepository): DefaultOAuth2AuthorizationRequestResolver =
    DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization").apply {
      setAuthorizationRequestCustomizer(withPkce());
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
