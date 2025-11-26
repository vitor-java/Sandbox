package net.olokw.sandbox.events;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import net.olokw.grandkits.api.customEvents.KitLoadEvent;
import net.olokw.grandkits.api.customEvents.PosKitLoadEvent;
import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.configs.PlayerData;
import net.olokw.sandbox.managers.CooldownManager;
import net.olokw.sandbox.managers.QueueManager;
import net.olokw.sandbox.utils.ArenaUtils;
import net.olokw.sandbox.utils.Message;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MainEvents implements Listener {
    @EventHandler
    public void playerLogin(AsyncPlayerPreLoginEvent  e) {
        UUID player = e.getUniqueId();
        Sandbox.instance.getMySQL().loadDataNotAsync(player); // na verdade estara async
    }

    // evento para posicionar o player no spawn, ao entrar no servidor
    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        Location loc = Bukkit.getWorld("spawn").getSpawnLocation().toCenterLocation();
        loc.setY(121.0625);
        Player p = e.getPlayer();
        p.teleport(loc);
        p.getInventory().clear();
        p.clearActivePotionEffects();
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setSaturation(5);
        p.setFireTicks(0);
    }

    // evento para remover o jogador de quaisquer filas, invites, etc + salvar os dados do cachê no banco de dados
    @EventHandler
    public void playerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        QueueManager queueManager = Sandbox.instance.getQueueManager();
        String lastId = queueManager.getQueueIdFromPlayer(uuid);

        if (lastId != null) {
            if (!lastId.contains("#")) {
                queueManager.tryKickingFromQueue(lastId, uuid);
            } else {
                Set<UUID> players = new HashSet<>(queueManager.playersUUIDinQueue(lastId));
                String msg;

                if (lastId.startsWith(p.getName() + "#")) {
                    msg = "<color:#FF1951>\uD83D\uDDE1 <gray>› <white>O organizador debandou a sua fila.";
                } else {
                    msg = "<color:#FF1951>\uD83D\uDDE1 <gray>› <white>O jogador " + p.getName() + " saiu da sua fila.";
                }

                for (UUID uuid1 : players) {
                    Player target = Bukkit.getPlayer(uuid1);
                    if (target != null && target.isOnline()) {
                        target.sendMessage(Message.process(msg));
                    }
                }

                if (lastId.startsWith(p.getName() + "#")) {
                    queueManager.deleteQueue(lastId);
                }
            }
        }

        Sandbox.instance.getInviteManager().expireAllInvitesFromSomeone(uuid);
        Sandbox.instance.getActionBarUtils().stopActionBar(uuid);
        Sandbox.instance.getPlayerDataManager().unloadAndSave(uuid);
    }


    // eventos para que o mundo sandbox não salve nenhuma chunk. Dessa forma, as chunks resetam ao serem descarregadas.
    @EventHandler
    public void disableAutoSave(ChunkUnloadEvent e){
        if (e.getChunk().getWorld().getName().startsWith("rtp")){
            for (Entity entity : e.getChunk().getEntities()) {
                if (!entity.getType().equals(EntityType.PLAYER)){
                    entity.remove();
                }
            }
            e.setSaveChunk(false);
        }
    }

    @EventHandler
    public void disableAutoSaveB(WorldLoadEvent e){
        if (e.getWorld().getName().startsWith("rtp")){
            e.getWorld().setAutoSave(false);
        }
        e.getWorld().setGameRule(GameRule.DO_MOB_SPAWNING, false);
        e.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        e.getWorld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        e.getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        e.getWorld().setDifficulty(Difficulty.HARD);
    }

    // eventos para limitar as 'armor-trim' aos jogadores vips.
    @EventHandler
    public void antiTrim(PlayerInteractEvent e){
        if (e.getPlayer().isOp()) return;
        if (e.getClickedBlock() != null) {
            Material type = e.getClickedBlock().getType();
            if (type.equals(Material.SMITHING_TABLE)) e.setCancelled(true);

            if (type.equals(Material.ANVIL)) {
                if (!e.getPlayer().hasPermission("balacobaco.vip")) {
                    e.setCancelled(true);
                }
            }
        }
    }

    // Evento para enviar uma mensagem/aviso no menu de enfeites.
    @EventHandler
    public void inventoryCloseEvent(InventoryCloseEvent e) {
        if (!e.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) {
            if (e.getView().getTitle().equalsIgnoreCase("Enfeites")) {
                e.getPlayer().sendMessage(Message.process("<aqua>⛏ <gray>› <white>Esse enfeite será aplicado na próxima vez que você carregar algum kit."));
            }
        }
    }


    // evento para carregar o último kit do jogador ao cachê dele + avisar no chat para todos jogadores que estiverem no mesmo mundo.
    // Dessa forma, testes de combate não serão prejudicados por trapaças.

    @EventHandler
    public void kitLoadEvent(KitLoadEvent e){
        String[] kit = new String[2];
        if (e.getOwner() != null) {
            kit[0] = e.getOwner().toString();
        } else {
            kit[0] = "null";
        }
        kit[1] = e.getLoadedKit();
        Sandbox.instance.getPlayerDataManager().get(e.getPlayer().getUniqueId()).setLastKit(kit);
        World world = e.getPlayer().getWorld();
        if (world.getName().startsWith("rtp")) {
            world.getPlayers().forEach(p -> {
                if (p != e.getPlayer()) {
                    p.sendMessage(Message.process("<aqua>⛏ <gray>› <white>" + e.getPlayer().getName() + " carregou o kit " + kit[1] + "."));
                }
            });
        }
    }

    // evento para sobrepor o comando de ignore de outros plugins pelo daqui (plugin core).

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        if (command.startsWith("/ignore")) {
            event.setCancelled(true);
            Bukkit.dispatchCommand(player, command.replace("/ignore", "fodartp:ignore"));
        } else if (command.startsWith("/ignorar")) {
            event.setCancelled(true);
            Bukkit.dispatchCommand(player, command.replace("/ignorar", "fodartp:ignore"));
        } else if (command.startsWith("/k") || command.startsWith("/kit") || command.startsWith("/kits")) {
            CooldownManager cooldownManager = Sandbox.instance.getCooldownManager();
            if (cooldownManager.isInCooldown(player.getUniqueId(), 3)) {
                player.sendMessage(Message.process("<red>⌚ Aguarde " + cooldownManager.getRemainingTimeFormated(player.getUniqueId(), 3) + " para usar isso novamente!"));
                event.setCancelled(true);
            } else {
                cooldownManager.add(player.getUniqueId(), 3, 2);
            }
        }
    }

    // eventos para remover itens ilegais a depender das condições do jogador (caso não seja vip ou caso esteja em áreas nas quais certos itens são ilegais).

    @EventHandler
    public void posKitLoadEvent(PosKitLoadEvent e){
        Player p = e.getPlayer();
        boolean isInArena;
        if (p.getWorld().getName().equalsIgnoreCase("spawn")) {
            Location loc = p.getLocation();
            ArenaUtils arenaUtils = Sandbox.instance.getArenaUtils();
            isInArena = arenaUtils.isInsideArena(loc);
        } else {
            isInArena = false;
        }
        Inventory inventory = p.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                Sandbox.instance.getTrimUtils().checkAndSetTrim(p, item);
                if (isInArena) {
                    if (item.getType() == Material.MACE) {
                        inventory.setItem(i, null);
                    } else {
                        Sandbox.instance.getCheckerUtils().checkAndRemoveBlast(item);
                    }
                }
            }
        }
        p.updateInventory();
    }





    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!player.hasPermission("balacobaco.vip")) return;

        Inventory clickedInventory = e.getClickedInventory();
        Inventory topInventory = e.getView().getTopInventory();

        if (clickedInventory != null && clickedInventory.equals(topInventory)) {

            if (e.getAction() == InventoryAction.PLACE_ALL ||
                    e.getAction() == InventoryAction.PLACE_ONE ||
                    e.getAction() == InventoryAction.PLACE_SOME) {

                ItemStack cursorItem = e.getCursor();
                if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                    Sandbox.instance.getTrimUtils().checkAndRemoveTrim(cursorItem);
                }
            }
        }
    }

    // evento para bloquear redstone

    @EventHandler
    public void redstoneBlocker(BlockRedstoneEvent e){
        if (e.getNewCurrent() != 0) {
            e.setNewCurrent(0);
        }
    }

    // evento para impedir o compartilhamento de itens ilegais ou exclusivos.

    @EventHandler
    public void itemDropEvent(PlayerDropItemEvent e){
        ItemStack item = e.getItemDrop().getItemStack();
        Sandbox.instance.getTrimUtils().checkAndRemoveTrim(item);
        checkRenamed(item);
    }

    private void checkRenamed(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.setDisplayName(null);
        item.setItemMeta(meta);
    }

    // evento para setar o local de morte (caso não tenha sido em um combate da fast-queue) + remover itens exclusivos dos drops.

    @EventHandler
    public void playerDeath(PlayerDeathEvent e){
        Player p = e.getPlayer();
        if (p.getWorld().getName().startsWith("rtp")) {
            PlayerData data = Sandbox.instance.getPlayerDataManager().get(p.getUniqueId());
            if (!data.isLastTeleportWasQueue()) {
                Sandbox.instance.getPlayerDataManager().get(p.getUniqueId()).setBackLocation(p.getLocation());
                p.sendMessage(Message.process("<gold>☠ <gray>› <white>Utilize /back para voltar ao seu local de morte."));
            }
        }
        for (ItemStack item : e.getDrops()) {
            Sandbox.instance.getTrimUtils().checkAndRemoveTrim(item);
            checkRenamed(item);
        }
    }

    // evento para setar o local de respawn no spawn do servidor, sempre.

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e){
        Location loc = Bukkit.getWorld("spawn").getSpawnLocation().toCenterLocation();
        loc.setY(121.0625);
        e.setRespawnLocation(loc);
    }

    // evento para simular um efeito de totem de imortalidade no BOT de treino.

    @EventHandler
    public void npcDamage(EntityDamageEvent e){
        if (e.getEntity().getWorld().getName().startsWith("rtp")){
            if (e.getEntity().getType().equals(EntityType.ZOMBIE)){
                LivingEntity livingEntity = (LivingEntity) e.getEntity();
                livingEntity.setNoDamageTicks(20);
                if (e.getFinalDamage() >= livingEntity.getHealth()){
                    e.setDamage(0);
                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ITEM_TOTEM_USE, 0.5F, 1);
                    e.getEntity().getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, e.getEntity().getLocation().add(0, 1, 0), 50, 0, 0, 0, 0.3);
                    livingEntity.setHealth(4);
                    livingEntity.clearActivePotionEffects();
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 12, 2));
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 40, 0));
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 1));
                }
            }
        }
    }

    // evento para corrigir o knockback do BOT de treino,

    @EventHandler
    public void npcKB(EntityKnockbackEvent e){
        if (e.getEntity().getWorld().getName().startsWith("rtp")){
            if (e.getEntity() instanceof Zombie) {
                Vector v = e.getKnockback();
                v.multiply(0.6);
                v.setY(e.getKnockback().getY() * 1.1);
                e.setKnockback(v);
            }
        }
    }

    // evento para limitar as entidades colocadas em uma mesma chunk, para evitar sistemas de lag.

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent e) {
        Entity entity = e.getEntity();
        if (entity.getType().equals(EntityType.END_CRYSTAL)) return;
        if (entity.getType().equals(EntityType.ITEM_FRAME)) return;
        if (!entity.getWorld().getName().startsWith("rtp")) return;
        Chunk chunk = entity.getLocation().getChunk();
        long count = countEntities(chunk);

        if (count >= 7) {
            entity.remove();
            e.setCancelled(true);
        }
    }

    private long countEntities(Chunk chunk) {
        long count = 0;
        for (Entity e : chunk.getEntities()) {
            if (!(e instanceof Player) && !(e instanceof EnderCrystal) && !(e instanceof Item) && !(e instanceof ExperienceOrb)) {
                count++;
            }
        }
        return count;
    }

    // evento para que os end crystals posicionados não fiquem carregando chunks inativas.

    @EventHandler
    public void onCrystalSpawn(EntitySpawnEvent e) {
        if (e.getEntityType().equals(EntityType.END_CRYSTAL)) {
            EnderCrystal crystal = (EnderCrystal) e.getEntity();
            crystal.setPersistent(false);
        }
    }

    // evento para fazer a "Golden Head" funcionar corretamente, sem deixar frascos, e setando um cooldown.

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e){
        if (e.getItem().getType().equals(Material.HONEY_BOTTLE)) {
            Player p = e.getPlayer();
            if (Sandbox.instance.getCooldownManager().isInCooldown(p.getUniqueId(), 152)) {
                e.setCancelled(true);
                return;
            }
            Sandbox.instance.getCooldownManager().add(p.getUniqueId(), 152, 10);
            Bukkit.getScheduler().runTaskLater(Sandbox.instance, () -> {
                p.getInventory().removeItem(new ItemStack(Material.GLASS_BOTTLE));
            }, 1);
            p.setCooldown(Material.HONEY_BOTTLE,  200);

        }
    }



}
