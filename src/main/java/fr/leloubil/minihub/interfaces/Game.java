package fr.leloubil.minihub.interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Classe qui sert a représenter une partie
 */
public abstract class Game {

    public abstract Location getWaitZone();

    /**
     * Méthode pour récuperer le nom du mini jeu;
     * @return Le nom du mini jeu
     */
    public abstract String getMiniGameName();

    /**
     * Méthode pour récuperer un nom unique a chaque partie/lobby;
     * @return Le nom de la partie/lobby
     */
    public abstract String getName();


    /**
     * Méthode appelée lorsque un joueur utilise /leave ou se déconnecte d'une partie
     * @param p le joueur
     * @return si il a bien quitté
     */
    public boolean leave(Player p){
        if(this.isLobby()) {
            leaveLobby(p);
            return true;
        }
        return false;
    }

    /**
     * Méthode pour savoir si cet objet représente un lobby
     * @return si c'est un lobby
     */
    public abstract boolean isLobby();

    /**
     * Méthode appelée lorsque un joueur utilise /leave ou se déconnecte d'un lobby
     * @param p le joueur
     */
    public void leaveLobby(Player p){
        if(!this.isLobby()) return;
    }

    /**
     * Méthode utilisée pour récuperer tous les joueurs de la partie
     * @return una arrayList des joueurs
     */
    public abstract ArrayList<Player> getPlayers();
}
