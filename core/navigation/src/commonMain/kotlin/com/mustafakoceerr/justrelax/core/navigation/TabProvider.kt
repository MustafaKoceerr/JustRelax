package com.mustafakoceerr.justrelax.core.navigation

import cafe.adriel.voyager.navigator.tab.Tab

/**
 * Feature modüllerinin, Tab'ların gerçek sınıflarını bilmeden
 * Tab değiştirebilmesi için gerekli arayüz.
 */
interface TabProvider {
    val homeTab: Tab
    val mixerTab: Tab
    val savedTab: Tab
    val aiTab: Tab
    val timerTab: Tab
}