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
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.dto.RuleItem
import com.xray.core.rust.client.xcra.handler.RouterHandler


class RuleViewModel(
    application: Application,
    val index: Int
) :
    AndroidViewModel(application = application) {

    var ruleItem: RuleItem

    var remarks by mutableStateOf("")
        private set

    var remarksError by mutableStateOf(false)

    var domain by mutableStateOf("")
        private set
    var domainError by mutableStateOf(false)


    var ip by mutableStateOf("")
        private set

    var ipError by mutableStateOf(false)

    var port by mutableStateOf("")
        private set

    var portError by mutableStateOf(false)
    var protocol by mutableStateOf("")
        private set
    var network by mutableStateOf("")
        private set

    var outbound by mutableStateOf("")
        private set

    var locked by mutableStateOf(false)
        private set

    init {
        ruleItem = RuleItem()
        if (index >= 0) {
            val rule = RouterHandler.getRule(index)
            if (rule != null) {
                ruleItem = rule
            }
        }
        remarks = ruleItem.remarks.orEmpty()
        locked = ruleItem.locked == true
        domain = ruleItem.domain.orEmpty().joinToString(",")
        ip = ruleItem.ip.orEmpty().joinToString(",")
        port = ruleItem.port.orEmpty()
        protocol = ruleItem.protocol.orEmpty().joinToString(" | ")
        network = ruleItem.network.orEmpty().joinToString(" | ")
        outbound = ruleItem.outboundTag

        var outbounds = application.resources.getStringArray(R.array.rule_lab_outbounds)
        if (outbound.isEmpty() || !outbounds.contains(outbound)) {
            outbound = outbounds[0]
        }
    }


    fun updateRemarks(remarks: String) {
        this.remarks = remarks
        remarksError = false
    }

    fun updateDomain(domain: String) {
        this.domain = domain
        domainError = false
    }

    fun updateIp(ip: String) {
        this.ip = ip
        ipError = false
    }

    fun updatePort(port: String) {
        this.port = port
        portError = false
    }

    fun updateProtocol(protocol: String) {
        this.protocol = protocol
    }

    fun updateNetwork(network: String) {
        this.network = network
    }

    fun updateOutbound(outbound: String) {
        this.outbound = outbound
    }

    fun updateLocked(locked: Boolean) {
        this.locked = locked
    }


    fun saveRule(): Boolean {
        val domains = domain.takeIf { it.isNotEmpty() }
            ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
        val ips = ip.takeIf { it.isNotEmpty() }
            ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
        val ports = port.takeIf { it.isNotEmpty() }
        val protocols = protocol.takeIf { it.isNotEmpty() }
            ?.split(" | ")?.map { it.trim() }?.filter { it.isNotEmpty() }
        val networks = network.takeIf { it.isNotEmpty() }
            ?.split(" | ")?.map { it.trim() }?.filter { it.isNotEmpty() }



        ruleItem.apply {
            remarks = (this@RuleViewModel).remarks
            locked = (this@RuleViewModel).locked
            domain = domains
            ip = ips
            port = ports
            protocol = protocols
            network = networks
            outboundTag = outbound
        }

        if (ruleItem.remarks.isNullOrEmpty()) {
            remarksError = true
            return false
        }

        RouterHandler.saveRule(index, ruleItem)

        return true
    }

    fun deleteRule(): Boolean {
        if (index >= 0) {
            RouterHandler.removeRule(index)
            return true
        }
        return false
    }
}

private val LocalRuleViewModel = staticCompositionLocalOf<RuleViewModel> {
    error("RuleViewModel not provided")
}

object RuleViewModelAccessor {
    /**
     * Retrieves the current [AssetViewModel] at the call site's position in the hierarchy.
     */
    val ruleViewModel: RuleViewModel
        @Composable @ReadOnlyComposable get() = LocalRuleViewModel.current

}

@Composable
fun RuleViewModel(
    ruleViewModel: RuleViewModel,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalRuleViewModel.provides(ruleViewModel)) {
        content()
    }
}