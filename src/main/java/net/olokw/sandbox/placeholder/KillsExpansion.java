package net.olokw.sandbox.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class KillsExpansion extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "rtp";
    }

    @Override
    public String getAuthor() {
        return "Olokw";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.startsWith("topkills_")) {
            String[] parts = params.split("_");
            if (parts.length == 2) {
                String positionString = parts[1];
                return PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer("Olokw"), "%slipcorpvpstats_top_kills_" + positionString + "_name%");
            }
        }
        return null;
    }

    // para corrigir o bug da placeholder do sistema de kills requerir um offlineplayer para dar retorno
    // - já que o desenvolvedor do plugin não corrigiu

}
