package me.mucloud.plugin.XY.RandomSell.internal.Shop

import me.clip.placeholderapi.PlaceholderAPI
import me.mucloud.plugin.XY.RandomSell.external.hook.PAPIHooker
import me.mucloud.plugin.XY.RandomSell.internal.MessageLevel
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender
import me.mucloud.plugin.XY.RandomSell.internal.configuration.ConfigurationReader
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory

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
    private var seller: Seller? = null

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
        seller = Seller(this, Content[pos])
        seller?.toView()
    }

    fun cancelSell(){
        seller?.close()
        seller = null
        open()
    }

    fun open(){
        isView = true
        Owner.openInventory(INV)
    }

    fun open(target: Player){
        target.openInventory(INV)
    }

    fun getOwner(): Player{
        return Owner
    }

}

object RepoGUIListener: Listener {

    @EventHandler fun onListen(ice: InventoryClickEvent){
        val p = ice.whoClicked as Player
        val repo = RepoPool.getRepo(p)
        if(repo != null && repo.isView && ice.view.title == repo.Title && ice.slotType == InventoryType.SlotType.CONTAINER){
            ice.isCancelled = true
            when(val slot = ice.slot){
                in repo.Content.indices -> { repo.sell(slot) }
            }
        }
    }

}