package com.section11.crossclip.framework.utils

import android.os.Build

actual object DeviceInfo {
    actual fun getDeviceInfo(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
    }
}