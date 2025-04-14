package com.lnd.booksrf.utils

import android.os.Build

inline fun isAtLeastAndroid(versionCode: Int, action: () -> Unit) {
    if (Build.VERSION.SDK_INT >= versionCode) {
        action()
    }
}