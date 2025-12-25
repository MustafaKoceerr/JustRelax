package com.mustafakoceerr.justrelax.navigation

import com.mustafakoceerr.justrelax.core.navigation.TabProvider
import com.mustafakoceerr.justrelax.tabs.HomeTab
import com.mustafakoceerr.justrelax.tabs.MixerTab
import com.mustafakoceerr.justrelax.tabs.TimerTab

class TabProviderImpl : TabProvider {
    override val homeTab = HomeTab
    override val mixerTab = MixerTab // TODO: MixerTab yap
    override val savedTab = HomeTab // TODO: SavedTab yap
    override val aiTab = HomeTab    // TODO: AiTab yap
    override val timerTab = TimerTab

//    override val mixerTab = MixerTab
//    override val savedTab = SavedTab
//    override val aiTab = AiTab
//    override val timerTab = TimerTab
}

