package fr.leloubil.minihub;

import com.google.common.collect.ImmutableMap;
import fr.leloubil.minihub.interfaces.Game;
import lombok.Getter;
import net.lotary.lotaryapi.utils.CustomPlayer;
import net.lotary.lotaryapi.utils.ItemBuilder;
import net.lotary.lotaryapi.utils.LotaBooster;
import net.lotary.modération.mods.ModManager;
import net.lotary.modération.mods.ModPlayer;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.function.Supplier;

import static fr.leloubil.minihub.Listeners.bousolle;

public final class MiniHub extends JavaPlugin {


    public static final String BOOSTER_GUI_NAME = ChatColor.AQUA + "BOOSTERS !";
    public static HashMap<UUID,Game> games = new HashMap<>();


    @Getter
    public static Location lobby;


    @Getter
    public static ArrayList<MiniIcon> minigames = new ArrayList<>();
    public static ItemStack boosterMenu = new ItemBuilder(Material.BLAZE_POWDER).name(ChatColor.GOLD + "BOOSTERS !").make();

    private static Supplier<Inventory> BOOSTERS_INVENTORY = () -> Bukkit.createInventory(null,InventoryType.CHEST,BOOSTER_GUI_NAME);

    @Getter
    public static ItemStack hidemush = new ItemStack(Material.BROWN_MUSHROOM);
    @Getter
    public static ItemStack showmush = new ItemStack(Material.RED_MUSHROOM);
    @Getter
    public static ItemStack hidelobmush = new ItemStack(Material.BROWN_MUSHROOM);
    @Getter
    public static ItemStack showlobmush = new ItemStack(Material.RED_MUSHROOM);
    @Getter
    public static ItemStack leavedoor = new ItemStack(Material.WOOD_DOOR);
    @Getter
    public static ItemStack toUp = new ItemStack(Material.LADDER);
    static{
        ItemMeta m = hidemush.getItemMeta();
        m.setDisplayName(ChatColor.ITALIC.toString() + ChatColor.GOLD + "Masquer les autres joueurs");
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Clique pour cacher les joueurs qui attendent une autre partie !");
        m.setLore(lore);
        hidemush.setItemMeta(m);

        ItemMeta me = showmush.getItemMeta();
        me.setDisplayName(ChatColor.ITALIC.toString() + ChatColor.YELLOW + "Afficher les autres joueurs");
        ArrayList<String> lorrr = new ArrayList<>();
        lorrr.add("Clique pour afficher les joueurs qui attendent une autre partie !");
        me.setLore(lorrr);
        showmush.setItemMeta(me);

        m = hidelobmush.getItemMeta();
        m.setDisplayName(ChatColor.ITALIC.toString() + ChatColor.GOLD + "Masquer les autres joueurs");
        lore = new ArrayList<>();
        lore.add("Clique pour cacher les joueurs !");
        m.setLore(lore);
        hidemush.setItemMeta(m);

        me = showlobmush.getItemMeta();
        me.setDisplayName(ChatColor.ITALIC.toString() + ChatColor.YELLOW + "Afficher les autres joueurs");
        lorrr = new ArrayList<>();
        lorrr.add("Clique pour afficher les joueurs !");
        me.setLore(lorrr);
        showmush.setItemMeta(me);

        ItemMeta le = leavedoor.getItemMeta();
        le.setDisplayName(ChatColor.ITALIC.toString() + ChatColor.DARK_RED + "Quitter");
        ArrayList<String> loree = new ArrayList<>();
        loree.add("Clique pour quitter la partie !");
        le.setLore(loree);
        leavedoor.setItemMeta(le);

        ItemMeta a = toUp.getItemMeta();
        a.setDisplayName(ChatColor.ITALIC.toString() + ChatColor.GREEN + "Remonter");
        ArrayList<String> al = new ArrayList<>();
        al.add("Clique pour remonter !");
        a.setLore(al);
        toUp.setItemMeta(a);
    }

    @Override
    public void onEnable() {
        loadConfig();
        fr.leloubil.minihub.Listeners.prepareInv();
        getCommand("hub").setExecutor(new PlayerCommands());
        getCommand("leave").setExecutor(new PlayerCommands());
        getServer().getPluginManager().registerEvents(new Listeners(),this);
    }

    private void loadConfig() {
        saveDefaultConfig();
        if(Bukkit.getWorld("lobby") == null) Bukkit.createWorld(new WorldCreator("lobby"));
        lobby = StrToLocation(getConfig().getString("spawn"));
        for (String s : getConfig().getStringList("minigames")) {
            if(s.split("-->").length == 1) minigames.add(new MiniIcon(StringToItemStack(s.split("-->")[0]),lobby));
            else minigames.add(new MiniIcon(StringToItemStack(s.split("-->")[0]),StrToLocation(s.split("-->")[1])));
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Location StrToLocation(String str){
        if(!(str.startsWith("[") && str.endsWith("]"))) return null;
        try {
            str = str.substring(1,str.length() - 1);
            String[] splitted = str.split(";");
            if (splitted.length < 4 || splitted.length > 6) return null;
            String worldname = splitted[0];
            double x = Double.parseDouble(splitted[1]);
            double y = Double.parseDouble(splitted[2]);
            double z = Double.parseDouble(splitted[3]);
            float yaw = 0f;
            float pitch = 0f;
            if (splitted.length > 4) yaw = Float.parseFloat(splitted[4]);
            if (splitted.length == 6) pitch = Float.parseFloat(splitted[5]);
            return new Location(Bukkit.getWorld(worldname) == null ? Bukkit.getWorlds().get(0) : Bukkit.getWorld(worldname) , x, y, z, yaw, pitch);
        }
        catch (NumberFormatException e){
            return null;
        }
    }

    public static String LocToString(Location loc){
        if(loc == null) loc = new Location(Bukkit.getWorlds().get(0),0,0,0);
        String temp = "[";
        temp+= loc.getWorld() == null ? Bukkit.getWorlds().get(0) : loc.getWorld().getName() + ";";
        temp+= loc.getX() + ";";
        temp+= loc.getY() + ";";
        temp+= loc.getZ() + ";";
        temp+= loc.getYaw() + ";";
        temp+= loc.getPitch() + "]";
        return temp;
    }

    public static ItemStack StringToItemStack(String i){
        ItemStack temp;
        String[] splitted = i.split(";",-1);
        String material = splitted[0].split("~")[0];
        int data = 0;
        if(splitted[0].split("~").length == 2){
            data = Integer.parseInt(splitted[0].split("~")[1]);
            splitted[0] = splitted[0].split("~")[0];
        }
        Integer amount = Integer.parseInt(splitted[1]);
        switch (splitted[0]) {
            case "Potion":
            case "Splash_Potion":
                Potion p = new Potion(1);
                if (splitted[0].equals("Splash_Potion")) {
                    p.setSplash(true);
                }
                String[] effects = splitted[3].split("~");
                String typename = effects[0].split("=")[0];
                PotionEffectType type = PotionEffectType.getByName(typename);
                p.setType(PotionType.getByEffect(type));
                temp = p.toItemStack(amount);
                PotionMeta potionMeta = (PotionMeta) temp.getItemMeta();
                for (String effect : effects) {
                    String name = effect.split("=")[0];
                    Integer lv = Integer.valueOf(effect.split("=")[1]);
                    Integer time = Integer.valueOf(effect.split("=")[2]);
                    potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(name), time * 20, lv), true);
                }
                temp.setItemMeta(potionMeta);
                break;
            case "SPAWN_EGG":
                SpawnEgg egg = new SpawnEgg(EntityType.valueOf(splitted[3]));
                temp = egg.toItemStack();
                temp.setAmount(amount);
                break;
            default:
                temp = new ItemStack(Material.getMaterial(material), amount,  (short) data);
                break;
        }

        String[] miscs = splitted[2].split("~");
        ItemMeta tempmeta = temp.getItemMeta();
        boolean falsench = false;
        for (String miscfact : miscs){
            String[] facts = miscfact.split("=",-1);
            switch (facts[0]){
                case "ench":
                    String enchname = facts[1];
                    Integer enchlv = Integer.parseInt(facts[2]);
                    if(enchname.equals("false")) falsench = true;
                    else tempmeta.addEnchant(Enchantment.getByName(enchname),enchlv,true);
                    break;
                case "name":
                    String name = facts[1];
                    tempmeta.setDisplayName(name);
                    break;
                case "lore":
                    String[] lore = facts[1].split("//");
                    List<String> lorelist = Arrays.asList(lore);
                    tempmeta.setLore(lorelist);
                    break;
            }
        }
        temp.setItemMeta(tempmeta);
        if(falsench){
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(temp);
            NBTTagCompound tag = null;
            if (!nmsStack.hasTag()) {
                tag = new NBTTagCompound();
                nmsStack.setTag(tag);
            }
            if (tag == null) tag = nmsStack.getTag();
            NBTTagList ench = new NBTTagList();
            tag.set("ench", ench);
            nmsStack.setTag(tag);
            temp = CraftItemStack.asCraftMirror(nmsStack);
        }
        return temp;
    }

    public static void giveItems(Player p){
        if(MiniHub.games.containsKey(p.getUniqueId())){
            p.getInventory().setHeldItemSlot(1);
            p.getInventory().setItem(0,MiniHub.getHidemush());
            p.getInventory().setItem(7,MiniHub.getToUp());
            p.getInventory().setItem(8,MiniHub.getLeavedoor());
        } else {
            p.getInventory().setHeldItemSlot(5);
            p.getInventory().setItem(4,bousolle);
            p.getInventory().setItem(2,boosterMenu);
            p.getInventory().setItem(0,MiniHub.getHidelobmush());
        }
    }

    public static boolean isNotMod(Player p){
        if(!ModManager.mods.containsKey(p)) return true;
        ModPlayer modPlayer = ModManager.mods.get(p);
        if(modPlayer == null) return true;
        return modPlayer.isInModerationMod();
    }

    static final ImmutableMap<String,String> messageLoreMap = ImmutableMap.<String, String>builder()
            .put("ALREADY-ONE",ChatColor.BLUE + "Un booster est déja actif !")
            .put("TIME-NOT-PASSED",ChatColor.BLUE + "Tu ne peut plus activer ce booster aujourd'hui !")
            .put("GOOD","Tu peut activer ce booster !")
            .build();

    static void openBoostersGui(Player p){
        Inventory inv = BOOSTERS_INVENTORY.get();
        CustomPlayer player = CustomPlayer.get(p);
        List<LotaBooster> boosterList = player.getBoosters();
        for (int i = 0; i < boosterList.size(); i++) {
            LotaBooster booster = boosterList.get(i);
            CustomPlayer.BoosterResponse response = player.canActivateBooster(booster);
            ItemBuilder item = new ItemBuilder(Material.GHAST_TEAR)
                    .name(ChatColor.AQUA + "Booster : " + ChatColor.GOLD + " x" + ChatColor.BLUE + ChatColor.BOLD + booster.getValue())
                    .amount(response.getQuantity())
                    .lore("Activable : " + (response.isValue() ? "Oui" : "Non"))
                    .lore(messageLoreMap.get(response.getMessage()));
            if(!booster.getDescription().equals("")) item.lore(booster.getDescription());
            if(!response.isInfinite()) item.lore("Quantitée : " + response.getQuantity());
            inv.setItem(i,item.make());
        }
        p.openInventory(inv);
    }

    static void boosterGuiListener(InventoryClickEvent e){
        CustomPlayer player = CustomPlayer.get((Player) e.getWhoClicked());
        List<LotaBooster> boosterList = player.getBoosters();
        int slot = e.getSlot();
        if(boosterList.size() < slot + 1) return;
        LotaBooster clicked = boosterList.get(e.getSlot());
        CustomPlayer.BoosterResponse response = player.canActivateBooster(clicked);
        if(response.isValue()){
            player.setActiveBooster(clicked);
            player.sendMessage(ChatColor.GREEN + "Bravo , tu as bien activé ce booster");
            player.removeOne(clicked);
        }
        else {
            player.sendMessage(ChatColor.RED + "Tu ne peut pas activer ce booster !");
        }
    }
}
