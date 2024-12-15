package tr.com.mohamed.localsitesviewer;

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tr.com.mohamed.localsitesviewer.ui.theme.LocalSitesViewerTheme
import tr.com.mohamed.localsitesviewer.ui.theme.Typography

class MainActivity : ComponentActivity() {
    private val networkScanner = NetworkScanner();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        enableEdgeToEdge();
        // setContentView(R.layout.activity_main);

        setContent {
            MyApp();
        }

//        val scanButton: Button = findViewById(R.id.scanButton);
//        val resultTextView: TextView = findViewById(R.id.resultTextView);
//
//        scanButton.setOnClickListener {
//            resultTextView.text = "Scanning...";
//
//            CoroutineScope(Dispatchers.Main).launch {
//                val activeSites = networkScanner.scanNetwork();
//                resultTextView.text = if (activeSites.isEmpty()) {
//                    "No active sites found on the network."
//                } else {
//                    activeSites.joinToString(separator = "\n");
//                }
//            }
//        }
    }
}

@Composable
fun MyApp() {
    val isLoading = remember { mutableStateOf(false) }
    val isClicked = remember { mutableStateOf(false) }
    val activeSites = remember { mutableStateOf<List<List<String>>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LocalSitesViewerTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { innerPadding ->
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
                            isClicked.value = true;
                            isLoading.value = true;
                            coroutineScope.launch {
                                val aSites = NetworkScanner().scanNetwork();
                                activeSites.value = aSites;
                                isLoading.value = false;
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Load Local Sites",
                            style = Typography.bodyLarge
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
                        if (activeSites.value.isNotEmpty()) {
                            items(activeSites.value) {
                                HostCards(it)
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun HostCards(siteInfo: List<String>) {
    val title = siteInfo[0];
    val ip = siteInfo[1];
    Card(
        modifier = Modifier
            .fillMaxSize()
            .height(100.dp)
            .padding(top = 10.dp),
        border = BorderStroke(width = 2.dp, color = Color.Blue)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title.ifEmpty { "No Title!" },
                style = Typography.titleLarge
            )
            Text(
                text = ip,
                style = Typography.bodyLarge
            )
        }
    }
}