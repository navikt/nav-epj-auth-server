package no.nav.helse.navepjauthserver

import no.nav.helse.navepjauthserver.Cluster.Companion.profiler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@SpringBootApplication
@EnableWebSecurity(debug = true)
class NavEpjAuthServerApplication

fun main(args: Array<String>) {
    runApplication<NavEpjAuthServerApplication>(*args){
        setAdditionalProfiles(*profiler)
    }
}
