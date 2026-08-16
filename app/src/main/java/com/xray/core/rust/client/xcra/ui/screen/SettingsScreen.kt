package com.xray.core.rust.client.xcra.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.enums.AllowInsecure
import com.xray.core.rust.client.xcra.ui.model.SettingsViewModelAccessor

/* ================= ENTRY ================= */

@Composable
fun SettingsScreen() {
    val model = SettingsViewModelAccessor.settingsViewModel
    val logLevelsCore = stringArrayResource(R.array.core_log_levels).toList()
    val allowInsecureList = AllowInsecure.asList()
    val logLevelsHev = stringArrayResource(R.array.hev_log_level).toList()
    val vpnAddressList = stringArrayResource(R.array.vpn_interface_address).toList()
    val generalTitle = stringResource(R.string.title_pref_general)
    val coreTitle = stringResource(R.string.title_pref_core)
    val vpnTitle = stringResource(R.string.title_pref_vpn)
    val subscriptionsTitle = stringResource(R.string.title_pref_subscriptions)
    val proxyPort = stringResource(R.string.title_pref_socks_port)
    val resolveAddressTitle = stringResource(R.string.title_outbound_domain_resolve)
    val resolveAddressSummery = stringResource(R.string.summery_outbound_domain_resolve)
    val proxyTitle = stringResource(R.string.title_pref_proxy_sharing_enabled)
    val proxySummary = stringResource(R.string.summary_pref_proxy_sharing_enabled)
    val dnsTitle = stringResource(R.string.title_pref_vpn_interface_dns)
    val mtuTitle = stringResource(R.string.title_pref_vpn_interface_mtu)
    val nodeTestTitle = stringResource(R.string.title_pref_delay_test_url)
    val logLevelTitle = stringResource(R.string.title_pref_log_level)
    val allowInsecureTitle = stringResource(R.string.title_pref_allow_insecure)
    val vpnAddressTitle = stringResource(R.string.title_pref_vpn_interface_address)
    val logLevelTunnelTitle = stringResource(R.string.title_pref_vpn_tunnel_log_level)
    val ipv6Title = stringResource(R.string.title_pref_prefer_ipv6)
    val ipv6Summery = stringResource(R.string.summary_pref_prefer_ipv6)
    val bypassTitle = stringResource(R.string.title_pref_vpn_bypass_lan)
    val bypassSummery = stringResource(R.string.summery_pref_vpn_bypass_lan)
    val httpTitle = stringResource(R.string.title_pref_append_http_proxy)
    val httpSummery = stringResource(R.string.summary_pref_append_http_proxy)
    val tcpTimeoutTitle = stringResource(R.string.title_pref_tunnel_tcp_rw_timeout)
    val udpTimeoutTitle = stringResource(R.string.title_pref_tunnel_udp_rw_timeout)
    val autoUpdateSubscriptionTitle = stringResource(R.string.title_pref_auto_update_subscription)
    val autoUpdateSubscriptionSummery =
        stringResource(R.string.summary_pref_auto_update_subscription)
    val autoUpdateIntervalTitle = stringResource(R.string.title_pref_auto_update_interval)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {

        category(generalTitle) {
            editText(
                title = nodeTestTitle,
                value = model.nodeTestUrl,
                singleLine = false,
                onChange = {
                    model.updateNodeTestUrl(it)
                }
            )
        }
        divider()
        category(subscriptionsTitle) {
            check(
                title = autoUpdateSubscriptionTitle,
                summary = autoUpdateSubscriptionSummery,
                value = model.autoUpdate,
                onChange = {
                    model.updateAutoUpdate(it)
                }
            )
            editNumber(
                title = autoUpdateIntervalTitle,
                value = model.autoUpdateInterval.toString(),
                onChange = {
                    model.updateAutoUpdateInterval(it)
                },
                disable = !model.autoUpdate
            )
        }
        divider()
        category(coreTitle) {
            check(
                title = proxyTitle,
                summary = proxySummary,
                value = model.proxySharing,
                onChange = {
                    model.updateProxySharing(it)
                }
            )
            check(
                title = resolveAddressTitle,
                summary = resolveAddressSummery,
                value = model.resolveAddress,
                onChange = {
                    model.updateResolveAddress(it)
                }
            )
            editNumber(
                title = proxyPort,
                value = model.socksPort.toString(),
                onChange = {
                    model.updateSocksPort(it)
                }
            )
            list(
                title = allowInsecureTitle,
                entries = allowInsecureList,
                value = model.allowInsecure,
                onChange = {
                    model.updateAllowInsecure(it)
                }
            )
            list(
                title = logLevelTitle,
                entries = logLevelsCore,
                value = model.logLevel,
                onChange = {
                    model.updateLogLevel(it)
                }
            )
        }
        divider()

        category(vpnTitle) {
            list(
                title = vpnAddressTitle,
                entries = vpnAddressList,
                value = model.vpnInterfaceAddress,
                onChange = {
                    model.updateVpnInterfaceAddress(it)
                }
            )

            editText(
                title = dnsTitle,
                value = model.vpnInterfaceDns,
                singleLine = false,
                onChange = {
                    model.updateVpnInterfaceDns(it)
                }
            )

            editNumber(
                title = mtuTitle,
                value = model.vpnInterfaceMTU.toString(),
                onChange = {
                    model.updateVpnInterfaceMTU(it)
                }
            )

            check(
                title = ipv6Title,
                summary = ipv6Summery,
                value = model.vpnInterfaceIpv6Enable,
                onChange = {
                    model.updateVpnInterfaceIpv6Enable(it)
                }
            )

            check(
                title = bypassTitle,
                summary = bypassSummery,
                value = model.vpnInterfaceBypassLanEnable,
                onChange = {
                    model.updateVpnInterfaceBypassLanEnable(it)
                }
            )
            check(
                title = httpTitle,
                summary = httpSummery,
                value = model.vpnInterfaceHttpProxyEnable,
                onChange = {
                    model.updateVpnInterfaceHttpProxyEnable(it)
                }
            )
            list(
                title = logLevelTunnelTitle,
                entries = logLevelsHev,
                value = model.vpnInterfaceLogLevel,
                onChange = {
                    model.updateVpnInterfaceLogLevel(it)
                }
            )

            editNumber(
                title = tcpTimeoutTitle,
                value = model.vpnInterfaceTcpTimeout.toString(),
                onChange = {
                    model.updateVpnInterfaceTcpTimeout(it)
                }
            )

            editNumber(
                title = udpTimeoutTitle,
                value = model.vpnInterfaceUdpTimeout.toString(),
                onChange = {
                    model.updateVpnInterfaceUdpTimeout(it)
                }
            )
        }
    }
}

/* ================= COMPONENTS ================= */

@Composable
private fun CheckboxPref(
    title: String,
    summary: String?,
    value: Boolean = false,
    onChange: (Boolean) -> Unit
) {
    PreferenceRow(
        title = title,
        summary = summary?.let { summary }
    ) {
        Checkbox(
            checked = value,
            onCheckedChange = { onChange(it) }
        )
    }
}

@Composable
fun EditDialogPref(
    title: String,
    value: String,
    onChange: (String) -> Unit,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    disable: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(showDialog) {
        if (showDialog) {
            focusRequester.requestFocus()
        } else {
            focusRequester.freeFocus()
        }
    }

    PreferenceRow(
        title = title,
        summary = value,
        modifier = Modifier.clickable {
            if (!disable) {
                showDialog = true
            }
        },
        disable = disable
    ) {}
    if (showDialog) {
        var temp by remember {
            mutableStateOf(
                TextFieldValue(
                    text = value,
                    selection = TextRange(value.length)
                )
            )
        }
        AlertDialog(
            shape = MaterialTheme.shapes.extraSmall,
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                TextField(
                    value = temp,
                    onValueChange = { temp = it },
                    singleLine = singleLine,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = TextFieldDefaults.colors().copy(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onChange(temp.text)
                    showDialog = false
                }) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        )
    }
}


@Composable
fun ListDialogPref(
    title: String,
    entries: List<String>,
    value: String,
    onChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val selectedIndex = entries.indexOf(value).coerceAtLeast(0)

    PreferenceRow(
        title = title,
        summary = entries[selectedIndex],
        modifier = Modifier.clickable { showDialog = true }
    ) {}

    if (showDialog) {
        AlertDialog(
            shape = MaterialTheme.shapes.extraSmall,
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    entries.forEachIndexed { index, label ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onChange(entries[index])
                                    showDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = index == selectedIndex,
                                onClick = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}


/* ================= HELPERS ================= */

@Composable
fun PreferenceRow(
    title: String,
    summary: String?,
    modifier: Modifier = Modifier,
    disable: Boolean = false,
    trailing: @Composable () -> Unit,
) {
    val contentColor = if (disable) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    } else null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor ?: LocalContentColor.current
            )

            if (summary != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor ?: LocalContentColor.current
                )
            }
        }

        CompositionLocalProvider(
            LocalContentColor provides (contentColor ?: LocalContentColor.current)
        ) {
            trailing()
        }
    }
}


/* ================= SHORTCUT LAZY LIST ================= */
private fun LazyListScope.category(
    title: String,
    content: LazyListScope.() -> Unit
) {
    item {
        Text(
            text = title,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
    content()
}

private fun LazyListScope.divider(
) {
    item {
        HorizontalDivider(
            modifier = Modifier.padding(4.dp)
        )
    }
}

private fun LazyListScope.check(
    title: String,
    summary: String,
    value: Boolean = false,
    onChange: (Boolean) -> Unit
) = item {
    CheckboxPref(title, summary, value, onChange)
}

private fun LazyListScope.editText(
    title: String,
    value: String,
    singleLine: Boolean = true,
    onChange: (String) -> Unit,
    disable: Boolean = false
) = item {
    EditDialogPref(
        title = title,
        value = value,
        onChange = onChange,
        singleLine = singleLine,
        keyboardType = KeyboardType.Number,
        disable = disable
    )
}

private fun LazyListScope.editNumber(
    title: String,
    value: String,
    onChange: (String) -> Unit,
    disable: Boolean = false
) = item {
    EditDialogPref(
        title = title,
        value = value,
        onChange = onChange,
        keyboardType = KeyboardType.Number,
        disable = disable
    )
}


private fun LazyListScope.list(
    title: String,
    entries: List<String>,
    value: String,
    onChange: (String) -> Unit
) = item {
    ListDialogPref(
        title = title,
        entries = entries,
        value = value,
        onChange = onChange,
    )
}