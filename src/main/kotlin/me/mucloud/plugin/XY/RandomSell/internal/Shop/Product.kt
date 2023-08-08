package me.mucloud.plugin.XY.RandomSell.internal.Shop

import me.mucloud.plugin.XY.RandomSell.external.hook.VaultHooker
import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import java.util.Date
import kotlin.random.Random

object ProductPool{

    private val POOL: MutableList<Product> = emptyList<Product>().toMutableList()

    fun toProductList(): ArrayList<Product> {
        val out: ArrayList<Product> = ArrayList(RepoPool.Capacity)
        val r = Random(Date().time)
        var lo = 0

        while(lo < RepoPool.Capacity){
            val rd = POOL[r.nextInt(0, POOL.size)]
            if(out.contains(rd)){
                continue
            }
            out.add(rd)
            lo++
        }

        return out
    }

    fun reg(product: Product){
        POOL.forEach{
            if(it.ICON == product.ICON) return
        }
        POOL.add(product)

    }

    fun close(){
        POOL.clear()
    }

}

class Product(

    // 基本
    val ICON: Material,
    private val DisplayName: String,
    private val DisplayLore: List<String>,
    val Price: Double,

    // 控制限量
    // private var Infinity: Boolean 无限收购等下一版本
    val Limit: Int,

){

    private var Remain: Int = Limit

    fun prePayForPlayer(target: Player, take: Int /*声明的收购数量*/): SellResponse{
        if(take == 0){
            return SellResponse.ZERO
        }
        if(take > Remain){
            return SellResponse.OVERFLOW
        }
        if(!target.inventory.contains(ICON, take)){
            return SellResponse.NOT_ENOUGH
        }
        return SellResponse.DONE
    }

    fun payForPlayer(target: Player, take: Int){
        VaultHooker.ECON.depositPlayer(target, take *Price)
        MessageSender.sendMessage(MessageLevel.NOTICE, target, "&a&l收购完成")
    }

    fun toICON(): ItemStack{
        val out = ItemStack(ICON)
        val meta = out.itemMeta
        meta?.setDisplayName(DisplayName)
        meta?.lore = DisplayLore
        out.itemMeta = meta
        return out
    }

}