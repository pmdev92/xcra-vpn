package com.xray.core.rust.client.xcra.ui.model

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.dto.AppInfo
import com.xray.core.rust.client.xcra.dto.core.PerAppSettings
import com.xray.core.rust.client.xcra.handler.ApplicationHander
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.util.JsonUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Collator


class PerAppViewModel(
    application: Application,
) :
    AndroidViewModel(application) {

    var isLoading: MutableState<Boolean> = mutableStateOf(false)
        private set

    var filter by mutableStateOf("")
        private set
    var perAppProxyEnabled: MutableState<Boolean> = mutableStateOf(false)
        private set
    var bypassAppsEnabled: MutableState<Boolean> = mutableStateOf(false)
        private set
    private var _apps: MutableStateFlow<List<AppInfo>> =
        MutableStateFlow<List<AppInfo>>(listOf())

    private var appsList = mutableListOf<AppInfo>()

    val apps: StateFlow<List<AppInfo>> = _apps


    init {
        loadApps()
    }

    @Synchronized
    fun loadApps() {
        startLoading()
        viewModelScope.launch {
            try {
                val selectedAppList =
                    DatabaseHandler.decodeSettingsStringSet(AppConfig.PREF_PER_APP_PROXY_APPS_SET)
                val appsList = ApplicationHander.loadNetworkAppList(application)
                if (selectedAppList != null) {
                    appsList.forEach { app ->
                        app.isSelected =
                            selectedAppList.contains(app.packageName)
                    }
                    appsList.sortedWith { p1, p2 ->
                        when {
                            p1.isSelected > p2.isSelected -> -1
                            p1.isSelected < p2.isSelected -> 1
                            p1.isSystemApp > p2.isSystemApp -> 1
                            p1.isSystemApp < p2.isSystemApp -> -1
                            p1.appName.lowercase() > p2.appName.lowercase() -> 1
                            p1.appName.lowercase() < p2.appName.lowercase() -> -1
                            p1.packageName > p2.packageName -> 1
                            p1.packageName < p2.packageName -> -1
                            else -> 0
                        }
                    }
                } else {
                    val collator = Collator.getInstance()
                    appsList.sortedWith(compareBy(collator) { it.appName })
                }
                this@PerAppViewModel.appsList = appsList
            } catch (e: Exception) {
                App.log("Error loading apps $e")
            }
            validateApps()
            stopLoading()
        }
    }

    private fun validateApps() {
        startLoading()

        val upperFilter = filter.uppercase()

        _apps.value = if (filter.isEmpty()) {
            appsList.toList()
        } else {
            appsList.filter { item ->
                item.appName.uppercase().contains(upperFilter) ||
                        item.packageName.uppercase().contains(upperFilter)
            }
        }
        stopLoading()
    }

    fun updateFilter(value: String) {
        filter = value
        validateApps()
    }

    private fun setLoading(value: Boolean) {
        isLoading.value = value
    }

    fun startLoading() {
        setLoading(true)
    }

    fun stopLoading() {
        setLoading(false)
    }

    fun toggleItem(item: AppInfo) {
        _apps.update { list ->
            list.map {
                if (it.packageName == item.packageName) {
                    it.copy(isSelected = !it.isSelected)
                } else it
            }
        }
    }

    fun activeItemString(item: String) {
        _apps.update { list ->
            list.map {
                var enable = false
                if (it.packageName == item) {
                    enable = true
                }
                it.copy(isSelected = enable)
            }
        }
    }

    fun toggleAll() {
        var selectAll = false
        for (item in _apps.value) {
            if (!item.isSelected) {
                selectAll = true
                break
            }
        }
        if (selectAll) {
            _apps.update { list ->
                list.map {
                    it.copy(isSelected = true)
                }
            }
        } else {
            _apps.update { list ->
                list.map {
                    it.copy(isSelected = false)
                }
            }
        }
    }

    fun import(str: String): Boolean {
        try {
            val settings = JsonUtil.fromJson(str, PerAppSettings::class.java)
            setPerAppProxyEnable(settings.enable)
            setBypassAppsEnable(settings.bypass)
            for (item in settings.items) {
                activeItemString(item)
            }
            return true
        } catch (_: Exception) {

        }
        return false
    }

    fun export(): String {
        val items: MutableList<String> = mutableListOf()
        for (app in apps.value) {
            if (app.isSelected) {
                items.add(app.packageName)
            }
        }
        val settings = PerAppSettings(
            enable = perAppProxyEnabled.value,
            bypass = bypassAppsEnabled.value,
            items = items,
        )
        return JsonUtil.toJson(settings)
    }

    fun setPerAppProxyEnable(value: Boolean) {
        DatabaseHandler.encodeSettings(AppConfig.PREF_PER_APP_PROXY, value)
        perAppProxyEnabled.value = value
    }

    fun setBypassAppsEnable(value: Boolean) {
        DatabaseHandler.encodeSettings(AppConfig.PREF_BYPASS_APPS, value)
        bypassAppsEnabled.value = value
    }

}

val LocalPerAppViewModel = staticCompositionLocalOf<PerAppViewModel> {
    error("GroupListViewModel not provided")
}


object PerAppViewModelAccessor {
    /**
     * Retrieves the current [GroupListViewModel] at the call site's position in the hierarchy.
     */
    val perAppViewModel: PerAppViewModel
        @Composable @ReadOnlyComposable get() = LocalPerAppViewModel.current

}

@Composable
fun PerAppViewModel(
    perAppViewModel: PerAppViewModel,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalPerAppViewModel.provides(perAppViewModel)) {
        content()
    }
}