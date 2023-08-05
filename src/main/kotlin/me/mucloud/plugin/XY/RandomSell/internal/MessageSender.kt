package me.mucloud.plugin.XY.RandomSell.internal

import me.mucloud.plugin.XY.RandomSell.Main
import me.mucloud.plugin.XY.RandomSell.external.hook.PAPIHooker

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

import java.util.logging.Logger

object MessageSender{

    private lateinit var plugin: Main
    private lateinit var PREFIX: String
    private lateinit var CCS: ConsoleCommandSender
    private lateinit var LOGGER: Logger

    //    private lateinit var Sendmode: SendMode 其他发送方式等下个版本

    fun init(main: Main){
        plugin = main
        CCS = main.server.consoleSender
        LOGGER = main.logger

        PREFIX = transColor("&7&l[&6&lXY&7&l-&e&lRandomSell&7&l] &b&l>>> ")
    }

    fun sendMessage(level: MessageLevel, target: CommandSender, msg: String){
        var pre: String = when(level){
            MessageLevel.NORMAL -> "&7&l| $msg"
            MessageLevel.NOTICE -> PREFIX + msg
            MessageLevel.NONE -> msg
        }
        pre = if(PAPIHooker.isHook && target is Player){
            PlaceholderAPI.setPlaceholders(target, pre)
        }else{
            pre
        }

        target.sendMessage(transColor(pre))
    }

    fun sendMessageToConsole(level: MessageLevel, msg: String){
        sendMessage(level, CCS, msg)
    }
    
    fun broadcastMessage(msg: String){
        Bukkit.getOnlinePlayers().forEach {
            sendMessage(MessageLevel.NOTICE, it, msg)
        }
    }

    fun LOG_INFO(msg: String){
        LOGGER.info(msg)
    }

    fun LOG_WARN(msg: String){
        LOGGER.warning(msg)
    }

    fun LOG_ERR(msg: String){
        LOGGER.severe(msg)
    }

    fun transColor(msg: String): String{
        return ChatColor.translateAlternateColorCodes('&', msg)
    }

}

// 等待更新
//enum class SendMode{
//    MESSAGE,
//    ACTIONBAR,
//    BOSSBAR
//}

enum class MessageLevel{
    NORMAL,
    NOTICE,
    NONE,
}