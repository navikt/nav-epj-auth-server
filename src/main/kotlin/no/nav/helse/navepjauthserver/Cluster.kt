package no.nav.helse.navepjauthserver

import no.nav.helse.navepjauthserver.Constants.DEV_GCP
import no.nav.helse.navepjauthserver.Constants.LOCAL
import no.nav.helse.navepjauthserver.Constants.NAIS_IMAGE_NAME
import no.nav.helse.navepjauthserver.Constants.NAIS_NAMESPACE_NAME
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
    internal const val NAIS_NAMESPACE_NAME = "NAIS_NAMESPACE"
    internal  const val NAIS_IMAGE_NAME = "NAIS_APP_IMAGE"
}

internal enum class Cluster(private val clusterName: String) {
    TEST_CLUSTER(TEST),
    LOCAL_CLUSTER(LOCAL),
    DEV_GCP_CLUSTER(DEV_GCP),
    PROD_GCP_CLUSTER(PROD_GCP);

    companion object {

        private val cluster = getenv(Constants.NAIS_CLUSTER_NAME) ?: Constants.LOCAL
        val current = entries.firstOrNull { it.clusterName == cluster } ?: LOCAL_CLUSTER
        val image = getenv(NAIS_IMAGE_NAME) ?: "n/a"
        val namespace = getenv(NAIS_NAMESPACE_NAME) ?: Constants.LOCAL
        val isProd = current == PROD_GCP_CLUSTER
        val isDev = current == DEV_GCP_CLUSTER
        val isNais = current in listOf(DEV_GCP_CLUSTER, PROD_GCP_CLUSTER)
        val profiler =
            when (current) {
                TEST_CLUSTER, LOCAL_CLUSTER ->
                    arrayOf(current.clusterName).also {
                        setProperty(Constants.NAIS_CLUSTER_NAME, current.clusterName)
                    }
                DEV_GCP_CLUSTER -> arrayOf(Constants.DEV, DEV_GCP, Constants.GCP)
                PROD_GCP_CLUSTER -> arrayOf(Constants.PROD, PROD_GCP, Constants.GCP)
            }
    }
}
