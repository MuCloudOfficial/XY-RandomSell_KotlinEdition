package me.mucloud.plugin.XY.RandomSell

import me.mucloud.plugin.XY.RandomSell.external.hook.PAPIHooker
import me.mucloud.plugin.XY.RandomSell.external.hook.VaultHooker
import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender
import me.mucloud.plugin.XY.RandomSell.internal.Shop.RepoPool
import me.mucloud.plugin.XY.RandomSell.internal.command.CommandManager
import me.mucloud.plugin.XY.RandomSell.internal.configuration.ConfigurationReader
import org.bukkit.plugin.java.JavaPlugin

class Main(
    var NAME: String = "§6§lXY§7§l-§e§lRandomSell",
    var ID: String = "xyrs"
) : JavaPlugin(){

    override fun onEnable() {
        MessageSender.init(this)

        MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "§a§l $NAME 正在启动")

        ConfigurationReader.init(this)
        CommandManager.init(this) // Load Command -> "/xyrs"
        VaultHooker.checkHook(this)
        PAPIHooker.checkHook() // 检查 PlaceholderAPI 支持

        RepoPool.launch(server.consoleSender, ConfigurationReader)
    }

    override fun onDisable() {
        MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "§a§l $NAME 正在关闭")
    }

}