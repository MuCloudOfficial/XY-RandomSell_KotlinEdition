package me.mucloud.plugin.XY.RandomSell.external.hook

import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender
import org.bukkit.Bukkit

object PAPIHooker {

    var isHook: Boolean = false

    fun checkHook(){
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null){
            MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "&6&l未检测到 PlaceholderAPI, 全局变量将不可用")
        }else{
            MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "&6&l已检测到 PlaceholderAPI, 全局变量可以使用")
            isHook = true
        }
    }

}