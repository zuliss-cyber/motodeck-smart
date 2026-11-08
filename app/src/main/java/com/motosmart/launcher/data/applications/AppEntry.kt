package com.motosmart.launcher.data.applications

import android.graphics.drawable.Drawable
import com.motosmart.launcher.data.preferences.AppRef

data class AppEntry(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable
) {
    val ref: AppRef get() = AppRef(packageName, activityName)
    val key: String get() = "$packageName/$activityName"
}
