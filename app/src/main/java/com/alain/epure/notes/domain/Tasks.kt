package com.alain.epure.notes.domain

// État d'une tâche (SPEC F-12, DEC-0041). Le caractère entre crochets est celui du fichier `taches.md`.
enum class TaskState(val marker: Char) {
    TODO(' '),
    DOING('>'),
    BLOCKED('!'),
    DONE('x')
}

// Une tâche de la liste « Tâches » (SPEC F-12, DEC-0014).
data class Task(val text: String, val state: TaskState = TaskState.TODO) {
    val done: Boolean get() = state == TaskState.DONE
}

// Appui court en mode simple (SPEC F-12.3) : faite ↔ à faire. Une tâche « en cours » ou « bloquée » devient faite.
fun Task.toggledDone(): Task = copy(state = if (done) TaskState.TODO else TaskState.DONE)

// Lit le fichier `taches.md` : une tâche par ligne, « - [ ] texte » (à faire), « - [>] » (en cours), « - [!] » (bloquée), « - [x] » (faite). Les autres lignes sont ignorées.
fun parseTasks(content: String): List<Task> =
    content.lineSequence().mapNotNull { line ->
        val match = TASK_LINE.matchEntire(line.trim()) ?: return@mapNotNull null
        Task(text = match.groupValues[2], state = stateOf(match.groupValues[1].single()))
    }.toList()

// Écrit le fichier `taches.md` : une tâche par ligne.
fun formatTasks(tasks: List<Task>): String =
    tasks.joinToString(separator = "") { task ->
        "- [${task.state.marker}] ${task.text}\n"
    }

// Ligne d'une note transformée en tâche (SPEC F-13.3) : sans espaces, sans puce initiale. Null si vide.
fun lineToTaskText(line: String): String? {
    val text = line.trim().replace(LEADING_BULLET, "").trim()
    return text.ifEmpty { null }
}

// Ordre d'affichage. Mode simple (SPEC F-12.3) : les tâches non faites d'abord, les faites en bas.
// Mode kanban (DEC-0041) : à faire, en cours, bloquées, puis faites. L'ordre d'origine est conservé dans chaque groupe.
fun displayOrder(tasks: List<Task>, kanban: Boolean): List<Int> =
    tasks.indices.sortedBy { index ->
        val task = tasks[index]
        if (kanban) task.state.ordinal else if (task.done) 1 else 0
    }

// Widget en mode kanban (SPEC F-14.1, DEC-0051) : tâches non terminées, groupées par état (à faire, en cours, bloquée).
// Un état sans tâche n'apparaît pas. Les tâches faites ne sont jamais incluses.
fun openTasksByState(tasks: List<Task>): List<Pair<TaskState, List<Task>>> =
    listOf(TaskState.TODO, TaskState.DOING, TaskState.BLOCKED).mapNotNull { state ->
        tasks.filter { it.state == state }.takeIf { it.isNotEmpty() }?.let { state to it }
    }

private fun stateOf(marker: Char): TaskState = when (marker) {
    '>' -> TaskState.DOING
    '!' -> TaskState.BLOCKED
    'x', 'X' -> TaskState.DONE
    else -> TaskState.TODO
}

private val TASK_LINE = Regex("""-\s\[( |>|!|x|X)]\s(.+)""")
private val LEADING_BULLET = Regex("""^[-*•]\s*""")
