package net.olokw.sandbox.managers;

import net.olokw.sandbox.configs.PlayerData;
import net.olokw.sandbox.utils.MySQL;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private MySQL mySQL;
    private final Map<UUID, PlayerData> playerDatas;

    public PlayerDataManager(MySQL mySQL) {
        this.mySQL = mySQL;
        this.playerDatas = new HashMap<>();
    }

    public void set(UUID uuid, PlayerData playerData) {
        playerDatas.put(uuid, playerData);
    }

    public void unloadAndSave(UUID uuid){
        mySQL.saveData(uuid, playerDatas.get(uuid));
        playerDatas.remove(uuid);
    }

    public PlayerData get(UUID uuid){
        return playerDatas.get(uuid);
    }
}
