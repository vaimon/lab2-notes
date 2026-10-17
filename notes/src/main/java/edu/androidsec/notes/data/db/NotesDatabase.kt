package edu.androidsec.notes.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        private const val NAME = "notes.db"

        fun create(context: Context): NotesDatabase =
            Room.databaseBuilder(context.applicationContext, NotesDatabase::class.java, NAME)
                .addCallback(SeedCallback)
                .build()
    }

    /**
     * Стартовые данные: три заметки с id 1, 2, 3, чтобы ссылке `notes://note/1`
     * было что открывать сразу после установки.
     */
    private object SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            val now = System.currentTimeMillis()
            db.execSQL(
                "INSERT INTO notes (id, title, body, photo_path, updated_at) VALUES " +
                    "(1, 'Подготовиться к защите ЛР2', " +
                    "'Проверить каждую точку входа через adb: am start, am broadcast, content query.', NULL, ${now - 2000}), " +
                    "(2, 'Прочитать лекцию 6', " +
                    "'Явные и неявные интенты, android:exported, PendingIntent и FLAG_IMMUTABLE.', NULL, ${now - 1000}), " +
                    "(3, 'Список покупок', " +
                    "'Молоко, хлеб, кофе.', NULL, $now)"
            )
        }
    }
}
