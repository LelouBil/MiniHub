package fr.leloubil.minihub;

import fr.leloubil.minihub.interfaces.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerCommands implements CommandExecutor {
    private Player p;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        p = (Player) sender;
        switch (command.getName()) {
            case "hub":
            case "leave":
                if(fr.leloubil.minihub.MiniHub.games.containsKey(p.getUniqueId())) {
                    Game l = MiniHub.games.get(p.getUniqueId());
                    if(l.isLobby())l.leaveLobby(p);
                    else l.leave(p);
                }
        }
        return true;
    }

    private boolean error(String s) {
        return false;
    }
}
