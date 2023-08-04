package me.mucloud.plugin.XY.RandomSell.external

import me.mucloud.plugin.XY.RandomSell.Main
import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender

import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

object VersionChecker{

    val remoteSources: Array<String> = arrayOf(
        "https://raw.githubusercontent.com/MuCloudOfficial/XY-RandomSell/master/src/main/resources/plugin.yml",
        "https://gitee.com/MuCloudOfficial/XY-RandomSell/raw/master/src/main/resources/plugin.yml")

    lateinit var version: String
    lateinit var versionCN: String
    var versionInternal: Int = -1

    lateinit var remoteVersion: String
    var remoteVersionInternal: Int = -1
    lateinit var remoteVersionCN: String

    fun init(main: Main){
        fetchCurrentVer(main)
        fetchNewVer(main, main.server.consoleSender)
    }

    private fun fetchCurrentVer(main: Main){
        val reader = YamlConfiguration().also {
            it.load(BufferedReader(InputStreamReader(main.getResource("plugin.yml")!!)))
        }
        version = reader.getString("version")!!
        versionCN = reader.getString("versionCN")!!
        versionInternal = reader.getInt("versionInternal")
    }

    private fun fetchNewVer(main: Main, caller: CommandSender){
        object : BukkitRunnable(){
            override fun run() {
                var remoteReader: BufferedReader? = null
                var success: Boolean = false
                for(i in remoteSources){
                    try {
                        remoteReader = BufferedReader(InputStreamReader(URL(i).openStream()))
                    }catch (e: IOException){
                        success = false
                        continue
                    }
                    success = true
                }

                if(success){
                    val reader: FileConfiguration = YamlConfiguration()
                    reader.load(remoteReader!!)

                    remoteVersion = reader.getString("version")!!
                    remoteVersionCN = reader.getString("versionCN")!!
                    remoteVersionInternal = reader.getInt("versionInternal")
                }

                val msg = """
                    &7&l| 找到新版本
                    &7&l| 
                    &7&l| 
                """.trimIndent()

                when(caller){
                    is Player -> MessageSender.sendMessage(MessageLevel.NONE, caller, msg)
                    is ConsoleCommandSender -> MessageSender.sendMessageToConsole(MessageLevel.NONE, msg)
                }
            }
        }.runTaskAsynchronously(main)
    }

}