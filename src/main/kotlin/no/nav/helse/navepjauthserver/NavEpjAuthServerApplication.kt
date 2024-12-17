package no.nav.helse.navepjauthserver

import no.nav.helse.navepjauthserver.Cluster.Companion.profiler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class NavEpjAuthServerApplication

fun main(args: Array<String>) {
    runApplication<NavEpjAuthServerApplication>(*args){
        setAdditionalProfiles(*profiler)
    }
}
