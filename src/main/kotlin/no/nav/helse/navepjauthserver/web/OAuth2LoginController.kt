package no.nav.helse.navepjauthserver.web

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class OAuth2LoginController {

  @GetMapping("/")
  fun index(
    model: Model,
    @RegisteredOAuth2AuthorizedClient("nav-epj-auth-server") authorizedClient: OAuth2AuthorizedClient,
    @AuthenticationPrincipal oAuth2User: OAuth2User
  ): ResponseEntity<Model> {
    model.addAttribute("userName", oAuth2User.name)
    model.addAttribute("clientName", authorizedClient.clientRegistration.clientName)
    model.addAttribute("userAttributes", oAuth2User.attributes)
    return ResponseEntity.ok(model)
  }

  @GetMapping("/this-should/trigger-oauth2")
  fun protectedResource(): ResponseEntity<String> {
    return ResponseEntity.ok("Hello from protected resource!")
  }

}