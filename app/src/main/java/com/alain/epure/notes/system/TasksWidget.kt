package com.alain.epure.notes.system

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.Spanned
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import com.alain.epure.notes.MainActivity
import com.alain.epure.notes.R
import com.alain.epure.notes.data.SettingsStore
import com.alain.epure.notes.data.TaskStore
import com.alain.epure.notes.domain.Task
import com.alain.epure.notes.domain.TaskState
import com.alain.epure.notes.domain.openTasksByState
import com.alain.epure.notes.domain.parseTasks
import com.alain.epure.notes.ui.theme.Black
import com.alain.epure.notes.ui.theme.TextPrimary
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// Widget « Tâches » de l'écran d'accueil (SPEC F-14, DEC-0016) : les tâches à faire ; un toucher ouvre l'écran Tâches.
class TasksWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> manager.updateAppWidget(id, buildViews(context, showTasks = true)) }
    }

    companion object {
        // Redessine tous les widgets « Tâches » (accueil et écran verrouillé), après un changement de tâches ou de réglage.
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val home = manager.getAppWidgetIds(ComponentName(context, TasksWidget::class.java))
            val lock = manager.getAppWidgetIds(ComponentName(context, TasksLockWidget::class.java))
            val showLock = runBlocking { SettingsStore(context).lockScreenTasks.first() }
            home.forEach { id -> manager.updateAppWidget(id, buildViews(context, showTasks = true)) }
            lock.forEach { id -> manager.updateAppWidget(id, buildViews(context, showTasks = showLock)) }
        }

        // Contenu du widget : les tâches à faire, ou un texte neutre si les tâches sont masquées (écran verrouillé).
        // En mode kanban (DEC-0051), les tâches sont groupées par état, avec un libellé en italique par groupe.
        fun buildViews(context: Context, showTasks: Boolean): RemoteViews {
            val body = if (!showTasks) {
                context.getString(R.string.tasks_widget_hidden)
            } else {
                val tasks = parseTasks(TaskStore(File(context.filesDir, "taches.md")).read())
                val kanban = runBlocking { SettingsStore(context).tasksKanban.first() }
                if (kanban) kanbanBody(context, tasks) else simpleBody(context, tasks)
            }
            return RemoteViews(context.packageName, R.layout.widget_tasks).apply {
                setTextViewText(R.id.widget_title, context.getString(R.string.tasks_title))
                setTextViewText(R.id.widget_body, body)
                setTextColor(R.id.widget_title, TextPrimary.toArgb())
                setTextColor(R.id.widget_body, TextPrimary.toArgb())
                setInt(R.id.widget_root, "setBackgroundColor", Black.toArgb())
                setOnClickPendingIntent(R.id.widget_root, openTasksIntent(context))
            }
        }

        // Mode simple : liste à puces des tâches non terminées.
        private fun simpleBody(context: Context, tasks: List<Task>): CharSequence {
            val openTasks = tasks.filterNot { it.done }
            return if (openTasks.isEmpty()) {
                context.getString(R.string.tasks_empty)
            } else {
                openTasks.joinToString(separator = "\n") { "• ${it.text}" }
            }
        }

        // Mode kanban (DEC-0051) : un libellé en italique par état non vide, suivi de ses tâches ; une ligne vide entre les groupes.
        private fun kanbanBody(context: Context, tasks: List<Task>): CharSequence {
            val groups = openTasksByState(tasks)
            if (groups.isEmpty()) return context.getString(R.string.tasks_empty)
            val builder = SpannableStringBuilder()
            groups.forEachIndexed { index, (state, groupTasks) ->
                if (index > 0) builder.append("\n\n")
                val start = builder.length
                builder.append(context.getString(stateLabel(state)))
                builder.setSpan(StyleSpan(Typeface.ITALIC), start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                groupTasks.forEach { builder.append("\n• ").append(it.text) }
            }
            return builder
        }

        private fun stateLabel(state: TaskState): Int = when (state) {
            TaskState.TODO -> R.string.task_state_todo
            TaskState.DOING -> R.string.task_state_doing
            TaskState.BLOCKED -> R.string.task_state_blocked
            TaskState.DONE -> R.string.task_state_done
        }

        private fun openTasksIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_OPEN_TASKS, true)
            return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

// Widget « Tâches » pour l'écran verrouillé (DEC-0035). Le contenu n'apparaît que si l'option est activée dans Réglages.
class TasksLockWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val showLock = runBlocking { SettingsStore(context).lockScreenTasks.first() }
        appWidgetIds.forEach { id -> manager.updateAppWidget(id, TasksWidget.buildViews(context, showLock)) }
    }
}
