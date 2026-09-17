package com.example.androidinterviewprep.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}

val Context.dataStore by preferencesDataStore(name = "settings_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val REMINDERS_ENABLED_KEY = booleanPreferencesKey("reminders_enabled")
        private val THEME_PREFERENCE_KEY = stringPreferencesKey("theme_preference")
    }

    val remindersEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[REMINDERS_ENABLED_KEY] ?: false
        }

    val themePreference: Flow<ThemePreference> = context.dataStore.data
        .map { preferences ->
            val themeString = preferences[THEME_PREFERENCE_KEY] ?: ThemePreference.SYSTEM.name
            try {
                ThemePreference.valueOf(themeString)
            } catch (e: IllegalArgumentException) {
                ThemePreference.SYSTEM
            }
        }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REMINDERS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setThemePreference(theme: ThemePreference) {
        context.dataStore.edit { preferences ->
            preferences[THEME_PREFERENCE_KEY] = theme.name
        }
    }
}
