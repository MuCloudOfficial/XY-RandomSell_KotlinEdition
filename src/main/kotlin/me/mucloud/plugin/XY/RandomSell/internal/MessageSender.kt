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
    private lateinit var Sendmode: SendMode
    private lateinit var CCS: ConsoleCommandSender
    private lateinit var LOGGER: Logger

    fun init(main: Main){
        plugin = main
        CCS = main.server.consoleSender
        LOGGER = main.logger

        Sendmode = SendMode.valueOf("")
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

enum class SendMode{
    MESSAGE,
    ACTIONBAR,
    BOSSBAR
}

enum class MessageLevel{
    NORMAL,
    NOTICE,
    NONE,
}