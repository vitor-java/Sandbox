package net.olokw.sandbox.utils;

import net.olokw.sandbox.Sandbox;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class AnnounceUtils {
    public void startAnnounces() {

        String[] anuncios = new String[4];
        anuncios[0] = "\n<dark_gray>【<red>AVISO<dark_gray>】 <white>Adquira Tags, MVP e Sessões de BULLET em nossa loja! <aqua><click:OPEN_URL:\"https://loja.balacobaco.net\">CLIQUE AQUI</click>.\n";
        anuncios[1] = "\n<dark_gray>【<red>AVISO<dark_gray>】 <white>Quer desativar esses anúncios chatos? Adquira MVP em nossa loja! <aqua><click:OPEN_URL:\"https://loja.balacobaco.net\">CLIQUE AQUI</click>.\n";
        anuncios[2] = "\n<dark_gray>【<red>AVISO<dark_gray>】 <white>Ajude a manter esse servidor ativo! <aqua><click:OPEN_URL:\"https://loja.balacobaco.net\">CLIQUE AQUI</click>.\n";
        anuncios[3] = "\n<dark_gray>【<red>AVISO<dark_gray>】 <white>Key-place é proíbido! Descubra todas as regras do servidor no nosso discord! <aqua><click:OPEN_URL:\"https://discord.balacobaco.net\">CLIQUE AQUI</click>.\n";

        Random random = new Random();

        new BukkitRunnable() {
            @Override
            public void run() {
                String anuncio = anuncios[random.nextInt(anuncios.length)];
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission("balacobaco.vip")) {
                        p.sendMessage(Message.process(anuncio));
                    }
                }
            }
        }.runTaskTimerAsynchronously(Sandbox.instance, 20 * 60 * 8, 20 * 60 * 8);

    }
}
