package me.mucloud.plugin.XY.RandomSell.internal.Shop

import me.mucloud.plugin.XY.RandomSell.external.hook.VaultHooker
import me.mucloud.plugin.XY.RandomSell.internal.MessageSender

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object GUIRESOURCES {

    private fun createICON(isPlus: Boolean, amount: Int): ItemStack{
        return ItemStack(Material.PAPER).also {
            val meta = it.itemMeta
            meta?.setDisplayName(MessageSender.transColor("${if(isPlus) "&a&l+" else "&4&l-"}$amount"))
            meta?.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            it.itemMeta = meta
        }
    }

    val EI = ItemStack(Material.GRAY_STAINED_GLASS_PANE).also {
        val meta = it.itemMeta
        meta?.setDisplayName("")
        meta?.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        it.itemMeta = meta
    }

    val P1 = createICON(true, 1)

    val P5 = createICON(true, 5)

    val PX = createICON(true, 10)

    val M1 = createICON(false, 1)

    val M5 = createICON(false, 5)

    val MX = createICON(false, 10)

    val OK = fun(response: SellResponse, amount: Int, price: Double): ItemStack{
        return ItemStack(
            if(response == SellResponse.DONE){
                Material.GREEN_STAINED_GLASS_PANE
            }else{
                Material.RED_STAINED_GLASS_PANE
            }
        ).also {
            val meta = it.itemMeta
            meta?.setDisplayName(MessageSender.transColor("&a&l确认收购"))
            meta?.lore = when(response){
                SellResponse.DONE -> {
                    listOf(
                        "&7&l| &a&l当前可以进行收购",
                        "&7&l| &a&l收购完成后你将获得 ${amount *price} Coin",
                    )
                }
                SellResponse.NOT_ENOUGH -> {
                    listOf(
                        "&7&l| &4&l当前不可以进行收购",
                        "&7&l| &4&l你的背包里没有足够的物品用于完成收购"
                    )
                }
                SellResponse.OVERFLOW -> {
                    listOf(
                        "&7&l| &4&l当前不可以进行收购",
                        "&7&l| &4&l欲收购的数量超过了限额"
                    )
                }
                SellResponse.ZERO -> {
                    listOf(
                        "&7&l| &4&l当前不可以进行收购",
                        "&7&l| &4&l当前还未定义收购数量"
                    )
                }
            }

            meta?.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            it.itemMeta = meta
        }
    }

    val CANCEL = ItemStack(Material.RED_STAINED_GLASS_PANE).also {
        val meta = it.itemMeta
        meta?.setDisplayName(MessageSender.transColor("&4&l取消"))
        meta?.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        it.itemMeta = meta
    }

    val SELLGUIFRAME = Bukkit.createInventory(null, 3 *9, MessageSender.transColor("&6&l请调整收购数")).also{
        val l1: ArrayList<ItemStack> = ArrayList(9)
        val l2: ArrayList<ItemStack> = ArrayList(9)
        val l3: ArrayList<ItemStack> = ArrayList(9)
        for(i in 0..8){
            if(i != 4){
                l1[i] = EI
            }
        }

        for(i in 0..8){
            when(i){
                0 -> {
                    l2[i] = EI
                    l2[8 - i] = EI
                }
                1 -> {
                    l2[i] = MX
                    l2[8 - i] = PX
                }
                2 -> {
                    l2[i] = M5
                    l2[8 - i] = P5
                }
                3 -> {
                    l2[i] = M1
                    l2[8 - i] = P1
                }
                4 -> {
                    l2[i] = EI
                }
            }
        }

        for(i in 0..8){
            when(i){
                2 -> {
                    l3[i] = CANCEL
                }
                else -> l3[i] = EI
            }
        }

        val out = l1 + l2 + l3

        it.contents = out.toTypedArray()
    }

}
