package fr.leloubil.minihub;

import fr.leloubil.minihub.interfaces.Game;
import net.lotary.lotaryapi.inventories.CustomInventory;
import net.lotary.lotaryapi.utils.ItemBuilder;
import net.lotary.modération.events.ModJoinEvent;
import net.lotary.modération.events.ModLeaveEvent;
import net.lotary.modération.events.ModRandomTPEvent;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Listeners implements Listener {


    public static ItemStack bousolle = new ItemStack(Material.COMPASS);
    public static Inventory bousolleInv = Bukkit.createInventory(null,9,ChatColor.DARK_PURPLE + "Mini-Jeux");
    public static void prepareInv() {
        ItemMeta bousolleMeta = bousolle.getItemMeta();
        bousolleMeta.setDisplayName(ChatColor.DARK_PURPLE + "Mini-Jeux");
        bousolleMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Bruh"));
        bousolle.setItemMeta(bousolleMeta);
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

    @EventHandler(priority = EventPriority.HIGH)
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
    public void onWeatherChange(WeatherChangeEvent e){
        if(e.toWeatherState()) e.setCancelled(true);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e){
        Player p = e.getPlayer();
        updateHideShow();
        if(!notClear.contains(p.getUniqueId())) {
            p.setHealth(20);
            p.setFoodLevel(20);
            e.getPlayer().setGameMode(GameMode.ADVENTURE);
            if (e.getPlayer().getWorld().getName().equals("lobby")) {
                MiniHub.giveItems(p);
            }
        }
        notClear.remove(p.getUniqueId());
    }

    @EventHandler
    public void onModJoin(ModJoinEvent e){
        if(MiniHub.games.containsKey(e.getPlayer().getUniqueId())){
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "Tu ne peut pas faire ceci en partie !");
        }
        updateHideShow();
    }

    private List<UUID> notClear = new ArrayList<>();

    @EventHandler
    public void onModRandomTP(ModRandomTPEvent e){
        Player target = e.getTargetPlayer();
        if(MiniHub.games.containsKey(target.getUniqueId())){
            notClear.add(e.getPlayer().getUniqueId());
        }
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
                        if(MiniHub.isNotMod(Bukkit.getPlayer(u)))player.showPlayer(Bukkit.getPlayer(u));
                    });
                }else if(player.getInventory().getItem(0) != null && player.getInventory().getItem(0).isSimilar(MiniHub.getShowmush())){
                    MiniHub.games.get(player.getUniqueId()).getPlayers().forEach(player1 -> {
                        if(MiniHub.isNotMod(player1)) player.showPlayer(player1);
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
                    if(player == null) return;
                    if(MiniHub.isNotMod(p))player.showPlayer(p);
                    if(MiniHub.isNotMod(player))p.showPlayer(player);
                });
            }
            else {
                w.getPlayers().forEach(player -> {
                    if(player == null) return;
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
            e.getPlayer().openInventory(bousolleInv);
            return;
        }
        if(e.getItem().isSimilar(MiniHub.boosterMenu)){
            MiniHub.openBoostersGui(e.getPlayer());
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
        ItemStack blue_glass_pane = new ItemBuilder(Material.STAINED_GLASS_PANE, (byte) 11).name("").make();
        ItemStack light_blue_glass_pane = new ItemBuilder(Material.STAINED_GLASS_PANE,(byte) 3).name("").make();
        ItemStack coming_soon_bars = new ItemBuilder(Material.IRON_FENCE).name(ChatColor.ITALIC.toString() + ChatColor.AQUA + "Coming Soon...").enchantment(Enchantment.SILK_TOUCH).make();
        ItemStack lotawars_flint_and_steel = new ItemBuilder(Material.FLINT_AND_STEEL).name(ChatColor.BOLD.toString() + ChatColor.GOLD + "LotaWars").make();
        ItemStack shop_diamong = new ItemBuilder(Material.DIAMOND).name(ChatColor.BOLD.toString() + ChatColor.BLUE + "Boosters !").make();
        CustomInventory customInventory = new CustomInventory(MiniHub.getInstance(),ChatColor.GOLD + "Mini-Jeux !",false,null,ChatColor.GOLD + "Mini-Jeux !",45);
        customInventory.fill(blue_glass_pane);
        customInventory.fillSlots(light_blue_glass_pane, new int[]{10,16,19,20,21,23,24,25,29,33});
        customInventory.fillSlots(coming_soon_bars,new int[]{11,15});
        customInventory.addItem(lotawars_flint_and_steel,22);
        customInventory.addItem(shop_diamong,28);
        bousolleInv = customInventory.build();
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
        if(e.getCurrentItem().isSimilar(MiniHub.boosterMenu)){
            MiniHub.openBoostersGui((Player) e.getWhoClicked());
            return;
        }
        if(e.getClickedInventory().getName().equals(MiniHub.BOOSTER_GUI_NAME)){
            MiniHub.boosterGuiListener(e);
            return;
        }
        if(e.getClickedInventory().getName().equals(MiniHub.BOOSTER_SHOP_NAME)){
            MiniHub.boosterShopListener(e);
            return;
        }

        if(e.getClickedInventory().getName().equals(MiniHub.BOOSTER_CONFIRM_NAME)){
            MiniHub.boosterConfirmListener(e);
            return;
        }
        if(e.getCurrentItem().isSimilar(bousolle)) {
            e.getWhoClicked().openInventory(bousolleInv);
            return;
        }
        if(e.getInventory().equals(bousolleInv)){
            if(e.getCurrentItem().getType().equals(Material.FLINT_AND_STEEL)){
                e.getWhoClicked().closeInventory();
                e.getWhoClicked().teleport(new Location(Bukkit.getWorld("lobby"),-88,97,91,-30,0));
            }
            else if(e.getCurrentItem().getType().equals(Material.DIAMOND)){
                MiniHub.openBoostersShop((Player) e.getWhoClicked());
            }
        }
    }

}
