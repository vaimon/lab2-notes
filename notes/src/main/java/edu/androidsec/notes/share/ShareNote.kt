package edu.androidsec.notes.share

import android.content.Context
import android.widget.Toast
import edu.androidsec.notes.domain.Note
import java.io.File

/**
 * TODO(ipc-5): экспорт заметки файлом через FileProvider.
 *
 * Лекция 6, блок «ContentProvider: FileProvider и grantUriPermissions»; проверяется шагами 2 и 6
 * чек-листа.
 *
 * Сейчас функция только пишет файл в `cacheDir/exports/` (это делает [NoteExporter]) и
 * показывает Toast. Нужно отправить файл другому приложению:
 * - `file://`-ссылки на свои файлы другим приложениям не отдаются (с API 24 это
 *   `FileUriExposedException`); вместо них — `content://` от `FileProvider.getUriForFile`.
 * - Объявите провайдер в манифесте: `androidx.core.content.FileProvider`, authority
 *   [AUTHORITY], не экспортирован, `grantUriPermissions="true"`, meta-data
 *   `android.support.FILE_PROVIDER_PATHS` со ссылкой на `@xml/file_paths`.
 * - Создайте `res/xml/file_paths.xml` и откройте в нём ОДИН каталог: `cache-path` с именем
 *   `exports` и путём `exports/` (именно туда пишет [NoteExporter]).
 * - Соберите `Intent.ACTION_SEND` (`type = "text/plain"`, `EXTRA_STREAM`, `clipData`) и добавьте
 *   `FLAG_GRANT_READ_URI_PERMISSION`: получатель получает временное право на чтение только этого
 *   URI, и только пока обрабатывает интент. Запускайте через `Intent.createChooser`.
 *
 * Проверка: `adb shell content read --uri content://edu.androidsec.notes.files/exports/1.txt`
 * должен быть отклонён (провайдер не экспортирован), а «Поделиться файлом» в приложении — работать.
 */
object ShareNote {
    const val AUTHORITY = "edu.androidsec.notes.files"

    fun shareNoteAsFile(context: Context, note: Note): File {
        val file = NoteExporter.export(context, note)
        Toast.makeText(context, "TODO(ipc-5): отправить ${file.name} через FileProvider", Toast.LENGTH_SHORT).show()
        return file
    }
}
