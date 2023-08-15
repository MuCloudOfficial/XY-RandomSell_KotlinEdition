package me.mucloud.plugin.XY.RandomSell.internal.command

import me.mucloud.plugin.XY.RandomSell.Main
import me.mucloud.plugin.XY.RandomSell.external.VersionChecker
import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender
import me.mucloud.plugin.XY.RandomSell.internal.Shop.ProductPool
import me.mucloud.plugin.XY.RandomSell.internal.Shop.RepoPool

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object CommandManager : CommandExecutor{

    fun init(main: Main){
        main.getCommand(main.ID)!!.setExecutor(this)
    }

    fun close(main: Main){
        main.getCommand(main.ID)!!.setExecutor(null)
    }

    private fun sendInfo(sender: CommandSender){
        MessageSender.sendMessage(MessageLevel.NONE, sender, """
            &7&l| &6&lXY&7&l-&b&lRandomSell
            &7&l| &6&lVersion &b&l${VersionChecker.version} &7&l| &6&l版本 &b&l${VersionChecker.versionCN}
            &7&l| &6&l作者: &7&lMu_Cloud
            &7&l|==============================================
            &7&l| &a&l当前存在 &e&l${ProductPool.getSize()} &a&l个商品项
            ${if(RepoPool.isOpen()){
                "&7&l| &a&l当前商店池正常运行&7&l\n            | &a&l当前已启动的&7&l/&6&l总收购商店数: &a&l${RepoPool.getOpenedSize()}&7&l/&6&l${RepoPool.getSize()}"
            }else{
                "&7&l| &6&l当前商店池未运行"
            }}
            &7&l|================================================
            &7&l| &a/xyrs info          &7>>> &6显示该页面
            &7&l| &a/xyrs gui (玩家名)   &7>>> &6显示收购商店页面
            &7&l| &a/xyrs refreshAll    &7>>> &6立刻刷新所有随机商店
            &7&l| &a/xyrs refresh [玩家名] &7>>> &6立即刷新某玩家的随机商店
            &7&l| &a/xyrs open [玩家名]  &7>>> &6开启指定玩家的收购商店
            &7&l| &a/xyrs close [玩家名]  &7>>> &6关闭指定玩家的收购商店
            &7&l| &a/xyrs version    &7>>> &6检查插件更新
            &7&l|======== &e&lMade in Starry Sky &7&l| &e&l星空制造 &7&l=========
        """.trimIndent())
    }

    override fun onCommand(sender: CommandSender, cmd: Command, s: String, ss: Array<out String>): Boolean {
        if(cmd.name.equals("xyrs", true)){
            if(ss.isEmpty()){
                sendInfo(sender)
            }else{
                when(ss[0].lowercase()){
                    "info" -> sendInfo(sender)
                    "gui" -> gui.run(sender, ss.copyOfRange(1, ss.size))
                    "refresh" -> refresh.run(sender, ss.copyOfRange(1, ss.size))
                    "refreshall" -> refreshAll.run(sender)
                    "open" -> open.run(sender, ss.copyOfRange(1, ss.size))
                    "close" -> close.run(sender, ss.copyOfRange(1, ss.size))
                    "version" -> version.run(sender)
                    else -> MessageSender.sendMessage(MessageLevel.NOTICE, sender, "&4&l子命令输入错误，请重新输入")
                }
            }
            return true
        }
        return false
    }

}