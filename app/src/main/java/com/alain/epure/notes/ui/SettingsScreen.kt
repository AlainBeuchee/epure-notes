package com.alain.epure.notes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alain.epure.notes.R
import com.alain.epure.notes.ui.theme.LocalNoteColors

// Liste de réglages (DEC-0024) : sauvegarde (F-18) et « À propos ».
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onStartBackup: () -> Unit,
    onStopBackup: () -> Unit,
    onRestore: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenTasks: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val setting by viewModel.backupSetting.collectAsState()
    val textSize by viewModel.textSize.collectAsState()
    val dayTheme by viewModel.dayTheme.collectAsState()
    val deletionAllowed by viewModel.deletionAllowed.collectAsState()
    val tasksKanban by viewModel.tasksKanban.collectAsState()
    val lockScreenTasks by viewModel.lockScreenTasks.collectAsState()
    var confirmStop by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalNoteColors.current.background)
            .systemBarsPadding()
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            color = LocalNoteColors.current.textPrimary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (setting.active) confirmStop = true else onStartBackup()
                }
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(
                    if (setting.active) R.string.settings_backup_on else R.string.settings_backup_off
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = LocalNoteColors.current.textPrimary
            )
            if (setting.active) {
                setting.folderName?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalNoteColors.current.textSecondary
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.backup_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = LocalNoteColors.current.textSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Text(
            text = stringResource(
                if (lockScreenTasks) R.string.settings_lock_widget_on else R.string.settings_lock_widget_off
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = LocalNoteColors.current.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = viewModel::toggleLockScreenTasks)
                .sizeIn(minHeight = 48.dp)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )
        Text(
            text = stringResource(if (dayTheme) R.string.settings_theme_day else R.string.settings_theme_night),
            style = MaterialTheme.typography.bodyLarge,
            color = LocalNoteColors.current.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = viewModel::toggleDayTheme)
                .sizeIn(minHeight = 48.dp)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )
        // Freewriting « oui » = effacement interdit (SPEC F-8.3, DEC-0015) ; la valeur stockée reste « effacement autorisé ».
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = viewModel::toggleDeletion)
                .sizeIn(minHeight = 48.dp)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(
                    if (deletionAllowed) R.string.settings_freewriting_off else R.string.settings_freewriting_on
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = LocalNoteColors.current.textPrimary
            )
            Text(
                text = stringResource(
                    if (deletionAllowed) R.string.settings_freewriting_hint_off else R.string.settings_freewriting_hint_on
                ),
                style = TextStyle(
                    color = LocalNoteColors.current.textSecondary,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic
                )
            )
        }
        Text(
            text = stringResource(if (tasksKanban) R.string.settings_tasks_kanban else R.string.settings_tasks_simple),
            style = MaterialTheme.typography.bodyLarge,
            color = LocalNoteColors.current.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = viewModel::toggleTasksKanban)
                .sizeIn(minHeight = 48.dp)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )
        Text(
            text = stringResource(R.string.settings_text_size, textSize),
            style = MaterialTheme.typography.bodyLarge,
            color = LocalNoteColors.current.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = viewModel::cycleTextSize)
                .sizeIn(minHeight = 48.dp)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )
        Text(
            text = stringResource(R.string.settings_restore),
            style = MaterialTheme.typography.bodyLarge,
            color = LocalNoteColors.current.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onRestore)
                .sizeIn(minHeight = 48.dp)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )
        Text(
            text = stringResource(R.string.settings_about),
            style = MaterialTheme.typography.bodyLarge,
            color = LocalNoteColors.current.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenAbout)
                .sizeIn(minHeight = 48.dp)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(color = LocalNoteColors.current.textSecondary, thickness = 1.dp)
        Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                }
                Row {
                CommandButton(
                    label = stringResource(R.string.action_back),
                    description = stringResource(R.string.action_back),
                    onClick = onBack
                )
                CommandButton(
                    label = stringResource(R.string.action_open_notes),
                    description = stringResource(R.string.action_open_notes),
                    onClick = onOpenNotes
                )
                CommandButton(
                    label = stringResource(R.string.action_tasks),
                    description = stringResource(R.string.action_tasks),
                    onClick = onOpenTasks
                )
                }
            }
    }

    if (confirmStop) {
        AlertDialog(
            onDismissRequest = { confirmStop = false },
            text = { Text(text = stringResource(R.string.backup_stop_question)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmStop = false
                    onStopBackup()
                }) {
                    Text(text = stringResource(R.string.action_stop))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmStop = false }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
