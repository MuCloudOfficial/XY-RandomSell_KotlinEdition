package me.mucloud.plugin.XY.RandomSell.internal.configuration

import me.mucloud.plugin.XY.RandomSell.Main
import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender
import me.mucloud.plugin.XY.RandomSell.internal.Shop.Product
import me.mucloud.plugin.XY.RandomSell.internal.Shop.ProductPool
import me.mucloud.plugin.XY.RandomSell.internal.Shop.RepoPool

import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration

import java.io.File
import java.util.*

object ConfigurationReader{

    private lateinit var main: Main

    private lateinit var configFile: File
    private lateinit var configFileReader: YamlConfiguration
    private lateinit var configVersion: String

    private lateinit var PREFIX: String

    private var GUIRow: Int = 3
    private lateinit var GUITitle: String
    private var Refresh: Int = 0
    private var Capacity: Int = 0

    // 设置文件兼容序列
    private var compatibleConfigVersions: Array<String> = arrayOf(
        "StarrySky_C1.0",
    )

    fun init(plugin: Main) {

        main = plugin

        configFile = File(main.dataFolder, "config.yml")

        if(!main.dataFolder.exists()){
            main.dataFolder.mkdir()
        }
        if(!configFile.exists()){
            main.saveDefaultConfig()
        }

        validateConfig()
    }

    private fun validateConfig(){

        var validated = true
        val checkSum: Array<String> = arrayOf(
            "GUIRow",
            "GUITitle",
            "Prefix",
            "Refresh",
            "Capacity",
            "Products",
        )

        configFileReader = YamlConfiguration.loadConfiguration(configFile)
        if(!configFileReader.isSet("version")){
            main.saveDefaultConfig()
        }
        configVersion = configFileReader.getString("version")!!

        if(configVersion !in compatibleConfigVersions){
            MessageSender.LOG_WARN("设置文件已不在支持范围内，原设置文件已备份")
            configFile.renameTo(File(configFile.parentFile, "${configFile.name}_${Date().time}_old"))
            main.saveDefaultConfig()
        }

        configVersion = configFileReader.getString("version")!!

        MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "§a§l已检测到设置文件，版本 §b§l$configVersion")

        checkSum.forEach {
            if(configFileReader.isSet(it)){
                if(it == "GUIMode" && configFileReader.getInt(it) !in 1..2){
                    MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "&6设置文件中 $it 设定值非法, 正在生成新设置文件")
                    validated = false
                }else if(it == "GUIRow" && configFileReader.getInt(it) !in 3..6){
                    MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "&6设置文件中 $it 设定值非法, 正在生成新设置文件")
                    validated = false
                }else if(it == "Refresh" && configFileReader.getInt(it) <= 0){
                    MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "&6设置文件中 $it 设定值非法, 商店将无法启动")
                    RepoPool.setRefreshStatus(false)
                }else if(it == "Capacity" && configFileReader.getInt(it) !in 1 .. 54){
                    MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "&6设置文件中 $it 设定值非法, 商店将无法启动")
                    RepoPool.setCapacityStatus(false)
                }else if(it == "Products"){
                    if(configFileReader.getList(it)!!.isEmpty()){
                        MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "&6设置文件中无商品, 商店将无法启动")
                        RepoPool.setProductStatus(false)
                    }else{
                        validateProducts()
                    }
                }
            }else{
                validated = false
            }
        }

        if(!validated){
            MessageSender.LOG_ERR("当前设置文件不合法，该设置文件已备份")
            configFile.renameTo(File(configFile.parentFile, "${configFile.name}_${Date().time}.yml_old"))
            main.saveDefaultConfig()
        }

        fetchConfig()

    }

    private fun validateProducts(){
        val checkSum = arrayOf(
            "material",
            "name",
            "lore",
            "price",
            "amount",
        )

        var sum: Int = 0

        loop@for(p in configFileReader.getList("Products")!!){
            val map: Map<String, *> = p as Map<String, *>
            for(c in checkSum){
                if(!map.contains(c)){
                    continue@loop
                }
            }
            sum++
        }

        MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "&a从当前配置文件中检测到 $sum 个有效商品")
        fetchConfig()
    }

    private fun fetchConfig(){

        GUIRow = configFileReader.getInt("GUIRow")
        GUITitle = MessageSender.transColor(configFileReader.getString("GUITitle")!!)

        PREFIX = MessageSender.transColor(configFileReader.getString("Prefix")!!)
        Refresh = configFileReader.getInt("Refresh")
        Capacity = configFileReader.getInt("Capacity")

        for(p in configFileReader.getList("Products")!!){
            val map: Map<String, *> = p as Map<String, *>
            ProductPool.reg(
                Product(
                    if (Material.matchMaterial(map["material"] as String) != null) Material.matchMaterial(map["material"] as String)!! else continue,
                    map["name"] as String,
                    map["lore"] as List<String>,
                    map["price"] as Double,
                    map["amount"] as Int
                )
            )
        }

    }

    fun getGUIRow(): Int{
        return GUIRow
    }

    fun getGUITitle(): String{
        return GUITitle
    }

    fun getPrefix(): String{
        return PREFIX
    }

    fun getRefresh(): Int{
        return Refresh
    }

    fun getCapacity(): Int{
        return Capacity
    }

}
