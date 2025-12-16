package com.mustafakoceerr.justrelax.navigation

import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.feature.ai.navigation.AiNavigator
import com.mustafakoceerr.justrelax.feature.home.navigation.HomeNavigator
import com.mustafakoceerr.justrelax.feature.settings.SettingsScreen

class AiNavigatorImpl : AiNavigator {
    override fun toSettings(): AppScreen {
        return SettingsScreen
    }
}