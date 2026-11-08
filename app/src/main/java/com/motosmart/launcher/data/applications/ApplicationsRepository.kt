package com.motosmart.launcher.data.applications

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Process
import android.os.UserHandle
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.motosmart.launcher.data.preferences.AppRef
import java.text.Collator
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Reads launchable activities through [LauncherApps], which is the supported API for
 * home-screen replacements. No QUERY_ALL_PACKAGES permission is required.
 */
class ApplicationsRepository(private val context: Context) {

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val packageManager = context.packageManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val collator = Collator.getInstance(Locale.getDefault()).apply { strength = Collator.PRIMARY }

    private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
    val apps: StateFlow<List<AppEntry>> = _apps.asStateFlow()

    private var receiverRegistered = false

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refresh()
        }
    }

    init {
        refresh()
        registerPackageReceiver()
    }

    fun refresh() {
        scope.launch {
            _apps.value = loadApplications()
        }
    }

    private fun loadApplications(): List<AppEntry> {
        val user = Process.myUserHandle()
        return try {
            launcherApps.getActivityList(null, user)
                .mapNotNull { activity ->
                    runCatching {
                        AppEntry(
                            packageName = activity.applicationInfo.packageName,
                            activityName = activity.componentName.className,
                            label = activity.label?.toString()?.takeIf { it.isNotBlank() }
                                ?: activity.applicationInfo.loadLabel(packageManager).toString(),
                            icon = activity.getBadgedIcon(0)
                        )
                    }.getOrNull()
                }
                .sortedWith { first, second -> collator.compare(first.label, second.label) }
        } catch (_: SecurityException) {
            emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun find(ref: AppRef): AppEntry? =
        _apps.value.firstOrNull { it.packageName == ref.packageName && it.activityName == ref.className }

    fun find(packageName: String): AppEntry? =
        _apps.value.firstOrNull { it.packageName == packageName }

    /**
     * Builds the out-of-the-box dock by resolving the standard system intents.
     * Any slot that cannot be resolved is skipped safely.
     */
    fun resolveDefaultDock(): List<AppRef> {
        val intents = listOf(
            Intent(Intent.ACTION_DIAL).addCategory(Intent.CATEGORY_DEFAULT),
            Intent(Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q=maps"))
                .addCategory(Intent.CATEGORY_DEFAULT),
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC),
            Intent(Settings.ACTION_SETTINGS),
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        )
        return intents.mapNotNull { intent ->
            val resolved = packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            ).firstOrNull { info ->
                val packageName = info.activityInfo?.packageName
                packageName != null && packageName != context.packageName
            }
            resolved?.activityInfo?.let { AppRef(it.packageName, it.name) }
        }.distinctBy { it.packageName }.take(6)
    }

    fun launch(entry: AppEntry, sourceBounds: Rect? = null): Boolean {
        return try {
            launcherApps.startMainActivity(
                ComponentName(entry.packageName, entry.activityName),
                Process.myUserHandle(),
                sourceBounds,
                null
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    fun launchRef(ref: AppRef): Boolean {
        val entry = find(ref) ?: return false
        return launch(entry)
    }

    fun launchIntent(intent: Intent): Boolean {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openAppInfo(ref: AppRef) {
        launchIntent(
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${ref.packageName}")
            }
        )
    }

    fun openMusicApp(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC)
        return launchIntent(intent)
    }

    fun openCamera(): Boolean {
        return launchIntent(Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE))
    }

    fun openDialer(): Boolean {
        return launchIntent(Intent(Intent.ACTION_DIAL))
    }

    fun openDefaultHomeSettings() {
        launchIntent(Intent(android.provider.Settings.ACTION_HOME_SETTINGS))
    }

    @Suppress("UnusedReceiverParameter")
    fun userHandle(): UserHandle = Process.myUserHandle()

    private fun registerPackageReceiver() {
        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        runCatching {
            ContextCompat.registerReceiver(
                context,
                packageReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            receiverRegistered = true
        }
    }
}
