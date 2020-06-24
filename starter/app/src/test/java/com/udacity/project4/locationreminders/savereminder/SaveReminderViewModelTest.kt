package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.util.MainCoroutineRule
import com.udacity.project4.util.DataGenerator
import com.udacity.project4.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    private lateinit var app: Application

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() = mainCoroutineRule.runBlockingTest {
        // Do not use Koin in testing.
        stopKoin()

        app = ApplicationProvider.getApplicationContext()
        fakeDataSource = FakeDataSource()

        saveReminderViewModel = SaveReminderViewModel(app, fakeDataSource)
    }

    @Test
    fun saveReminder_checkLoading() = mainCoroutineRule.runBlockingTest {
        // Show loading.
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(DataGenerator.getValidReminderDataItem())
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Hide loading.
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun saveReminder_success() = mainCoroutineRule.runBlockingTest {
        saveReminderViewModel.validateAndSaveReminder(DataGenerator.getValidReminderDataItem())

        // Assert toast text.
        assertEquals(
            saveReminderViewModel.showToast.getOrAwaitValue(),
            app.getString(R.string.reminder_saved)
        )

        // Assert back navigation.
        assertEquals(
            saveReminderViewModel.navigationCommand.getOrAwaitValue(),
            NavigationCommand.Back
        )
    }

    @Test
    fun validateReminderDataItem() = mainCoroutineRule.runBlockingTest {
        // Reminder without title
        assertThat(
            saveReminderViewModel.validateAndSaveReminder(DataGenerator.getReminderWithNoTitle()),
            `is`(false)
        )
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title)
        )

        // Reminder without set location
        assertThat(
            saveReminderViewModel.validateAndSaveReminder(DataGenerator.getReminderWithNoLocation()),
            `is`(false)
        )
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )

        // Valid reminder
        assertThat(
            saveReminderViewModel.validateAndSaveReminder(DataGenerator.getValidReminderDataItem()),
            `is`(true)
        )
    }
}
