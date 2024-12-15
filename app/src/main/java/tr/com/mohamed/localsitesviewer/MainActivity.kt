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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val activeSites = remember { mutableStateOf<List<List<String>>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    fun onLoadingButtonClicked() {
        if (isLoading.value) return;
        isClicked.value = true;
        isLoading.value = true;

        coroutineScope.launch {
            val aSites = NetworkScanner().scanNetwork();
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
                            HostCards(it)
                        }
                    }
                }
            }
        })
    }
}

@Composable
fun HostCards(siteInfo: List<String>) {
    val ctx = LocalContext.current;

    val title = siteInfo[0];
    val url = siteInfo[1];
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
                text = title.ifEmpty { "No Title" }, style = Typography.titleLarge
            )
            Text(
                text = url, style = Typography.bodyLarge
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { openURL(url, ctx); },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "External", style = Typography.bodyLarge)
                }
                Button(
                    onClick = { openUrlInAppBrowser(url, ctx); },
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