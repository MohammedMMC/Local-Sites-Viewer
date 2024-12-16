package tr.com.mohamed.localsitesviewer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class NetworkScanner(
    searchIpId: String,
    private val portsToCheck: List<String>,
    timeout: Long = 1,
    private val maxConcurrentRequests: Int = 30
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(timeout, TimeUnit.SECONDS)
        .readTimeout(timeout, TimeUnit.SECONDS)
        .writeTimeout(timeout, TimeUnit.SECONDS)
        .build();
    private var subnetPrefix = "192.168.$searchIpId";

    suspend fun scanNetwork(): MutableList<NetworkScanRes> = coroutineScope {
        val activeUrls = mutableListOf<NetworkScanRes>();
        val semaphore = Semaphore(maxConcurrentRequests);

        coroutineScope {
            val jobs = (1..30).flatMap { host ->
                portsToCheck.map { port ->
                    async(Dispatchers.IO) {
                        val ip = "$subnetPrefix.$host:$port";

                        semaphore.withPermit {
                            val siteInfo = getUrlInfo("http://$ip");
                            if (siteInfo !== null) {
                                println("SUCCESS: ${siteInfo.url}");
                                synchronized(activeUrls) {
                                    siteInfo.ip = ip;
                                    activeUrls.add(siteInfo);
                                }
                            }
                        }
                    }
                }
            }

            jobs.awaitAll()
        }

        activeUrls;
    }

    private fun getUrlInfo(url: String): NetworkScanRes? {
        return try {
            val request = Request.Builder().url(url).build();
            val response = client.newCall(request).execute();
            NetworkScanRes(url, getHTMLTitle(response.body.string()));
        } catch (e: Exception) {
             null;
        }
    }

    private fun getHTMLTitle(input: String): String = "<title>(.*?)</title>".toRegex()
        .find(input)?.groupValues?.get(1) ?: "";
}

class NetworkScanRes (
    val url: String = "",
    val title: String = "",
    var ip: String = ""
) {

}

suspend fun <T> Semaphore.withPermit(block: suspend () -> T): T {
    acquire()
    return try {
        block();
    } finally {
        release();
    }
}