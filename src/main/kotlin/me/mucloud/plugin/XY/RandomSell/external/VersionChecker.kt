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
import java.nio.charset.StandardCharsets

object VersionChecker{

    private lateinit var main: Main

    val remoteSources: Array<String> = arrayOf(
        "https://raw.githubusercontent.com/MuCloudOfficial/XY-RandomSell_KotlinEdition/master/src/main/resources/plugin.yml",
        "https://gitee.com/MuCloudOfficial/XY-RandomSell_KotlinEdition/raw/master/src/main/resources/plugin.yml")

    lateinit var version: String
    lateinit var versionCN: String
    var versionInternal: Double = 0.0

    lateinit var remoteVersion: String
    var remoteVersionInternal: Double = 0.0
    lateinit var remoteVersionCN: String

    fun init(main: Main){
        this.main = main
        fetchCurrentVer()
        fetchNewVer(main.server.consoleSender)
    }

    private fun fetchCurrentVer(){
        val reader = YamlConfiguration().also {
            it.load(BufferedReader(InputStreamReader(main.getResource("plugin.yml")!!, StandardCharsets.UTF_8)))
        }
        version = reader.getString("version")!!
        versionCN = reader.getString("versionCN")!!
        versionInternal = reader.getDouble("versionInternal")
    }

    fun fetchNewVer(caller: CommandSender){
        object : BukkitRunnable(){
            override fun run() {
                var remoteReader: BufferedReader? = null
                var success = false
                var hasNewerVer = false
                for(i in remoteSources){
                    try {
                        val url = URL(i).openConnection()
                        url.connectTimeout = 2000
                        remoteReader = BufferedReader(InputStreamReader(url.getInputStream(), StandardCharsets.UTF_8))
                    }catch (e: IOException){
                        continue
                    }
                    success = true
                }

                if(success){
                    val reader: FileConfiguration = YamlConfiguration()
                    reader.load(remoteReader!!)

                    remoteVersion = reader.getString("version")!!
                    remoteVersionCN = reader.getString("versionCN")!!
                    remoteVersionInternal = reader.getDouble("versionInternal")

                    if(remoteVersionInternal > versionInternal){
                        hasNewerVer = true
                    }

                }else{
                    MessageSender.sendMessage(MessageLevel.NOTICE, caller, "&6&l未获取到新版本信息, 获取新版本信息请移步至本插件的项目页")
                    return
                }

                val msg = if(hasNewerVer){
                    """
                    &7&l| &e&l找到新版本
                    &7&l| ${if(!version.equals(remoteVersion, true)){ "$version($versionCN) >>> &e&l$remoteVersion($remoteVersionCN) &7&l| $versionInternal >>> &e&l$remoteVersionInternal" } else "$versionInternal >>> &e&l$remoteVersionInternal"}
                    &7&l| 
                    &7&l| &a&l新版本下载地址:
                    &7&l| &a&lhttps://gitee.com/MuCloudOfficial/XY-RandomSell_KotlinEdition/releases/tag/${remoteVersion}_${remoteVersionInternal}
                    &7&l| &a&lhttps://github.com/MuCloudOfficial/XY-RandomSell_KotlinEdition/releases/tag/${remoteVersion}_${remoteVersionInternal}
                    """.trimIndent()
                }else{
                    "&7&l| &a&l当前已最新版本"
                }

                when(caller){
                    is Player -> MessageSender.sendMessage(MessageLevel.NONE, caller, msg)
                    is ConsoleCommandSender -> MessageSender.sendMessageToConsole(MessageLevel.NONE, msg)
                }
            }
        }.runTaskAsynchronously(main)
    }

}