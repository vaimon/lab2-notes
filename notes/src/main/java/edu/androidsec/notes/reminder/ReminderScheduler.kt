package edu.androidsec.notes.reminder

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import edu.androidsec.notes.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Планировщик напоминаний. Для лабораторной достаточно задержки в корутине уровня процесса:
 * настоящие напоминания делаются через AlarmManager/WorkManager, это тема другой работы.
 *
 * TODO(ipc-4): уведомление с PendingIntent.
 *
 * Лекция 6, блок «PendingIntent и mutability (Android 12/14)»; проверяется шагом 5 чек-листа.
 *
 * Сейчас уведомление строится без PendingIntent: нажатие на него ничего не делает, кнопки
 * «Готово» нет. Нужно:
 * - нажатие на уведомление открывает нужную заметку: `setContentIntent` с
 *   `PendingIntent.getActivity`, ЯВНЫЙ интент на `MainActivity` (компонент указан классом,
 *   данные — `NoteDeepLink.uriFor(noteId)`, действие VIEW) и флаг `FLAG_IMMUTABLE`;
 * - кнопка «Готово» (`addAction`, строка `reminder_action_done`): `PendingIntent.getBroadcast`
 *   с явным интентом на [ReminderActionReceiver], действием [ReminderActionReceiver.ACTION_DONE],
 *   extra [ReminderActionReceiver.EXTRA_NOTE_ID] и тем же `FLAG_IMMUTABLE`;
 * - подумайте, почему неизменяемый PendingIntent важен: получатель не сможет подменить
 *   компонент или extras.
 */
class ReminderScheduler(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private val jobs = HashMap<Long, Job>()

    fun schedule(noteId: Long, title: String, delayMillis: Long = DEFAULT_DELAY_MILLIS) {
        val text = title.ifBlank { context.getString(R.string.note_untitled) }
        synchronized(jobs) {
            jobs.remove(noteId)?.cancel()
            jobs[noteId] = scope.launch {
                delay(delayMillis)
                showNotification(noteId, text)
            }
        }
    }

    fun cancel(noteId: Long) {
        synchronized(jobs) { jobs.remove(noteId)?.cancel() }
    }

    private fun showNotification(noteId: Long, title: String) {
        if (!context.hasNotificationPermission()) {
            Log.w(TAG, "POST_NOTIFICATIONS not granted, reminder for note $noteId dropped")
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(noteId.toInt(), notification)
    }

    companion object {
        const val TAG = "ReminderScheduler"
        /** Id канала фиксирован заданием. */
        const val CHANNEL_ID = "reminders"
        const val DEFAULT_DELAY_MILLIS = 10_000L
    }
}
