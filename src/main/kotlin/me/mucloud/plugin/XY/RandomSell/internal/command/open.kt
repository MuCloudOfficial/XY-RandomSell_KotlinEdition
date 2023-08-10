package me.mucloud.plugin.XY.RandomSell.internal.command

import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender
import me.mucloud.plugin.XY.RandomSell.internal.Shop.RepoPool

import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

object open {

    fun run(sender: CommandSender, ss: Array<String>){
        if(sender is ConsoleCommandSender || sender.hasPermission("xyrsp.open")){
            if(ss.size != 1){
                MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4&l你输入的参数数量错误")
            }else{
                RepoPool.setOpenRepo(sender, ss[0], true)
            }
        }else{
            MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4&l你没有权限执行该命令")
        }
    }

}