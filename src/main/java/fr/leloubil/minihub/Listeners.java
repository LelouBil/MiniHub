package fr.leloubil.minihub;

import fr.leloubil.minihub.interfaces.Game;
import net.lotary.modération.events.ModJoinEvent;
import net.lotary.modération.events.ModLeaveEvent;
import net.lotary.modération.mods.ModManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class Listeners implements Listener {


    public static ItemStack bousolle = new ItemStack(Material.COMPASS);
    public static Inventory bousolleInv = Bukkit.createInventory(null,9,ChatColor.DARK_PURPLE + "Mini-Jeux");
    public static void prepareInv() {
        ItemMeta bousolleMeta = bousolle.getItemMeta();
        bousolleMeta.setDisplayName(ChatColor.DARK_PURPLE + "Mini-Jeux");
        bousolleMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Bruh"));
        bousolle.setItemMeta(bousolleMeta);
        setBousolle();
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        e.setJoinMessage("");
        updateHideShow();
        Player p = e.getPlayer();
        p.getInventory().clear();
        MiniHub.giveItems(p);
        e.getPlayer().teleport(MiniHub.getLobby());
        e.getPlayer().setGameMode(GameMode.ADVENTURE);
        e.getPlayer().setBedSpawnLocation(MiniHub.getLobby(),true);
        Bukkit.getWorld("lobby").getPlayers().stream().filter(pl -> !MiniHub.games.containsKey(pl.getUniqueId())).forEach(pl -> pl.sendMessage(e.getPlayer().getDisplayName() + " s'est connecté !"));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        e.getRecipients().clear();
        e.getRecipients().add(e.getPlayer());
        Player p = e.getPlayer();
        String worldName = p.getWorld().getName();
        if(!MiniHub.games.containsKey(p.getUniqueId())) {
            Bukkit.getWorlds().forEach(w -> {
                if (w.getName().equals(worldName)) {
                    w.getPlayers().forEach(player -> {
                        e.getRecipients().add(player);
                    });
                }
            });
        }
        else {
            e.getRecipients().addAll(MiniHub.games.get(p.getUniqueId()).getPlayers());
        }
    }

    @EventHandler(priority = EventPriority.LOW,ignoreCancelled = true)
    public void OnHungerChange(FoodLevelChangeEvent e){
        if(e.getEntity().getLocation().getWorld().getName().equals("lobby")) e.setCancelled(true);
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e){
        UUID id = e.getPlayer().getUniqueId();
        e.getPlayer().getInventory().clear();
        e.getPlayer().getInventory().setHelmet(null);
        e.getPlayer().getInventory().setChestplate(null);
        e.getPlayer().getInventory().setLeggings(null);
        e.getPlayer().getInventory().setBoots(null);

        e.setQuitMessage("");
        updateHideShow();
        if(e.getPlayer().getLocation().getWorld().getName().equals("lobby")){
            Bukkit.getWorld("lobby").getPlayers().forEach(p -> {
                if(!MiniHub.games.containsKey(p.getUniqueId())) p.sendMessage(e.getPlayer().getDisplayName() + " est parti !");
            });
        }
        if(MiniHub.games.containsKey(e.getPlayer().getUniqueId())){
            Game l = MiniHub.games.get(id);
            if(e.getPlayer() == null) return;
            if(l.isLobby() )l.leaveLobby(e.getPlayer());
            else l.leave(e.getPlayer());
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e){
        if(!e.getEntityType().equals(EntityType.VILLAGER) && !e.getEntityType().equals(EntityType.DROPPED_ITEM)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e){
        Player p = e.getPlayer();
        p.setHealth(20);
        p.setFoodLevel(20);
        updateHideShow();
        p.getWorld().getPlayers().forEach(Listeners::perWorld);
        e.getPlayer().setGameMode(GameMode.ADVENTURE);
        if(e.getPlayer().getWorld().getName().equals("lobby")){
            MiniHub.giveItems(p);
        }
    }

    @EventHandler
    public void onModJoin(ModJoinEvent e){
        updateHideShow();
    }

    @EventHandler
    public void onModLeave(ModLeaveEvent e){
        updateHideShow();
    }

    public static void updateHideShow() {
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        for (Player player : players) {
            perWorld(player);
        }

        Bukkit.getWorld("lobby").getPlayers().forEach(player -> {

            if(MiniHub.games.containsKey(player.getUniqueId())){
                player.getWorld().getPlayers().forEach(player::hidePlayer);
                if(player.getInventory().getItem(0) != null && player.getInventory().getItem(0).isSimilar(MiniHub.getHidemush())){
                    MiniHub.games.keySet().forEach(u -> {
                        if(!ModManager.mods.get(Bukkit.getPlayer(u)).isInModerationMod())player.showPlayer(Bukkit.getPlayer(u));
                    });
                }else if(player.getInventory().getItem(0) != null && player.getInventory().getItem(0).isSimilar(MiniHub.getShowmush())){
                    MiniHub.games.get(player.getUniqueId()).getPlayers().forEach(player1 -> {
                        if(!ModManager.mods.get(player1).isInModerationMod()) player.showPlayer(player1);
                    });
                }
            }
            else {
                player.getWorld().getPlayers().forEach(player::hidePlayer);
                player.getWorld().getPlayers().forEach(pl -> {
                    if(!MiniHub.games.containsKey(pl.getUniqueId())) {
                        if(player.getInventory().getItem(0) != null && player.getInventory().getItem(0).equals(MiniHub.getHidelobmush())){
                            player.showPlayer(pl);
                        }
                    }
                });
            }
        });
    }

    public static void perWorld(Player p) {
        String worldName = p.getWorld().getName();
        Bukkit.getWorlds().forEach(w -> {
            if(w.getName().equals(worldName)) {
                w.getPlayers().forEach(player -> {
                    if(!ModManager.mods.get(p).isInModerationMod())player.showPlayer(p);
                    if(!ModManager.mods.get(player).isInModerationMod())p.showPlayer(player);
                });
            }
            else {
                w.getPlayers().forEach(player -> {
                    player.hidePlayer(p);
                    p.hidePlayer(player);
                });
            }
        });
    }

    @EventHandler
    public void DropListener(PlayerDropItemEvent e){
        Player p = e.getPlayer();
        if(p.getWorld().getName().equals("lobby") && p.getGameMode() == GameMode.ADVENTURE) e.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e){
        if(e.getEntityType() != EntityType.PLAYER) return;
        if(e.getEntity().getLocation().getWorld().getName().equals("lobby")) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onCraft(CraftItemEvent e){
        if(MiniHub.games.containsKey(e.getWhoClicked().getUniqueId()) || e.getWhoClicked().getWorld().getName().equals("lobby")) e.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onLobbyInteract(PlayerInteractEvent e){
        if(!e.getPlayer().getWorld().getName().equals("lobby") || !e.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) return;
        if(e.getItem() == null) return;
        if(e.getItem().isSimilar(bousolle)){
            setBousolle();
            e.getPlayer().openInventory(bousolleInv);
            return;
        }
        if(e.getItem().isSimilar(MiniHub.getHidelobmush())){
            e.getPlayer().setItemInHand(MiniHub.getShowlobmush());
            updateHideShow();
            return;
        }
        if(e.getItem().isSimilar(MiniHub.getShowlobmush())){
            e.getPlayer().setItemInHand(MiniHub.getHidelobmush());
            updateHideShow();
            return;
        }
        if(e.getItem().isSimilar(MiniHub.getHidemush())){
            e.getPlayer().setItemInHand(MiniHub.getShowmush());
            updateHideShow();
            return;
        }
        if(e.getItem().isSimilar(MiniHub.getShowmush())){
            e.getPlayer().setItemInHand(MiniHub.getHidemush());
            updateHideShow();
        }
        if(e.getItem().isSimilar(MiniHub.getToUp())){
            if(! MiniHub.games.containsKey(e.getPlayer().getUniqueId())) return;
            Game l =  MiniHub.games.get(e.getPlayer().getUniqueId());
            e.getPlayer().teleport(l.getWaitZone());
        }
        if(e.getItem().isSimilar(MiniHub.getLeavedoor())){
            if(! MiniHub.games.containsKey(e.getPlayer().getUniqueId())) return;
            Game l =  MiniHub.games.get(e.getPlayer().getUniqueId());
            if(l.isLobby())l.leaveLobby(e.getPlayer());
        }
    }

    static void setBousolle() {
        if(bousolleInv.getItem(0) == null){
            final int[] in = {0};
            MiniHub.getMinigames().forEach((i) -> {
                bousolleInv.setItem(in[0],i.getItemStack());
                in[0]++;
            });
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryClickEvent e){
        if(e.getWhoClicked().getWorld().getName().equals("lobby") && e.getWhoClicked().getGameMode() == GameMode.ADVENTURE) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e){
        if(e.getWhoClicked().getWorld().getName().equals("lobby") && e.getWhoClicked().getGameMode() == GameMode.ADVENTURE) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW,ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e){
        if(!e.getWhoClicked().getWorld().getName().equals("lobby") || e.getWhoClicked().getGameMode() != GameMode.ADVENTURE) return;
        if(e.getCurrentItem() == null) return;
        e.setCancelled(true);
        if(e.getCurrentItem().isSimilar(bousolle)) {
            setBousolle();
            e.getWhoClicked().openInventory(bousolleInv);
            return;
        }
        if(e.getInventory().equals(bousolleInv)){
            e.getWhoClicked().closeInventory();

            Optional<MiniIcon> icon = MiniHub.getMinigames().stream().filter(ic -> ic.getItemStack().equals(e.getCurrentItem())).findFirst();
            if(!icon.isPresent()) return;
            e.getWhoClicked().teleport(icon.get().getLocation());
        }
    }

}
