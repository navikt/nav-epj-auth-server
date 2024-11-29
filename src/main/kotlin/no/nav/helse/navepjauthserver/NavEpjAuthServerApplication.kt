package no.nav.helse.navepjauthserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NavEpjAuthServerApplication

fun main(args: Array<String>) {
  runApplication<NavEpjAuthServerApplication>(*args)
}
