package me.mucloud.plugin.XY.RandomSell

import me.mucloud.plugin.XY.RandomSell.external.VersionChecker
import me.mucloud.plugin.XY.RandomSell.external.hook.PAPIHooker
import me.mucloud.plugin.XY.RandomSell.external.hook.VaultHooker
import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender
import me.mucloud.plugin.XY.RandomSell.internal.Shop.*
import me.mucloud.plugin.XY.RandomSell.internal.command.CommandManager
import me.mucloud.plugin.XY.RandomSell.internal.configuration.ConfigurationReader

import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

class Main(
    private var NAME: String = "§6§lXY§7§l-§e§lRandomSell",
    var ID: String = "xyrs"
) : JavaPlugin(){

    override fun onEnable() {
        MessageSender.init(this)

        MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "§a§l $NAME 正在启动")

        ConfigurationReader.init(this)
        CommandManager.init(this) // Load Command -> "/xyrs"
        VaultHooker.checkHook(this)
        PAPIHooker.checkHook() // 检查 PlaceholderAPI 支持

        Bukkit.getPluginManager().registerEvents(RepoGUIListener, this)
        Bukkit.getPluginManager().registerEvents(RepoPoolListener, this)
        Bukkit.getPluginManager().registerEvents(SellGUIListener, this)

        RepoPool.launch(server.consoleSender, ConfigurationReader)
        RepoPool.regTimerTask(this)

        VersionChecker.init(this)
    }

    override fun onDisable() {

        MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "§a§l $NAME 正在关闭")
        CommandManager.close(this)
        RepoPool.close()
        ProductPool.close()

        HandlerList.unregisterAll(this)
    }

}