package com.logan.project90.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.preferencesDataStore
import com.logan.project90.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "project90_settings")

class SettingsDataStore(
    private val context: Context
) : SettingsRepository {
    override val onboardingComplete: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[ONBOARDING_COMPLETE] ?: false }

    override val discretionaryTimeMinutes: Flow<Int?> =
        context.dataStore.data.map { prefs -> prefs[DISCRETIONARY_TIME_MINUTES] }

    override suspend fun completeOnboarding(discretionaryTimeMinutes: Int) {
        context.dataStore.edit { prefs: MutablePreferences ->
            prefs[ONBOARDING_COMPLETE] = true
            prefs[DISCRETIONARY_TIME_MINUTES] = discretionaryTimeMinutes
        }
    }

    private companion object {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val DISCRETIONARY_TIME_MINUTES = intPreferencesKey("discretionary_time_minutes")
    }
}
