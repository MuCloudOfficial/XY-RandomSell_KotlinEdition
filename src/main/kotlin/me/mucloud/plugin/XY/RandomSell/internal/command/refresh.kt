package me.mucloud.plugin.XY.RandomSell.internal.command

import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender
import me.mucloud.plugin.XY.RandomSell.internal.Shop.RepoPool
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

object refresh {

    fun run(sender: CommandSender, ss: Array<String>){
        if(sender is ConsoleCommandSender || sender.hasPermission("xyrsp.refresh")){
            if(ss.size == 1){
                val player = Bukkit.getPlayer(ss[0])
                if(player == null){
                    MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4&l未找到玩家")
                }else{
                    RepoPool.refresh(sender, player)
                    MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&a&l已更新了 ${player.displayName} 的随机收购商店")
                }
            }
        }else{
            MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4&l你没有权限执行该命令")
        }
    }

}