package me.tanluc.starfarm.event;

import me.tanluc.starfarm.StarsFarm;
import me.tanluc.starfarm.data.User;
import me.tanluc.starfarm.fileManager.GuiManager;
import me.tanluc.starfarm.ui.AssistantGui;
import me.tanluc.starfarm.ui.Gui;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Objects;

public class UiClick implements Listener {
    public static String beforeGui;
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        User user = User.of((OfflinePlayer) p);

        if (e.getSlot() == -999) {
            return;
        }

        Inventory clickedInventory = e.getClickedInventory();
        if (clickedInventory == null || clickedInventory.getTitle() == null) {
            return;
        }

        if (GuiManager.loadGuiFileConfiguration() != null) {
            for (File file : GuiManager.loadGuiFileConfiguration()) {
                String fileName = file.getName();
                fileName = fileName.substring(0, fileName.lastIndexOf('.'));

                FileConfiguration guiConfig = YamlConfiguration.loadConfiguration(file);
                String title = guiConfig.getString("title");
                String assistantTitle = StarsFarm.cc.getString("warehouseTitle");
                if (e.getView().getTitle().equals(title)) {
                    e.setCancelled(true);
                    if (e.getSlot() == 53) {
                        if (StarsFarm.players.contains(p)) {
                            StarsFarm.players.remove(p);
                            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 50.0F, 50.0F);
                        } else {
                            StarsFarm.players.add(p);
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50.0F, 50.0F);
                        }
                        Gui.OpenMenu(p, fileName);
                    }
                    if (e.getSlot() == 49) {
                        if (guiConfig.getBoolean("Gui.Back.close")) {
                            e.getWhoClicked().closeInventory();
                        }
                        if (guiConfig.getString("Gui.Back.command") != null && !guiConfig.getString("Gui.Back.command").isEmpty()) {
                            p.performCommand(guiConfig.getString("Gui.Back.command"));
                        }
                    }

                    if (e.getClickedInventory() == p.getOpenInventory().getTopInventory() &&
                            e.getCurrentItem() != null &&
                            e.getCurrentItem().hasItemMeta())
                        if (e.getCurrentItem().getType() == Material.GOLD_NUGGET) {
                            for (String key : guiConfig.getConfigurationSection("Menu").getKeys(false)) {
                                if (e.getSlot() - 9 == guiConfig.getInt("Menu." + key + ".slot")) {
                                    ItemStack is = ((Inventory)Objects.<Inventory>requireNonNull(e.getClickedInventory())).getItem(e.getSlot() - 9);
                                    assert is != null;
                                    String type = is.getType().name();
                                    int have = user.getHave(type);
                                    int sell = user.getSell(type);
                                    int max = 0;
                                    double price = 0;
                                    String itemName = "";

                                    ConfigurationSection blockSupport = guiConfig.getConfigurationSection("Menu");
                                    ItemStack clickedItem = e.getView().getItem(e.getSlot() - 9);
                                    if (clickedItem != null) {
                                        String clickedItemType = clickedItem.getType().toString();

                                        for (String blockkey : blockSupport.getKeys(false)) {
                                            if (blockkey.equalsIgnoreCase(clickedItemType)) {
                                                max = guiConfig.getInt("Menu." + blockkey + ".maxSell");
                                                price = guiConfig.getDouble("Menu." + blockkey + ".price");
                                                itemName = guiConfig.getString("Menu." + blockkey + ".name");
                                                break;
                                            }
                                        }
                                    }
                                    if (have > 0) {
                                        if (sell > max) {
                                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50.0F, 50.0F);
                                            p.closeInventory();
                                            p.sendMessage("§r[" + StarsFarm.prefix + "§r]§e Đã đạt giới hạn ngày hôm nay");
                                            System.out.println("Sell total: " + sell + " " + "Max total: " + max);
                                            continue;
                                        } else if (max > sell) {
                                            int km = max - sell;
                                            if (have < km)
                                                km = have;
                                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50.0F, 50.0F);
                                            p.closeInventory();
                                            double totalPrice = km * price;
                                            p.sendMessage(StarsFarm.messageManager.sellSuccess().replace("{name}", ChatColor.translateAlternateColorCodes('&', itemName)).replace("{totalPrice}", String.valueOf(totalPrice)).replace("{amount}", String.valueOf(km)).replace("{price}", String.valueOf(price)));
                                            StarsFarm.getEconomy().depositPlayer((OfflinePlayer)p, km * price);
                                            user.setHave(type, have - km);
                                            user.setSell(type, sell + km);
                                            continue;
                                        }
                                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 50.0F, 50.0F);
                                        p.closeInventory();
                                        p.sendMessage(StarsFarm.messageManager.sellMaxToday());
                                        continue;
                                    }
                                    p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 50.0F, 50.0F);
                                    p.closeInventory();
                                    p.sendMessage(StarsFarm.messageManager.notStoraged());
                                }
                            }
                        } else if (StarsFarm.materials.contains(e.getCurrentItem().getType())) {
                            String type = e.getCurrentItem().getType().name();
                            if (user.getHave(type) > 0) {
                                if (p.getInventory().firstEmpty() == -1) {
                                    p.closeInventory();
                                    p.sendMessage(StarsFarm.messageManager.inventoryFull());
                                } else {
                                    AssistantGui.StorageGui(p, e.getCurrentItem().getType());
                                    beforeGui = fileName;
                                }
                            } else {
                                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 50.0F, 50.0F);
                                p.sendMessage(StarsFarm.messageManager.notStoraged());
                            }
                        }
                } else if (e.getView().getTitle().equals(assistantTitle)) {
                    e.setCancelled(true);
                    if (e.getCurrentItem() != null && e.getClickedInventory() == p.getOpenInventory().getTopInventory()) {
                        if (e.getCurrentItem().hasItemMeta()) {
                            Gui.OpenMenu(p, beforeGui);
                        } else if (StarsFarm.materials.contains(e.getCurrentItem().getType())) {
                            String type = e.getCurrentItem().getType().name();
                            int amountToAdd = 0;

                            if (e.isRightClick()) {
                                amountToAdd = 1;
                            } else {
                                amountToAdd = e.getCurrentItem().getAmount();
                            }

                            if (p.getInventory().firstEmpty() != -1) {
                                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50.0F, 50.0F);
                                user.setHave(type, user.getHave(type) - amountToAdd);
                                ItemStack itemToAdd = e.getCurrentItem().clone();
                                itemToAdd.setAmount(amountToAdd);
                                p.getInventory().addItem(itemToAdd);
                                AssistantGui.StorageGui(p, e.getCurrentItem().getType());
                            } else {
                                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 50.0F, 50.0F);
                                p.closeInventory();
                                p.sendMessage(StarsFarm.messageManager.inventoryFull());
                            }
                        }
                    }
                }

            }
        }
    }
}
