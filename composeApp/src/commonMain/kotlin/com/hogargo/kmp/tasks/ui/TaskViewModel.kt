package com.hogargo.kmp.tasks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hogargo.kmp.db.Task
import com.hogargo.kmp.household.HouseholdViewModel
import com.hogargo.kmp.household.notifications.HouseholdNotifier
import com.hogargo.kmp.tasks.data.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import com.hogargo.kmp.pet.PetRepository
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class TaskFilter {
    ALL, PENDING, COMPLETED
}

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class TaskViewModel(
    private val taskRepository: TaskRepository,
    private val householdViewModel: HouseholdViewModel,
    private val notifier: HouseholdNotifier? = null,
    private val petRepository: PetRepository? = null,
) : ViewModel() {

    private val _currentFilter = MutableStateFlow(TaskFilter.ALL)
    val currentFilter: StateFlow<TaskFilter> = _currentFilter.asStateFlow()

    private val activeHouseholdId: StateFlow<String?> = householdViewModel.household
        .combine(householdViewModel.household) { household, _ -> household?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val tasks: StateFlow<List<Task>> = combine(
        activeHouseholdId,
        _currentFilter,
    ) { householdId, filter ->
        householdId to filter
    }.flatMapLatest { (householdId, filter) ->
        if (householdId.isNullOrEmpty()) {
            flowOf(emptyList())
        } else {
            when (filter) {
                TaskFilter.ALL -> taskRepository.observeTasks(householdId)
                TaskFilter.PENDING -> taskRepository.observePendingTasks(householdId)
                TaskFilter.COMPLETED -> taskRepository.observeCompletedTasks(householdId)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHouseholdTasks: StateFlow<List<Task>> = activeHouseholdId.flatMapLatest { householdId ->
        if (householdId.isNullOrEmpty()) flowOf(emptyList())
        else taskRepository.observeTasks(householdId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Long> = activeHouseholdId.flatMapLatest { householdId ->
        if (householdId.isNullOrEmpty()) flowOf(0L)
        else taskRepository.observePendingCount(householdId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val completedCount: StateFlow<Long> = activeHouseholdId.flatMapLatest { householdId ->
        if (householdId.isNullOrEmpty()) flowOf(0L)
        else taskRepository.observeCompletedCount(householdId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val streakDays: StateFlow<Int> = allHouseholdTasks.combine(completedCount) { tasks, _ ->
        calculateStreak(tasks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCoins: StateFlow<Long> = allHouseholdTasks.combine(completedCount) { tasks, _ ->
        tasks.filter { it.isCompleted }.sumOf { it.coinReward }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun setFilter(filter: TaskFilter) {
        _currentFilter.value = filter
    }

    fun createTask(
        title: String,
        category: String,
        assignedMemberId: String,
        assignedMemberName: String,
        assignedToOther: Boolean,
        coinReward: Int,
        dueDate: String,
        dueTime: String,
    ) {
        val householdId = householdViewModel.household.value?.id ?: return
        taskRepository.createTask(
            householdId = householdId,
            title = title,
            category = category,
            assignedMemberId = assignedMemberId,
            assignedMemberName = assignedMemberName,
            assignedToOther = assignedToOther,
            coinReward = coinReward,
            dueDate = dueDate,
            dueTime = dueTime,
        )
    }

    fun toggleTaskCompleted(task: Task) {
        if (!task.isCompleted) {
            taskRepository.markTaskCompleted(task.id)
            val householdId = activeHouseholdId.value
            if (!householdId.isNullOrEmpty()) {
                petRepository?.boostHappinessOnTaskCompleted(householdId)
            }
        }
    }

    fun deleteTask(taskId: String) {
        taskRepository.deleteTask(taskId)
    }

    fun scheduleDailyStreakReminder() {
        notifier?.requestPermission()
        notifier?.scheduleStreakReminder(
            id = "streak_reminder_8pm",
            title = "🔥 ¡Mantén tu racha en HogarGo!",
            body = "Tienes tareas pendientes. Complétalas antes de las 8:00 pm para no perder tu racha.",
            hour = 20,
            minute = 0,
        )
    }

    fun sendStreakReminderNow() {
        notifier?.requestPermission()
        val pending = pendingCount.value
        val body = if (pending > 0) {
            "Tienes $pending tarea(s) pendiente(s). ¡Complétalas antes de las 8:00 pm para mantener la racha de tu hogar!"
        } else {
            "¡Todas las tareas están al día! Tu racha sigue protegida hoy 🔥"
        }
        notifier?.show(
            id = "streak_reminder_test",
            title = "🔥 Recordatorio de Racha — HogarGo",
            body = body,
        )
    }

    fun isBefore8PM(): Boolean {
        val nowMillis = Clock.System.now().toEpochMilliseconds()
        val currentHour = kotlinx.datetime.Instant.fromEpochMilliseconds(nowMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault()).hour
        return currentHour < 20
    }

    private fun calculateStreak(tasks: List<Task>): Int {
        val completed = tasks.filter { it.isCompleted }
        return completed.size
    }
}
