package no.nav.helse.navepjauthserver.smart

import org.slf4j.LoggerFactory.getLogger
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer

// TODO inject datasource
class FederatedIdentityIdTokenCustomizer : OAuth2TokenCustomizer<JwtEncodingContext> {

  companion object {
    private val log = getLogger(FederatedIdentityIdTokenCustomizer::class.java)
    private val ID_TOKEN_CLAIMS = hashSetOf(
      IdTokenClaimNames.ISS,
      IdTokenClaimNames.SUB,
      IdTokenClaimNames.AUD,
      IdTokenClaimNames.EXP,
      IdTokenClaimNames.IAT,
      IdTokenClaimNames.AUTH_TIME,
      IdTokenClaimNames.NONCE,
      IdTokenClaimNames.ACR,
      IdTokenClaimNames.AMR,
      IdTokenClaimNames.AZP,
      IdTokenClaimNames.AT_HASH,
      IdTokenClaimNames.C_HASH
    )
  }

  override fun customize(context: JwtEncodingContext?) {
    requireNotNull(context) { "JWT encoding context must not be null" }

    val thirdPartyClaims = extractClaims(context.getPrincipal())

    log.info("token type: {}", context.tokenType.value)

    when (context.tokenType.value) {
      OidcParameterNames.ID_TOKEN -> {
        context.claims.claims { existingClaims ->
          // Remove conflicting claims set by this authorization server
          existingClaims.keys.removeAll(thirdPartyClaims.keys)

          // Remove standard id_token claims that could cause problems with clients
          ID_TOKEN_CLAIMS.removeAll(thirdPartyClaims.keys)

          // Add all other claims directly to id_token
          existingClaims.putAll(thirdPartyClaims)

          val principal = context.getPrincipal<Authentication>()

          if (principal is OidcUser) {
            existingClaims["fhirUser"] =
              "Practitioner/${principal.subject}" // TODO get from FHIR datasource
          }
        }
      }
    }
  }

  private fun extractClaims(principal: Authentication): Map<String, Any> {
    log.info("principal: {}", principal)
    val claims = when (val userPrincipal = principal.principal) {
      is OidcUser -> userPrincipal.idToken?.claims.orEmpty()
      is OAuth2User -> userPrincipal.attributes
      else -> emptyMap()
    }

    return claims.toMutableMap()
  }

}