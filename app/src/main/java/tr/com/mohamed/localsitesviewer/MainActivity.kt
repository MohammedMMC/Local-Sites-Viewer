package tr.com.mohamed.localsitesviewer;

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONArray
import tr.com.mohamed.localsitesviewer.ui.theme.LocalSitesViewerTheme
import tr.com.mohamed.localsitesviewer.ui.theme.Typography
import java.net.NetworkInterface

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
    val ctx = LocalContext.current;
    val isLoading = remember { mutableStateOf(false) }
    val isClicked = remember { mutableStateOf(false) }
    val subnetPrefix = remember { mutableStateOf("") }
    val portsToCheck = remember { mutableStateOf(getList(ctx, "ports")) }
    val activeSites = remember { mutableStateOf<List<NetworkScanRes>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    if (portsToCheck.value.isEmpty()) {
        portsToCheck.value = saveList(
            ctx, "ports", listOf<String>(
                "+ Add",
                "80",
                "8080",
                "3300",
                "3030",
                "5500",
                "5050"
            )
        )
    }

    fun getLocalIp(): String {
        return NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { it.inetAddresses.toList() }
            .firstOrNull { !it.isLoopbackAddress && it.hostAddress?.contains(':') == false }
            ?.hostAddress ?: "192.168.1.1";
    }

    fun onLoadingButtonClicked() {
        if (isLoading.value) return;
        subnetPrefix.value = getLocalIp().substringBeforeLast(".");
        isClicked.value = true;
        isLoading.value = true;

        coroutineScope.launch {
            val aSites = NetworkScanner(
                subnetPrefix = subnetPrefix.value,
                portsToCheck = portsToCheck.value
            ).scanNetwork();
            activeSites.value = aSites;
            isLoading.value = false;
        }
    }

    fun showModal(
        port: String,
        forDelete: Boolean = false, context: Context
    ) {
        val portInput = EditText(context);
        portInput.hint = "Your port, Example: 3030, 3300";

        val builder = AlertDialog.Builder(context);
        builder.setTitle(if (forDelete) "Port: $port" else "Add a Port")
            .setMessage("Are you sure you want to delete the port?")
            .setPositiveButton(if (forDelete) "Delete" else "Add") { _, _ ->
                if (forDelete) {
                    portsToCheck.value = portsToCheck.value.filter { it != port };
                    saveList(context, "ports", portsToCheck.value);
                } else {
                    val port2Add = portInput.text.toString();

                    if (port2Add.contains(",")) {
                        val ports2Add = port2Add.split(",");
                        ports2Add.forEach {
                            portsToCheck.value += it.filter { it != ' ' };
                        }
                    } else {
                        portsToCheck.value += port2Add;
                    }

                    saveList(context, "ports", portsToCheck.value);
                }
                Toast.makeText(
                    context,
                    if (forDelete) "Deleted Successfully!" else "Added Successfully!",
                    Toast.LENGTH_SHORT
                ).show();
            }
        builder.setNeutralButton("Cancel") { dialog, _ -> dialog.dismiss() }
        if (!forDelete) {
            builder.setView(portInput);
        }

        val dialog = builder.create();
        dialog.show();
    }

    @Composable
    fun PortsCard(port: String, modifier: Modifier = Modifier) {
        if (port == "+ Add") {
            Card(
                onClick = {
                    showModal(
                        forDelete = false,
                        context = ctx,
                        port = port
                    )
                },
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = port,
                    modifier = Modifier.padding(
                        vertical = 15.dp,
                        horizontal = 10.dp
                    ),
                );
            }
            return;
        }

        Card(
            onClick = {
                showModal(
                    forDelete = true,
                    context = ctx,
                    port = port
                )
            },
            modifier = modifier,
            border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = port,
                modifier = Modifier.padding(vertical = 15.dp, horizontal = 10.dp),
            );
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
                Card(
                    border = BorderStroke(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${subnetPrefix.value}.*",
                            style = Typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
//                        BasicTextField(
//                            modifier = Modifier
//                                .width(IntrinsicSize.Min)
//                                .widthIn(min = 20.dp),
//                            value = subnetPrefix.value,
//                            textStyle = Typography.titleLarge.plus(
//                                TextStyle(
//                                    textAlign = TextAlign.Center,
//                                    color = MaterialTheme.colorScheme.onSurface
//                                )
//                            ),
//                            onValueChange = { icv ->
//                                if (icv.isEmpty() || icv.all { it.isDigit() } && icv.toIntOrNull()
//                                        ?.let { it in 0..254 } == true) subnetPrefix.value = icv
//                            }
//                        )
//                        Text(
//                            text = ".*",
//                            style = Typography.titleLarge,
//                            color = Color.Gray
//                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 15.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.padding(top = 15.dp)
                    ) {
                        LazyRow {
                            items(portsToCheck.value) {
                                if (it == "+ Add") {
                                    PortsCard(port = it);
                                } else {
                                    PortsCard(
                                        modifier = Modifier.padding(start = 15.dp),
                                        port = it
                                    );
                                }
                            }
                        }
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

fun saveList(context: Context, key: String, list: List<String>): List<String> {
    val jsonArray = JSONArray(list);
    context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        .edit().putString(key, jsonArray.toString()).apply();
    return list;
}

fun getList(context: Context, key: String): List<String> {
    val jsonString = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        .getString(key, "[]") ?: "[]";
    return List(JSONArray(jsonString).length()) { i ->
        JSONArray(jsonString).getString(i)
    }
}