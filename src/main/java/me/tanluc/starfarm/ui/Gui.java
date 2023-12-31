package me.tanluc.starfarm.ui;

import java.io.File;
import java.util.*;

import me.tanluc.starfarm.data.User;
import me.tanluc.starfarm.StarsFarm;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static me.tanluc.starfarm.StarsFarm.getInstance;

public class Gui {

    public static void OpenMenu(Player p, String guiName) {
        File guiFile = new File(getInstance().getDataFolder(), "gui/" + guiName + ".yml");
        if (!guiFile.exists()) {
            return;
        }
        FileConfiguration guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        String title = guiConfig.getString("title");
        if (title == null) {
            // Handle if the specified GUI file does not have a title configured
            return;
        }

        Inventory ui = Bukkit.createInventory(null, 54, title);
        ((StarsFarm) StarsFarm.getPlugin(StarsFarm.class)).LoadConfig();
        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 50.0F, 50.0F);

        setupZSItems(p, ui, guiConfig);
        setupMenuItems(p, ui , guiConfig, 1, 2);
        setupOnOffButton(p, ui ,guiConfig);

        p.openInventory(ui);
    }
    private static void setupZSItems(Player p, Inventory ui, FileConfiguration guiConfig) {
        // ------------------ BOTTOM
        List<String> bottomLore = guiConfig.getStringList("Gui.Bottom.lore");
        int bottomData = guiConfig.getInt("Gui.Bottom.data");
        ItemStack bottom = new ItemStack(Material.getMaterial(guiConfig.getString("Gui.Bottom.material")), 1, (short) bottomData);
        ItemMeta bottomMeta = bottom.getItemMeta();
        assert bottomMeta != null;
        bottomMeta.setDisplayName(guiConfig.getString("Gui.Bottom.name"));
        bottomMeta.setLore(bottomLore);
        bottom.setItemMeta(bottomMeta);
        int bti = 44;
        while (bti < 54) {
            ui.setItem(bti, bottom);
            bti++;
        }

        // -- BACK BUTTON --
        List<String> backLore = guiConfig.getStringList("Gui.Back.lore");
        int backData = guiConfig.getInt("Gui.Back.data");
        ItemStack back = new ItemStack(Material.getMaterial(guiConfig.getString("Gui.Back.material")), 1, (short) backData);
        ItemMeta backMeta = back.getItemMeta();
        assert backMeta != null;
        backMeta.setDisplayName(guiConfig.getString("Gui.Back.name"));
        backMeta.setLore(backLore);
        back.setItemMeta(backMeta);
        ui.setItem(49, back);

        // ------------------ BORDER
        List<String> lore = guiConfig.getStringList("Gui.Border.lore");
        int ZS1data = guiConfig.getInt("Gui.Border.data");
        ItemStack bl = new ItemStack(Material.getMaterial(guiConfig.getString("Gui.Border.material")), 1, (short) ZS1data);
        ItemMeta bm = bl.getItemMeta();
        assert bm != null;
        bm.setDisplayName(guiConfig.getString("Gui.Border.name"));
        bm.setLore(lore);
        bl.setItemMeta(bm);
        int i = 0;
        while (i < 45) {
            ui.setItem(i, bl);
            i++;
        }

        // ------------------ INSIDE
        List<String> l1 = guiConfig.getStringList("Gui.Inside.lore");
        int ZS2data = guiConfig.getInt("Gui.Inside.data");
        bl = new ItemStack(Material.getMaterial(guiConfig.getString("Gui.Inside.material")), 1, (short) ZS2data);
        bm = bl.getItemMeta();
        assert bm != null;
        bm.setDisplayName(guiConfig.getString("Gui.Inside.name"));
        bm.setLore(l1);
        bl.setItemMeta(bm);
        i = 10;
        int a = 1;
        while (i <= 43) {
            ui.setItem(i, bl);
            i++;
            i++;
            if (a == 4) {
                a = 0;
                i++;
            }
            a++;
        }
    }

    private static void setupMenuItems(Player p, Inventory ui, FileConfiguration guiConfig, int currentPage, int totalPages) {
        User user = User.of((OfflinePlayer) p);

        for (String key : guiConfig.getConfigurationSection("Menu").getKeys(false)) {
            int modelData = guiConfig.getInt("Menu." + key + ".data");
            String itemName = guiConfig.getString("Menu." + key + ".name");

            ItemStack itemStack = new ItemStack(Material.getMaterial(key.toUpperCase()), 1, (short) modelData);
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(itemName);

            ArrayList<String> loreList = new ArrayList<>();
            String type = itemStack.getType().name();

            for (String lore : guiConfig.getStringList("Menu." + key + ".lore")) {
                loreList.add(lore.replace("{0}", String.valueOf(user.getHave(type))));
            }

            itemMeta.setLore(loreList);
            itemStack.setItemMeta(itemMeta);

            int slot = guiConfig.getInt("Menu." + key + ".slot");
            ui.setItem(slot, itemStack);

            if (slot < 48) {
                String sellMaterial = guiConfig.getString("Gui.Sell.material");
                String sellName = guiConfig.getString("Gui.Sell.name");

                ItemStack sellItemStack = new ItemStack(Objects.requireNonNull(Material.getMaterial(sellMaterial)));
                ItemMeta sellItemMeta = sellItemStack.getItemMeta();
                assert sellItemMeta != null;
                sellItemMeta.setDisplayName(sellName.replace("{0}", itemName));

                ArrayList<String> sellLoreList = new ArrayList<>();
                int maxSell = guiConfig.getInt("Menu." + key + ".maxSell");
                String sellType = itemStack.getType().name();

                for (String sellLore : guiConfig.getStringList("Gui.Sell.lore")) {
                    sellLore = sellLore.replace("{1}", user.getSell(sellType) + "/" + maxSell);

                    if (user.getHave(sellType) >= maxSell - user.getSell(sellType)) {
                        sellLore = sellLore.replace("{2}", String.valueOf(maxSell - user.getSell(sellType)));
                        sellLore = sellLore.replace("{3}", String.valueOf((maxSell - user.getSell(sellType)) * guiConfig.getDouble("Menu." + key + ".price")));
                    } else {
                        sellLore = sellLore.replace("{2}", String.valueOf(user.getHave(sellType)));
                        sellLore = sellLore.replace("{3}", String.valueOf(user.getHave(sellType) * guiConfig.getDouble("Menu." + key + ".price")));
                    }

                    sellLoreList.add(sellLore);
                }

                sellItemMeta.setLore(sellLoreList);
                sellItemStack.setItemMeta(sellItemMeta);
                ui.setItem(slot + 9, sellItemStack);
            }
        }
    }

    private static void setupOnOffButton(Player p, Inventory ui, FileConfiguration guiConfig) {
        int shouData = guiConfig.getInt("Gui.Button.data");
        int shouSlot = guiConfig.getInt("Gui.Button.slot");

        ItemStack shouItem = new ItemStack(Material.getMaterial(guiConfig.getString("Gui.Button.material")), 1, (short) shouData);
        ItemMeta shouMeta = shouItem.getItemMeta();
        if (shouMeta != null) {
            shouMeta.setDisplayName(guiConfig.getString("Gui.Button.name"));
            ArrayList<String> shouLore = new ArrayList<>();
            for (String s : guiConfig.getStringList("Gui.Button.lore")) {
                shouLore.add(s.replace("{0}", StarsFarm.players.contains(p) ?
                        guiConfig.getString("Gui.Button.true") :
                        guiConfig.getString("Gui.Button.false")));
            }
            shouMeta.setLore(shouLore);
            shouItem.setItemMeta(shouMeta);
        }
        ui.setItem(shouSlot, shouItem);
    }

}
