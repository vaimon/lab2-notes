package edu.androidsec.notes.ui.navigation

import kotlinx.serialization.Serializable

/** Типобезопасные маршруты Navigation Compose. */

@Serializable
data object ListRoute

/** Экран заметки; `id == NEW_NOTE_ID` — новая заметка. */
@Serializable
data class NoteRoute(val id: Long) {
    companion object {
        const val NEW_NOTE_ID = 0L
    }
}
