package me.mucloud.plugin.XY.RandomSell.internal.command

import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender
import me.mucloud.plugin.XY.RandomSell.internal.Shop.RepoPool

import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

object refreshAll {

    fun run(sender: CommandSender){
        if(sender is ConsoleCommandSender || sender.hasPermission("xyrsp.refresh")){
            RepoPool.refreshAll()
            MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&a&l当前随机收购商店池已全部更新")
        }else{
            MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4&l你没有权限执行该命令")
        }
    }

}