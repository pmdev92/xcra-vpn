package com.xray.core.rust.client.xcra.ui.activity

import AboutScreen
import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.tencent.mmkv.MMKV
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.BuildConfig
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.enums.AboutItem
import com.xray.core.rust.client.xcra.extension.toast
import com.xray.core.rust.client.xcra.extension.toastError
import com.xray.core.rust.client.xcra.extension.toastSuccess
import com.xray.core.rust.client.xcra.ui.theme.XcraVPNTheme
import com.xray.core.rust.client.xcra.util.Utils
import com.xray.core.rust.client.xcra.util.ZipUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class AboutActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                try {
                    showFileChooser()
                } catch (e: Exception) {
                    App.log("Failed to show file chooser $e")
                }
            } else {
                toast(R.string.toast_permission_denied)
            }
        }
    private val extDir by lazy { File(Utils.backupPath(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
        val path = extDir.absolutePath
        enableEdgeToEdge()
        setContent {
            XcraVPNTheme {
                Scaffold(
                    topBar = {
                        TopBarSettings(
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
                        path = path,
                        onClick = {
                            when (it) {
                                AboutItem.Backup -> {
                                    val ret = backupConfiguration(extDir.absolutePath)
                                    if (ret.first) {
                                        toastSuccess(R.string.toast_success)
                                    } else {
                                        toastError(R.string.toast_failure)
                                    }
                                }

                                AboutItem.Share -> {
                                    val ret = backupConfiguration(cacheDir.absolutePath)
                                    if (ret.first) {
                                        startActivity(
                                            Intent.createChooser(
                                                Intent(Intent.ACTION_SEND).setType("application/zip")
                                                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    .putExtra(
                                                        Intent.EXTRA_STREAM,
                                                        FileProvider.getUriForFile(
                                                            this,
                                                            BuildConfig.APPLICATION_ID + ".cache",
                                                            File(ret.second)
                                                        )
                                                    ), getString(R.string.title_configuration_share)
                                            )
                                        )
                                    } else {
                                        toastError(R.string.toast_failure)
                                    }
                                }

                                AboutItem.Restore -> {
                                    val permission =
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            Manifest.permission.READ_MEDIA_IMAGES
                                        } else {
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        }

                                    if (ContextCompat.checkSelfPermission(
                                            this,
                                            permission
                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    ) {
                                        try {
                                            showFileChooser()
                                        } catch (e: Exception) {
                                            App.log("Failed to show file chooser $e")
                                        }
                                    } else {
                                        requestPermissionLauncher.launch(permission)
                                    }

                                }

                                AboutItem.Source -> {

                                    Utils.openUri(this, AppConfig.APP_URL)
                                }

                                AboutItem.License -> {
                                    val webView = android.webkit.WebView(this)
                                    webView.loadUrl("file:///android_asset/open_source_licenses.html")
                                    android.app.AlertDialog.Builder(this)
                                        .setTitle("Open source licenses")
                                        .setView(webView)
                                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                        .show()
                                }

                                AboutItem.Feedback -> {
                                    Utils.openUri(this, AppConfig.APP_ISSUES_URL)
                                }

                                AboutItem.Telegram -> {
                                    Utils.openUri(this, AppConfig.TG_CHANNEL_URL)
                                }

                                AboutItem.PrivacyPolicy -> {
                                    Utils.openUri(this, AppConfig.APP_PRIVACY_POLICY)
                                }
                            }
                        }
                    )
                }
            }
        }
    }


    private fun backupConfiguration(outputZipFilePos: String): Pair<Boolean, String> {
        val dateFormated = SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss",
            Locale.getDefault()
        ).format(System.currentTimeMillis())
        val folderName = "${getString(R.string.app_name)}-${dateFormated}"
        val backupDir = this.cacheDir.absolutePath + "/$folderName"
        val outputZipFilePath = "$outputZipFilePos/$folderName.zip"

        val count = MMKV.backupAllToDirectory(backupDir)
        if (count <= 0) {
            return Pair(false, "")
        }

        if (ZipUtil.zipFromFolder(backupDir, outputZipFilePath)) {
            return Pair(true, outputZipFilePath)
        } else {
            return Pair(false, "")
        }
    }

    private fun restoreConfiguration(zipFile: File): Boolean {
        val backupDir = this.cacheDir.absolutePath + "/${System.currentTimeMillis()}"

        if (!ZipUtil.unzipToFolder(zipFile, backupDir)) {
            return false
        }

        val count = MMKV.restoreAllFromDirectory(backupDir)
        return count > 0
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        try {
            chooseFile.launch(Intent.createChooser(intent, getString(R.string.title_file_chooser)))
        } catch (e: android.content.ActivityNotFoundException) {
            App.log("File chooser activity not found $e")
            toast(R.string.toast_require_file_manager)
        }
    }

    private val chooseFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (result.resultCode == RESULT_OK && uri != null) {
                try {
                    val targetFile =
                        File(this.cacheDir.absolutePath, "${System.currentTimeMillis()}.zip")
                    contentResolver.openInputStream(uri).use { input ->
                        targetFile.outputStream().use { fileOut ->
                            input?.copyTo(fileOut)
                        }
                    }
                    if (restoreConfiguration(targetFile)) {
                        toastSuccess(R.string.toast_success)
                    } else {
                        toastError(R.string.toast_failure)
                    }
                } catch (e: Exception) {
                    App.log("Error during file restore $e")
                    toastError(R.string.toast_failure)
                }
            }
        }
}

@Composable
private fun Screen(
    modifier: Modifier = Modifier,
    path: String,
    onClick: (AboutItem) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        AboutScreen(
            path = path,
            onClick = onClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarSettings(
    onBackPress: () -> Unit
) {
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
            Text(stringResource(R.string.title_about))
        },
    )
}