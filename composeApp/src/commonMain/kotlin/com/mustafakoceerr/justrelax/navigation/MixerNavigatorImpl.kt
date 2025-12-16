package com.mustafakoceerr.justrelax.navigation

import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.feature.home.navigation.HomeNavigator
import com.mustafakoceerr.justrelax.feature.mixer.navigation.MixerNavigator
import com.mustafakoceerr.justrelax.feature.settings.SettingsScreen

class MixerNavigatorImpl : MixerNavigator {
    override fun toSettings(): AppScreen {
        return SettingsScreen
    }
}