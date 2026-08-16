package com.xray.core.rust.client.xcra.ui.model

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.dto.AssetItem
import com.xray.core.rust.client.xcra.extension.isNotNullEmpty
import com.xray.core.rust.client.xcra.extension.toast
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.util.Utils
import java.io.File


class AssetViewModel(
    application: Application,
    var uuid: String? = null,
    var assetItem: AssetItem? = null
) :
    AndroidViewModel(application = application) {

    var remarks by mutableStateOf("")
        private set

    var remarksError by mutableStateOf(false)
    var url by mutableStateOf("")
        private set
    var urlError by mutableStateOf(false)


    init {
        if (assetItem != null) {
            remarks = assetItem?.remarks ?: ""
            url = assetItem?.url ?: ""
        } else {
            assetItem = AssetItem()
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
     * save asset
     */
    fun saveAssets(): Boolean {

        val extDir = File(Utils.userAssetPath(application))
        if (uuid.isNotNullEmpty()) {
            val file = extDir.resolve(assetItem?.remarks.orEmpty())
            if (file.exists()) {
                try {
                    file.delete()
                } catch (e: Exception) {
                    App.log("Failed to delete asset file: ${file.path} $e")
                }
            }
        }

        val assetList = DatabaseHandler.decodeAssetItems()
        if (assetList.any { it.second.remarks == assetItem?.remarks && it.first != uuid }) {
            application.toast(R.string.asset_remark_is_duplicate)
            return false
        }

        if (remarks.isEmpty()) {
            remarksError = true
        }
        if (url.isEmpty()) {
            urlError = true
        }
        if (url.isNotEmpty()) {
            if (!Utils.isValidUrl(url)) {
                urlError = true
            }
        }
        if (remarksError || urlError) {
            return false
        }
        assetItem?.let {
            it.remarks = remarks
            it.url = url
            it.editTime = System.currentTimeMillis()
            uuid = DatabaseHandler.encodeAsset(uuid.orEmpty(), it)
        }
        return true
    }

    fun deleteAsset(): Boolean {
        uuid?.let {
            DatabaseHandler.removeAsset(it)
            return true
        }
        return false
    }
}

private val LocalAssetViewModel = staticCompositionLocalOf<AssetViewModel> {
    error("AssetViewModel not provided")
}

object AssetViewModelAccessor {
    /**
     * Retrieves the current [AssetViewModel] at the call site's position in the hierarchy.
     */
    val assetViewModel: AssetViewModel
        @Composable @ReadOnlyComposable get() = LocalAssetViewModel.current

}

@Composable
fun AssetViewModel(
    assetViewModel: AssetViewModel,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAssetViewModel.provides(assetViewModel)) {
        content()
    }
}