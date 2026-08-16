package com.xray.core.rust.client.xcra.dto.core

import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.dto.RuleItem
import com.xray.core.rust.client.xcra.dto.core.inbound.Inbound
import com.xray.core.rust.client.xcra.dto.core.log.Log
import com.xray.core.rust.client.xcra.dto.core.outbound.Outbound
import com.xray.core.rust.client.xcra.dto.core.router.Router
import com.xray.core.rust.client.xcra.handler.DatabaseHandler
import com.xray.core.rust.client.xcra.util.JsonUtil

data class Config(
    val log: Log = Log(),
    val inbounds: MutableList<Inbound>,
    val outbounds: MutableList<Outbound>,
    val router: Router?,
) {


    companion object {

        fun createTemplate(
        ): Config {
            val inbounds = mutableListOf<Inbound>()
            val outbounds = mutableListOf<Outbound>()

            val config = Config(
                inbounds = inbounds,
                outbounds = outbounds,
                router = Router(rules = mutableListOf())
            )
            return config
        }
    }


    /**
     * Configures more outbound settings for xray.
     *
     * Adds direct and block outbound.
     */
    fun addMoreOutbounds() {
        outbounds.add(
            Outbound(
                "direct",
                protocol = "freedom",
                settings = Outbound.FreedomOutboundSetting(),
                streamSettings = null
            )
        )
        outbounds.add(
            Outbound(
                "block",
                protocol = "block",
                settings = Outbound.BlockOutboundSetting(),
                streamSettings = null
            )
        )
    }


    /**
     * Configures routing settings for xray.
     *
     * Adds routing rules from saved rules.
     *
     * @return true if routing configuration was successful, false otherwise
     */
    fun addRouting(): Boolean {
        try {
            val rulesetItems = DatabaseHandler.decodeRoutingRules()
            rulesetItems?.forEach { rule ->
                addRule(rule)
            }
        } catch (e: Exception) {
            App.log("Failed to configure routing $e")
            return false
        }
        return true
    }

    /**
     * Adds a specific rule item to the router configuration.
     *
     * @param item The rule item to add
     */
    private fun addRule(item: RuleItem?) {
        try {
            if (item == null || !item.enabled) {
                return
            }
            val rule = JsonUtil.fromJson(JsonUtil.toJson(item), Router.Rule::class.java)
            router?.rules?.add(rule)
        } catch (e: Exception) {
            App.log("Failed to apply router rule $e")
        }
    }
}