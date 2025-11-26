package net.olokw.sandbox.configs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.utils.Message;
import net.olokw.sandbox.utils.X1Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InviteConfig {
    private BukkitTask task;
    private UUID invited;
    private UUID sender;
    private int type;
    private InviteConfig inviteConfig;
    private String kit;

    public InviteConfig(int type, UUID invited, UUID sender, String kit) {
        this.task = task;
        this.invited = invited;
        this.sender = sender;
        this.type = type;
        this.kit = kit;
        inviteConfig = this;
    }

    private Component getTypeMessage(int type) {
        Component msg;
        String senderName = Bukkit.getPlayer(sender).getName();
        switch (type) {
            case 0 -> {

                Component acceptPart = Component.text("\nACEITAR")
                        .hoverEvent(HoverEvent.showText(Message.process("<gray>Clique aqui para aceitar.")))
                        .clickEvent(ClickEvent.runCommand("/tpaccept " + senderName))
                        .color(NamedTextColor.GREEN);

                Component middle = Component.text(" | ")
                        .color(NamedTextColor.GRAY);

                Component denyPart = Component.text("RECUSAR\n")
                        .hoverEvent(HoverEvent.showText(Message.process("<gray>Clique aqui para recusar.")))
                        .clickEvent(ClickEvent.runCommand("/tpadeny " + senderName))
                        .color(NamedTextColor.RED);


                return Message.process("\n<blue>\uD83D\uDDE1 <gray>› <white>O jogador " + senderName + " te enviou um pedido de teleporte. ").append(acceptPart).append(middle).append(denyPart);
            }
            case 1 -> {

                Component acceptPart = Component.text("\nACEITAR")
                        .hoverEvent(HoverEvent.showText(Message.process("<gray>Clique aqui para aceitar.")))
                        .clickEvent(ClickEvent.runCommand("/x1 aceitar " + senderName))
                        .color(NamedTextColor.GREEN);

                Component middle = Component.text(" | ")
                        .color(NamedTextColor.GRAY);

                Component denyPart = Component.text("RECUSAR\n")
                        .hoverEvent(HoverEvent.showText(Message.process("<gray>Clique aqui para recusar.")))
                        .clickEvent(ClickEvent.runCommand("/x1 recusar " + senderName))
                        .color(NamedTextColor.RED);

                if (kit == null) {
                    return Message.process("\n<blue>\uD83D\uDDE1 <gray>› <white>O jogador " + senderName + " te desafiou para um X1. ").append(acceptPart).append(middle).append(denyPart);
                } else {
                    X1Utils x1Utils = Sandbox.instance.getX1Utils();
                    return Message.process("\n<blue>\uD83D\uDDE1 <gray>› <white>O jogador " + senderName + " te desafiou para um X1 com o kit ").append(x1Utils.getKitFormattedName(kit)).append(Message.process(".")).append(acceptPart).append(middle).append(denyPart);
                }

            }
        }
        return null;
    }

    // 0 -> aceitou
    // 1 -> recusou
    // 2 -> expirou

    private Component getSendMessage(int type) {
        Component msg;
        String invitedName = Bukkit.getPlayer(invited).getName();
        switch (type) {
            case 0 -> {
                return Message.process("<blue>\uD83D\uDDE1 <gray>› <white>Você enviou um pedido de TPA para o jogador " + invitedName + ".");
            }
            case 1 -> {
                if (kit == null) {
                    return Message.process("<blue>\uD83D\uDDE1 <gray>› <white>Você desafiou " + invitedName + " para um X1.");
                } else {
                    X1Utils x1Utils = Sandbox.instance.getX1Utils();
                    return Message.process("<blue>\uD83D\uDDE1 <gray>› <white>Você desafiou " + invitedName + " para um X1 com o kit ").append(x1Utils.getKitFormattedName(kit)).append(Message.process("."));
                }
            }
        }
        return null;
    }

    private Component getResultMessage(int type, int result) {
        Component msg;
        String invitedName = Bukkit.getPlayer(invited).getName();
        switch (type) {
            case 0 -> {
                if (result == 0) {
                    return Message.process("<blue>\uD83D\uDDE1 <gray>› <green>O jogador " + invitedName + " aceitou seu pedido de teleporte.");
                } else if (result == 1) {
                    return Message.process("<blue>\uD83D\uDDE1 <gray>› <red>O jogador " + invitedName + " recusou seu pedido de teleporte.");
                } else {
                    return Message.process("<blue>\uD83D\uDDE1 <gray>› <red>O seu pedido de teleporte para o jogador " + invitedName + " expirou.");
                }
            }
            case 1 -> {
                if (kit == null) {
                    if (result == 0) {
                        return Message.process("<blue>\uD83D\uDDE1 <gray>› <green>O jogador " + invitedName + " aceitou seu convite de X1.");
                    } else if (result == 1) {
                        return Message.process("<blue>\uD83D\uDDE1 <gray>› <red>O jogador " + invitedName + " recusou seu convite de X1.");
                    } else {
                        return Message.process("<blue>\uD83D\uDDE1 <gray>› <red>O seu convite de X1 para o jogador " + invitedName + " expirou.");
                    }
                } else {
                    if (result == 0) {
                        return Message.process("<blue>\uD83D\uDDE1 <gray>› <green>O jogador " + invitedName + " aceitou seu convite de X1.");
                    } else if (result == 1) {
                        return Message.process("<blue>\uD83D\uDDE1 <gray>› <red>O jogador " + invitedName + " recusou seu convite de X1.");
                    } else {
                        return Message.process("<blue>\uD83D\uDDE1 <gray>› <red>O seu convite de X1 para o jogador " + invitedName + " expirou.");
                    }
                }
            }
        }
        return null;
    }

    private Component getResultMessageForInvited(int type, int result) {
        Component msg;
        String senderName = Bukkit.getPlayer(sender).getName();
        switch (type) {
            case 0 -> {
                if (result == 0) {
                    return Message.process("<blue>\uD83D\uDDE1 <gray>› <green>Você aceitou o pedido de teleporte de " + senderName + ".");
                } else if (result == 1) {
                    return Message.process("<blue>\uD83D\uDDE1 <gray>› <red>Você recusou o pedido de teleporte de " + senderName + ".");
                } else {
                    return Message.process("<blue>\uD83D\uDDE1 <gray>› <red>O pedido de teleporte de " + senderName + " expirou.");
                }
            }
            case 1 -> {
                if (result == 0) {
                    return Message.process("<blue>\uD83D\uDDE1 <gray>› <green>Você aceitou o convite de X1 de " + senderName + ".");
                } else if (result == 1) {
                    return Message.process("<blue>\uD83D\uDDE1 <gray>› <red>Você recusou o convite de X1 de " + senderName + ".");
                } else {
                    return Message.process("<blue>\uD83D\uDDE1 <gray>› <red>O convite de X1 de " + senderName + " expirou.");
                }
            }
        }
        return null;
    }

    public void sendInvite() {
        Bukkit.getPlayer(sender).sendMessage(getSendMessage(type));
        Bukkit.getPlayer(invited).sendMessage(getTypeMessage(type));
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (isCancelled()) {
                    return;
                }

                Bukkit.getPlayer(sender).sendMessage(getResultMessage(type, 2));
                Bukkit.getPlayer(invited).sendMessage(getResultMessageForInvited(type, 2));
                Sandbox.instance.getInviteManager().expireInvite(inviteConfig);

            }
        }.runTaskLaterAsynchronously(Sandbox.instance, 20 * 30);
        this.task = task;
    }

    public void acceptInvite() {
        task.cancel();

        Player senderP = Bukkit.getPlayer(sender);
        Player invitedP = Bukkit.getPlayer(invited);
        senderP.sendMessage(getResultMessage(type, 0));
        invitedP.sendMessage(getResultMessageForInvited(type, 0));

        if (type == 0) {
            senderP.teleport(invitedP);
        } else if (type == 1) {
            Set<UUID> players = new HashSet<>();
            players.add(sender);
            players.add(invited);
            Sandbox.instance.getTeleportUtils().randomTeleport(players, false, kit);
        }

    }

    public void denyInvite() {
        task.cancel();
        Bukkit.getPlayer(sender).sendMessage(getResultMessage(type, 1));
        Bukkit.getPlayer(invited).sendMessage(getResultMessageForInvited(type, 1));
    }

    public int getType() {
        return type;
    }

    public UUID getSender() {
        return sender;
    }

    public UUID getInvited() {
        return invited;
    }

    public BukkitTask getTask() {
        return task;
    }

    public String getKit() {
        return kit;
    }
}
