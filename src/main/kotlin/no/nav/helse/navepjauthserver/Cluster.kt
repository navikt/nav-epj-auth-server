package no.nav.helse.navepjauthserver

import no.nav.helse.navepjauthserver.Constants.DEV
import no.nav.helse.navepjauthserver.Constants.DEV_GCP
import no.nav.helse.navepjauthserver.Constants.GCP
import no.nav.helse.navepjauthserver.Constants.LOCAL
import no.nav.helse.navepjauthserver.Constants.NAIS_CLUSTER_NAME
import no.nav.helse.navepjauthserver.Constants.PROD
import no.nav.helse.navepjauthserver.Constants.PROD_GCP
import no.nav.helse.navepjauthserver.Constants.TEST
import java.lang.System.*

internal object Constants {
    internal const val LOCAL = "local"
    internal const val GCP = "gcp"
    internal const val TEST = "test"
    internal const val DEV = "dev"
    internal const val PROD = "prod"
    internal  const val DEV_GCP = "${DEV}-${GCP}"
    internal const val PROD_GCP = "${PROD}-${GCP}"
    internal const val NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME"
}

internal enum class Cluster(private val clusterName: String) {
    TEST_CLUSTER(TEST),
    LOCAL_CLUSTER(LOCAL),
    DEV_GCP_CLUSTER(DEV_GCP),
    PROD_GCP_CLUSTER(PROD_GCP);

    companion object {

        private val cluster = getenv(NAIS_CLUSTER_NAME) ?: LOCAL
        val current = entries.firstOrNull { it.clusterName == cluster } ?: LOCAL_CLUSTER
        val isProd = current == PROD_GCP_CLUSTER
        val isDev = current == DEV_GCP_CLUSTER
        val isNais = isProd || isDev
        val profiler =
            when (current) {
                TEST_CLUSTER, LOCAL_CLUSTER ->
                    arrayOf(current.clusterName).also {
                        setProperty(NAIS_CLUSTER_NAME, current.clusterName)
                    }
                DEV_GCP_CLUSTER -> arrayOf(DEV, DEV_GCP, GCP)
                PROD_GCP_CLUSTER -> arrayOf(PROD, PROD_GCP, GCP)
            }
    }
}
