package dev.anmitali.stir.ui.common

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import dev.anmitali.stir.alarm.ExactAlarmPermission

@Composable
fun PermissionsBanner() {
    val context = LocalContext.current
    var exactAlarmGranted by remember { mutableStateOf(ExactAlarmPermission.isGranted(context)) }
    var notificationsGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        exactAlarmGranted = ExactAlarmPermission.isGranted(context)
        notificationsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsGranted = it
    }

    if (!exactAlarmGranted) {
        BannerCard(
            text = "Stir needs permission to schedule exact alarms so they ring on time.",
            actionLabel = "Allow",
            onAction = { context.startActivity(ExactAlarmPermission.settingsIntent(context)) },
        )
    } else if (!notificationsGranted) {
        BannerCard(
            text = "Stir needs notification permission to show the alarm when it fires.",
            actionLabel = "Allow",
            onAction = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
        )
    }
}

@Composable
private fun BannerCard(text: String, actionLabel: String, onAction: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            androidx.compose.material3.Button(
                onClick = onAction,
                shape = MaterialTheme.shapes.small,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text(actionLabel)
            }
        }
    }
}
