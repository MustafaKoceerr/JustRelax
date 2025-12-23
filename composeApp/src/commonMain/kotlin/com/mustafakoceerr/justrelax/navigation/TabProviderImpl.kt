package com.mustafakoceerr.justrelax.navigation

import com.mustafakoceerr.justrelax.core.navigation.TabProvider
import com.mustafakoceerr.justrelax.tabs.HomeTab

class TabProviderImpl : TabProvider {
    override val homeTab = HomeTab
        override val mixerTab = HomeTab
    override val savedTab = HomeTab
    override val aiTab = HomeTab
    override val timerTab = HomeTab

//    override val mixerTab = MixerTab
//    override val savedTab = SavedTab
//    override val aiTab = AiTab
//    override val timerTab = TimerTab
}

