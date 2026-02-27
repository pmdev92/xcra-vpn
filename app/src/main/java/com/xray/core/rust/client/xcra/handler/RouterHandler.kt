package com.xray.core.rust.client.xcra.handler

import android.content.Context
import android.content.res.AssetManager
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.dto.RuleItem
import com.xray.core.rust.client.xcra.util.JsonUtil
import com.xray.core.rust.client.xcra.util.Utils
import java.io.File
import java.io.FileOutputStream

object RouterHandler {

    /**
     * Initialize assets.
     * @param context The application context.
     * @param assets The AssetManager.
     */
    fun initAssets(context: Context, assets: AssetManager) {
        val extFolder = Utils.userAssetPath(context)
        try {
            val geo = arrayOf("geosite.dat", "geoip.dat")
            assets.list("")
                ?.filter {
                    geo.contains(it)
                }
                ?.filter { !File(extFolder, it).exists() }
                ?.forEach {
                    val target = File(extFolder, it)
                    assets.open(it).use { input ->
                        FileOutputStream(target).use { output ->
                            input.copyTo(output)
                        }
                    }
                    App.log("Copied from apk assets folder to ${target.absolutePath}")
                }
        } catch (e: Exception) {
            App.log("asset copy failed $e")
        }
    }

    /**
     * Get all rules.
     * @return The RuleItem.
     */
    fun getRules(): MutableList<RuleItem> {
        return DatabaseHandler.decodeRoutingRules() ?: return mutableListOf()
    }

    /**
     * Get a rule by index.
     * @param index The index of the rule.
     * @return The RuleItem.
     */
    fun getRule(index: Int): RuleItem? {
        if (index < 0) return null
        val rulesetList = DatabaseHandler.decodeRoutingRules()
        if (rulesetList.isNullOrEmpty()) return null

        return rulesetList[index]
    }

    /**
     * Save a rules.
     * @param rules The RuleItems to save.
     */
    fun saveRules(rules: MutableList<RuleItem>) {
        DatabaseHandler.encodeRoutingRules(rules)
    }

    /**
     * Save a routing ruleset.
     * @param index The index of the ruleset.
     * @param rule The RuleItem to save.
     */
    fun saveRule(index: Int, rule: RuleItem?) {
        if (rule == null) return
        var rulesetList = DatabaseHandler.decodeRoutingRules()
        if (rulesetList.isNullOrEmpty()) {
            rulesetList = mutableListOf()
        }
        if (index < 0 || index >= rulesetList.count()) {
            rulesetList.add(0, rule)
        } else {
            rulesetList[index] = rule
        }
        DatabaseHandler.encodeRoutingRules(rulesetList)
    }

    /**
     * Remove a routing rule by index.
     * @param index The index of the ruleset.
     */
    fun removeRule(index: Int) {
        if (index < 0) return

        val rulesetList = DatabaseHandler.decodeRoutingRules()
        if (rulesetList.isNullOrEmpty()) return

        if (rulesetList.count() > index) {
            rulesetList.removeAt(index)
            DatabaseHandler.encodeRoutingRules(rulesetList)
        }
    }

    /**
     * Reset rules.
     * @param content The content of the rules.
     * @return True if successful, false otherwise.
     */
    fun resetRules(content: String?): Boolean {
        if (content.isNullOrEmpty()) {
            return false
        }
        try {
            val rulesetList =
                JsonUtil.fromJson(content, Array<RuleItem>::class.java).toMutableList()
            if (rulesetList.isEmpty()) {
                return false
            }

            resetRules(rulesetList)
            return true
        } catch (e: Exception) {
            App.log("aaaaaaa Failed to reset router rules $e")
            return false
        }
    }

    /**
     * Common method to reset routing rulesets.
     * @param rules The list of rules.
     */
    private fun resetRules(rules: MutableList<RuleItem>) {
        val rulesNew: MutableList<RuleItem> = mutableListOf()
        DatabaseHandler.decodeRoutingRules()?.forEach { key ->
            if (key.locked == true) {
                rulesNew.add(key)
            }
        }

        rulesNew.addAll(rules)

        rulesNew.forEach { item ->
            item.id = Utils.getUuid()
        }

        DatabaseHandler.encodeRoutingRules(rulesNew)
    }
}