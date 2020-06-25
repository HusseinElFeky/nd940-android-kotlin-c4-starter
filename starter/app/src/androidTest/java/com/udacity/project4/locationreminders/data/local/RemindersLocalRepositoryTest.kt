package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.DataGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Medium Test to test the repository.
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database: RemindersDatabase
    private lateinit var remindersRepository: RemindersLocalRepository

    private lateinit var reminder: ReminderDTO

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        remindersRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)

        reminder = DataGenerator.getReminderDto1()
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - a new reminder saved in the database.
        remindersRepository.saveReminder(reminder)

        // WHEN - Reminder retrieved by ID.
        val result = remindersRepository.getReminder(reminder.id) as Result.Success
        val data = result.data

        // THEN - Same reminder is returned.
        assertThat(data.id, `is`(reminder.id))
        assertThat(data.title, `is`(reminder.title))
        assertThat(data.description, `is`(reminder.description))
        assertThat(data.location, `is`(reminder.location))
        assertThat(data.latitude, `is`(reminder.latitude))
        assertThat(data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteReminders_returnsEmptyList() = runBlocking {
        remindersRepository.saveReminder(reminder)
        var reminders = remindersRepository.getReminders() as Result.Success
        assertThat(reminders.data, hasItem(reminder))

        remindersRepository.deleteAllReminders()
        reminders = remindersRepository.getReminders() as Result.Success
        assertThat(reminders.data.isEmpty(), `is`(true))
    }

    @Test
    fun getUnsavedReminder_returnsError() = runBlocking {
        val unsavedReminder = remindersRepository.getReminder(reminder.id) as Result.Error
        assertThat(unsavedReminder.message, `is`("Reminder not found!"))
    }
}
