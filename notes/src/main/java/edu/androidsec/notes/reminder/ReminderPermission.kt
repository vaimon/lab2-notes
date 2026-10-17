package edu.androidsec.notes.reminder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * TODO(ipc-2): разрешение на уведомления.
 *
 * Лекция 5, блок «Runtime-разрешения: rationale, отказ, повторный отказ»;
 * проверяется шагом 3 чек-листа.
 *
 * Функция должна возвращать лямбду «запросить разрешение и, если оно есть, выполнить [onGranted]».
 * Правила:
 * - разрешение запрашивается только в момент, когда оно понадобилось (нажата кнопка «Напомнить»),
 *   а не при запуске приложения;
 * - до Android 13 (API 33) разрешения `POST_NOTIFICATIONS` не существует: сразу [onGranted];
 * - если система советует объяснить (`shouldShowRequestPermissionRationale`), сначала показать
 *   объяснение (например, `AlertDialog`), затем системный диалог;
 * - после отказа приложение продолжает работать (сообщение, без падения);
 * - после повторного отказа система больше не спрашивает: предложить открыть настройки
 *   (`Settings.ACTION_APP_NOTIFICATION_SETTINGS`).
 *
 * Пригодятся `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())`
 * и строки `permission_*` из `res/values/strings.xml`.
 * Не забудьте `<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>` (ipc-1).
 *
 * Сейчас это заглушка: показывает Toast и ничего не запрашивает.
 */
@Composable
fun rememberNotificationPermission(onGranted: () -> Unit): () -> Unit {
    val context = LocalContext.current
    return {
        Toast.makeText(context, "TODO(ipc-2): запрос разрешения на уведомления", Toast.LENGTH_SHORT).show()
    }
}

/** Есть ли у приложения право показывать уведомления (до API 33 — всегда). */
fun Context.hasNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
