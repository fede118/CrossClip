package com.section11.crossclip.framework.utils

import kotlinx.browser.window

actual object DeviceInfo {
    actual fun getDeviceInfo(): String {
        val userAgent = window.navigator.userAgent
        return when {
            userAgent.contains("Mac") -> "Mac (Web)"
            userAgent.contains("Windows") -> "Windows (Web)"
            userAgent.contains("Linux") -> "Linux (Web)"
            userAgent.contains("iPhone") -> "iPhone (Web)"
            userAgent.contains("iPad") -> "iPad (Web)"
            userAgent.contains("Android") -> "Android (Web)"
            else -> "Unknown (Web)"
        }.plus(userAgent)
    }
}