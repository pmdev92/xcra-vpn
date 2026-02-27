package com.xray.core.rust.client.xcra.ui.activity


import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.dto.AssetItem
import com.xray.core.rust.client.xcra.dto.getAssetUrlItemProperties
import com.xray.core.rust.client.xcra.enums.AddAssetType
import com.xray.core.rust.client.xcra.extension.concatUrl
import com.xray.core.rust.client.xcra.extension.isNotNullEmpty
import com.xray.core.rust.client.xcra.extension.toast
import com.xray.core.rust.client.xcra.extension.toastError
import com.xray.core.rust.client.xcra.extension.toastSuccess
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.handler.RouterHandler
import com.xray.core.rust.client.xcra.ui.theme.XcraVPNTheme
import com.xray.core.rust.client.xcra.util.HttpUtil
import com.xray.core.rust.client.xcra.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection

class AssetListActivity : ScannerActivity() {
    private var deleteUuid = ""
    private var showSourcesDialog by mutableStateOf(false)
    private var showDeleteDialog by mutableStateOf(false)
    private var geoFilesSources by mutableStateOf("")
    private var isLoading by mutableStateOf(false)

    private var assetsItemsList by mutableStateOf<List<Pair<String, AssetItem>>>(emptyList())

    private val builtInGeoFiles = arrayOf("geosite.dat", "geoip.dat")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        geoFilesSources = getGeoFilesSourcesPref()
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
        enableEdgeToEdge()
        setContent {
            XcraVPNTheme {
                Scaffold(
                    topBar = {
                        TopBarAssets(
                            onAddPress = {
                                when (it) {
                                    AddAssetType.FILE -> {
                                        showFileChooser()
                                    }

                                    AddAssetType.URL -> {
                                        startActivity(
                                            Intent(
                                                this,
                                                AssetActivity::class.java
                                            )
                                        )
                                    }

                                    AddAssetType.QRCODE -> {
                                        importQrCode()
                                    }
                                }
                            },
                            onDownloadPress = {
                                downloadAssetFiles()
                            },
                            onBackPress = {
                                finish()
                            },
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
                { innerPadding ->
                    Screen(
                        modifier = Modifier.padding(innerPadding),
                        geoFilesSources = geoFilesSources,
                        onGeoFilesSourcesClick = {
                            showSourcesDialog = true
                        },
                        assetItems = assetsItemsList,
                        onEdit = {
                            val intent = Intent(this@AssetListActivity, AssetActivity::class.java)
                            intent.putExtra(AssetActivity.ASSET_UUID, it)
                            startActivity(intent)
                        },
                        onRemove = {
                            showDeleteDialog = true
                            deleteUuid = it
                        },
                        isLoading = isLoading
                    )
                    if (showSourcesDialog) {
                        SourceDialog(
                            onSelected = {
                                DatabaseHandler.encodeSettings(AppConfig.PREF_GEO_FILES_SOURCES, it)
                                geoFilesSources = getGeoFilesSourcesPref()
                            },
                            onDismiss = { showSourcesDialog = false }
                        )
                    }
                    if (showDeleteDialog) {
                        DeleteDialog(
                            onConfirm = {
                                if (deleteUuid.isNotNullEmpty()) {
                                    val extDir = File(Utils.userAssetPath(this))
                                    val asset = assetsItemsList.find { it.first == deleteUuid }
                                    if (asset != null) {
                                        val target = File(extDir, asset.second.remarks)
                                        try {
                                            target.delete()
                                        } catch (_: Exception) {
                                        }
                                    }
                                    DatabaseHandler.removeAsset(deleteUuid)
                                    deleteUuid = ""
                                    initAssets()
                                }
                            },
                            onDismiss = { showDeleteDialog = false }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initAssets()
    }

    private fun getGeoFilesSourcesPref(): String {
        return DatabaseHandler.decodeSettingsString(AppConfig.PREF_GEO_FILES_SOURCES)
            ?: AppConfig.GEO_FILES_SOURCES.first()
    }

    private fun addBuiltInAssets(assets: List<Pair<String, AssetItem>>): List<Pair<String, AssetItem>> {
        val list = mutableListOf<Pair<String, AssetItem>>()
        builtInGeoFiles
            .filter { geoFile -> assets.none { it.second.remarks == geoFile } }
            .forEach {
                list.add(
                    Utils.getUuid() to AssetItem(
                        it,
                        String.format(AppConfig.GITHUB_DOWNLOAD_URL, getGeoFilesSourcesPref())
                            .concatUrl(it),
                        locked = true
                    )
                )
            }

        return list + assets
    }

    private fun getAssetsItems(): List<Pair<String, AssetItem>> {
        val assets = DatabaseHandler.decodeAssetItems()

        return addBuiltInAssets(assets)
    }

    fun initAssets() {
        lifecycleScope.launch(Dispatchers.Default) {
            RouterHandler.initAssets(this@AssetListActivity, assets = assets)
            withContext(Dispatchers.Main) {
                assetsItemsList = getAssetsItems()
            }
        }
    }

    private fun importQrCode() {
        openScanner()
    }

    override fun scanResult(text: String?) {
        try {
            if (!Utils.isValidUrl(text)) {
                toast(R.string.toast_invalid_url)
                return
            }
            startActivity(
                Intent(this, AssetListActivity::class.java)
                    .putExtra(AssetActivity.ASSET_URL_QRCODE, text)
            )
        } catch (e: Exception) {
            App.log("Failed to import asset from URL $e")
        }
    }

    private fun showFileChooser() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        requestStoragePermissionLauncher.launch(permission)
    }

    private val requestStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            try {
                chooseFile.launch(
                    Intent.createChooser(
                        intent,
                        getString(R.string.title_file_chooser)
                    )
                )
            } catch (_: android.content.ActivityNotFoundException) {
                toast(R.string.toast_require_file_manager)
            }
        } else {
            toast(R.string.toast_permission_denied)
        }
    }

    private val chooseFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (result.resultCode == RESULT_OK && uri != null) {
                val assetId = Utils.getUuid()
                runCatching {
                    val assetItem = AssetItem(
                        getCursorName(uri) ?: uri.toString(),
                        "file"
                    )
                    val assetList = DatabaseHandler.decodeAssetItems()
                    if (assetList.any { it.second.remarks == assetItem.remarks && it.first != assetId }) {
                        toast(R.string.asset_remark_is_duplicate)
                    } else {
                        DatabaseHandler.encodeAsset(assetId, assetItem)
                        copyAssetFile(uri)
                    }
                }.onFailure {
                    toastError(R.string.toast_asset_copy_failed)
                    DatabaseHandler.removeAsset(assetId)
                }
            }
        }

    private fun getCursorName(uri: Uri): String? = try {
        contentResolver.query(uri, null, null, null, null)?.let { cursor ->
            cursor.run {
                if (moveToFirst()) getString(getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                else null
            }.also { cursor.close() }
        }
    } catch (e: Exception) {
        App.log("Failed to get cursor name $e")
        null
    }

    private fun copyAssetFile(uri: Uri) {
        val extDir = File(Utils.userAssetPath(this))
        val targetFile = File(extDir, getCursorName(uri) ?: uri.toString())
        contentResolver.openInputStream(uri).use { inputStream ->
            targetFile.outputStream().use { fileOut ->
                inputStream?.copyTo(fileOut)
                toastSuccess(R.string.toast_success)
                runOnUiThread {
                    assetsItemsList = getAssetsItems()
                }
            }
        }
    }

    private fun downloadAssetFiles() {
        isLoading = true
        toast(R.string.asset_downloading_content)
        val assets = getAssetsItems()

        var resultCount = 0
        lifecycleScope.launch(Dispatchers.IO) {
            assets.forEach {
                try {
                    val result = downloadGeo(it.second)
                    if (result)
                        resultCount++
                } catch (e: Exception) {
                    App.log("Failed to download geo file: ${it.second.remarks} $e")
                }
            }
            withContext(Dispatchers.Main) {
                if (resultCount > 0) {
                    toast(getString(R.string.asset_update_config_count, resultCount))
                    assetsItemsList = getAssetsItems()
                } else {
                    toast(getString(R.string.toast_failure))
                }
                isLoading = false
            }
        }
    }

    private fun downloadGeo(item: AssetItem): Boolean {
        val extDir = File(Utils.userAssetPath(this))
        val targetTemp = File(extDir, item.remarks + "_temp")
        val target = File(extDir, item.remarks)
        App.log("Downloading geo file: ${item.remarks} from ${item.url}")

        val conn =
            HttpUtil.createProxyConnection(
                urlStr = item.url,
                connectTimeout = 15000,
                readTimeout = 15000,
                needStream = true
            )
                ?: return false
        try {
            val inputStream = conn.inputStream
            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                FileOutputStream(targetTemp).use { output ->
                    inputStream.copyTo(output)
                }

                targetTemp.renameTo(target)
            }
            return true
        } catch (e: Exception) {
            App.log("Failed to download geo file: ${item.remarks} $e")
            return false
        } finally {
            conn.disconnect()
        }
    }
}


@Composable
private fun Screen(
    modifier: Modifier = Modifier,
    geoFilesSources: String,
    onGeoFilesSourcesClick: () -> Unit,
    assetItems: List<Pair<String, AssetItem>>,
    onEdit: (String) -> Unit,
    onRemove: (String) -> Unit,
    isLoading: Boolean
) {


    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGeoFilesSourcesClick() }
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.asset_geo_files_sources),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = geoFilesSources,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.title_user_asset_setting),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(assetItems) { item ->
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        AssetItemRow(
                            uuid = item.first,
                            asset = item.second,
                            onEdit = onEdit,
                            onRemove = onRemove
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarAssets(
    onAddPress: (AddAssetType) -> Unit,
    onDownloadPress: () -> Unit,
    onBackPress: () -> Unit
) {
    var showAddMenu by remember { mutableStateOf(false) }
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    onBackPress()
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back"
                )
            }
        },
        title = {
            Text(stringResource(R.string.title_assets))
        },
        actions = {
            Box {
                IconButton(onClick = {
                    showAddMenu = true
                }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add"
                    )
                }
                AddAssetMenu(
                    expanded = showAddMenu,
                    onAddPress = onAddPress,
                    onDismiss = {
                        showAddMenu = false
                    })
            }
            IconButton(onClick = {
                onDownloadPress()
            }) {
                Icon(
                    imageVector = Icons.Filled.CloudDownload,
                    contentDescription = "Download"
                )
            }
        }
    )
}

@Composable
private fun AddAssetMenu(
    expanded: Boolean,
    onAddPress: (AddAssetType) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.asset_menu_item_add_file)) },
            onClick = {
                onAddPress(AddAssetType.FILE)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.asset_menu_item_add_url)) },
            onClick = {
                onAddPress(AddAssetType.URL)
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.asset_menu_item_scan_qrcode)) },
            onClick = {
                onAddPress(AddAssetType.QRCODE)
                onDismiss()
            }
        )
    }
}

@Composable
fun SourceDialog(
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sources = AppConfig.GEO_FILES_SOURCES
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.small,
        text = {
            Column {
                sources.forEach { value ->
                    Text(
                        text = value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelected(value)
                                onDismiss()
                            }
                            .padding(horizontal = 4.dp)
                            .padding(vertical = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("OK")
            }
        },
    )
}

@Composable
fun AssetItemRow(
    uuid: String,
    asset: AssetItem,
    onEdit: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    val context = LocalContext.current
    val isVisibleEdit = if (asset.locked == true) {
        false
    } else {
        asset.url.let { it != "file" }
    }

    val isVisibleDelete = asset.locked != true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Text(
                text = asset.remarks,
                maxLines = 2,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = getAssetUrlItemProperties(asset, context = context),
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (isVisibleEdit) {
            IconButton(
                onClick = {
                    onEdit(uuid)
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit"
                )
            }
        }
        if (isVisibleDelete) {
            IconButton(
                onClick = {
                    onRemove(uuid)
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete"
                )
            }
        }
    }
}

@Composable
private fun DeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.small,
        text = {
            Text(text = stringResource(R.string.del_config_confirm))
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(text = stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(android.R.string.cancel))
            }
        }
    )
}