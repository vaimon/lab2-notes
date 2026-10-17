package edu.androidsec.notes.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClientUiState(
    val isLoading: Boolean = true,
    val titles: List<String> = emptyList(),
    /** Текст исключения, если запрос отклонён; показывается красным. */
    val error: String? = null,
    val diagnostics: Diagnostics? = null,
)

class ClientViewModel(
    private val titlesClient: TitlesClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientUiState())
    val uiState: StateFlow<ClientUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val diagnostics = titlesClient.diagnostics()
            titlesClient.queryTitles()
                .onSuccess { titles ->
                    _uiState.update {
                        it.copy(isLoading = false, titles = titles, error = null, diagnostics = diagnostics)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            titles = emptyList(),
                            error = "${error::class.java.simpleName}: ${error.message}",
                            diagnostics = diagnostics,
                        )
                    }
                }
        }
    }
}
