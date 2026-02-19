package com.xray.core.rust.client.xcra.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.ui.model.GroupListViewModel
import com.xray.core.rust.client.xcra.ui.model.SettingsViewModel
import com.xray.core.rust.client.xcra.ui.screen.SettingsScreen
import com.xray.core.rust.client.xcra.ui.theme.XcraVPNTheme

class SettingsActivity : ComponentActivity(), GroupListViewModel.GroupListModelInterface {
    private lateinit var settingsViewModel: SettingsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            setResult()
        }

        settingsViewModel = SettingsViewModel(application)
        enableEdgeToEdge()
        setContent {
            SettingsViewModel(settingsViewModel) {
                XcraVPNTheme {
                    Scaffold(
                        topBar = {
                            TopBarSettings(
                                onBackPress = {
                                    setResult()
                                },
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    { innerPadding ->
                        Screen(
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    fun setResult() {
        setResult(RESULT_OK, Intent())
        finish()
    }


    override fun addGroup() {
        val intent = Intent(this, GroupActivity::class.java)
        startActivity(intent)
    }

    override fun editGroup(uuid: String) {
        val intent = Intent(this, GroupActivity::class.java)
        intent.putExtra(GroupActivity.GROUP_UUID, uuid)
        startActivity(intent)
    }

    override fun shareGroup(uuid: String) {

    }
}

@Composable
private fun Screen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        SettingsScreen()
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
            Text(stringResource(R.string.title_setting))
        },
    )
}