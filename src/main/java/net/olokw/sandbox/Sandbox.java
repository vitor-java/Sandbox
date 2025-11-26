package net.olokw.sandbox;

import net.olokw.sandbox.commands.*;
import net.olokw.sandbox.configs.PluginConfig;
import net.olokw.sandbox.events.Listeners;
import net.olokw.sandbox.managers.*;
import net.olokw.sandbox.placeholder.KillsExpansion;
import net.olokw.sandbox.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class Sandbox extends JavaPlugin {
    public static Sandbox instance;
    private ArenaUtils arenaUtils;
    private ResetManager resetManager;
    private KillsExpansion killsExpansion;
    private ConfigLoader configLoader;
    private MySQL mySQL;
    private PlayerDataManager playerDataManager;
    private TeleportUtils teleportUtils;
    private ActionBarUtils actionBarUtils;
    private QueueManager queueManager;
    private CooldownManager cooldownManager;
    private TrimUtils trimUtils;
    private InviteManager inviteManager;
    private RestartUtils restartUtils;
    private CheckerUtils checkerUtils;
    private X1Utils x1Utils;
    private PluginConfig pluginConfig;
    private BotUtils botUtils;
    private GCUtils gcUtils;


    @Override
    public void onEnable() {

        instance = this;
        configLoader = new ConfigLoader();
        configLoader.load();

        teleportUtils = new TeleportUtils();
        arenaUtils = new ArenaUtils();
        resetManager = new ResetManager();
        killsExpansion = new KillsExpansion();
        playerDataManager = new PlayerDataManager(mySQL);
        actionBarUtils = new ActionBarUtils();
        cooldownManager = new CooldownManager();
        trimUtils = new TrimUtils();
        x1Utils = new X1Utils();
        inviteManager = new InviteManager();
        restartUtils = new RestartUtils();
        checkerUtils = new CheckerUtils();
        botUtils = new BotUtils();
        gcUtils = new GCUtils();
        queueManager = new QueueManager(teleportUtils, playerDataManager, actionBarUtils);



        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            killsExpansion.register();
        }

        this.getCommand("queue").setExecutor(new Queue(queueManager));
        this.getCommand("ignorar").setExecutor(new Ignore());
        this.getCommand("rtp").setExecutor(new Rtp());
        this.getCommand("spawn").setExecutor(new Spawn());
        this.getCommand("arena").setExecutor(new Arena());
        this.getCommand("trim").setExecutor(new Trim());
        this.getCommand("tpa").setExecutor(new Tpa());
        this.getCommand("tpaccept").setExecutor(new Tpaccept());
        this.getCommand("tpdeny").setExecutor(new Tpdeny());
        this.getCommand("x1").setExecutor(new X1());
        this.getCommand("back").setExecutor(new Back());
        this.getCommand("bot").setExecutor(new Bot());
        this.getCommand("restartnow").setExecutor(new RestartNow());

        Listeners.register();
        for (Player p : Bukkit.getOnlinePlayers()){
            mySQL.loadData(p.getUniqueId());
        }

        restartUtils.startRestartChecker();
        gcUtils.startGarbageCollector();

        AnnounceUtils announceUtils = new AnnounceUtils();
        announceUtils.startAnnounces();

    }

    @Override
    public void onDisable() {

        for (Player p : Bukkit.getOnlinePlayers()){
            mySQL.saveDataNotAsync(p.getUniqueId());
        }

        for (UUID botUUID : botUtils.getBots()) {
            Entity e = Bukkit.getEntity(botUUID);
            if (e != null) e.remove();
        }

        for (Location loc : resetManager.getBlocksLocationsAndClearSet()) {
            loc.getBlock().setType(Material.AIR);
        }

        mySQL.close();

    }

    public InviteManager getInviteManager() {
        return inviteManager;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public TeleportUtils getTeleportUtils() {
        return teleportUtils;
    }

    public ActionBarUtils getActionBarUtils() {
        return actionBarUtils;
    }

    public void setMySQL(MySQL mySQL) {
        this.mySQL = mySQL;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ArenaUtils getArenaUtils() {
        return arenaUtils;
    }

    public ResetManager getResetManager() {
        return resetManager;
    }

    public TrimUtils getTrimUtils() {
        return trimUtils;
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public void setPluginConfig(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    public RestartUtils getRestartUtils() {
        return restartUtils;
    }

    public CheckerUtils getCheckerUtils() {
        return checkerUtils;
    }

    public X1Utils getX1Utils() {
        return x1Utils;
    }

    public BotUtils getBotUtils() {
        return botUtils;
    }
}
