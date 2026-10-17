package edu.androidsec.notes.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.androidsec.notes.data.NotesRepository
import edu.androidsec.notes.domain.Note
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesListViewModel(
    private val repository: NotesRepository,
) : ViewModel() {

    /** Список заметок из Room; `null` — список ещё загружается. */
    val notes: StateFlow<List<Note>?> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}
