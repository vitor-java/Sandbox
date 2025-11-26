package net.olokw.sandbox.utils;


import net.olokw.sandbox.Sandbox;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BotUtils {

    private final Set<UUID> bots = new HashSet<>();
    private ItemStack[] equipment;
    private ItemStack offhand;

    public BotUtils() {
        ItemStack capacete = new ItemStack(Material.NETHERITE_HELMET);
        ItemStack peitoral = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemStack calca = new ItemStack(Material.NETHERITE_LEGGINGS);
        ItemStack bota = new ItemStack(Material.NETHERITE_BOOTS);
        ItemMeta metaCapacete = capacete.getItemMeta();
        metaCapacete.addEnchant(Enchantment.PROTECTION, 4, true);
        metaCapacete.setUnbreakable(true);
        capacete.setItemMeta(metaCapacete);
        ItemMeta metaPeitoral = peitoral.getItemMeta();
        metaPeitoral.addEnchant(Enchantment.PROTECTION, 4, true);
        metaPeitoral.setUnbreakable(true);
        peitoral.setItemMeta(metaPeitoral);
        ItemMeta metaCalca = calca.getItemMeta();
        metaCalca.addEnchant(Enchantment.BLAST_PROTECTION, 4, true);
        metaCalca.setUnbreakable(true);
        calca.setItemMeta(metaCalca);
        ItemMeta metaBota = bota.getItemMeta();
        metaBota.addEnchant(Enchantment.FEATHER_FALLING, 4, true);
        metaBota.addEnchant(Enchantment.PROTECTION, 4, true);
        metaBota.setUnbreakable(true);
        bota.setItemMeta(metaBota);
        equipment = new ItemStack[] {
                bota,
                calca,
                peitoral,
                capacete
        };
        offhand = new ItemStack(Material.TOTEM_OF_UNDYING);
    }


    public void generateBot(org.bukkit.Location loc) {
        Zombie zombie = loc.getWorld().spawn(loc, Zombie.class, z -> {
            z.setSilent(true);
            z.setAggressive(false);
            z.setAware(false);
            z.setAdult();
            z.getEquipment().setArmorContents(equipment);
            z.getEquipment().setItemInOffHand(offhand);
        });

        UUID uuid = zombie.getUniqueId();
        bots.add(zombie.getUniqueId());

        new BukkitRunnable() {
            @Override
            public void run() {
                Zombie z = (Zombie) Bukkit.getEntity(uuid);
                if (z == null || z.isDead()) {
                    bots.remove(uuid);
                    cancel();
                    return;
                }

                // checar players no mesmo mundo num raio de 50 blocos
                boolean found = false;
                double maxDist = 50 * 50; // distanceSquared

                for (Player p : z.getWorld().getPlayers()) {
                    if (p.getLocation().distanceSquared(z.getLocation()) <= maxDist) {
                        found = true;
                        break;
                    }
                }

                // se não encontrou nenhum player, remove o bot
                if (!found) {
                    z.remove();
                    bots.remove(uuid);
                    cancel();
                }
            }
        }.runTaskTimer(Sandbox.instance, 1200, 1200);
    }

    public Set<UUID> getBots() {
        return bots;
    }
}
