package me.mucloud.plugin.XY.RandomSell.internal.command

import me.mucloud.plugin.XY.RandomSell.external.VersionChecker
import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender

import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

object version {

    fun run(sender: CommandSender){
        if(sender is ConsoleCommandSender || sender.hasPermission("xyrsp.version")){
            MessageSender.sendMessage(MessageLevel.NONE, sender, """
                &7&l| 当前版本: ${VersionChecker.version}&7&l(&b&l${VersionChecker.versionCN}&7&l) 内部版本号: &6&l${VersionChecker.versionInternal}
                &7&l| &a&l正在检查版本
            """.trimIndent())
            VersionChecker.fetchNewVer(sender)
        }else{
            MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4&l你没有权限执行该命令")
        }
    }

}