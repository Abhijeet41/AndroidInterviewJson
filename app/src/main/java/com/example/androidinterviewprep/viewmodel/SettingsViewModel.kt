package com.example.androidinterviewprep.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidinterviewprep.data.ReminderPreferences
import com.example.androidinterviewprep.util.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val reminderPreferences: ReminderPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val remindersEnabled = reminderPreferences.remindersEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            reminderPreferences.setRemindersEnabled(enabled)
            if (enabled) {
                ReminderScheduler.scheduleReminders(context)
            } else {
                ReminderScheduler.cancelReminders(context)
            }
        }
    }

    fun triggerTestReminder() {
        ReminderScheduler.scheduleTestReminder(context)
    }
}
