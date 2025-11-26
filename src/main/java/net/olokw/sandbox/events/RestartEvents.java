package net.olokw.sandbox.events;

import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.utils.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class RestartEvents implements Listener {
    // evento para impedir a entrada de jogadores em momento de reinício do servidor.
    @EventHandler
    public void onJoin(PlayerLoginEvent e){
        if (Sandbox.instance.getRestartUtils().isRestarting()) {
            e.disallow(PlayerLoginEvent.Result.KICK_FULL, Message.process("<red>Servidor reiniciando!"));
        }
    }
}
