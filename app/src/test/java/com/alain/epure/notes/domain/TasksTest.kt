package com.alain.epure.notes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TasksTest {

    @Test
    fun parsesOpenAndDoneTasks() {
        val content = "- [ ] appeler le labo\n- [x] commander\nun commentaire\n"
        assertEquals(
            listOf(Task("appeler le labo", TaskState.TODO), Task("commander", TaskState.DONE)),
            parseTasks(content)
        )
    }

    @Test
    fun parsesDoingAndBlockedTasks() {
        val content = "- [>] rédiger le rapport\n- [!] attendre le devis\n"
        assertEquals(
            listOf(Task("rédiger le rapport", TaskState.DOING), Task("attendre le devis", TaskState.BLOCKED)),
            parseTasks(content)
        )
    }

    @Test
    fun uppercaseXIsDone() {
        assertEquals(listOf(Task("commander", TaskState.DONE)), parseTasks("- [X] commander\n"))
    }

    @Test
    fun formatThenParseKeepsAllStates() {
        val tasks = listOf(
            Task("un", TaskState.TODO),
            Task("deux", TaskState.DOING),
            Task("trois", TaskState.BLOCKED),
            Task("quatre", TaskState.DONE)
        )
        assertEquals("- [ ] un\n- [>] deux\n- [!] trois\n- [x] quatre\n", formatTasks(tasks))
        assertEquals(tasks, parseTasks(formatTasks(tasks)))
    }

    @Test
    fun lineBecomesTaskWithoutBullet() {
        assertEquals("appeler le labo", lineToTaskText("  - appeler le labo  "))
        assertEquals("idée", lineToTaskText("• idée"))
    }

    @Test
    fun blankLineAddsNothing() {
        assertNull(lineToTaskText("   "))
        assertNull(lineToTaskText("-"))
    }

    @Test
    fun simpleToggleSwitchesDoneAndOpen() {
        assertEquals(TaskState.DONE, Task("a", TaskState.TODO).toggledDone().state)
        assertEquals(TaskState.DONE, Task("a", TaskState.DOING).toggledDone().state)
        assertEquals(TaskState.DONE, Task("a", TaskState.BLOCKED).toggledDone().state)
        assertEquals(TaskState.TODO, Task("a", TaskState.DONE).toggledDone().state)
    }

    @Test
    fun simpleOrderPutsDoneTasksAtTheBottomKeepingOrder() {
        val tasks = listOf(
            Task("a", TaskState.DONE),
            Task("b", TaskState.TODO),
            Task("c", TaskState.DONE),
            Task("d", TaskState.DOING)
        )
        assertEquals(listOf(1, 3, 0, 2), displayOrder(tasks, kanban = false))
    }

    @Test
    fun kanbanOrderGroupsByState() {
        val tasks = listOf(
            Task("a", TaskState.DONE),
            Task("b", TaskState.BLOCKED),
            Task("c", TaskState.TODO),
            Task("d", TaskState.DOING),
            Task("e", TaskState.TODO)
        )
        assertEquals(listOf(2, 4, 3, 1, 0), displayOrder(tasks, kanban = true))
    }

    @Test
    fun openTasksByStateSkipsDoneAndEmptyGroups() {
        val tasks = listOf(
            Task("a", TaskState.DONE),
            Task("b", TaskState.BLOCKED),
            Task("c", TaskState.TODO),
            Task("d", TaskState.TODO)
        )
        assertEquals(
            listOf(
                TaskState.TODO to listOf(Task("c", TaskState.TODO), Task("d", TaskState.TODO)),
                TaskState.BLOCKED to listOf(Task("b", TaskState.BLOCKED))
            ),
            openTasksByState(tasks)
        )
    }

    @Test
    fun openTasksByStateIsEmptyWhenAllDone() {
        assertEquals(emptyList<Pair<TaskState, List<Task>>>(), openTasksByState(listOf(Task("a", TaskState.DONE))))
    }
}
