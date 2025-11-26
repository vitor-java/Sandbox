package net.olokw.sandbox.events;

import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.managers.ResetManager;
import net.olokw.sandbox.utils.ArenaUtils;
import net.olokw.sandbox.utils.CheckerUtils;
import net.olokw.sandbox.utils.Message;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ArenaEvents implements Listener {

    private final Map<UUID, BukkitTask> noFallDamageTaggeds = new HashMap<>();
    private final Set<Material> blockedBlocks = new HashSet<>();
    private final Set<Material> blockedItems = new HashSet<>();

    public ArenaEvents() {
        Collections.addAll(blockedBlocks, Material.RESPAWN_ANCHOR, Material.ANVIL, Material.TNT, Material.COBWEB);
        Collections.addAll(blockedItems, Material.WATER_BUCKET, Material.LAVA_BUCKET, Material.TNT_MINECART);
    }

    // métodos para remover blocos quebrados do resetmanager + bloquear a quebra de blocos que são protegidos.

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!e.getBlock().getLocation().getWorld().getName().equalsIgnoreCase("spawn")) return;
        ResetManager resetManager = Sandbox.instance.getResetManager();

        Block block = e.getBlock();
        Location blockLoc = block.getLocation();
        Player player = e.getPlayer();


        if (!resetManager.checkBlockLocAndRemove(blockLoc)) {
            if (!player.isOp() || player.getGameMode() == GameMode.SURVIVAL) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void BlockExplodeA(BlockExplodeEvent e){
        if (!e.getBlock().getLocation().getWorld().getName().equalsIgnoreCase("spawn")) return;
        ResetManager resetManager = Sandbox.instance.getResetManager();
        Iterator it = e.blockList().iterator();
        while (it.hasNext()){
            Block block = (Block) it.next();
            if (!resetManager.checkBlockLocAndRemove(block.getLocation())){
                it.remove();
            } else {
                resetManager.addBlockLoc(block.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void BlockExplodeB(EntityExplodeEvent e){
        if (!e.getEntity().getLocation().getWorld().getName().equalsIgnoreCase("spawn")) return;
        ResetManager resetManager = Sandbox.instance.getResetManager();
        Iterator it = e.blockList().iterator();
        while (it.hasNext()){
            Block block = (Block) it.next();
            if (!resetManager.checkBlockLocAndRemove(block.getLocation())){
                it.remove();
            } else {
                resetManager.addBlockLoc(block.getLocation());
            }
        }
    }

    // método para adicionar blocos posicionados por jogadores no ResetManager + bloquear blocos específicos.

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!e.getBlock().getLocation().getWorld().getName().equalsIgnoreCase("spawn")) return;
        Player player = e.getPlayer();

        Material type = e.getBlock().getType();
        if (blockedBlocks.contains(type)){
            player.sendMessage(Message.process("<red>⚠ › Você não pode usar isso na arena plana!"));
            e.setCancelled(true);
            return;
        }

        if (!player.isOp() || player.getGameMode().equals(GameMode.SURVIVAL)) {

            if (e.getBlock().getType().equals(Material.CRAFTER)) {
                e.setCancelled(true);
                return;
            }

            Location location = e.getBlock().getLocation();
            ArenaUtils arenaUtils = Sandbox.instance.getArenaUtils();

            if (arenaUtils.isInsideArena(location)) {
                Sandbox.instance.getResetManager().addBlockLoc(location);
            } else {
                e.setCancelled(true);
            }
        }
    }

    // método para carregar o último kit automáticamente ao jogador + remover itens ilegais a depender da localização do jogador.

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        if (!e.getTo().getWorld().getName().equalsIgnoreCase("spawn")) return;
        ArenaUtils arenaUtils = Sandbox.instance.getArenaUtils();
        if (!arenaUtils.isInsideArena(e.getFrom())) {
            if (arenaUtils.isInsideArena(e.getTo())) {
                Player p = e.getPlayer();
                UUID uuid = p.getUniqueId();

                BukkitTask task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (isCancelled()) {
                            return;
                        }
                        noFallDamageTaggeds.remove(uuid);
                        cancel();
                    }
                }.runTaskLaterAsynchronously(Sandbox.instance, 20 * 10);

                if (noFallDamageTaggeds.containsKey(uuid)) {
                    noFallDamageTaggeds.get(uuid).cancel();
                } else {
                    noFallDamageTaggeds.put(p.getUniqueId(), task);
                }


                if (!isWearingArmor(p)) {
                    Sandbox.instance.getTeleportUtils().giveLastKit(p);
                    healPlayer(p);
                    p.playSound(p, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1, 1);
                    Inventory inventory = e.getPlayer().getInventory();
                    for (ItemStack item : e.getPlayer().getInventory()) {
                        if (item != null){
                            if (item.getType().equals(Material.MACE)) {
                                inventory.remove(item);
                            } else {
                                CheckerUtils checkerUtils = Sandbox.instance.getCheckerUtils();
                                checkerUtils.checkAndRemoveBlast(item);
                            }
                        }
                    }
                }
                Inventory inventory = e.getPlayer().getInventory();
                for (ItemStack item : e.getPlayer().getInventory()) {
                    if (item != null){
                        if (item.getType().equals(Material.MACE)) {
                            inventory.remove(item);
                        } else {
                            CheckerUtils checkerUtils = Sandbox.instance.getCheckerUtils();
                            checkerUtils.checkAndRemoveBlast(item);
                        }
                    }
                }

            }

        } else {
            if (e.getTo().getY() < 0) {
                Player p = e.getPlayer();
                Location loc = Bukkit.getWorld("spawn").getSpawnLocation().toCenterLocation();
                loc.setY(121.0625);
                p.teleport(loc);
                healPlayer(p);
                p.getInventory().clear();
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            }
        }
    }

    // método para curar o jogador.

    private void healPlayer (Player p) {
        p.clearActivePotionEffects();
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setSaturation(5);
        p.setFireTicks(0);
    }

    private boolean isWearingArmor(Player p) {
        for (ItemStack item : p.getInventory().getArmorContents()) {
            if (item != null && item.getType() != Material.AIR) return true;
        }
        return false;
    }

    // método para evitar o primeiro fall damage quando o jogador pula na arena.

    @EventHandler
    public void antiFallDamage(EntityDamageEvent e){
        if (e.getEntity() instanceof Player p) {
            UUID uuid = p.getUniqueId();
            if (noFallDamageTaggeds.containsKey(uuid)) {
                if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                    e.setCancelled(true);
                    noFallDamageTaggeds.get(uuid).cancel();
                    noFallDamageTaggeds.remove(uuid);
                }
            }
        }
    }

    // método para remover cristais do fim após um período de tempo

    @EventHandler
    public void EntitySpawn(EntityPlaceEvent e){
        if (!e.getEntity().getWorld().getName().equalsIgnoreCase("spawn")) return;
        if (!e.getEntity().getType().equals(EntityType.END_CRYSTAL)) {
            e.setCancelled(true);
            return;
        }
        
        Entity entity = e.getEntity();

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Sandbox.instance, new Runnable(){
            public void run(){
                if (entity.isDead()) return;
                Location loc = e.getEntity().getLocation();
                loc.add(0, 0.5, 0);
                entity.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.1, 0.1, 0.1, 0);
                entity.remove();
            }
        }, 30 * 20);
    }

    // método para bloquear danos no spawn

    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if (e.getEntity().getWorld().getName().equalsIgnoreCase("spawn")) {
            Location location = e.getEntity().getLocation();
            if (!Sandbox.instance.getArenaUtils().isInsideArena(location)) {
                e.setCancelled(true);
            }
        }
    }

    // método para bloquear fome no spawn

    @EventHandler
    public void antiHunger(FoodLevelChangeEvent e){
        if (e.getEntity().getWorld().getName().equalsIgnoreCase("spawn")) {
            Location location = e.getEntity().getLocation();
            if (!Sandbox.instance.getArenaUtils().isInsideArena(location)) {
                e.setCancelled(true);
            }
        }
    }

    // método para bloquear interações no spawn

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        if (e.getPlayer().isOp()) return;
        if (e.getClickedBlock() != null) {
            if (!e.getClickedBlock().getType().equals(Material.ENDER_CHEST)){
                if (!Sandbox.instance.getArenaUtils().isInsideArena(e.getClickedBlock().getLocation())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    // método para bloquear uso de certos itens na arena plana.

    @EventHandler
    public void onItemUse(PlayerInteractEvent e){
        if (e.getItem() == null) return;
        if (!e.getAction().isRightClick()) return;
        Material material = e.getItem().getType();
        if (blockedItems.contains(material)){
            Player p = e.getPlayer();
            if (p.getWorld().getName().equalsIgnoreCase("spawn")) {
                p.sendMessage(Message.process("<red>⚠ › Você não pode usar isso na arena plana!"));
                e.setCancelled(true);
            }
        }
    }

    // método para carregar o último kit ao adentrar a arena e bloquear itens ilegais.

    @EventHandler
    public void onPearl(PlayerTeleportEvent e) {
        if (!e.getTo().getWorld().getName().equalsIgnoreCase("spawn")) return;
        ArenaUtils arenaUtils = Sandbox.instance.getArenaUtils();
        if (!arenaUtils.isInsideArena(e.getFrom())) {
            if (arenaUtils.isInsideArena(e.getTo())) {
                Player p = e.getPlayer();
                if (!isWearingArmor(p)) {
                    Sandbox.instance.getTeleportUtils().giveLastKit(p);
                    healPlayer(p);
                    p.playSound(p, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1, 1);

                }
                Inventory inventory = e.getPlayer().getInventory();
                for (ItemStack item : e.getPlayer().getInventory()) {
                    if (item != null){
                        if (item.getType().equals(Material.MACE)) {
                            inventory.remove(item);
                        } else {
                            CheckerUtils checkerUtils = Sandbox.instance.getCheckerUtils();
                            checkerUtils.checkAndRemoveBlast(item);
                        }
                    }
                }
            }
        }
    }

    // métodos para bloquear o uso de peças de armadura com blast protection, ao invés de protection, caso não sejam a calça.
    // Dessa forma, nenhum jogador ficará sem tomar knockback de explosões na arena plana.

    @EventHandler
    public void onArmorEquip(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        if (!isPlayerInArena(player)) return;

        ItemStack armorItem = null;

        int slot = e.getSlot();
        ClickType click = e.getClick();

        if (click == ClickType.NUMBER_KEY) {
            int hotbarSlot = e.getHotbarButton();
            ItemStack fromHotbar = player.getInventory().getItem(hotbarSlot);
            if (slot >= 36 && slot <= 39) {
                if (fromHotbar != null && isArmor(fromHotbar.getType())) {
                    armorItem = fromHotbar;
                }
            }
        } else if (e.isShiftClick()) {
            ItemStack clicked = e.getCurrentItem();
            if (clicked != null && isArmor(clicked.getType())) {
                armorItem = clicked;
            }
        } else if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
            ItemStack cursorItem = e.getCursor();
            if (cursorItem != null && isArmor(cursorItem.getType())) {
                armorItem = cursorItem;
            }
        }

        if (armorItem != null) {
            Sandbox.instance.getCheckerUtils().checkAndRemoveBlast(armorItem);
        }
    }


    private boolean isArmor(Material material) {
        return material.name().endsWith("_HELMET")
                || material.name().endsWith("_CHESTPLATE")
                || material.name().endsWith("_LEGGINGS")
                || material.name().endsWith("_BOOTS");
    }

    // evento para bloquear wind charge

    @EventHandler
    public void onPlayerInteractB(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null || item.getType() == Material.AIR) return;
        Player p = e.getPlayer();
        if (p.getWorld().getName().equalsIgnoreCase("spawn")) {
            if (Sandbox.instance.getArenaUtils().isInsideArena(p.getLocation())) {
                Sandbox.instance.getCheckerUtils().checkAndRemoveBlast(item);
            } else if (item.getType().equals(Material.WIND_CHARGE)) {
                e.setCancelled(true);
            }
        }
    }

    // evento para bloquear a "pesca" de outros jogadores na região do spawn.

    @EventHandler
    public void onFish(PlayerFishEvent e){
        if (e.getCaught() instanceof Player p)

            if (p.getWorld().getName().equalsIgnoreCase("spawn")) {
                if (!Sandbox.instance.getArenaUtils().isInsideArena(p.getLocation())) {
                    e.setCancelled(true);
                }
            }
    }

    // evento para bloquear a "pesca" de outros jogadores na região do spawn.

    private boolean isPlayerInArena(Player player) {
        if (!player.getWorld().getName().equalsIgnoreCase("spawn")) {
            return false;
        }
        ArenaUtils arenaUtils = Sandbox.instance.getArenaUtils();
        return arenaUtils.isInsideArena(player.getLocation());
    }

    // evento para bloquear o uso de maces na arena plana do spawn.

    @EventHandler
    public void maceSwing(EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof Player p) {
            if (isPlayerInArena(p)) {
                PlayerInventory inventory = p.getInventory();
                ItemStack item = inventory.getItemInMainHand();
                if (item.getType().equals(Material.MACE)) {
                    inventory.remove(item);
                    e.setCancelled(true);
                }
            }
        }
    }

    // evento para remover itens jogados no chão, no spawn.

    @EventHandler
    public void itemDropEvent(PlayerDropItemEvent e){
        if (e.getPlayer().getWorld().getName().equalsIgnoreCase("spawn")) {
            e.getItemDrop().remove();
        }
    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent e){
        if (e.getPlayer().getWorld().getName().equalsIgnoreCase("spawn")) {
            e.getDrops().clear();
        }

    }





}
