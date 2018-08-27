package fr.leloubil.minihub;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class MiniIcon {

    @Getter
    private final ItemStack itemStack;

    @Getter
    private final Location location;

    public MiniIcon(ItemStack itemStack, Location location) {
        this.itemStack = itemStack;
        this.location = location;
    }
}
