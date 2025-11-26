package net.olokw.sandbox.managers;

import net.olokw.sandbox.configs.PlayerData;
import net.olokw.sandbox.utils.ActionBarUtils;
import net.olokw.sandbox.utils.Message;
import net.olokw.sandbox.utils.TeleportUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class QueueManager {
    private final Map<String, Set<UUID>> queues;
    private final Map<UUID, String> actualQueueID;
    private final TeleportUtils teleportUtils;
    private final PlayerDataManager playerDataManager;
    private final ActionBarUtils actionBarUtils;

    public QueueManager(TeleportUtils teleportUtils, PlayerDataManager playerDataManager, ActionBarUtils actionBarUtils) {
        this.queues = new HashMap<>();
        this.actualQueueID = new HashMap<>();
        this.teleportUtils = teleportUtils;
        this.playerDataManager = playerDataManager;
        this.actionBarUtils = actionBarUtils;
        queues.put("0", new HashSet<>()); // 0 = PC
        queues.put("1", new HashSet<>()); // 1 = mobile
    }

    public void addToDefault(Player p) {
        boolean isMobile = p.getDisplayName().startsWith(".");
        UUID uuid = p.getUniqueId();
        String id = isMobile ? "0" : "1";
        Set<UUID> playersWaiting = queues.get(id);

        if (playersWaiting.isEmpty()) {
            playersWaiting.add(uuid);
            actualQueueID.put(uuid, id);
            actionBarUtils.startActionBar(p, Message.process("<aqua>⌛ <white>Procurando oponente..."));
            for (Player p1 : Bukkit.getOnlinePlayers()) {
                if (p1 != p) {
                    p1.sendActionBar(Message.process("<aqua>⌛ <white>" + p.getName() + " está procurando um oponente."));
                }
            }
        } else {
            PlayerData playerData = playerDataManager.get(uuid);
            for (UUID playerWaiting : playersWaiting) {
                if (playerData != null && playerData.isIgnoring(playerWaiting)) continue;

                PlayerData waitingData = playerDataManager.get(playerWaiting);
                if (waitingData == null) continue;
                if (waitingData.isIgnoring(uuid)) continue;

                Set<UUID> toTeleport = new HashSet<>();
                toTeleport.add(playerWaiting);
                toTeleport.add(uuid);
                playersWaiting.remove(playerWaiting);
                actualQueueID.remove(playerWaiting);
                teleportUtils.randomTeleport(toTeleport, true, null);

                String foundMsg = "<aqua>\uD83D\uDD25 <white>Jogador encontrado!";
                actionBarUtils.stopActionBarAndSendFinalWarning(p, Message.process(foundMsg));
                Player waitingPlayer = Bukkit.getPlayer(playerWaiting);
                if (waitingPlayer != null && waitingPlayer.isOnline()) {
                    actionBarUtils.stopActionBarAndSendFinalWarning(waitingPlayer, Message.process(foundMsg));
                }
                return;
            }


            playersWaiting.add(uuid);
            actualQueueID.put(uuid, id);
            actionBarUtils.startActionBar(p, Message.process("<aqua>⌛ <white>Procurando oponente..."));
            for (Player p1 : Bukkit.getOnlinePlayers()) {
                if (p1 != p) {
                    p1.sendActionBar(Message.process("<aqua>⌛ <white>" + p.getName() + " está procurando um oponente."));
                }
            }
        }
    }

    public boolean tryCreatingCustom(Player organizer, int amount) {
        if (amount < 1) return false;
        String id = organizer.getDisplayName() + "#" + amount;
        UUID uuid = organizer.getUniqueId();
        Set<UUID> queue = new HashSet<>();
        queue.add(uuid);
        actualQueueID.put(uuid, id);
        queues.put(id, queue);
        actionBarUtils.startActionBar(organizer, Message.process("<aqua>⌛ <white>Procurando jogadores... <dark_gray>[<yellow>1<gold>/<yellow>" + amount + "<dark_gray>]"));
        return true;
    }

    public int checkAndAddToCustomQueue(String organizer, Player p) {
        UUID uuid = p.getUniqueId();
        Set<UUID> queue = null;
        int amount = 0;
        String id = "";

        for (String id1 : queues.keySet()) {
            if (id1.startsWith(organizer + "#")) {
                queue = queues.get(id1);
                String[] split = id1.split("#");
                amount = Integer.parseInt(split[1]);
                id = id1;
                break;
            }
        }

        if (queue == null) return 0; // fila não encontrada

        UUID organizerUUID = Bukkit.getOfflinePlayer(organizer).getUniqueId();
        PlayerData organizerData = playerDataManager.get(organizerUUID);
        if (organizerData.isIgnoring(uuid)) return 1;

        queue.add(uuid);
        actualQueueID.put(uuid, id);

        int sizeNow = queue.size();

        if (sizeNow == amount) {
            teleportUtils.randomTeleport(queue, false, null);
            queues.remove(id);

            String msg = amount > 2
                    ? "<aqua>\uD83D\uDD25 <white>Jogador encontrado!"
                    : "<aqua>\uD83D\uDD25 <white>Jogadores encontrados!";

            for (UUID u : queue) {
                actualQueueID.remove(u);
                Player target = Bukkit.getPlayer(u);
                if (target != null && target.isOnline()) {
                    actionBarUtils.stopActionBarAndSendFinalWarning(target, Message.process(msg));
                }
            }

        } else {
            String msg = "<aqua>⌛ <white>Procurando jogadores... <dark_gray>[<yellow>" + sizeNow + "<gold>/<yellow>" + amount + "<dark_gray>]";

            for (UUID u : queue) {
                actionBarUtils.stopActionBarAndStartNew(u, Message.process(msg));
            }

            Player organizerPlayer = Bukkit.getPlayer(organizerUUID);
            if (organizerPlayer != null && organizerPlayer.isOnline()) {
                actionBarUtils.stopActionBarAndStartNew(organizerUUID, Message.process(msg));
            }
        }

        return 2;
    }

    public String hasCreatedCustomQueueAndReturnId(UUID uuid) {
        String id = actualQueueID.get(uuid);
        if (id == null) return "";
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && id.startsWith(player.getName() + "#")) {
            return id;
        }
        return "";
    }

    public Set<String> playersInQueue(String id) {
        Set<UUID> players = queues.getOrDefault(id, Collections.emptySet());
        Set<String> names = new HashSet<>();
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) names.add(p.getName());
        }
        return names;
    }

    public boolean tryKickingFromQueue(String id, UUID uuid) {
        Set<UUID> queue = queues.get(id);
        if (queue == null || !queue.contains(uuid)) return false;

        queue.remove(uuid);
        actualQueueID.remove(uuid);
        actionBarUtils.stopActionBar(uuid);

        if (id.contains("#")) {
            String[] split = id.split("#");
            int amount = Integer.parseInt(split[1]);
            String msg = "<aqua>⌛ <white>Procurando jogadores... <dark_gray>[<yellow>" + queue.size() + "<gold>/<yellow>" + amount + "<dark_gray>]";
            for (UUID u : queue) {
                actionBarUtils.stopActionBarAndStartNew(u, Message.process(msg));
            }
        }

        return true;
    }

    public String getQueueIdFromPlayer(UUID uuid) {
        return actualQueueID.getOrDefault(uuid, null);
    }

    public void deleteQueue(String id) {
        Set<UUID> queue = queues.get(id);
        if (queue != null) {
            for (UUID uuid : queue) {
                actionBarUtils.stopActionBar(uuid);
                actualQueueID.remove(uuid);
            }
        }
        queues.remove(id);
    }

    public Set<String> getOrganizersNicknames() {
        Set<String> organizers = new HashSet<>();
        for (String id : queues.keySet()) {
            if (id.contains("#")) {
                String[] split = id.split("#");
                organizers.add(split[0]);
            }
        }
        return organizers;
    }

    public Set<UUID> playersUUIDinQueue(String id) {
        return queues.getOrDefault(id, Collections.emptySet());
    }
}
