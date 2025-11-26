package net.olokw.sandbox.utils;

import net.olokw.grandkits.GrandKits;
import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.configs.PlayerData;
import net.olokw.sandbox.managers.PlayerDataManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class TeleportUtils {
    public void randomTeleport(Set<UUID> players, boolean fastQueue, String selectedKit) {


        int radius = Math.max(players.size(), 3);

        Location center = getRandomDryLocation(1499, 30);

        if (center == null) return;

        World w = center.getWorld();

        List<Player> playerList = players.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();

        double angleIncrement = (2 * Math.PI) / playerList.size();
        double randomOffset = Math.random() * 2 * Math.PI;

        PotionEffect glowingEffect = new PotionEffect(PotionEffectType.GLOWING, 40, 1, true, true);
        PlayerDataManager playerDataManager = Sandbox.instance.getPlayerDataManager();

        for (int i = 0; i < playerList.size(); i++) {
            Player player = playerList.get(i);
            PlayerData data = playerDataManager.get(player.getUniqueId());
            data.setLastTeleportWasQueue(fastQueue);

            // para colocar todos os jogadores em uma posição circular ao redor de um centro
            double angle = randomOffset + i * angleIncrement;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);

            Location target = getSafeLocationAt(x, z, w);
            if (target == null) continue;

            Vector direction = center.toVector().subtract(target.toVector()).normalize();
            double dx = direction.getX();
            double dz = direction.getZ();
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            yaw = (yaw + 360) % 360;
            target.setYaw(yaw);
            target.setPitch(0f);

            player.teleport(target);
            if (selectedKit == null) {
                giveLastKit(player);
            }  else {
                GrandKits.instance.getKitUtils().loadSomeoneKit(player, null, selectedKit);
            }
            healPlayer(player);

            player.setNoDamageTicks(30);
            player.addPotionEffect(glowingEffect);
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.0f);
        }
    }

    // para pegar o mundo mais vazio.
    // o servidor está com async-world-ticking (1 thread diferente por mundo), então isso ajuda bastante na otimização.
    private World getBestWorld() {
        World bestWorld = null;
        int bestCount = Integer.MAX_VALUE;

        String[] worlds = {"rtp", "rtp1", "rtp2"};

        for (String worldName : worlds) {
            World checkWorld = Bukkit.getWorld(worldName);
            if (checkWorld == null) continue;

            int count = checkWorld.getPlayerCount();
            if (count < bestCount) {
                bestCount = count;
                bestWorld = checkWorld;
            }
        }

        return bestWorld;
    }


    // busca uma posição central segura em um raio específico
    private Location getRandomDryLocation(double maxRadius, int maxTries) {
        World world = getBestWorld();
        Random random = new Random();

        double borderSize = world.getWorldBorder().getSize();
        if (maxRadius > borderSize / 2) maxRadius = borderSize / 2;

        for (int i = 0; i < maxTries; i++) {
            double x = (random.nextDouble() - 0.5) * 2 * maxRadius;
            double z = (random.nextDouble() - 0.5) * 2 * maxRadius;
            int blockX = (int) x;
            int blockZ = (int) z;

            Block highest = world.getHighestBlockAt(blockX, blockZ);
            Material type = highest.getType();

            if (type != Material.WATER &&
                    type != Material.KELP &&
                    type != Material.KELP_PLANT &&
                    type != Material.SEAGRASS &&
                    type != Material.TALL_SEAGRASS) {

                // p evitar sufocamento
                return new Location(world, x, highest.getY(), z).add(0.5, 1, 0.5);
            }
        }

        return null;
    }

    private Location getSafeLocationAt(double x, double z, World world) {
        int y = world.getHighestBlockYAt((int) x, (int) z);
        return new Location(world, x, y, z).add(0, 1, 0);
    }


    private Location getSafeLocationAtLoc(Location loc) {
        World world = getBestWorld();
        int y = world.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());
        loc.setY(y);
        while (!loc.getBlock().isEmpty()) {
            loc.add(0, 1, 0);
        }
        return loc;
    }

    public void giveLastKit(Player player) {
        String[] lastKit = Sandbox.instance.getPlayerDataManager().get(player.getUniqueId()).getLastKit();
        if (lastKit.length < 2) return;
        if (lastKit[1].isEmpty()) return;
        if (lastKit[0].equalsIgnoreCase("null")) {
            GrandKits.instance.getKitUtils().loadSomeoneKit(player, null, lastKit[1]);
        } else {
            GrandKits.instance.getKitUtils().loadSomeoneKit(player, UUID.fromString(lastKit[0]), lastKit[1]);
        }
    }

    public void randomTeleportSingle(Player player)  {
        Location loc = getRandomDryLocation(1499, 15);
        if (loc == null) return;

        PlayerData data = Sandbox.instance.getPlayerDataManager().get(player.getUniqueId());
        data.setLastTeleportWasQueue(false);

        player.teleport(loc);
        giveLastKit(player);
        healPlayer(player);

        player.setNoDamageTicks(30);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.0f);
    }

    public void randomTeleportWithBot(Player player)  {
        Location loc = getRandomDryLocation(1499, 15);
        if (loc == null) return;

        PlayerData data = Sandbox.instance.getPlayerDataManager().get(player.getUniqueId());
        data.setLastTeleportWasQueue(false);

        BotUtils botUtils = Sandbox.instance.getBotUtils();
        botUtils.generateBot(loc);

        player.teleport(loc);
        giveLastKit(player);
        healPlayer(player);

        player.setNoDamageTicks(30);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.0f);
    }

    private void healPlayer (Player p) {
        p.clearActivePotionEffects();
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setSaturation(5);
        p.setFireTicks(0);
    }
}
