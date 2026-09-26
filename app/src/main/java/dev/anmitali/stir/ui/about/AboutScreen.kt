package dev.anmitali.stir.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class Attribution(val name: String, val license: String, val licenseUrl: String)

private val LIBRARIES = listOf(
    Attribution("AndroidX Core", "Apache License 2.0", "https://developer.android.com/jetpack/androidx/releases/core"),
    Attribution("AndroidX Lifecycle", "Apache License 2.0", "https://developer.android.com/jetpack/androidx/releases/lifecycle"),
    Attribution("AndroidX Activity Compose", "Apache License 2.0", "https://developer.android.com/jetpack/androidx/releases/activity"),
    Attribution("Jetpack Compose (UI, Material 3, Material Icons)", "Apache License 2.0", "https://developer.android.com/jetpack/androidx/releases/compose"),
    Attribution("AndroidX Navigation Compose", "Apache License 2.0", "https://developer.android.com/jetpack/androidx/releases/navigation"),
    Attribution("AndroidX Room", "Apache License 2.0", "https://developer.android.com/jetpack/androidx/releases/room"),
    Attribution("AndroidX DataStore", "Apache License 2.0", "https://developer.android.com/jetpack/androidx/releases/datastore"),
    Attribution("Kotlin and kotlinx.coroutines", "Apache License 2.0", "https://github.com/Kotlin/kotlinx.coroutines"),
)

private const val APACHE_LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    var selected by remember { mutableStateOf<Attribution?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Stir", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Stir",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A calm, offline alarm clock.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column {
                            Text("Author", style = MaterialTheme.typography.titleMedium)
                            Text("AnmiTaliDev", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("anmitalidev@nuros.org", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Column {
                            Text("License", style = MaterialTheme.typography.titleMedium)
                            Text("GNU General Public License v3.0", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Open source libraries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        LIBRARIES.forEachIndexed { index, library ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selected = library }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(library.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text(library.license, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (index < LIBRARIES.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selected?.let { library ->
        LicenseDialog(library = library, onDismiss = { selected = null })
    }
}

@Composable
private fun LicenseDialog(library: Attribution, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text(library.name) },
        text = {
            Text(
                "Licensed under the ${library.license}. You may use, modify, and redistribute this " +
                    "library under its terms, including in closed-source projects, as long as attribution " +
                    "and license notices are preserved.",
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(library.licenseUrl)))
                    onDismiss()
                },
                shape = MaterialTheme.shapes.small,
            ) {
                Text("Project page")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(APACHE_LICENSE_URL)))
                onDismiss()
            }) {
                Text("License text")
            }
        },
    )
}
