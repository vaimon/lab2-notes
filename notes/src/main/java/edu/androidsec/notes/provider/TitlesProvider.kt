package edu.androidsec.notes.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * TODO(ipc-6): провайдер заголовков «для своих».
 *
 * Лекция 6, блоки «ContentProvider: exported, read/write permissions» и «Кто вызывает:
 * Binder.getCallingUid»; лекция 5, блок «Custom permissions и signature»; проверяется шагом 7
 * чек-листа.
 *
 * - Объявите в манифесте `<permission>` с именем [PERMISSION] и `protectionLevel="signature"`:
 *   его получат только приложения, подписанные тем же ключом, что и «Заметки».
 * - Объявите провайдер: authority [AUTHORITY], экспортирован (иначе клиент его не увидит)
 *   и защищён этим разрешением. Одного `readPermission` мало: запись останется открытой.
 * - Реализуйте [query]: только путь `/notes` (`UriMatcher`), только колонки `_id` и `title`
 *   (`MatrixCursor`; данные — `requireContext().appContainer.database.noteDao().titlesBlocking()`).
 *   Полезно записать в logcat `Binder.getCallingUid()` и `callingPackage`: кто спрашивает.
 * - Провайдер только для чтения: [insert], [update], [delete] отвергают вызов
 *   (`UnsupportedOperationException`).
 *
 * Проверка: `adb shell content query --uri content://edu.androidsec.notes.provider/notes`
 * даёт `SecurityException` (у shell нет разрешения); модуль `:client` с той же подписью
 * показывает заголовки; тот же модуль с другой подписью получает `SecurityException`.
 *
 * Сейчас это заглушка: `query()` возвращает `null`, провайдер не объявлен в манифесте.
 */
class TitlesProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    companion object {
        const val TAG = "TitlesProvider"
        const val AUTHORITY = "edu.androidsec.notes.provider"
        const val PERMISSION = "edu.androidsec.notes.permission.READ_NOTES"
        const val PATH_NOTES = "notes"
        const val COLUMN_TITLE = "title"
    }
}
