package com.alain.epure.notes

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.os.Bundle
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.alain.epure.notes.domain.notesToImport
import com.alain.epure.notes.domain.sharedNoteText
import com.alain.epure.notes.domain.validNoteFiles
import com.alain.epure.notes.ui.AboutScreen
import com.alain.epure.notes.ui.EditorScreen
import com.alain.epure.notes.ui.EditorViewModel
import com.alain.epure.notes.ui.NotesScreen
import com.alain.epure.notes.ui.NotesViewModel
import com.alain.epure.notes.ui.SettingsScreen
import com.alain.epure.notes.ui.SettingsViewModel
import com.alain.epure.notes.ui.TasksScreen
import com.alain.epure.notes.ui.TasksViewModel
import com.alain.epure.notes.ui.theme.Black
import com.alain.epure.notes.ui.theme.DayBackground
import com.alain.epure.notes.system.TasksWidget
import com.alain.epure.notes.ui.theme.EpureNotesTheme
import com.alain.epure.notes.R
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Écrans de l'app. Navigation par état simple (CLAUDE.md), sans bibliothèque de navigation.
enum class Screen { Editor, Notes, Settings, About, Tasks }

class MainActivity : ComponentActivity() {

    // Texte reçu d'une autre app, en attente d'être mis dans une nouvelle note.
    private var sharedText by mutableStateOf<String?>(null)

    // Demande d'ouvrir l'écran Tâches (depuis le widget).
    private var openTasksRequested by mutableStateOf(false)

    private lateinit var container: AppContainer
    private lateinit var editorViewModel: EditorViewModel
    private lateinit var tasksViewModel: TasksViewModel
    private lateinit var pickBackupFolder: ActivityResultLauncher<Uri?>
    private lateinit var pickRestoreFolder: ActivityResultLauncher<Uri?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Barres système noires, icônes claires, quel que soit le thème du téléphone.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Black.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Black.toArgb())
        )
        container = AppContainer(applicationContext)
        editorViewModel = ViewModelProvider(
            this,
            viewModelFactory { initializer { EditorViewModel(container.noteStore, container.backup, container.settings) } }
        )[EditorViewModel::class.java]
        val notesViewModel = ViewModelProvider(
            this,
            viewModelFactory { initializer { NotesViewModel(container.noteStore) } }
        )[NotesViewModel::class.java]
        tasksViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer {
                    TasksViewModel(container.taskStore, container.backup, container.settings) { TasksWidget.updateAll(applicationContext) }
                }
            }
        )[TasksViewModel::class.java]
        val settingsViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                initializer {
                    SettingsViewModel(container.settings, container.backup) { TasksWidget.updateAll(applicationContext) }
                }
            }
        )[SettingsViewModel::class.java]

        // Le choix du dossier de sauvegarde passe par le sélecteur du système (SPEC F-18.3).
        pickBackupFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) enableBackup(uri)
        }
        pickRestoreFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) restoreFrom(uri)
        }
        // Dossier introuvable : la sauvegarde s'arrête et le réglage repasse à « non » (SPEC F-18.8).
        container.backup.onUnavailable = {
            disableBackup()
            Toast.makeText(this, R.string.backup_unavailable, Toast.LENGTH_LONG).show()
        }
        // Le dossier mémorisé est relu au démarrage, pour que les copies reprennent.
        lifecycleScope.launch {
            container.settings.backupFolder.collect { uri ->
                container.backup.treeUri = uri?.let { it.toUri() }
            }
        }

        // Enregistrement à l'arrêt de l'app (SPEC F-3.1).
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) editorViewModel.saveNow()
        })
        // Après une rotation, l'intention est encore là : on ne recrée pas la note.
        if (savedInstanceState == null) {
            handleShareIntent(intent)
            handleOpenTasks(intent)
        }
        setContent {
            val dayTheme by settingsViewModel.dayTheme.collectAsState()
            // Barres système adaptées au thème : sombres en Nuit, claires en Jour.
            LaunchedEffect(dayTheme) {
                enableEdgeToEdge(
                    statusBarStyle = systemBarStyle(dayTheme),
                    navigationBarStyle = systemBarStyle(dayTheme)
                )
            }
            EpureNotesTheme(day = dayTheme) {
                var screen by rememberSaveable { mutableStateOf(Screen.Editor) }
                // Mode immersif : barre d'état masquée dans l'éditeur, affichée ailleurs (SPEC F-2.7, DEC-0029).
                LaunchedEffect(screen) {
                    window.insetsController?.let { controller ->
                        if (screen == Screen.Editor) {
                            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            controller.hide(android.view.WindowInsets.Type.statusBars())
                        } else {
                            controller.show(android.view.WindowInsets.Type.statusBars())
                        }
                    }
                }
                val shared = sharedText
                LaunchedEffect(openTasksRequested) {
                    if (openTasksRequested) {
                        screen = Screen.Tasks
                        openTasksRequested = false
                    }
                }
                LaunchedEffect(shared) {
                    if (shared != null) {
                        editorViewModel.newNoteWith(shared)
                        screen = Screen.Editor
                        sharedText = null
                    }
                }
                when (screen) {
                    Screen.Editor -> EditorScreen(
                        viewModel = editorViewModel,
                        onOpenNotes = {
                            editorViewModel.saveNow()
                            screen = Screen.Notes
                        },
                        onOpenTasks = {
                            editorViewModel.saveNow()
                            screen = Screen.Tasks
                        },
                        onAddTask = { editorViewModel.addCurrentLineAsTask(tasksViewModel) },
                        onOpenSettings = {
                            editorViewModel.saveNow()
                            screen = Screen.Settings
                        }
                    )
                    Screen.Tasks -> TasksScreen(
                        viewModel = tasksViewModel,
                        onBack = { screen = Screen.Editor },
                        onOpenNotes = { screen = Screen.Notes },
                        onOpenSettings = { screen = Screen.Settings }
                    )
                    Screen.Notes -> NotesScreen(
                        viewModel = notesViewModel,
                        onOpenNote = { id ->
                            // La recherche en cours ne reste pas à l'écran de la liste ; la première occurrence est sélectionnée.
                            val query = notesViewModel.query.value
                            notesViewModel.setQuery("")
                            editorViewModel.openNote(id, query)
                            screen = Screen.Editor
                        },
                        onDeleteNote = { id ->
                            container.noteStore.delete(id)
                            container.backup.delete(id)
                            editorViewModel.onNoteDeleted(id)
                        },
                        onBack = { screen = Screen.Editor },
                        onNewNote = {
                            editorViewModel.newNote()
                            screen = Screen.Editor
                        },
                        onOpenTasks = { screen = Screen.Tasks },
                        onOpenSettings = { screen = Screen.Settings },
                        backupActive = container.backup.treeUri != null
                    )
                    Screen.Settings -> SettingsScreen(
                        viewModel = settingsViewModel,
                        onStartBackup = { pickBackupFolder.launch(null) },
                        onStopBackup = ::disableBackup,
                        onRestore = { pickRestoreFolder.launch(null) },
                        onOpenAbout = { screen = Screen.About },
                        onOpenNotes = { screen = Screen.Notes },
                        onOpenTasks = { screen = Screen.Tasks },
                        onBack = { screen = Screen.Editor }
                    )
                    Screen.About -> AboutScreen(onBack = { screen = Screen.Settings })
                }
            }
        }
    }

    // L'app est déjà ouverte et reçoit un nouveau texte (launchMode singleTop).
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
        handleOpenTasks(intent)
    }

    // Un toucher sur le widget « Tâches » ouvre l'écran Tâches (SPEC F-14).
    private fun handleOpenTasks(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_TASKS, false) == true) openTasksRequested = true
    }

    // Texte partagé, fichier .md partagé ou ouvert avec Épure Notes (SPEC F-10.1, F-10.3, F-10.4).
    private fun handleShareIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (text != null) {
                    sharedText = sharedNoteText(intent.getStringExtra(Intent.EXTRA_SUBJECT), text)
                    return
                }
                IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)?.let { importMarkdown(it) }
            }
            Intent.ACTION_VIEW -> intent.data?.let { importMarkdown(it) }
        }
    }

    // Copie le contenu d'un fichier .md dans une nouvelle note. Le fichier d'origine n'est ni modifié ni supprimé.
    private fun importMarkdown(uri: Uri) {
        if (displayName(uri)?.endsWith(".md", ignoreCase = true) != true) {
            Toast.makeText(this, R.string.share_not_markdown, Toast.LENGTH_LONG).show()
            return
        }
        readSharedFile(uri)?.let { sharedText = sharedNoteText(subject = null, text = it) }
    }

    // Nom du fichier partagé. L'accès est donné par le partage : aucune permission n'est nécessaire.
    private fun displayName(uri: Uri): String? =
        try {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
        } catch (error: SecurityException) {
            null
        }

    private fun readSharedFile(uri: Uri): String? =
        try {
            contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
        } catch (error: IOException) {
            null
        }

    // Activation : mémorise le dossier et recopie toutes les notes (SPEC F-18.5).
    private fun enableBackup(uri: Uri) {
        contentResolver.takePersistableUriPermission(uri, PERMISSION_FLAGS)
        container.backup.treeUri = uri
        editorViewModel.saveNow()
        container.backup.writeAll(container.noteStore.summaries().map { it.id to it.text })
        tasksViewModel.copyToBackup()
        lifecycleScope.launch { container.settings.setBackupFolder(uri.toString()) }
    }

    // Restauration : importe les notes du dossier sans écraser celles qui existent (SPEC F-18.7).
    private fun restoreFrom(folder: Uri) {
        lifecycleScope.launch {
            editorViewModel.saveNow()
            val files = withContext(Dispatchers.IO) { container.backup.readAll(folder) }
            files.firstOrNull { it.first == TasksViewModel.TASKS_FILE }?.let { (_, content) ->
                if (tasksViewModel.restoreIfEmpty(content)) {
                    Toast.makeText(this@MainActivity, R.string.tasks_restored, Toast.LENGTH_LONG).show()
                }
            }
            val message = if (validNoteFiles(files).isEmpty()) {
                getString(R.string.restore_none)
            } else {
                val existing = container.noteStore.summaries().map { it.id }.toSet()
                val toImport = notesToImport(existing, files)
                withContext(Dispatchers.IO) {
                    toImport.forEach { (id, text) -> container.noteStore.save(id, text) }
                }
                if (container.backup.treeUri != null) container.backup.writeAll(toImport)
                resources.getQuantityString(R.plurals.restore_result, toImport.size, toImport.size)
            }
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    // Arrêt : libère l'accès au dossier ; les copies existantes restent (SPEC F-18.9).
    private fun disableBackup() {
        container.backup.treeUri?.let { uri ->
            try {
                contentResolver.releasePersistableUriPermission(uri, PERMISSION_FLAGS)
            } catch (error: SecurityException) {
                // L'accès n'était déjà plus accordé : rien à libérer.
            }
        }
        container.backup.treeUri = null
        lifecycleScope.launch { container.settings.setBackupFolder(null) }
    }

    private fun systemBarStyle(day: Boolean): SystemBarStyle =
        if (day) {
            SystemBarStyle.light(DayBackground.toArgb(), DayBackground.toArgb())
        } else {
            SystemBarStyle.dark(Black.toArgb())
        }

    companion object {
        const val EXTRA_OPEN_TASKS = "open_tasks"
        private const val PERMISSION_FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    }
}
