package me.mucloud.plugin.XY.RandomSell.internal.Shop

import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender
import me.mucloud.plugin.XY.RandomSell.internal.Shop.GUIRESOURCES.CANCEL
import me.mucloud.plugin.XY.RandomSell.internal.Shop.GUIRESOURCES.M1
import me.mucloud.plugin.XY.RandomSell.internal.Shop.GUIRESOURCES.M5
import me.mucloud.plugin.XY.RandomSell.internal.Shop.GUIRESOURCES.MX
import me.mucloud.plugin.XY.RandomSell.internal.Shop.GUIRESOURCES.OK
import me.mucloud.plugin.XY.RandomSell.internal.Shop.GUIRESOURCES.P1
import me.mucloud.plugin.XY.RandomSell.internal.Shop.GUIRESOURCES.P5
import me.mucloud.plugin.XY.RandomSell.internal.Shop.GUIRESOURCES.PX
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

//object SellerPool{
//
//    private var POOL: MutableMap<Player, Seller> = emptyMap<Player, Seller>().toMutableMap()
//
//    fun reg(player: Player, seller: Seller){
//        if(POOL.containsKey(player)){
//            MessageSender.sendMessage(MessageLevel.NOTICE, player, "&4&l无法加载收购器，你可能还有未完成的收购")
//            return
//        }
//        POOL[player] = seller
//        seller.toView()
//    }
//
//    fun unreg(target: Player){
//        if(POOL.containsKey(target)){
//            POOL[target]!!.closeView()
//            POOL.remove(target)
//        }
//    }
//
//    fun getSeller(target: Player): Seller?{
//        return if(POOL.containsKey(target)){
//            POOL[target]
//        }else null
//    }
//
//}

class Seller(target: Repo, product: Product){

    val TargetRepo: Repo = target
    val TargetProduct: Product = product
    var CallAmount: Int = 0
    private val GUI: SellGUI = SellGUI(this, CallAmount)

    fun plus(amount: Int){
        if(CallAmount + amount >= TargetProduct.Limit){
            CallAmount = TargetProduct.Limit
        }else{
            CallAmount += amount
        }
    }

    fun minus(amount: Int){
        if(CallAmount - amount < 0){
            CallAmount = 0
        }else{
            CallAmount -= amount
        }
    }

    fun getCurrentSellResponse(): SellResponse{
        return TargetProduct.prePayForPlayer(TargetRepo.getOwner(), CallAmount)
    }

    fun sell(){
        TargetProduct.payForPlayer(TargetRepo.getOwner(), CallAmount)
    }


    fun toView(){
        GUI.preView()
        TargetRepo.getOwner().openInventory(GUI.INV)
    }

    fun close(){
        GUI.INV.viewers.forEach { it.closeInventory() }
    }

}

/**
 *
 *  SellGUI
 *      结构:
 *          ####I####       I >>> 代表即将要收购的项目
 *          #X51#15X#       1 5 X >>> 代表 1个 5个 10个 左半为减 右半为加
 *          ##C###A##       C A >>> 收购自定义数量 收购全部
 *
 *
 */
class SellGUI(
    val Seller: Seller,
    val CallAmount: Int
){

    val INV: Inventory = GUIRESOURCES.SELLGUIFRAME

    fun preView() {
        INV.setItem(4, Seller.TargetProduct.toICON())
        INV.setItem(51, OK(Seller.getCurrentSellResponse(), CallAmount, Seller.TargetProduct.Price))
    }

}

object SellGUIListener: Listener{

    @EventHandler fun onListen(ice: InventoryClickEvent){
        val p = ice.whoClicked as Player
        val repo = RepoPool.getRepo(p)
        if(repo != null){
            ice.isCancelled = true

            when(ice.cursor){
                CANCEL -> {

                    RepoPool.open(p, p)
                }

                OK -> when(seller.getCurrentSellResponse()){
                    SellResponse.DONE -> {
                        seller.sell()
                        SellerPool.unreg(p)
                    }
                    else -> {}
                }

                P1 -> seller.plus(1)
                P5 -> seller.plus(5)
                PX -> seller.plus(10)
                M1 -> seller.minus(1)
                M5 -> seller.minus(5)
                MX -> seller.minus(10)

                else -> {}
            }
        }
    }

}

enum class SellResponse {
    NOT_ENOUGH, // 玩家物品不足以收购
    OVERFLOW, // 收购数量超过了当前限制余量
    ZERO, // 0 收购状态
    DONE, // 正常完成
}
