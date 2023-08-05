package me.mucloud.plugin.XY.RandomSell.internal.Shop

import me.clip.placeholderapi.PlaceholderAPI
import me.mucloud.plugin.XY.RandomSell.external.hook.PAPIHooker
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
import me.mucloud.plugin.XY.RandomSell.internal.configuration.ConfigurationReader
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView

object RepoPool{

    // 无商品 停止
    private var NonProductStatus: Boolean = false
    // 未设置刷新时间 停止
    private var NonRefreshStatus: Boolean = false
    // 未设置随机容量 停止
    private var NonCapacityStatus: Boolean = false

    private var IsOpen: Boolean = false
    var Refresh: Int = 0
    var Capacity: Int = 0

    private val POOL: MutableMap<Player, Repo> = emptyMap<Player, Repo>().toMutableMap()

    fun Launch(sender: CommandSender, config: ConfigurationReader){
        if(NonCapacityStatus || NonRefreshStatus || NonProductStatus){
            MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4随机商品池无法启动, 可能出现了以下原因")
            if(NonProductStatus){
                MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4当前没有商品")
            }
            if(NonCapacityStatus){
                MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4当前容量值为 0")
            }
            if(NonRefreshStatus){
                MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4当前未设定刷新时间")
            }
        }else{
            IsOpen = true
            Refresh = config.getRefresh()
            Capacity = config.getCapacity()
        }
    }

    fun reg(user: Player){
        POOL[user] = Repo(user, ProductPool.toProductList())
    }

    fun del(user: Player){
        if(POOL.containsKey(user)){
            POOL.remove(user)
        }
    }

    fun setRefreshStatus(b: Boolean){
        NonRefreshStatus = b
    }

    fun setProductStatus(b: Boolean){
        NonProductStatus = b
    }

    fun setCapacityStatus(b: Boolean){
        NonCapacityStatus = b
    }

    fun getSize(): Int{
        return POOL.size
    }

    fun isViewing(target: Player): Boolean{
        return if(POOL.containsKey(target)){
            POOL[target]!!.isView
        }else{
            false
        }
    }

    fun open(source: Player, call: Player){
        if(!POOL.containsKey(source)){
            MessageSender.sendMessage(MessageLevel.NOTICE, source, "&6&l当前商店池中未注册该玩家")
            return
        }
        if(source == call){
            POOL[call]!!.open()
        }else if(source.isOp){
            POOL[call]!!.open(source)
        }else{
            MessageSender.sendMessage(MessageLevel.NOTICE, source, "&4&l商店池中存在该商店，但你没有权限打开该商店")
        }
    }

    fun getRepo(target: Player): Repo?{
        return if(POOL.containsKey(target)){
            POOL[target]
        }else null
    }

}

class Repo(owner: Player, init: ArrayList<Product>) {

    // 商店GUI框架
    val INV: Inventory
    var Title: String

    // 商店内容
    val Content: MutableList<Product> = emptyList<Product>().toMutableList()

    // 商店拥有者
    val Owner: Player = owner
    var isView: Boolean = false

    // 收购器
    val seller: Seller

    init {
        val rawTitle = ConfigurationReader.getGUITitle()

        Title = if(PAPIHooker.isHook) {
            PlaceholderAPI.setPlaceholders(owner, rawTitle)
        }else{
            rawTitle
        }

        INV = Bukkit.createInventory(null, ConfigurationReader.getGUIRow(), Title)

        for(i in init.indices){
            Content[i] = init[i]
        }

        preInitView()
        seller = Seller(Owner)
    }

    fun addProduct(product: Product): Int{ // 0 - Succeed | 1 - Too Much | 2 - Similar Product
        if(Content.size != 54){
            if(Content.contains(product)){
                return 2
            }
            Content.add(product)
            return 0
        }else{
            return 1
        }
    }

    fun delProduct(pos: Int): Int{ // 0 - Succeed | 1 - Empty POOL | 2 - No Position Exist
        if(Content.size == 1){
            return 1
        }else{
            if(pos !in Content.indices){
                return 2
            }
            Content.removeAt(pos)
            return 0
        }
    }

    private fun preInitView(){
        Content.forEach{ p ->
            INV.addItem()
        }
    }

    fun sell(pos: Int){
        if(pos !in Content.indices){
            return
        }
        seller.setProduct(Content[pos])
        seller.openSell()
    }

    fun closeRepo(){
        seller.clearSell()
        open()
    }

    fun open(){
        isView = true
        Owner.openInventory(INV)
    }

    fun open(target: Player){
        target.openInventory(INV)
    }

    fun closeRepoView(){
        isView = false
        INV.viewers.forEach{ it.closeInventory() }
    }

    class Seller(owner: Player){
        val Owner: Player = owner
        var Target: Product? = null
        val INV: Inventory = GUIRESOURCES.SELLGUIFRAME
        var callAmount: Int = 0
        var currentSellResponse: SellResponse = SellResponse.ZERO

        fun isEmpty(): Boolean{
            return Target == null
        }

        fun setProduct(product: Product){
            Target = product
            INV.setItem(4, product.toICON())
            updatePreSellStatus()
        }

        private fun updatePreSellStatus(){
            currentSellResponse = Target!!.prePayForPlayer(Owner, callAmount)
            INV.setItem(51, OK(currentSellResponse, callAmount, Target!!.Price))
        }

        fun plus(amount: Int){
            if(!isEmpty()){
                return
            }
            if(callAmount + amount > Target!!.Limit) {
                callAmount = Target!!.Limit
            }else{
                callAmount += amount
            }
            updatePreSellStatus()
        }

        fun minus(amount: Int){
            if(!isEmpty()){
                return
            }
            if(callAmount - amount < 0) {
                callAmount = 0
            }else{
                callAmount -= amount
            }
            updatePreSellStatus()
        }

        fun sell(){
            if(!isEmpty()){
                return
            }
            Target!!.payForPlayer(Owner, callAmount)
            Owner.inventory.remove(Target!!.toICON().also { it.amount = callAmount })
            clearSell()
        }

        fun openSell(){
            Owner.openInventory(INV)
        }

        fun clearSell(){
            Target = null
            callAmount = 0
            Owner.closeInventory()
        }

    }

}

object RepoGUIListener: Listener {

    @EventHandler fun onListen(ice: InventoryClickEvent){
        val p = ice.whoClicked as Player
        val repo = RepoPool.getRepo(p)
        if(repo != null && repo.isView && ice.view.title == repo.Title){
            ice.isCancelled = true
            when(val slot = ice.slot){
                in repo.Content.indices -> { repo.sell(slot) }
            }
        }
    }

}

object SellGUIListener: Listener{

    @EventHandler fun onListen(ice: InventoryClickEvent){
        val p = ice.whoClicked as Player
        val repo = RepoPool.getRepo(p)
        if(repo != null && repo.isView && !repo.seller.isEmpty()){
            ice.isCancelled = true
            val seller = repo.seller
            when(ice.currentItem){
                CANCEL -> {
                    seller.clearSell()
                    repo.open()
                }
                OK -> {
                    when(seller.currentSellResponse){
                        SellResponse.DONE -> seller.sell()
                        else -> {}
                    }
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

enum class SellResponse{
    DONE,
    ZERO,
    OVERFLOW,
    NOT_ENOUGH,
}