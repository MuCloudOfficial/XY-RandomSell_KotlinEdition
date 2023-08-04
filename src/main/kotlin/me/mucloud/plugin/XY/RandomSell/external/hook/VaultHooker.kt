package me.mucloud.plugin.XY.RandomSell.external.hook

import me.mucloud.plugin.XY.RandomSell.Main
import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredServiceProvider

object VaultHooker {

    lateinit var ECON: Economy

    fun checkHook(main: Main){

        val vault: Plugin? = Bukkit.getPluginManager().getPlugin("Vault")

        if(vault == null){
            MessageSender.LOG_ERR("要使用 XY-RandomSell 则服务器必须安装 Vault, 插件将自动关闭")
            main.server.pluginManager.disablePlugin(main)
        }else{
            val rsp: RegisteredServiceProvider<Economy>? = main.server.servicesManager.getRegistration(Economy::class.java)
            if(rsp == null){
                MessageSender.LOG_ERR("已检测到 Vault, 但服务器没有经济核心(EssentialsX, CMI, etc.), XY-RandomSell 未能启动")
                main.server.pluginManager.disablePlugin(main)
            }else{
                ECON = rsp.provider
                MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "&a&l 已检测到 Vault §7§l| &a&l${vault.description.version}")
            }
        }
    }

}