package com.finance.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.finance.app.utils.AppTheme
import com.finance.app.utils.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val currentTheme: StateFlow<AppTheme> = themePreferences.themeFlow

    fun setTheme(theme: AppTheme) {
        themePreferences.updateTheme(theme)
    }
}
