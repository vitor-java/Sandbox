package net.olokw.sandbox.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.managers.CooldownManager;
import net.olokw.sandbox.managers.QueueManager;
import net.olokw.sandbox.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Queue implements CommandExecutor, TabCompleter {

    private final QueueManager queueManager;

    public Queue(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        CooldownManager cooldownManager = Sandbox.instance.getCooldownManager();
        UUID uuid = p.getUniqueId();

        if (cooldownManager.isInCooldown(uuid, 2)) {
            p.sendMessage(Message.process("<red>⏚ Aguarde " + cooldownManager.getRemainingTimeFormated(uuid, 2) + " para usar isso novamente!"));
            return true;
        }

        if (args.length >= 1) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "criar" -> handleCriar(p, args);
                case "entrar" -> handleEntrar(p, args);
                case "expulsar" -> handleExpulsar(p, args);
                case "sair" -> handleSair(p);
                default -> p.sendMessage(Message.process("<red>Comando desconhecido."));
            }
        } else {
            handleDefault(p);
        }

        cooldownManager.add(uuid, 1, 1);
        return true;
    }

    private void handleCriar(Player p, String[] args) {
        int amount = 2;
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {}
        }

        if (amount <= 1) {
            p.sendMessage(Message.process("\n<color:#FF1951>🗡 <gray>› <red>A quantidade mínima de jogadores na fila é 2.\n"));
            return;
        }

        if (amount > 6 && !p.hasPermission("balacobaco.vip")) {
            p.sendMessage(Message.process("\n<color:#FF1951>🗡 <gray>› <red>Oops! Apenas jogadores MVPs podem criar filas customizadas para mais de 6 jogadores.\n"));
            return;
        }

        String lastId = queueManager.getQueueIdFromPlayer(p.getUniqueId());
        if (lastId != null && lastId.contains("#")) {
            if (lastId.startsWith(p.getName() + "#")) {
                p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <white>Você saiu da sua fila. A fila será deletada."));
                broadcastToQueue(lastId, Message.process("<color:#FF1951>🗡 <gray>› <white>O organizador debandou a sua fila."), null);
                queueManager.deleteQueue(lastId);
            } else {
                broadcastToQueue(lastId, Message.process("<color:#FF1951>🗡 <gray>› <white>O jogador " + p.getName() + " saiu da sua fila."), null);
            }
        }

        if (queueManager.tryCreatingCustom(p, amount)) {
            p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <green>Fila criada com sucesso!"));

            Component clickable = Component.text("Clique aqui para entrar na fila!")
                    .clickEvent(ClickEvent.runCommand("/fila entrar " + p.getName()))
                    .color(NamedTextColor.GREEN);

            Component announce = Message.process("<color:#FF1951>🗡 <gray>› <white>O jogador " + p.getName() + " está organizando uma fila customizada para " + amount + " jogadores. ")
                    .append(clickable);

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(p)) online.sendMessage(announce);
            }
        } else {
            p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <red>Quantidade inválida! Insira no comando um número maior ou igual a 2."));
        }
    }

    private void handleEntrar(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <red>Uso correto: /fila entrar <jogador>"));
            return;
        }

        String target = args[1];
        UUID uuid = p.getUniqueId();
        String lastId = queueManager.getQueueIdFromPlayer(uuid);

        int result = queueManager.checkAndAddToCustomQueue(target, p);
        switch (result) {
            case 1 -> p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <red>Esse jogador está te ignorando! Você não pode entrar na fila customizada dele."));
            case 2 -> {
                p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <green>Você entrou na fila de " + target + "!"));
                handleLeavePreviousQueue(p, lastId);
                broadcastToQueue(queueManager.getQueueIdFromPlayer(uuid), Message.process("<color:#FF1951>🗡 <gray>› <white>O jogador " + p.getName() + " entrou na sua fila."), uuid);
            }
            case 3 -> p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <red>Você já está nessa fila!"));
            default -> p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <red>Esse jogador não está organizando nenhuma fila no momento."));
        }
    }

    private void handleExpulsar(Player p, String[] args) {
        if (args.length < 2) return;

        String id = queueManager.hasCreatedCustomQueueAndReturnId(p.getUniqueId());
        if (id.isBlank()) return;

        String targetName = args[1];
        if (targetName.equalsIgnoreCase(p.getName())) {
            p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <red>Você não pode expulsar a si mesmo!"));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (queueManager.tryKickingFromQueue(id, target.getUniqueId())) {
            p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <green>Jogador expulso da fila!"));
            if (target.isOnline())
                target.getPlayer().sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <red>Você foi expulso da fila customizada de " + p.getName() + "!"));

            broadcastToQueue(id, Message.process("<color:#FF1951>🗡 <gray>› <white>O jogador " + targetName + " foi expulso da sua fila."), p.getUniqueId());
        } else {
            p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <red>Esse jogador não está na sua fila customizada."));
        }
    }

    private void handleSair(Player p) {
        handleLeavePreviousQueue(p, queueManager.getQueueIdFromPlayer(p.getUniqueId()));
    }

    private void handleDefault(Player p) {
        String id = queueManager.getQueueIdFromPlayer(p.getUniqueId());
        if (id == null) {
            queueManager.addToDefault(p);
            p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <white>Você entrou na fila rápida! Aguardando jogadores..."));
        } else {
            handleLeavePreviousQueue(p, id);
        }
    }

    private void handleLeavePreviousQueue(Player p, String id) {
        UUID uuid = p.getUniqueId();
        if (id == null) {
            p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <red>Você não está em nenhuma fila."));
            return;
        }

        queueManager.tryKickingFromQueue(id, uuid);

        if (!id.contains("#")) {
            p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <white>Você saiu da fila rápida."));
            return;
        }

        if (id.startsWith(p.getName() + "#")) {
            p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <white>Você saiu da sua fila. A fila será deletada."));
            broadcastToQueue(id, Message.process("<white>O organizador debandou a sua fila."), uuid);
            queueManager.deleteQueue(id);
        } else {
            String[] idSplit = id.split("#");
            String organizer = idSplit[0];
            p.sendMessage(Message.process("<color:#FF1951>🗡 <gray>› <white>Você saiu da fila customizada de " + organizer + "."));
            broadcastToQueue(id, Message.process("<color:#FF1951>🗡 <gray>› <white>O jogador " + p.getName() + " saiu da sua fila."), uuid);
        }
    }

    private void broadcastToQueue(String id, Component message, UUID exclude) {
        for (UUID uuid : queueManager.playersUUIDinQueue(id)) {
            if (!uuid.equals(exclude)) {
                Player target = Bukkit.getPlayer(uuid);
                if (target != null) target.sendMessage(message);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player p)) return Collections.emptyList();

        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.addAll(List.of("criar", "entrar", "sair"));
            if (!queueManager.hasCreatedCustomQueueAndReturnId(p.getUniqueId()).isBlank()) {
                list.add("expulsar");
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "criar" -> list.add("<quantidade>");
                case "expulsar" -> {
                    String id = queueManager.hasCreatedCustomQueueAndReturnId(p.getUniqueId());
                    if (!id.isBlank()) {
                        list.addAll(queueManager.playersInQueue(id));
                        list.remove(p.getName());
                    }
                }
                case "entrar" -> {
                    list.addAll(queueManager.getOrganizersNicknames());
                    list.remove(p.getName());
                }
            }
        }
        return StringUtil.copyPartialMatches(args[args.length - 1], list, new ArrayList<>());
    }
}
