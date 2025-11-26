package net.olokw.sandbox.utils;

import dev.triumphteam.gui.element.GuiItem;
import dev.triumphteam.gui.paper.Gui;
import dev.triumphteam.gui.paper.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.olokw.grandKits.GrandKits;
import net.olokw.grandKits.Utils.KitConfig;
import net.olokw.grandKits.Utils.KitUtils;
import net.olokw.sandbox.Sandbox;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class X1Utils {
    public void openKitSelectionGui(Player player, UUID invited, String selectedKit) {
        KitUtils kitUtils = GrandKits.instance.getKitUtils();
        List<String> presetKitList = kitUtils.getPresetKitList();

        final var gui = Gui.of(4)
                .title(Message.process("Selecione o Kit"))
                .statelessComponent(container -> { // This time we want a normal component

                    // We tell the component to remember the starting value `0`
                    // Which will return a MutableState<Integer> that can be used in the render function of the component

                    ;

                    boolean noKitIsSelected = selectedKit == null;
                    Component noKitName = Message.process("<aqua>Último Kit <dark_gray>[<gold>Ambos<dark_gray>]");
                    Component noKitLore;
                    if (noKitIsSelected) {
                        noKitLore = Message.process("<green>[Selecionado]");
                    } else {
                        noKitLore = Message.process("<gray>Clique para selecionar.");
                    }
                    GuiItem<Player, ItemStack> noKitItem = ItemBuilder.from(Material.BARRIER)
                            .name(noKitName)
                            .lore(noKitLore)
                            .glow(noKitIsSelected)
                            .asGuiItem((p1, context) -> {
                                if (!noKitIsSelected) {
                                    openKitSelectionGui(p1, invited, null);
                                    p1.playSound(p1, Sound.UI_BUTTON_CLICK, 1, 1);
                                }
                            });
                    container.setItem(0, noKitItem);


                    for (int i = 0; i < presetKitList.size() && i <= 26; i++) {
                        String kit = presetKitList.get(i);
                        boolean isSelected = kit.equalsIgnoreCase(selectedKit);
                        KitConfig kitConfig = kitUtils.getPresetKit(kit);
                        ItemStack item = deserializeItemStack(kitConfig.getItems().get(68));
                        Component lore;
                        if (isSelected) {
                            lore = Message.process("<green>[Selecionado]");
                        } else {
                            lore = Message.process("<gray>Clique para selecionar.");
                        }

                        GuiItem<Player, ItemStack> kitItem = ItemBuilder.from(item)
                                .glow(isSelected)
                                .lore(lore)
                                .asGuiItem((p1, context) -> {
                                    if (!isSelected) {
                                        openKitSelectionGui(p1, invited, kitConfig.getName());
                                        p1.playSound(p1, Sound.UI_BUTTON_CLICK, 1, 1);
                                    }
                                });

                        container.setItem(i + 1, kitItem);
                    }

                    GuiItem<Player, ItemStack> sendItem = ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE)
                            .name(Message.process("<green>ENVIAR"))
                            .glow()
                            .asGuiItem((p1, context) -> {
                                Sandbox.instance.getInviteManager().sendInvite(1, player.getUniqueId(), invited, selectedKit);
                                player.closeInventory();
                            });
                    for (int i = 27; i <= 35; i++) {
                        container.setItem(i, sendItem);
                    }

                })
                .build();

        new BukkitRunnable() {
            @Override
            public void run() {
                gui.open(player);
            }
        }.runTaskLater(Sandbox.instance, 0);

    }


    private ItemStack deserializeItemStack(String base64) {
        if (base64 == null) return null;
        byte[] data = Base64.getDecoder().decode(base64);
        return ItemStack.deserializeBytes(data);
    }

    public Component getKitFormattedName(String kitName) {
        return deserializeItemStack(GrandKits.instance.getKitUtils().getPresetKit(kitName).getItems().get(68)).getItemMeta().displayName();
    }


}
