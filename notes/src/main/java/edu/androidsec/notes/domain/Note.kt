package edu.androidsec.notes.domain

/** Заметка. `id == 0` означает ещё не сохранённую заметку. */
data class Note(
    val id: Long = 0L,
    val title: String = "",
    val body: String = "",
    /** Абсолютный путь к копии фото во внутреннем хранилище приложения или null. */
    val photoPath: String? = null,
    val updatedAt: Long = 0L,
) {
    val isNew: Boolean get() = id == 0L
}

/** Пара «идентификатор — заголовок» для списка и для провайдера заголовков. */
data class NoteTitle(
    val id: Long,
    val title: String,
)
