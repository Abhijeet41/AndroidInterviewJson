package com.example.androidinterviewprep.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidinterviewprep.data.AppPreferences
import com.example.androidinterviewprep.data.ThemePreference
import com.example.androidinterviewprep.util.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val remindersEnabled = appPreferences.remindersEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val themePreference = appPreferences.themePreference.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemePreference.SYSTEM
    )

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setRemindersEnabled(enabled)
            if (enabled) {
                ReminderScheduler.scheduleReminders(context)
            } else {
                ReminderScheduler.cancelReminders(context)
            }
        }
    }

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            appPreferences.setThemePreference(theme)
        }
    }

    fun triggerTestReminder() {
        ReminderScheduler.scheduleTestReminder(context)
    }
}
