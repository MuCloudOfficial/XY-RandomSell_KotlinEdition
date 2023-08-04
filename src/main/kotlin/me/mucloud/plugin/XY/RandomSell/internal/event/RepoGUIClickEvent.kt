package me.mucloud.plugin.XY.RandomSell.internal.event

import me.mucloud.plugin.XY.RandomSell.internal.Shop.Product
import me.mucloud.plugin.XY.RandomSell.internal.Shop.Repo
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class RepoGUIClickEvent(
    private var TargetRepo: Repo,
    private var TargetProduct: Product
): Event() {

    private val handlers: HandlerList = HandlerList()

    override fun getHandlers(): HandlerList {
        return handlers
    }

    fun toView(){

    }

}