package com.xray.core.rust.client.xcra.ui.model

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.AndroidViewModel
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.ui.component.GroupUiItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


class GroupListViewModel(
    application: Application,
    val groupListModelInterface: GroupListModelInterface
) :
    AndroidViewModel(application) {
    var isLoading: MutableState<Boolean> = mutableStateOf(false)
        private set

    private var _groups: MutableStateFlow<List<GroupUiItem>> =
        MutableStateFlow<List<GroupUiItem>>(listOf())

    val groups: StateFlow<List<GroupUiItem>> = _groups


    init {
        validateGroups()
    }

    @Synchronized
    fun validateGroups() {
        startLoading()
        val list = DatabaseHandler.decodeGroups()
        _groups.value = list.map { item ->
            GroupUiItem(item.first, item.second)
        }
        stopLoading()
    }


    fun toggleEnable(uuid: String, enabled: Boolean) {
        _groups.update { list ->
            list.map {
                if (it.uuid == uuid) {
                    val groupItem = it.groupItem.copy(enabled = enabled)
                    DatabaseHandler.encodeGroup(uuid, groupItem)
                    it.copy(groupItem = groupItem)

                } else it
            }
        }
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

    fun addGroup() {
        groupListModelInterface.addGroup()
    }


    fun editGroup(uuid: String) {
        groupListModelInterface.editGroup(uuid)
    }

    fun removeGroup(uuid: String) {
        DatabaseHandler.removeGroup(uuid)
        validateGroups()
    }

    fun shareGroup(uuid: String) {
        groupListModelInterface.shareGroup(uuid)
    }


    interface GroupListModelInterface {
        fun addGroup()
        fun editGroup(uuid: String)
        fun shareGroup(uuid: String)
    }
}

val LocalGroupListViewModel = staticCompositionLocalOf<GroupListViewModel> {
    error("GroupListViewModel not provided")
}


object GroupListViewModelAccessor {
    /**
     * Retrieves the current [GroupListViewModel] at the call site's position in the hierarchy.
     */
    val groupListViewModel: GroupListViewModel
        @Composable @ReadOnlyComposable get() = LocalGroupListViewModel.current

}

@Composable
fun GroupListViewModel(
    groupListViewModel: GroupListViewModel,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalGroupListViewModel.provides(groupListViewModel)) {
        content()
    }
}