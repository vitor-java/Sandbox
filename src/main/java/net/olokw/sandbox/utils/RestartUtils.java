package net.olokw.sandbox.utils;

import net.olokw.sandbox.Sandbox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;

public class RestartUtils {
    private SimpleDateFormat dateFormat;
    private boolean isRestarting = false;

    public void startRestartChecker() {
        dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

        int secondsUntilNextMinute = 60 - LocalTime.now().getSecond();
        long initialDelay = secondsUntilNextMinute * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                checkRestart();
            }
        }.runTaskTimerAsynchronously(Sandbox.instance, initialDelay, 20 * 60);

    }

    private void checkRestart() {
        Set<String> dates = Sandbox.instance.getPluginConfig().getRestartHours();
        for (String date : dates) {

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 10);
            String formattedDate = dateFormat.format(calendar.getTime());

            if (date.equalsIgnoreCase(formattedDate)) {
                restartServer10minBefore();
            }
        }

    }


    public void restartServer10minBefore() {

        Bukkit.getOnlinePlayers().forEach(p -> {
            p.sendMessage(Message.process("\n<color:#ffbb00>\uD83D\uDD14</color> O servidor vai reiniciar em 10 minutos!\n"));
        });

        Bukkit.getScheduler().runTaskLater(Sandbox.instance, () ->  {
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.sendMessage(Message.process("\n<color:#ffbb00>\uD83D\uDD14</color> O servidor vai reiniciar em 1 minuto!\n"));
            });
        }, 20 * 60 * 9);

        Bukkit.getScheduler().runTaskLater(Sandbox.instance, () ->  {
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.sendMessage(Message.process("\n<color:#ffbb00>\uD83D\uDD14</color> O servidor vai reiniciar em 10 segundos!\n"));
            });
        }, 20 * 60 * 9 + 50);

        Bukkit.getScheduler().runTaskLater(Sandbox.instance, this::realRestart, 20 * 60 * 10);


    }

    public void realRestart() {
        isRestarting = true;
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.kick(Message.process("<red>Servidor reiniciando!"));
        }

        Set<Location> locs = Sandbox.instance.getResetManager().getBlocksLocationsAndClearSet();
        for (Location loc : locs) {
            loc.getBlock().setType(Material.AIR);
        }

        Bukkit.getScheduler().runTaskLater(Sandbox.instance, () -> {
            Bukkit.getServer().restart();
        }, 20 * 20);
    }

    public boolean isRestarting() {
        return isRestarting;
    }
}
