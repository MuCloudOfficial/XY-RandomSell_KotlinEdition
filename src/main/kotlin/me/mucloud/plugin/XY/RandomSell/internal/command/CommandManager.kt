package me.mucloud.plugin.XY.RandomSell.internal.command

import me.mucloud.plugin.XY.RandomSell.Main
import me.mucloud.plugin.XY.RandomSell.external.VersionChecker
import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object CommandManager : CommandExecutor{

    fun init(main: Main){
        main.getCommand(main.ID)!!.setExecutor(this)
    }

    fun sendInfo(sender: CommandSender){
        MessageSender.sendMessage(MessageLevel.NONE, sender, """
            &7&l| &6&lXY&7&l-&b&lRandomSell
            &7&l| &6&lVersion &b&l${VersionChecker.version} &7&l| &6&l版本 &b&l${VersionChecker.versionCN}
            &7&l| &6&l作者: &7&lMu_Cloud
            &7&l|==============================================
            &7&l| &a/xyrs info       &7>>> &6显示该页面
            &7&l| &a/xyrs gui        &7>>> &6显示商店总页面
            &7&l| &a/xyrs refreshAll &7>>> &6立刻刷新所有随机商店
            &7&l|======== &e&lMade in Starry Sky &7&l| &e&l星空制造 &7&l========
        """.trimIndent())
    }

    override fun onCommand(sender: CommandSender, cmd: Command, s: String, ss: Array<out String>?): Boolean {
        return false
    }

}