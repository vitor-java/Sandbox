package net.olokw.sandbox.managers;

import net.olokw.sandbox.configs.InviteConfig;
import net.olokw.sandbox.utils.Message;
import org.bukkit.Bukkit;

import java.util.*;


public class InviteManager {
    private final Map<UUID, Set<InviteConfig>> pedidos;

    public InviteManager() {
        pedidos = new HashMap<>();
    }

    public void sendInvite(int type, UUID sender, UUID invited, String kit) {
        if (Bukkit.getPlayer(invited) == null) {
            Bukkit.getPlayer(sender).sendMessage(Message.process("<blue>\uD83D\uDDE1 <gray>› <red>Esse jogador está offline!"));
            return;
        }
        if (sender.equals(invited)) {
            Bukkit.getPlayer(sender).sendMessage(Message.process("<blue>\uD83D\uDDE1 <gray>› <red>Você não pode enviar um pedido para si mesmo!"));
            return;
        }
        if (getInvite(type, sender, invited) != null) {
            Bukkit.getPlayer(sender).sendMessage(Message.process("<blue>\uD83D\uDDE1 <gray>› <red>Você já tem um pedido ativo para esse jogador!"));
            return;
        }
        InviteConfig inviteConfig = new InviteConfig(type, invited, sender, kit);
        if (!pedidos.containsKey(sender)) {
            Set<InviteConfig> invites = new HashSet<>();
            invites.add(inviteConfig);
            pedidos.put(sender, invites);
        } else {
            pedidos.get(sender).add(inviteConfig);
        }
        inviteConfig.sendInvite();
    }

    public void expireInvite(InviteConfig config) {
        UUID sender = config.getSender();
        if (pedidos.containsKey(sender)) {
            if (pedidos.get(sender).size() == 1) {
                pedidos.remove(sender);
            } else {
                pedidos.get(sender).remove(config);
            }
        }
    }

    public void expireAllInvitesFromSomeone(UUID uuid) {
        if (pedidos.containsKey(uuid)) {
            for (InviteConfig config : pedidos.get(uuid)) {
                config.getTask().cancel();
            }
        }
        pedidos.remove(uuid);
    }

    private InviteConfig getInvite(int type, UUID sender, UUID invited) {
        if (pedidos.containsKey(sender)) {
            for (InviteConfig config : pedidos.get(sender)) {
                if (config.getType() == type && config.getInvited().equals(invited)) {
                    return config;
                }
            }
        }
        return null;
    }

    public void denyInvite(int type, UUID sender, UUID invited) {
        if (pedidos.containsKey(sender)) {
            InviteConfig config = getInvite(type, sender, invited);

            if (config != null) {
                if (pedidos.get(sender).size() == 1) {
                    pedidos.remove(sender);
                } else {
                    pedidos.get(sender).remove(config);
                }
                config.denyInvite();
            } else {
                Bukkit.getPlayer(sender).sendMessage(Message.process("<red>\uD83D\uDDE1 › Pedido não encontrado!"));
            }
        }
    }

    public void acceptInvite(int type, UUID sender, UUID invited) {
        if (pedidos.containsKey(sender)) {
            InviteConfig config = getInvite(type, sender, invited);
            if (config != null) {
                if (pedidos.get(sender).size() == 1) {
                    pedidos.remove(sender);
                } else {
                    pedidos.get(sender).remove(config);
                }
                config.acceptInvite();
            } else {
                Bukkit.getPlayer(sender).sendMessage(Message.process("<red>\uD83D\uDDE1 › Pedido não encontrado!"));
            }
        }
    }


}
