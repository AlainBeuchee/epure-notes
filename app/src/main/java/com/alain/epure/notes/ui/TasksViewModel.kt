package com.alain.epure.notes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alain.epure.notes.data.SettingsStore
import com.alain.epure.notes.data.TaskStore
import com.alain.epure.notes.domain.Task
import com.alain.epure.notes.domain.TaskState
import com.alain.epure.notes.domain.formatTasks
import com.alain.epure.notes.domain.parseTasks
import com.alain.epure.notes.domain.toggledDone
import com.alain.epure.notes.system.BackupFolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

// Liste de tâches unique (SPEC F-12). Chaque changement est enregistré dans `taches.md`, puis recopié si la sauvegarde est active.
class TasksViewModel(
    private val store: TaskStore,
    private val backup: BackupFolder,
    settings: SettingsStore,
    private val onChanged: () -> Unit = {}
) : ViewModel() {

    private val _tasks = MutableStateFlow(parseTasks(store.read()))
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    // Mode kanban (DEC-0041) : true si le réglage « Tâches : kanban » est activé.
    val kanban: StateFlow<Boolean> = settings.tasksKanban
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Ajoute une tâche à faire ; un texte vide n'ajoute rien (SPEC F-12.2).
    fun add(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        update(_tasks.value + Task(trimmed, TaskState.TODO))
    }

    // Marque faite ou à faire (SPEC F-12.3). L'index est celui de la liste enregistrée.
    fun toggle(index: Int) = edit(index) { it.toggledDone() }

    // Change l'état d'une tâche en mode kanban (DEC-0041).
    fun setState(index: Int, state: TaskState) = edit(index) { it.copy(state = state) }

    fun rename(index: Int, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        edit(index) { it.copy(text = trimmed) }
    }

    fun delete(index: Int) {
        update(_tasks.value.filterIndexed { position, _ -> position != index })
    }

    // Efface les tâches faites (SPEC F-12.5).
    fun clearDone() {
        update(_tasks.value.filterNot { it.done })
    }

    // Restauration (SPEC F-18.7) : la liste du dossier n'est importée que si la liste de l'app est vide.
    // Retourne true si des tâches ont été restaurées.
    fun restoreIfEmpty(content: String): Boolean {
        if (_tasks.value.isNotEmpty()) return false
        val restored = parseTasks(content)
        if (restored.isEmpty()) return false
        update(restored)
        return true
    }

    // Recopie `taches.md` dans le dossier de sauvegarde, à l'activation (SPEC F-18.5).
    fun copyToBackup() {
        backup.writeFile(TASKS_FILE, formatTasks(_tasks.value))
    }

    private fun edit(index: Int, change: (Task) -> Task) {
        update(_tasks.value.mapIndexed { position, task -> if (position == index) change(task) else task })
    }

    private fun update(tasks: List<Task>) {
        _tasks.value = tasks
        val content = formatTasks(tasks)
        store.save(content)
        backup.writeFile(TASKS_FILE, content)
        onChanged()
    }

    companion object {
        const val TASKS_FILE = "taches.md"
    }
}
