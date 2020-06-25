package com.udacity.project4.util

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

object DataGenerator {

    fun getReminderDto1(): ReminderDTO {
        return ReminderDTO(
            title = "Title 1",
            description = "Description 1",
            location = "Googleplex",
            latitude = 37.422004,
            longitude = -122.086246
        )
    }

    fun getReminderDto2(): ReminderDTO {
        return ReminderDTO(
            title = "Title 2",
            description = "Description 2",
            location = "Udacity",
            latitude = 37.399437,
            longitude = -122.108060
        )
    }

    fun getValidReminderDataItem(): ReminderDataItem {
        return ReminderDataItem(
            title = "Title",
            description = "Description",
            location = "Googleplex",
            latitude = 37.422004,
            longitude = -122.086246
        )
    }

    fun getReminderWithNoTitle(): ReminderDataItem {
        return ReminderDataItem(
            title = null,
            description = "Description",
            location = "Googleplex",
            latitude = 37.422004,
            longitude = -122.086246
        )
    }

    fun getReminderWithNoLocation(): ReminderDataItem {
        return ReminderDataItem(
            title = "Title",
            description = "Description",
            location = null,
            latitude = null,
            longitude = null
        )
    }
}
