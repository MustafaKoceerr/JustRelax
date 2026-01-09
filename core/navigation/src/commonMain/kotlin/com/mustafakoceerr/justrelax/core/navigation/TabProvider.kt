package com.mustafakoceerr.justrelax.core.navigation

import cafe.adriel.voyager.navigator.tab.Tab

interface TabProvider {
    val homeTab: Tab
    val mixerTab: Tab
    val savedTab: Tab
    val aiTab: Tab
    val timerTab: Tab
}