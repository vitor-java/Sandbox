package net.olokw.sandbox.commands;

import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.configs.PlayerData;
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

public class Ignore implements CommandExecutor, TabCompleter {

    private final List<String> LIST = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            if (args.length >= 1) {
                PlayerData playerData = Sandbox.instance.getPlayerDataManager().get(p.getUniqueId());
                UUID target = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
                if (playerData.isIgnoring(target)){
                    playerData.removeIgnored(target);
                    p.sendMessage(Message.process("<aqua>⛓ <gray>› <white>Você removeu " + args[0] + " da lista de ignorados."));
                } else {
                    playerData.addIgnored(target);
                    p.sendMessage(Message.process("<aqua>⛓ <gray>› <white>Você adicionou " + args[0] + " na lista de ignorados."));
                }
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
            if (sender instanceof Player) {
                LIST.clear();
                Bukkit.getOnlinePlayers().forEach(p -> {
                    LIST.add(p.getName());
                });
            }
        }

        return StringUtil.copyPartialMatches(args[args.length - 1], LIST, new ArrayList<>());
    }
}
