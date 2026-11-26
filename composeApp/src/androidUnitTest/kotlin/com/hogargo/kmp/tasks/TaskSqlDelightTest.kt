package com.hogargo.kmp.tasks

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.hogargo.kmp.db.HogarGoDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TaskSqlDelightTest {
    private lateinit var db: HogarGoDatabase

    @BeforeTest
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        HogarGoDatabase.Schema.create(driver)
        db = HogarGoDatabase(driver)
    }

    @Test
    fun insertAndQueryTasksByHousehold() {
        val q = db.tasksQueries

        q.insertTask(
            id = "task-1",
            householdId = "home-123",
            title = "Lavar platos",
            category = "Cocina",
            assignedMemberId = "member-1",
            assignedMemberName = "Jhonatan",
            assignedToOther = false,
            coinReward = 15,
            dueDate = "2026-05-10",
            dueTime = "14:00",
            isCompleted = false,
            completedAt = null,
            createdAt = 1000L,
        )

        val tasks = q.tasksByHousehold("home-123").executeAsList()
        assertEquals(1, tasks.size)

        val task = tasks.first()
        assertEquals("Lavar platos", task.title)
        assertEquals("Cocina", task.category)
        assertEquals("Jhonatan", task.assignedMemberName)
        assertEquals(15L, task.coinReward)
        assertFalse(task.isCompleted)
    }

    @Test
    fun markTaskAsCompleted() {
        val q = db.tasksQueries

        q.insertTask(
            id = "task-2",
            householdId = "home-123",
            title = "Sacar la basura",
            category = "Limpieza",
            assignedMemberId = "member-1",
            assignedMemberName = "Jhonatan",
            assignedToOther = false,
            coinReward = 10,
            dueDate = "2026-05-10",
            dueTime = "20:00",
            isCompleted = false,
            completedAt = null,
            createdAt = 1000L,
        )

        assertEquals(1L, q.countPendingTasks("home-123").executeAsOne())
        assertEquals(0L, q.countCompletedTasks("home-123").executeAsOne())

        // Marcar como completada
        val now = 2000L
        q.markTaskCompleted(completedAt = now, id = "task-2")

        val completedTasks = q.completedTasksByHousehold("home-123").executeAsList()
        assertEquals(1, completedTasks.size)
        assertTrue(completedTasks.first().isCompleted)
        assertEquals(now, completedTasks.first().completedAt)

        assertEquals(0L, q.countPendingTasks("home-123").executeAsOne())
        assertEquals(1L, q.countCompletedTasks("home-123").executeAsOne())
    }

    @Test
    fun deleteTasksByHousehold() {
        val q = db.tasksQueries

        q.insertTask(
            id = "task-3",
            householdId = "home-123",
            title = "Pasear al perro",
            category = "Mascotas",
            assignedMemberId = "",
            assignedMemberName = "Otra persona",
            assignedToOther = true,
            coinReward = 20,
            dueDate = "2026-05-11",
            dueTime = "08:00",
            isCompleted = false,
            completedAt = null,
            createdAt = 1000L,
        )

        q.deleteTasksByHousehold("home-123")
        assertTrue(q.tasksByHousehold("home-123").executeAsList().isEmpty())
    }

    @Test
    fun queryPendingAndCompletedSeparately() {
        val q = db.tasksQueries

        q.insertTask(
            id = "task-a",
            householdId = "home-123",
            title = "Barrer la sala",
            category = "Limpieza",
            assignedMemberId = "m1",
            assignedMemberName = "Jhonatan",
            assignedToOther = false,
            coinReward = 10,
            dueDate = "2026-05-10",
            dueTime = "10:00",
            isCompleted = false,
            completedAt = null,
            createdAt = 1000L,
        )
        q.insertTask(
            id = "task-b",
            householdId = "home-123",
            title = "Regar las plantas",
            category = "Jardín",
            assignedMemberId = "m2",
            assignedMemberName = "Dana",
            assignedToOther = false,
            coinReward = 15,
            dueDate = "2026-05-10",
            dueTime = "11:00",
            isCompleted = true,
            completedAt = 1500L,
            createdAt = 1000L,
        )

        val pending = q.pendingTasksByHousehold("home-123").executeAsList()
        val completed = q.completedTasksByHousehold("home-123").executeAsList()

        assertEquals(1, pending.size)
        assertEquals("task-a", pending.first().id)
        assertEquals(1, completed.size)
        assertEquals("task-b", completed.first().id)

        // Suma de recompensas de completadas
        val totalCoins = completed.sumOf { it.coinReward }
        assertEquals(15L, totalCoins)
    }

    @Test
    fun deleteIndividualTask() {
        val q = db.tasksQueries

        q.insertTask(
            id = "task-delete-me",
            householdId = "home-123",
            title = "Tarea temporal",
            category = "Otros",
            assignedMemberId = "m1",
            assignedMemberName = "Jhonatan",
            assignedToOther = false,
            coinReward = 5,
            dueDate = "2026-05-10",
            dueTime = "12:00",
            isCompleted = false,
            completedAt = null,
            createdAt = 1000L,
        )

        assertEquals(1, q.tasksByHousehold("home-123").executeAsList().size)
        q.deleteTask("task-delete-me")
        assertEquals(0, q.tasksByHousehold("home-123").executeAsList().size)
    }
}
