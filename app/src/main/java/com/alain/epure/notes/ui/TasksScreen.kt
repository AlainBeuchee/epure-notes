package com.alain.epure.notes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alain.epure.notes.R
import com.alain.epure.notes.domain.Task
import com.alain.epure.notes.domain.TaskState
import com.alain.epure.notes.domain.displayOrder
import com.alain.epure.notes.ui.theme.LocalNoteColors

// Liste « Tâches » (SPEC F-12) : le « + » de la barre ouvre le champ d'ajout (DEC-0045), appui court = faite / à faire, appui long = modifier ou supprimer.
@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    onBack: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val kanban by viewModel.kanban.collectAsState()
    var draft by rememberSaveable { mutableStateOf("") }
    // Champ d'ajout : visible seulement après un appui sur « + » (DEC-0045).
    var adding by rememberSaveable { mutableStateOf(false) }
    val addFocus = remember { FocusRequester() }
    var clearDoneAsked by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Int?>(null) }
    var editDraft by remember { mutableStateOf("") }

    BackHandler(onBack = onBack)
    // Déclaré après : le retour ferme d'abord le champ d'ajout s'il est ouvert (T-8).
    BackHandler(enabled = adding) { adding = false }
    LaunchedEffect(adding) {
        if (adding) addFocus.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalNoteColors.current.background)
            .systemBarsPadding()
    ) {
        Text(
            text = stringResource(R.string.tasks_title),
            style = MaterialTheme.typography.titleLarge,
            color = LocalNoteColors.current.textPrimary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
        )
        // Champ d'ajout (SPEC F-12.2) : ouvert par « + », sans libellé. Entrée valide et garde le champ ouvert.
        if (adding) {
            BasicTextField(
                value = draft,
                onValueChange = { draft = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    viewModel.add(draft)
                    draft = ""
                }),
                textStyle = TextStyle(color = LocalNoteColors.current.textPrimary, fontSize = 18.sp),
                cursorBrush = SolidColor(LocalNoteColors.current.textPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 48.dp)
                    .padding(horizontal = 24.dp)
                    .focusRequester(addFocus)
            )
        }
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.tasks_empty), color = LocalNoteColors.current.textSecondary)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                var previousState: TaskState? = null
                displayOrder(tasks, kanban).forEach { index ->
                    val task = tasks[index]
                    // Mode kanban (DEC-0041) : un en-tête par état, affiché une seule fois devant son groupe.
                    if (kanban && task.state != previousState) {
                        item(key = "header-${task.state}") { StateHeader(task.state) }
                    }
                    previousState = task.state
                    item(key = index) {
                        TaskItem(
                            task = task,
                            kanban = kanban,
                            onToggle = { viewModel.toggle(index) },
                            onSetState = { viewModel.setState(index, it) },
                            onEdit = {
                                editDraft = task.text
                                editing = index
                            },
                            onDelete = { viewModel.delete(index) }
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = LocalNoteColors.current.textSecondary, thickness = 1.dp)
        Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                CommandButton(
                    label = stringResource(R.string.action_new_note),
                    description = stringResource(R.string.cd_new_task),
                    onClick = { adding = true }
                )
                CommandButton(
                    label = stringResource(R.string.action_clear_done),
                    description = stringResource(R.string.cd_clear_done),
                    onClick = { clearDoneAsked = true }
                )
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
                    label = stringResource(R.string.action_settings),
                    description = stringResource(R.string.cd_settings),
                    onClick = onOpenSettings
                )
                }
            }
    }

    if (clearDoneAsked) {
        AlertDialog(
            onDismissRequest = { clearDoneAsked = false },
            text = { Text(text = stringResource(R.string.clear_done_question)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearDone()
                    clearDoneAsked = false
                }) {
                    Text(text = stringResource(R.string.action_clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { clearDoneAsked = false }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }

    editing?.let { index ->
        AlertDialog(
            onDismissRequest = { editing = null },
            text = {
                BasicTextField(
                    value = editDraft,
                    onValueChange = { editDraft = it },
                    singleLine = true,
                    textStyle = TextStyle(color = LocalNoteColors.current.textPrimary, fontSize = 18.sp),
                    cursorBrush = SolidColor(LocalNoteColors.current.textPrimary)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rename(index, editDraft)
                    editing = null
                }) {
                    Text(text = stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun TaskItem(
    task: Task,
    kanban: Boolean,
    onToggle: () -> Unit,
    onSetState: (TaskState) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Ligne visible de 40 dp (8 dp de marge haut et bas). La zone de toucher fait 48 dp
            // (charte, cible tactile) et déborde de 4 dp en haut et en bas, sans agrandir l'espace.
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = task.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (task.done) LocalNoteColors.current.textSecondary else LocalNoteColors.current.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 8.dp, bottom = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .requiredHeightIn(min = 48.dp)
                        .combinedClickable(
                            onClick = onToggle,
                            onLongClick = { menuOpen = true }
                        )
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                // Mode kanban : changement d'état au-dessus du trait (DEC-0041).
                if (kanban) {
                    TaskState.entries.forEach { state ->
                        DropdownMenuItem(
                            text = { Text(text = stringResource(stateLabel(state))) },
                            onClick = {
                                menuOpen = false
                                onSetState(state)
                            }
                        )
                    }
                    HorizontalDivider(color = LocalNoteColors.current.textSecondary, thickness = 1.dp)
                }
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.action_edit)) },
                    onClick = {
                        menuOpen = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.action_delete)) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    }
                )
            }
        }
        // Retrait d'une seule tâche faite (SPEC F-12.4, demande d'Alain).
        if (task.done) {
            // Même principe : hauteur visible de 40 dp, bouton de 48 dp centré.
            Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.requiredHeight(48.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = LocalNoteColors.current.textSecondary)
                ) {
                    Text(text = stringResource(R.string.action_remove))
                }
            }
        }
    }
}

// En-tête d'un groupe d'états en mode kanban (DEC-0041), affiché une seule fois.
@Composable
private fun StateHeader(state: TaskState) {
    Text(
        text = stringResource(stateLabel(state)),
        style = MaterialTheme.typography.bodySmall,
        color = LocalNoteColors.current.textSecondary,
        modifier = Modifier.padding(start = 24.dp, top = 32.dp, bottom = 4.dp)
    )
}

private fun stateLabel(state: TaskState): Int = when (state) {
    TaskState.TODO -> R.string.task_state_todo
    TaskState.DOING -> R.string.task_state_doing
    TaskState.BLOCKED -> R.string.task_state_blocked
    TaskState.DONE -> R.string.task_state_done
}
