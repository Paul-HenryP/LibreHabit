package com.paulhenryp.librehabit.model

import androidx.annotation.StringRes
import com.paulhenryp.librehabit.R

enum class AppTheme(@StringRes val displayNameResId: Int) {
    PURPLE(R.string.settings_theme_purple),
    FOREST_GREEN(R.string.settings_theme_forest_green),
    SYSTEM_DYNAMIC(R.string.settings_theme_system_dynamic)
}

enum class DarkModePreference(@StringRes val displayNameResId: Int) {
    SYSTEM(R.string.settings_dark_mode_system),
    LIGHT(R.string.settings_dark_mode_light),
    DARK(R.string.settings_dark_mode_dark)
}
