package com.udacity.project4.util

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

fun ReminderDTO.toUiModel(): ReminderDataItem {
    return ReminderDataItem(
        this.title,
        this.description,
        this.location,
        this.latitude,
        this.longitude,
        this.id
    )
}
