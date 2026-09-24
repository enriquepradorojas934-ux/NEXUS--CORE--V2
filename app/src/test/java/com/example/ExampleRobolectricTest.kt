package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.AppLimitEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.TaskEntity
import com.example.data.repository.PersonalOsRepository
import com.example.data.repository.PersonalOsRepositoryImpl
import com.example.domain.AppBudgetStatus
import com.example.domain.DailyCycleEngine
import com.example.domain.WellbeingEngine
import com.example.service.PersonalAiProductivityService
import com.example.service.UserProductivitySnapshot
import com.example.ui.PersonalOsViewModel
import com.example.ui.PersonalOsViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var database: AppDatabase
  private lateinit var repository: PersonalOsRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = PersonalOsRepositoryImpl(database.personalOsDao(), PersonalAiProductivityService(context))
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("LifeOS", appName)
  }

  @Test
  fun `verify daily cycle start calculation`() {
    val cycleDate = DailyCycleEngine.getCurrentCycleDate("05:00")
    assertTrue(cycleDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
  }

  @Test
  fun `verify wellbeing engine blocking`() {
    val limit = AppLimitEntity(
      appName = "Instagram",
      packageName = "com.instagram.android",
      dailyBudgetMinutes = 20,
      currentUsageMinutes = 20,
      isBlocked = true
    )
    val evaluated = WellbeingEngine.evaluate(limit)
    assertEquals(AppBudgetStatus.BLOCKED, evaluated.status)
    assertEquals(0, evaluated.remainingMinutes)
    assertEquals(1.0f, evaluated.percentUsed, 0.01f)
  }

  @Test
  fun `verify productivity snapshot creation`() {
    val snapshot = UserProductivitySnapshot(
      cycleDate = "2026-09-11",
      productiveMinutesToday = 120,
      pendingTasks = listOf("Escribir reporte", "Entrenar"),
      completedTasks = listOf("Reunión equipo"),
      habitsCompleted = 3,
      habitsTotal = 4,
      habitNames = listOf("Ejercicio", "Lectura", "Agua", "Meditación"),
      appUsageMinutes = 45,
      appBudgetMinutes = 95,
      blockedApps = emptyList(),
      activeGoals = listOf("Lanzamiento V1"),
      netFinancialBalance = 1500.0
    )

    assertEquals(120, snapshot.productiveMinutesToday)
    assertEquals(2, snapshot.pendingTasks.size)
    assertEquals(3, snapshot.habitsCompleted)
    assertEquals(45, snapshot.appUsageMinutes)
  }

  @Test
  fun `verify application class configuration`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertTrue(context is PersonalOsApplication)
  }

  @Test
  fun `verify repository bridges Room database and snapshot building`() = runBlocking {
    val cycle = "2026-09-11"
    repository.insertTask(
      TaskEntity(title = "Estudiar arquitectura", priority = "ALTA", estimatedMinutes = 60, category = "Estudio", cycleDate = cycle)
    )

    val tasks = repository.allTasks.first()
    assertEquals(1, tasks.size)
    assertEquals("Estudiar arquitectura", tasks.first().title)

    val snapshot = repository.buildCurrentCycleSnapshot(cycle)
    assertEquals(cycle, snapshot.cycleDate)
    assertEquals(1, snapshot.pendingTasks.size)
    assertEquals("Estudiar arquitectura", snapshot.pendingTasks.first())
  }

  @Test
  fun `verify viewModel connects with repository and factory`() = runBlocking {
    val factory = PersonalOsViewModelFactory(repository)
    val viewModel = factory.create(PersonalOsViewModel::class.java)

    assertNotNull(viewModel.uiState.value)
    viewModel.addTask("Nueva tarea de prueba", "MEDIA", 25, "Trabajo")

    // Esperar a que la corrutina en viewModelScope complete la inserción
    var found = false
    for (i in 1..20) {
      kotlinx.coroutines.delay(50)
      val tasks = repository.allTasks.first()
      if (tasks.any { it.title == "Nueva tarea de prueba" }) {
        found = true
        break
      }
    }
    assertTrue(found)
  }

  @Test
  fun `verify cloud sync repository integration and initial state`() = runBlocking {
    val syncRepo = repository.cloudSyncRepo
    assertNotNull(syncRepo)
    val syncState = syncRepo.syncState.value
    assertFalse(syncState.isSyncing)
    assertTrue(syncState.lastSyncSuccess)
    assertNotNull(syncState.statusMessage)
  }

  @Test
  fun `verify habit reminder scheduler calculates positive delay`() {
    val delay = com.example.worker.HabitReminderScheduler.calculateInitialDelayMillis(20, 0)
    assertTrue(delay > 0)
    assertTrue(delay <= 24 * 60 * 60 * 1000L)
  }

  @Test
  fun `verify habit reminder scheduling and cancellation with WorkManager`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val config = androidx.work.Configuration.Builder()
      .setMinimumLoggingLevel(android.util.Log.DEBUG)
      .build()
    try {
      androidx.work.WorkManager.initialize(context, config)
    } catch (e: IllegalStateException) {
      // ya inicializado
    }
    com.example.worker.HabitReminderScheduler.scheduleDailyReminder(context, 20, 30)
    com.example.worker.HabitReminderScheduler.cancelDailyReminder(context)
  }

  @Test
  fun `verify habit completion toggle updates Room and streak`() = runBlocking {
    val habit = HabitEntity(title = "Lectura 20 min", frequency = "DIARIO", category = "Estudio", targetValue = 20)
    repository.insertHabit(habit)

    val habits = repository.activeHabits.first()
    assertEquals(1, habits.size)
    val savedHabit = habits.first()
    assertEquals(0, savedHabit.currentStreak)

    val cycle = "2026-09-12"
    val log = HabitLogEntity(habitId = savedHabit.id, cycleDate = cycle, isCompleted = true, valueAchieved = 20)
    repository.insertHabitLog(log)

    val logs = repository.getHabitLogsByCycle(cycle).first()
    assertTrue(logs.any { it.habitId == savedHabit.id && it.isCompleted })
  }

  @Test
  fun `verify natural language engine parses workouts and finance correctly`() {
    val workoutCommand = "Entrené pecho y bíceps 50 min"
    val entriesWorkout = com.example.domain.NaturalLanguageEngine.parseGlobalCommand(workoutCommand)
    assertTrue(entriesWorkout.any { it.type == com.example.data.model.GlobalEntryType.WORKOUT })

    val expenseCommand = "Gasté 15000 en comida de supermercado"
    val entriesExpense = com.example.domain.NaturalLanguageEngine.parseGlobalCommand(expenseCommand)
    assertTrue(entriesExpense.any { it.type == com.example.data.model.GlobalEntryType.TRANSACTION_EXPENSE })

    val incomeCommand = "Cobré 85000 por consultoría"
    val entriesIncome = com.example.domain.NaturalLanguageEngine.parseGlobalCommand(incomeCommand)
    assertTrue(entriesIncome.any { it.type == com.example.data.model.GlobalEntryType.TRANSACTION_INCOME })

    val timeCommand = "Bloque de estudio 45m de 10:00 a 10:45"
    val entriesTime = com.example.domain.NaturalLanguageEngine.parseGlobalCommand(timeCommand)
    assertTrue(entriesTime.any { it.type == com.example.data.model.GlobalEntryType.TIME_TRACKING })
  }

  @Test
  fun `verify workout session persistence and querying`() = runBlocking {
    val session = com.example.data.model.WorkoutSessionEntity(
      cycleDate = "2026-09-20",
      title = "Fuerza Tren Superior",
      durationMinutes = 60,
      targetGoal = "Hipertrofia",
      notes = "Series pesadas al 85%"
    )
    val id = repository.insertWorkoutSession(session)
    assertTrue(id > 0)

    val sessions = repository.allWorkoutSessions.first()
    assertEquals(1, sessions.size)
    assertEquals("Fuerza Tren Superior", sessions.first().title)
  }
}

