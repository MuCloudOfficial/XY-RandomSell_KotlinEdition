package me.mucloud.plugin.XY.RandomSell.internal.event

import me.mucloud.plugin.XY.RandomSell.internal.Shop.Repo
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class RepoContentChangedEvent(
    val player: Player,
    val repo: Repo,
): Event(){

    private val handlerList = HandlerList()

    override fun getHandlers(): HandlerList {
        return handlerList
    }

}