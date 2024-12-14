package tr.com.mohamed.localsitesviewer;

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.OkHttpClient
import okhttp3.Request
import tr.com.mohamed.localsitesviewer.ui.theme.LocalSitesViewerTheme
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView;
    private val foundHosts = mutableListOf<String>();
    private val client = OkHttpClient.Builder()
        .connectTimeout(500, TimeUnit.MILLISECONDS)
        .readTimeout(100, TimeUnit.MILLISECONDS)
        .build();

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        enableEdgeToEdge();
        setContentView(R.layout.activity_main);

        val scanButton: Button = findViewById(R.id.scanButton);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.layoutManager = LinearLayoutManager(this);

        val adapter = HostsAdapter(foundHosts);
        recyclerView.adapter = adapter;

        scanButton.setOnClickListener {
            foundHosts.clear();
            adapter.notifyDataSetChanged();
            thread { scanNetwork(adapter); }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun scanNetwork(adapter: HostsAdapter) {
        val baseIp = "192.168.1.";
        val ports = listOf(3030, 5050, 5500, 3300, 80, 8080);

        for (i in 1..254) {
            for (port in ports) {
                val url = "http://$baseIp$i:$port";
                if (isHostAvailable(url)) {
                    foundHosts.add(url);
                    runOnUiThread { adapter.notifyDataSetChanged(); }
                }
            }
        }
    }

    private fun isHostAvailable(url: String): Boolean {
        return try {
            val request = Request.Builder().url(url).build();
            val response = client.newCall(request).execute();
            println("SUCCESS: $url");
            response.isSuccessful;
        } catch (e: Exception) {
            println("Failed: $url");
            false;
        }
    }
}
