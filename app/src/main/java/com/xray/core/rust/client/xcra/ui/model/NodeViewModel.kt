package com.xray.core.rust.client.xcra.ui.model

import android.app.Application
import androidx.annotation.ArrayRes
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
import com.xray.core.rust.client.xcra.dto.NodeItem
import com.xray.core.rust.client.xcra.enums.ConfigType
import com.xray.core.rust.client.xcra.extension.toast
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.util.JsonUtil
import com.xray.core.rust.client.xcra.util.Utils


class NodeViewModel(application: Application) : AndroidViewModel(application) {
    private var isNew: Boolean = true
    private var editUuid: String? = null
    var isShowDelete by mutableStateOf(false)
        private set
    private var groupId: String? = null
    private lateinit var nodeItem: NodeItem

    constructor(
        application: Application,
        editUuid: String,
        config: NodeItem,
        subscriptionId: String?
    ) : this(
        application
    ) {
        this.nodeItem = config
        this.editUuid = editUuid
        this.groupId = subscriptionId
        titleProtocol = TitleItem(config.configType.getTitle())
        isNew = false
        isShowDelete = true
        initialize()
    }

    constructor(context: Application, configType: ConfigType, subscriptionId: String?) : this(
        context
    ) {
        this.nodeItem = NodeItem.create(configType)
        this.groupId = subscriptionId
        titleProtocol = TitleItem(nodeItem.configType.getTitle())
        isNew = true
        isShowDelete = false
        initialize()
    }


    private fun firstArrayString(@ArrayRes int: Int): String {
        return application.resources.getStringArray(int).firstOrNull().orEmpty()
    }

    private fun initialize() {
        if (!isNew) {
            applyNodeItem()
        }
        validateNodeFields()
    }

    var items = mutableStateOf<List<Item>>(listOf())
        private set

    lateinit var titleProtocol: TitleItem
    val divider = DividerItem()
    val titleGeneral = TitleItem(application.getString(R.string.title_general))
    val transportTitle: TitleItem = TitleItem(application.getString(R.string.title_transport))


    val remarks = TextField(R.string.node_lab_remarks)
    val address = TextField(R.string.node_lab_address)
    val port = IntegerField(R.string.node_lab_port)


    val username = TextField(R.string.node_lab_username)
    val password = TextField(R.string.node_lab_uuid)
    val uuid = TextField(R.string.node_lab_uuid)


    val vlessEncryption = TextField(R.string.node_lab_vless_encryption, "none")
    val vlessFlow = DropField(R.string.node_lab_vless_flow, R.array.flows)
    val vmessSecurity = DropField(R.string.node_lab_security, R.array.vmess_securities)
    val shadowSocksMethods = DropField(
        R.string.node_lab_security,
        R.array.shadow_socks_methods,
        firstArrayString(R.array.shadow_socks_methods),
    )
    val tuicCongestionControl =
        DropField(
            R.string.node_lab_tuic_congestion_control,
            R.array.tuic_udp_relay_mode,
            firstArrayString(R.array.tuic_udp_relay_mode),
            isCapitalize = true
        )
    val tuicUdpRelayMode =
        DropField(
            R.string.node_lab_tuic_udp_relay_mode,
            R.array.shadow_socks_methods,
            firstArrayString(R.array.shadow_socks_methods),
            isCapitalize = true
        )
    val tuicHeartbeat = TextField(R.string.node_lab_host, "10s")
    val hysteria2ObfsPassword = TextField(R.string.node_lab_host)
    val hysteria2PortHopping = TextField(R.string.node_lab_host)
    val hysteria2PortHoppingInterval = TextField(R.string.node_lab_host)

    val transport = TransportDropField(R.string.node_lab_transport, R.array.transports)
    val headerType =
        DropField(
            R.string.node_lab_tcp_header_type,
            R.array.tcp_header_type,
            firstArrayString(R.array.tcp_header_type),
            isCapitalize = true
        )
    val xhttpMode =
        DropField(
            R.string.node_lab_xhttp_mode,
            R.array.xhttp_mode,
            firstArrayString(R.array.xhttp_mode),
            isCapitalize = true
        )


    val host = TextField(R.string.node_lab_host)
    val path = TextField(R.string.node_lab_path)
    val serviceName = TextField(R.string.node_lab_path)

    val security = TlsDropField(
        R.string.node_lab_security,
        R.array.securities,
        firstArrayString(R.array.securities),
    )
    val alpn = TlsDropField(
        R.string.node_lab_security_alpn,
        R.array.security_alpn,
        firstArrayString(R.array.security_alpn),
    )
    val sni = TextField(R.string.node_lab_sni)
    val insecure =
        DropField(R.string.node_lab_insecure, R.array.bools, firstArrayString(R.array.bools))
    val publicKey = TextField(R.string.node_lab_public_key)
    val shortId = TextField(R.string.node_lab_short_id)

    fun updateField(field: Configurable, value: String) {
        field.updateValue(value)
        validateNodeFields()
    }

    fun validateNodeFields() {
        val current = mutableListOf(
            titleGeneral,
            remarks,
            address,
            port,
            divider,
            titleProtocol
        )
        when (this.nodeItem.configType) {
            ConfigType.SOCKS5 -> {
                current.add(username)
                current.add(password)
            }

            ConfigType.VLESS -> {
                current.add(uuid)
                current.add(vlessEncryption)
                current.add(vlessFlow)
            }

            ConfigType.VMESS -> {
                current.add(uuid)
                current.add(vmessSecurity)
            }

            ConfigType.TROJAN -> {
                current.add(password)
            }

            ConfigType.SHADOWSOCKS -> {
                current.add(password)
            }

            ConfigType.TUIC -> {
                current.add(password)
                current.add(uuid)
                current.add(tuicCongestionControl)
                current.add(tuicUdpRelayMode)
                current.add(tuicHeartbeat)
            }

            ConfigType.HYSTERIA2 -> {
                current.add(password)
                current.add(hysteria2ObfsPassword)
                current.add(hysteria2PortHopping)
                current.add(hysteria2PortHoppingInterval)
            }
        }
        if (this.nodeItem.configType.hasTransport()) {
            current.add(divider)
            current.add(transportTitle)
            current.add(transport)
            if (transport.isTcp()) {
                current.add(headerType)
                if (headerType.value == "http") {
                    current.add(host)
                    current.add(path)
                }
            }
            if (transport.isWebsocket()) {
                current.add(host)
                current.add(path)
            }
            if (transport.isHttpupgrade()) {
                current.add(host)
                current.add(path)
            }
            if (transport.isHttp2()) {
                current.add(host)
                current.add(path)
            }
            if (transport.isGrpc()) {
                current.add(serviceName)
            }
            if (transport.isXHttp()) {
                current.add(xhttpMode)
                current.add(host)
                current.add(path)
            }
            current.add(security)
            if (security.isTls()) {
                current.add(sni)
                current.add(insecure)
                current.add(alpn)
            } else if (security.isReality()) {
                current.add(sni)
                current.add(publicKey)
                current.add(shortId)
                current.add(alpn)
            }
        }
        items = mutableStateOf(
            current
        )
    }

    fun applyNodeItem() {

        //general items
        remarks.updateValue(nodeItem.remarks)
        address.updateValue(nodeItem.address)
        port.updateValue(nodeItem.port)
        username.updateValue(nodeItem.username)
        password.updateValue(nodeItem.password)
        uuid.updateValue(nodeItem.uuid)

        //vless items
        vlessEncryption.updateValue(nodeItem.vlessEncryption)
        vlessFlow.updateValue(nodeItem.vlessFlow)

        //vmess items
        vmessSecurity.updateValue(nodeItem.vmessSecurity)

        //shadow-socks items
        shadowSocksMethods.updateValue(nodeItem.shadowSocksMethod)

        //tuic items
        tuicCongestionControl.updateValue(nodeItem.tuicCongestionControl)
        tuicUdpRelayMode.updateValue(nodeItem.tuicUdpRelayMode)
        tuicHeartbeat.updateValue(nodeItem.tuicHeartbeat)

        //hysteria2 items
        hysteria2ObfsPassword.updateValue(nodeItem.hysteria2ObfsPassword)
        hysteria2PortHopping.updateValue(nodeItem.hysteria2PortHopping)
        hysteria2PortHoppingInterval.updateValue(nodeItem.hysteria2PortHoppingInterval)

        transport.updateValue(nodeItem.transport)
        host.updateValue(nodeItem.host)
        path.updateValue(nodeItem.path)
        headerType.updateValue(nodeItem.headerType)
        serviceName.updateValue(nodeItem.serviceName)

        xhttpMode.updateValue(nodeItem.xhttpMode)


        security.updateValue(nodeItem.security)
        sni.updateValue(nodeItem.sni)
        alpn.updateValue(nodeItem.alpn)
        nodeItem.insecure?.let {
            if (it) {
                insecure.updateValue("True")
            } else {
                insecure.updateValue("False")
            }
        }

        publicKey.updateValue(nodeItem.publicKey)
        shortId.updateValue(nodeItem.shortId)
    }

    /**
     * save node item
     */
    fun saveNode(): Boolean {
        if (address.value.trim().isEmpty()) {
            address.updateError(application)
            return false
        }
        val portValue = port.value.trim()
        if (nodeItem.configType != ConfigType.HYSTERIA2) {
            val portValue = Utils.parseInt(portValue)
            if (portValue !in 1..<65535) {
                port.updateError(application)
                return false
            }
        }
        when (nodeItem.configType) {
            ConfigType.VLESS, ConfigType.VMESS -> {
                if (uuid.value.trim().isEmpty()) {
                    uuid.updateError(application)
                    return false
                }
            }

            ConfigType.TROJAN, ConfigType.SHADOWSOCKS, ConfigType.HYSTERIA2 -> {
                if (password.value.trim().isEmpty()) {
                    password.updateError(application)
                    return false
                }
            }

            ConfigType.TUIC -> {
                if (uuid.value.trim().isEmpty()) {
                    uuid.updateError(application)
                    return false
                }
                if (password.value.trim().isEmpty()) {
                    password.updateError(application)
                    return false
                }
            }

            else -> {

            }
        }

        saveCommon(nodeItem)
        saveStreamSettings(nodeItem)
        saveTls(nodeItem)
        if (nodeItem.groupId.isEmpty() && !groupId.isNullOrEmpty()) {
            nodeItem.groupId = groupId.orEmpty()
        }
        App.log("Saved config is " + JsonUtil.toJsonPretty(nodeItem))
        if (isNew) {
            nodeItem.addedTime = System.currentTimeMillis()
        }
        DatabaseHandler.encodeNodeItem(this.editUuid.orEmpty(), nodeItem, true)
        return true
    }

    private fun saveCommon(nodeItem: NodeItem) {
        var remarks = remarks.value.trim()
        if (remarks.isEmpty()) {
            remarks = Utils.getDateTimeFormated()
        }
        nodeItem.remarks = remarks
        nodeItem.address = address.value.trim()
        nodeItem.port = port.value.trim()

        nodeItem.username = username.value.trim()
        nodeItem.password = password.value.trim()
        nodeItem.uuid = uuid.value.trim()

        nodeItem.vlessEncryption = vlessEncryption.value.trim()
        nodeItem.vlessFlow = vlessFlow.value.trim()
        nodeItem.vmessSecurity = vmessSecurity.value.trim()

        nodeItem.shadowSocksMethod = shadowSocksMethods.value.trim()

        nodeItem.tuicCongestionControl = tuicCongestionControl.value.trim()
        nodeItem.tuicUdpRelayMode = tuicUdpRelayMode.value.trim()
        nodeItem.tuicHeartbeat = tuicHeartbeat.value.trim()

        nodeItem.hysteria2ObfsPassword = hysteria2ObfsPassword.value.trim()
        nodeItem.hysteria2PortHopping = hysteria2PortHopping.value.trim()
        nodeItem.hysteria2PortHoppingInterval = hysteria2PortHoppingInterval.value.trim()
    }

    private fun saveStreamSettings(nodeItem: NodeItem) {
        nodeItem.transport = transport.value.trim()
        nodeItem.headerType = headerType.value.trim()
        nodeItem.host = host.value.trim()
        nodeItem.path = path.value.trim()
        nodeItem.serviceName = serviceName.value.trim()
        nodeItem.xhttpMode = xhttpMode.value.trim()
    }

    private fun saveTls(nodeItem: NodeItem) {

        nodeItem.security = security.value.trim()
        nodeItem.sni = sni.value.trim()
        nodeItem.alpn = alpn.value.trim()
        nodeItem.insecure = insecure.value.trim().toBoolean()
        nodeItem.publicKey = publicKey.value.trim()
        nodeItem.shortId = shortId.value.trim()
    }


    /**
     * delete server config
     */
    fun deleteNode(): Boolean {
        editUuid?.let {
            if (it != DatabaseHandler.getSelectNodeUUID()) {
                DatabaseHandler.removeNode(it)
            } else {
                application.toast(R.string.toast_action_not_allowed)
                return false
            }
        }
        return true
    }
}

interface Item

class DividerItem(
) : Item

class TitleItem(
    var title: String
) : Item

abstract class Configurable(
    val titleResId: Int
) : Item {
    var isError by mutableStateOf(false)
        private set

    internal fun updateError(application: Application) {
        this.isError = true
        val title = application.getString(titleResId).lowercase()
        application.toast("The $title field is invalid.")
    }

    internal fun clearError() {
        this.isError = false
    }

    fun updateValue(newValue: String?) {
        newValue?.let {
            updateValue(it)
        }
    }

    internal abstract fun updateValue(newValue: String)
}


class IntegerField(
    titleResId: Int,
    initialValue: String = ""
) : Configurable(titleResId) {

    var value by mutableStateOf(initialValue)
        private set

    override fun updateValue(newValue: String) {
        if (newValue.isEmpty()) {
            value = newValue
            clearError()
        } else {
            val intValue = newValue.toIntOrNull()
            if (intValue != null) {
                value = "$intValue"
                clearError()
            }
        }
    }

    fun getValue(): Int? {
        return value.toIntOrNull()
    }
}

class TextField(
    titleResId: Int,
    initialValue: String = "",
) : Configurable(titleResId) {
    var value by mutableStateOf(initialValue)
        private set

    override fun updateValue(newValue: String) {
        value = newValue
        clearError()
    }
}

open class DropField(
    titleResId: Int,
    val itemsResId: Int,
    initialValue: String = "",
    val isCapitalize: Boolean = false
) : Configurable(titleResId) {
    var value by mutableStateOf(initialValue)
        private set

    override fun updateValue(newValue: String) {
        value = newValue
        clearError()
    }
}

class TransportDropField(
    titleResId: Int,
    itemsResId: Int,
    initialValue: String = ""
) : DropField(titleResId, itemsResId, initialValue) {

    internal fun isTcp(): Boolean {
        return (value.equals("tcp", true))
    }

    internal fun isWebsocket(): Boolean {
        return (value.equals("websocket", true))
    }

    internal fun isHttpupgrade(): Boolean {
        return (value.equals("httpupgrade", true))
    }

    internal fun isXHttp(): Boolean {
        return (value.equals("xhttp", true))
    }

    internal fun isHttp2(): Boolean {
        return (value.equals("http/2", true))
    }

    internal fun isGrpc(): Boolean {
        return (value.equals("grpc", true))
    }
}

class TlsDropField(
    titleResId: Int,
    itemsResId: Int,
    initialValue: String = ""
) : DropField(titleResId, itemsResId, initialValue) {

    internal fun isTls(): Boolean {
        return (value.equals("tls", true))
    }

    internal fun isReality(): Boolean {
        return (value.equals("reality", true))
    }
}


private val LocalNodeViewModel = staticCompositionLocalOf<NodeViewModel> {
    error("NodeViewModel not provided")
}

object NodeViewModelAccessor {
    /**
     * Retrieves the current [NodeViewModel] at the call site's position in the hierarchy.
     */
    val nodeViewModel: NodeViewModel
        @Composable @ReadOnlyComposable get() = LocalNodeViewModel.current

}

@Composable
fun NodeViewModel(
    nodeViewModel: NodeViewModel,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalNodeViewModel.provides(nodeViewModel)) {
        content()
    }
}