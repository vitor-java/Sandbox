package net.olokw.sandbox.commands;

import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.managers.InviteManager;
import net.olokw.sandbox.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Tpa implements CommandExecutor, TabCompleter {

    private final List<String> LIST = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            if (args.length >= 1) {
                InviteManager inviteManager = Sandbox.instance.getInviteManager();
                String nickname = args[0];
                Player invited = Bukkit.getPlayer(nickname);
                if (invited == null) {
                    p.sendMessage(Message.process("<blue>\uD83D\uDDE1 <gray>› <red>Jogador não encontrado!"));
                    return true;
                }
                UUID uuid = invited.getUniqueId();
                inviteManager.sendInvite(0, p.getUniqueId(), uuid, null);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

        if (args.length > 1) {
            if (sender instanceof Player) {
                LIST.clear();
            }
        }

        if (args.length == 1) {
            if (sender instanceof Player p1) {
                LIST.clear();
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (p != p1){
                        LIST.add(p.getName());
                    }
                });
            }
        }

        return StringUtil.copyPartialMatches(args[args.length - 1], LIST, new ArrayList<>());
    }
}
