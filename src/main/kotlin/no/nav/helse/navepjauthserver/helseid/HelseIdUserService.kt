package no.nav.helse.navepjauthserver.helseid

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service

@Service
class HelseIdUserService : OAuth2UserService<OidcUserRequest, OidcUser> {
  override fun loadUser(userRequest: OidcUserRequest?): OidcUser {
    requireNotNull(userRequest) { "userRequest must not be null" }

    // Extract relevant claims from the ID token
    val userFhirId = userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName

    /** TODO
     * This should be matched via HelseID Behandler Kategori, so for example
     * LE = Practitioner
     */
    val userFhirType = "Practitioner"

    // Create user info with SMART-specific claims
    val userInfo = OidcUserInfo.builder()
      .subject(userFhirId)
      .claim(
        "fhirUser",
        "$userFhirType/$userFhirId"
      ) // This should be mapped to your FHIR server's practitioner
      .profile("$userFhirType/$userFhirId")
      .build()

    return object : OidcUser {
      override fun getName(): String = userFhirId

      override fun getAttributes(): MutableMap<String, Any> = userInfo.claims

      override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        mutableListOf(SimpleGrantedAuthority("ROLE_USER"))

      override fun getClaims(): MutableMap<String, Any> = userInfo.claims

      override fun getUserInfo(): OidcUserInfo = userInfo

      override fun getIdToken(): OidcIdToken =
        OidcIdToken.withTokenValue("token").claim("sub", userFhirId).build()
    }
  }

}
