package com.hogargo.kmp.tasks.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.hogargo.kmp.db.HogarGoDatabase
import com.hogargo.kmp.db.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Repositorio del modulo de Tareas del Hogar.
 * Gestiona la creacion, completado, filtrado y eliminacion de tareas persistidas en SQLDelight.
 */
class TaskRepository(
    private val db: HogarGoDatabase,
    private val now: () -> Long = ::nowMillis,
) {
    private val q = db.tasksQueries

    fun observeTasks(householdId: String): Flow<List<Task>> =
        q.tasksByHousehold(householdId).asFlow().mapToList(Dispatchers.Default)

    fun observePendingTasks(householdId: String): Flow<List<Task>> =
        q.pendingTasksByHousehold(householdId).asFlow().mapToList(Dispatchers.Default)

    fun observeCompletedTasks(householdId: String): Flow<List<Task>> =
        q.completedTasksByHousehold(householdId).asFlow().mapToList(Dispatchers.Default)

    fun observePendingCount(householdId: String): Flow<Long> =
        q.countPendingTasks(householdId).asFlow().mapToOne(Dispatchers.Default)

    fun observeCompletedCount(householdId: String): Flow<Long> =
        q.countCompletedTasks(householdId).asFlow().mapToOne(Dispatchers.Default)

    @OptIn(ExperimentalUuidApi::class)
    fun createTask(
        householdId: String,
        title: String,
        category: String,
        assignedMemberId: String,
        assignedMemberName: String,
        assignedToOther: Boolean,
        coinReward: Int,
        dueDate: String,
        dueTime: String,
    ): String {
        val id = Uuid.random().toString()
        q.insertTask(
            id = id,
            householdId = householdId,
            title = title,
            category = category,
            assignedMemberId = assignedMemberId,
            assignedMemberName = assignedMemberName,
            assignedToOther = assignedToOther,
            coinReward = coinReward.toLong(),
            dueDate = dueDate,
            dueTime = dueTime,
            isCompleted = false,
            completedAt = null,
            createdAt = now(),
        )
        return id
    }

    fun markTaskCompleted(taskId: String, completedAt: Long = now()) {
        q.markTaskCompleted(completedAt = completedAt, id = taskId)
    }

    fun deleteTask(taskId: String) {
        q.deleteTask(taskId)
    }

    fun deleteTasksByHousehold(householdId: String) {
        q.deleteTasksByHousehold(householdId)
    }
}

@OptIn(ExperimentalTime::class)
private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
