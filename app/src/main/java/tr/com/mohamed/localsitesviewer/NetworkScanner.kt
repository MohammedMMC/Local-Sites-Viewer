package tr.com.mohamed.localsitesviewer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class NetworkScanner {
    private val client = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.SECONDS)
        .readTimeout(1, TimeUnit.SECONDS)
        .writeTimeout(1, TimeUnit.SECONDS)
        .build();
    private val portsToCheck = listOf(3030, 5050, 5500, 3300, 80, 8080);
    private val subnetPrefix = "192.168.1.";
    private val maxConcurrentRequests = 50;

    suspend fun scanNetwork(): MutableList<List<String>> = coroutineScope {
        val activeUrls = mutableListOf<List<String>>();
        val semaphore = Semaphore(maxConcurrentRequests);

        coroutineScope {
            val jobs = (1..30).flatMap { host ->
                portsToCheck.map { port ->
                    async(Dispatchers.IO) {
                        val ip = "$subnetPrefix$host";
                        val url = "http://$ip:$port";

                        semaphore.withPermit {
                            val siteInfo = getUrlInfo(url);
                            if (siteInfo.isNotEmpty()) {
                                println("SUCCESS: ${siteInfo[1]}");
                                synchronized(activeUrls) {
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

    private fun getUrlInfo(url: String): List<String> {
        return try {
            val request = Request.Builder().url(url).build();
            val response = client.newCall(request).execute();
            listOf(getHTMLTitle(response.body.string()), url);
        } catch (e: Exception) {
            listOf();
        }
    }

    private fun getHTMLTitle(input: String): String = "<title>(.*?)</title>".toRegex()
        .find(input)?.groupValues?.get(1) ?: "";
}

suspend fun <T> Semaphore.withPermit(block: suspend () -> T): T {
    acquire()
    return try {
        block();
    } finally {
        release();
    }
}