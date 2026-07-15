package dev.anmitali.stir.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dev.anmitali.stir.R
import dev.anmitali.stir.alarm.ui.AlarmRingActivity

const val ACTION_STOP = "dev.anmitali.stir.action.STOP"
const val ACTION_SNOOZE = "dev.anmitali.stir.action.SNOOZE"

object AlarmNotifications {

    const val CHANNEL_ID = "alarms"
    const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_alarm_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_alarm_description)
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }

    fun buildRingingNotification(context: Context, state: RingUiState?, alarmId: Long): android.app.Notification {
        val fullScreenIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            Intent(context, AlarmRingActivity::class.java)
                .putExtra(EXTRA_ALARM_ID, alarmId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title =
            state?.label?.takeIf { it.isNotBlank() } ?: context.getString(R.string.notification_alarm_title_default)
        val text = if (state?.phase == RingPhase.QUIET) {
            context.getString(R.string.alarm_ring_quiet_phase_hint)
        } else {
            context.getString(R.string.notification_alarm_title_default)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenIntent, true)
            .setContentIntent(fullScreenIntent)

        if (state == null || state.canStop) {
            val stopIntent = PendingIntent.getBroadcast(
                context,
                alarmId.toInt(),
                Intent(context, AlarmActionReceiver::class.java).setAction(ACTION_STOP)
                    .putExtra(EXTRA_ALARM_ID, alarmId),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            builder.addAction(0, context.getString(R.string.notification_action_stop), stopIntent)

            if (state == null || state.snoozeEnabled) {
                val snoozeIntent = PendingIntent.getBroadcast(
                    context,
                    alarmId.toInt() + 1,
                    Intent(context, AlarmActionReceiver::class.java).setAction(ACTION_SNOOZE)
                        .putExtra(EXTRA_ALARM_ID, alarmId),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                builder.addAction(0, context.getString(R.string.notification_action_snooze), snoozeIntent)
            }
        }

        return builder.build()
    }
}
