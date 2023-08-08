package me.mucloud.plugin.XY.RandomSell.internal.command

import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender
import me.mucloud.plugin.XY.RandomSell.internal.Shop.RepoPool

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object gui {

    fun run(sender: CommandSender, ss: Array<String>){
        if(sender is Player){
            if(!sender.hasPermission("xyrsp.gui")){
                MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4&l你没有权限执行该命令")
            }else{
                if(ss.size == 1){
                    RepoPool.open(sender, sender)
                }else if(ss.size == 2){
                    val target = Bukkit.getPlayer(ss[1])
                    if(target == null){
                        MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4&l未找到该玩家")
                    }else{
                        MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&6&l你正试图打开 &a&l${target.displayName} &6&l的收购商店")
                        RepoPool.open(sender, target)
                    }
                }else{
                    MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4&l你输入的命令参数过多")
                }
            }
        }else{
            MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4&l该命令仅玩家可用")
            return
        }
    }

}