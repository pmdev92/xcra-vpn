package com.xray.core.rust.client.xcra.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import com.xray.core.rust.client.xcra.dto.GroupItem
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.util.Utils


class GroupViewModel(var uuid: String? = null, var groupItem: GroupItem? = null) :
    ViewModel() {

    var remarks by mutableStateOf("")
        private set


    var remarksError by mutableStateOf(false)
    var url by mutableStateOf("")
        private set
    var urlError by mutableStateOf(false)
    var enabled by mutableStateOf(true)
    var autoUpdate by mutableStateOf(true)
    var allowInsecureUrl by mutableStateOf(false)

    var isShowDelete by mutableStateOf(false)
        private set

    init {
        if (groupItem != null) {
            remarks = groupItem?.remarks ?: ""
            url = groupItem?.url ?: ""
            enabled = groupItem?.enabled ?: true
            autoUpdate = groupItem?.autoUpdate ?: true
            allowInsecureUrl = groupItem?.allowInsecureUrl ?: false
            isShowDelete = true
        } else {
            groupItem = GroupItem()
        }
    }

    fun updateRemarks(remarks: String) {
        this.remarks = remarks
        remarksError = false
    }

    fun updateUrl(url: String) {
        this.url = url
        urlError = false
    }

    /**
     * save group
     */
    fun saveGroup(): Boolean {
        if (remarks.isEmpty()) {
            remarksError = true
        }
        if (url.isNotEmpty()) {
            if (!Utils.isValidUrl(url)) {
                urlError = true
            }
            if (!Utils.isValidSubscriptionUrl(url)) {
                if (!allowInsecureUrl) {
                    urlError = true
                }
            }
        }
        if (remarksError || urlError) {
            return false
        }

        groupItem?.let {
            it.remarks = remarks
            it.url = url
            it.enabled = enabled
            it.autoUpdate = autoUpdate
            it.allowInsecureUrl = allowInsecureUrl
            it.editTime = System.currentTimeMillis()
            uuid = DatabaseHandler.encodeGroup(uuid.orEmpty(), it)
        }
        return true
    }

    fun deleteGroup(): Boolean {
        uuid?.let {
            DatabaseHandler.removeGroup(it)
            return true
        }
        return false
    }
}

private val LocalGroupViewModel = staticCompositionLocalOf<GroupViewModel> {
    error("GroupViewModel not provided")
}

object GroupViewModelAccessor {
    /**
     * Retrieves the current [GroupViewModel] at the call site's position in the hierarchy.
     */
    val groupViewModel: GroupViewModel
        @Composable @ReadOnlyComposable get() = LocalGroupViewModel.current

}

@Composable
fun GroupViewModel(
    groupViewModel: GroupViewModel,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalGroupViewModel.provides(groupViewModel)) {
        content()
    }
}