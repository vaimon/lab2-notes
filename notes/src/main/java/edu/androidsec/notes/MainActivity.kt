package edu.androidsec.notes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import edu.androidsec.notes.deeplink.IncomingNote
import edu.androidsec.notes.deeplink.NoteDeepLink
import edu.androidsec.notes.ui.navigation.NotesNavHost
import edu.androidsec.notes.ui.theme.NotesTheme
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Единственная Activity приложения. Входящий интент (deep link, нажатие на уведомление)
 * разбирается в [NoteDeepLink.parse]; результат уходит в граф навигации.
 * `launchMode="singleTop"`: повторный интент приходит в [onNewIntent], а не создаёт новую Activity.
 */
class MainActivity : ComponentActivity() {

    private val incoming = MutableStateFlow<IncomingNote>(IncomingNote.None)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            incoming.value = NoteDeepLink.parse(intent)
        }
        setContent {
            NotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NotesNavHost(
                        incoming = incoming,
                        onIncomingConsumed = { incoming.value = IncomingNote.None },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incoming.value = NoteDeepLink.parse(intent)
    }
}
