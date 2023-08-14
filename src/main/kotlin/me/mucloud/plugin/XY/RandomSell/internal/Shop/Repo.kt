package me.mucloud.plugin.XY.RandomSell.internal.Shop

import me.mucloud.plugin.XY.RandomSell.Main
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

import me.clip.placeholderapi.PlaceholderAPI

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

object RepoPool{

    // 无商品 停止
    private var NonProductStatus: Boolean = false
    // 未设置刷新时间 停止
    private var NonRefreshStatus: Boolean = false
    // 未设置随机容量 停止
    private var NonCapacityStatus: Boolean = false

    private var IsOpen: Boolean = false
    var Refresh: Int = 0 // minute
    var Remain: Int = 0  // Second
    var Capacity: Int = 0

    private val POOL: MutableMap<Player, Repo> = emptyMap<Player, Repo>().toMutableMap()
    private lateinit var RefreshTask: BukkitTask

    fun launch(sender: CommandSender, config: ConfigurationReader){
        if(NonCapacityStatus || NonRefreshStatus || NonProductStatus){
            MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4随机商品池无法启动, 可能出现了以下原因")
            if(NonProductStatus){
                MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4当前没有商品")
            }
            if(NonCapacityStatus){
                MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4当前容量值为 0 或高于商品池池存量")
            }
            if(NonRefreshStatus){
                MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4当前未设定刷新时间")
            }
        }else{
            IsOpen = true
            Refresh = config.getRefresh()
            Remain = Refresh * 60
            Capacity = config.getCapacity()
        }
    }

    fun regTimerTask(main: Main){
        if(!isOpen()){
            MessageSender.sendMessageToConsole(MessageLevel.NOTICE, "&4&l由于商店池未启动，商店池刷新任务被取消创建")
            return
        }
        RefreshTask = object : BukkitRunnable() {
            override fun run(){
                when(Remain){
                    60,30,5,4,3,2,1 -> MessageSender.broadcastMessage("&6&l随机收购商店即将在 &4&l$Remain &6&l秒后刷新")
                    0 -> {
                        refreshAll()
                        Remain = Refresh * 60
                        return
                    }
                }
                Remain--
            }
        }.runTaskTimer(main, 0, 20L)
    }

    fun refresh(sender: CommandSender, target: Player){
        if(!POOL.containsKey(target)){
            MessageSender.sendMessage(MessageLevel.NOTICE, sender, "&6&l当前商店池中未注册该玩家")
            return
        }
        POOL[target]!!.refresh()
    }

    fun refreshAll(){
        POOL.forEach{
            it.value.refresh()
        }
    }

    fun close(){
        POOL.forEach{
            if(!it.value.seller.isEmpty()){
                it.value.seller.clearSell()
            }
            if(it.value.isView){
                it.value.closeRepoView()
            }
        }
        POOL.clear()
        RefreshTask.cancel()
    }

    fun reg(user: Player){
        POOL[user] = Repo(user, ProductPool.toProductList())
    }

    fun del(user: Player){
        if(POOL.containsKey(user)){
            POOL[user]!!.closeRepoView()
            POOL.remove(user)
        }
    }

    fun setRefreshStatus(b: Boolean){
        NonRefreshStatus = !b
    }

    fun setProductStatus(b: Boolean){
        NonProductStatus = !b
    }

    fun setCapacityStatus(b: Boolean){
        NonCapacityStatus = !b
    }

    fun isOpen(): Boolean{
        return IsOpen
    }

    fun getSize(): Int{
        return POOL.size
    }

    fun getOpenedSize(): Int{
        var out = 0
        POOL.forEach{
            if(it.value.isOpen){
                out++
            }
        }
        return out
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
            if(!POOL[call]!!.open()){
                MessageSender.sendMessage(MessageLevel.NORMAL, source, "&4&l该商店没有被开启")
            }
        }else if(source.isOp){
            POOL[call]!!.open(source)
        }else{
            MessageSender.sendMessage(MessageLevel.NOTICE, source, "&4&l你没有权限执行该命令")
        }
    }

    fun getRepo(target: Player): Repo?{
        return if(POOL.containsKey(target)){
            POOL[target]
        }else null
    }

    fun setOpenRepo(sender: CommandSender, call: String, open: Boolean){
        val target = Bukkit.getPlayer(call)
        if(target == null){
            MessageSender.sendMessage(MessageLevel.NORMAL, sender, "&4&l未找到该玩家")
        }else{
            if(getRepo(target) == null){
                MessageSender.sendMessage(MessageLevel.NOTICE, sender, "&4&l未找到该玩家对应的收购商店")
            }else{
                getRepo(target)!!.isOpen = open
                if(open){
                    MessageSender.sendMessage(MessageLevel.NOTICE, sender, "&a&l该玩家的收购商店已开启")
                }else{
                    MessageSender.sendMessage(MessageLevel.NOTICE, sender, "&6&l该玩家的收购商店已关闭")
                }
            }
        }
    }

}

class Repo(owner: Player, init: ArrayList<Product>) {

    // 开关
    var isOpen = true

    // 商店GUI框架
    val INV: Inventory
    var Title: String

    // 商店内容
    var Content: MutableList<Product> = emptyList<Product>().toMutableList()

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

    fun refresh(){
        Content = ProductPool.toProductList()
        if(!seller.isEmpty()){
            seller.clearSell()
        }
        if(isView){
            Owner.updateInventory()
        }
    }

    // 暂时用不到，等下一个版本
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

    // 暂时用不到，等下一个版本
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
            INV.addItem(p.toICON())
        }
    }

    fun sell(pos: Int){
        if(pos !in Content.indices){
            return
        }
        seller.setProduct(Content[pos])
        seller.openSell()
    }

    fun open(): Boolean{
        if(!isOpen){
            return false
        }
        isView = true
        Owner.openInventory(INV)
        return true
    }

    fun open(target: Player): Boolean{
        target.openInventory(INV)
        return true
    }

    fun closeRepoView(){
        if(!seller.isEmpty()){
            seller.clearSell()
        }
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

    @EventHandler fun onListen(ice: InventoryCloseEvent){
        val p = ice.player as Player
        val repo = RepoPool.getRepo(p)
        if(repo != null && repo.isView && ice.view.title == repo.Title){
            repo.closeRepoView()
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

    @EventHandler fun onListen(ice: InventoryCloseEvent){
        val p = ice.player as Player
        val repo = RepoPool.getRepo(p)
        if(repo != null && repo.isView && !repo.seller.isEmpty()){
            repo.closeRepoView()
        }
    }

}

object RepoPoolListener: Listener{

    @EventHandler fun onListen(pje: PlayerJoinEvent){
        if(RepoPool.isOpen()){
            RepoPool.reg(pje.player)
        }
    }

    @EventHandler fun onListen(pqe: PlayerQuitEvent){
        if(RepoPool.isOpen()){
            RepoPool.del(pqe.player)
        }
    }

}

enum class SellResponse{
    DONE,
    ZERO,
    OVERFLOW,
    NOT_ENOUGH,
}