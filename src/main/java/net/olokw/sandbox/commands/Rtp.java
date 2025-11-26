package net.olokw.sandbox.commands;

import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.managers.CooldownManager;
import net.olokw.sandbox.utils.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Rtp implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            UUID uuid = p.getUniqueId();
            CooldownManager cooldownManager = Sandbox.instance.getCooldownManager();
            if (cooldownManager.isInCooldown(uuid, 2)) {
                p.sendMessage(Message.process("<red>⌚ Aguarde " + cooldownManager.getRemainingTimeFormated(uuid, 2) + " para usar isso novamente!"));
                return true;
            }
            Sandbox.instance.getTeleportUtils().randomTeleportSingle(p);
            cooldownManager.add(uuid, 2, 3);
        }
        return true;
    }
}
