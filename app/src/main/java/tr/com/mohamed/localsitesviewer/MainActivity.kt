package tr.com.mohamed.localsitesviewer;

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tr.com.mohamed.localsitesviewer.ui.theme.LocalSitesViewerTheme
import tr.com.mohamed.localsitesviewer.ui.theme.Typography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        enableEdgeToEdge();

        setContent {
            MyApp();
        }
    }
}

@Composable
fun MyApp() {
    val isLoading = remember { mutableStateOf(false) }
    val isClicked = remember { mutableStateOf(false) }
    val searchIpId = remember { mutableStateOf("1") }
    val portsToCheck = remember { mutableStateOf(listOf<String>("3030", "5050", "5500", "3300", "80", "8080")) }
    val activeSites = remember { mutableStateOf<List<NetworkScanRes>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    fun onLoadingButtonClicked() {
        if (isLoading.value) return;
        isClicked.value = true;
        isLoading.value = true;

        coroutineScope.launch {
            val aSites = NetworkScanner(
                searchIpId = searchIpId.value,
                portsToCheck = portsToCheck.value
            ).scanNetwork();
            activeSites.value = aSites;
            isLoading.value = false;
        }
    }

    LaunchedEffect(Unit) {
        onLoadingButtonClicked();
    }

    LocalSitesViewerTheme {
        Scaffold(modifier = Modifier.fillMaxSize(), content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding(),
                        start = 15.dp,
                        end = 15.dp
                    )
                    .statusBarsPadding()
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "IP RANGE:  ",
                        style = Typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "192.168.",
                        style = Typography.titleLarge,
                        color = Color.Gray
                    )
                    BasicTextField(
                        modifier = Modifier
                            .width(IntrinsicSize.Min)
                            .widthIn(min = 20.dp),
                        value = searchIpId.value,
                        textStyle = Typography.titleLarge.plus(
                            TextStyle(
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        ),
                        onValueChange = { icv ->
                            if (icv.isEmpty() || icv.all { it.isDigit() } && icv.toIntOrNull()
                                    ?.let { it in 0..254 } == true) searchIpId.value = icv
                        }
                    )
                    Text(
                        text = ".*",
                        style = Typography.titleLarge,
                        color = Color.Gray
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (port in portsToCheck.value) {
                        PortsCard(port);
                    }
                }
                Button(
                    onClick = {
                        onLoadingButtonClicked();
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = "Load Local Sites", style = Typography.bodyLarge,
                        modifier = Modifier.padding(5.dp)
                    )
                }

                if (isLoading.value) {
                    Text(text = "Loading...", modifier = Modifier.padding(top = 15.dp))
                }

                if (!isLoading.value && activeSites.value.isEmpty()) {
                    Text(text = "No Sites Found", modifier = Modifier.padding(top = 15.dp))
                }

                LazyColumn(
                    modifier = Modifier.padding(top = 15.dp)
                ) {
                    if (!isLoading.value && activeSites.value.isNotEmpty()) {
                        items(activeSites.value) {
                            HostCard(it)
                        }
                    }
                }
            }
        })
    }
}

@Composable
fun PortsCard(port: String) {

}

@Composable
fun HostCard(siteInfo: NetworkScanRes) {
    val ctx = LocalContext.current;

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 15.dp),
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    vertical = 10.dp,
                    horizontal = 15.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = siteInfo.title.ifEmpty { "No Title" }, style = Typography.titleLarge
            )
            Text(
                text = siteInfo.ip.ifEmpty { siteInfo.url }, style = Typography.bodyLarge
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { openURL(siteInfo.url, ctx); },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "External", style = Typography.bodyLarge)
                }
                Button(
                    onClick = { openUrlInAppBrowser(siteInfo.url, ctx); },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Internal", style = Typography.bodyLarge)
                }
            }
        }
    }
}

fun openURL(url: String, ctx: Context) =
    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)));

fun openUrlInAppBrowser(url: String, ctx: Context) =
    CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build().launchUrl(ctx, Uri.parse(url))