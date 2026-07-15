package dev.anmitali.stir.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import dev.anmitali.stir.alarm.ExactAlarmPermission

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    var exactAlarmGranted by remember { mutableStateOf(ExactAlarmPermission.isGranted(context)) }
    var notificationsGranted by remember {
        mutableStateOf(
            !needsNotificationPermission ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED,
        )
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        exactAlarmGranted = ExactAlarmPermission.isGranted(context)
    }

    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsGranted = it
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primaryContainer) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "Welcome to Stir",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "A calm alarm clock that works fully offline. Two quick permissions make sure your alarms actually wake you up.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(Modifier.height(32.dp))

            PermissionStepCard(
                title = "Exact alarms",
                subtitle = "Lets Stir ring at the exact minute you set, even in Doze",
                granted = exactAlarmGranted,
                onGrant = { context.startActivity(ExactAlarmPermission.settingsIntent(context)) },
            )

            if (needsNotificationPermission) {
                Spacer(Modifier.height(12.dp))
                PermissionStepCard(
                    title = "Notifications",
                    subtitle = "Needed to show the alarm and its Stop/Snooze actions",
                    granted = notificationsGranted,
                    onGrant = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onFinished,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Text("Get started")
            }
            if (!exactAlarmGranted || (needsNotificationPermission && !notificationsGranted)) {
                TextButton(onClick = onFinished, modifier = Modifier.fillMaxWidth()) {
                    Text("Skip for now")
                }
            }
        }
    }
}

@Composable
private fun PermissionStepCard(title: String, subtitle: String, granted: Boolean, onGrant: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (granted) {
                Icon(Icons.Filled.Check, contentDescription = "Granted", tint = MaterialTheme.colorScheme.primary)
            } else {
                TextButton(onClick = onGrant) { Text("Allow") }
            }
        }
    }
}
