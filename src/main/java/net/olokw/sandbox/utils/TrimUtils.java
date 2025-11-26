package net.olokw.sandbox.utils;

import dev.triumphteam.gui.click.GuiClick;
import dev.triumphteam.gui.element.GuiItem;
import dev.triumphteam.gui.paper.Gui;
import dev.triumphteam.gui.paper.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.configs.PlayerData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TrimUtils {
    private List<TrimMaterial> materials = new ArrayList<>();
    private List<TrimPattern> patterns = new ArrayList<>();

    public TrimUtils() {
        materials.addAll(MATERIALS.values());
        patterns.addAll(PATTERNS.values());
    }

    private <T> int getPos(T object, List<?> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(object)) {
                return i;
            }
        }
        return 0;
    }

    public void checkAndSetTrim(Player p, ItemStack item) {
        String material1 = item.getType().toString();
        if (!material1.endsWith("_HELMET")
                && !material1.endsWith("_CHESTPLATE")
                && !material1.endsWith("_LEGGINGS")
                && !material1.endsWith("_BOOTS")) {
            return;
        }

        PlayerData playerData = Sandbox.instance.getPlayerDataManager().get(p.getUniqueId());
        ArmorTrim armorTrim = playerData.getTrim(PlayerData.ArmorType.fromItem(item));

        ArmorMeta meta = (ArmorMeta) item.getItemMeta();
        meta.setTrim(armorTrim);
        item.setItemMeta(meta);
    }

    public void checkAndRemoveTrim(ItemStack item) {
        String material = item.getType().toString();
        if (!material.endsWith("_HELMET")
                && !material.endsWith("_CHESTPLATE")
                && !material.endsWith("_LEGGINGS")
                && !material.endsWith("_BOOTS")) {
            return;
        }
        ArmorMeta meta = (ArmorMeta) item.getItemMeta();
        meta.setTrim(null);
        item.setItemMeta(meta);
    }

    public String getMaterialString(TrimMaterial material) {
        for (String s : MATERIALS.keySet()) {
            if (MATERIALS.get(s).equals(material)) {
                return s;
            }
        }
        return null;
    }

    public String getPatternString(TrimPattern pattern) {
        for (String s : PATTERNS.keySet()) {
            if (PATTERNS.get(s).equals(pattern)) {
                return s;
            }
        }
        return null;
    }

    public static final Map<String, TrimMaterial> MATERIALS = Map.of(
            "amethyst", TrimMaterial.AMETHYST,
            "copper", TrimMaterial.COPPER,
            "diamond", TrimMaterial.DIAMOND,
            "emerald", TrimMaterial.EMERALD,
            "gold", TrimMaterial.GOLD,
            "iron", TrimMaterial.IRON,
            "lapis", TrimMaterial.LAPIS,
            "netherite", TrimMaterial.NETHERITE,
            "quartz", TrimMaterial.QUARTZ,
            "redstone", TrimMaterial.REDSTONE
    );

    public static final Map<String, TrimPattern> PATTERNS = Map.ofEntries(
            Map.entry("sentry", TrimPattern.SENTRY),
            Map.entry("dune", TrimPattern.DUNE),
            Map.entry("coast", TrimPattern.COAST),
            Map.entry("wild", TrimPattern.WILD),
            Map.entry("ward", TrimPattern.WARD),
            Map.entry("eye", TrimPattern.EYE),
            Map.entry("vex", TrimPattern.VEX),
            Map.entry("tide", TrimPattern.TIDE),
            Map.entry("snout", TrimPattern.SNOUT),
            Map.entry("rib", TrimPattern.RIB),
            Map.entry("spire", TrimPattern.SPIRE),
            Map.entry("raiser", TrimPattern.RAISER),
            Map.entry("silence", TrimPattern.SILENCE),
            Map.entry("wayfinder", TrimPattern.WAYFINDER),
            Map.entry("shaper", TrimPattern.SHAPER)
    );

    private int getTrimPatternPos(ArmorTrim trim) {
        TrimPattern pattern;
        if (trim == null) {
            pattern = null;
        } else {
            pattern = trim.getPattern();
        }
        return getPos(pattern, patterns);
    }

    private int getTrimMaterialPos(ArmorTrim trim) {
        TrimMaterial material;
        if (trim == null) {
            material = null;
        } else {
            material = trim.getMaterial();
        }
        return getPos(material, materials);
    }

    private @NotNull GuiItem<Player, ItemStack> getArmorGuiItem(PlayerData.ArmorType type, PlayerData data) {
        Material material;
        Component name;
        switch (type) {
            case HELMET -> {
                name = Message.process("<aqua>Capacete");
                material = Material.NETHERITE_HELMET;
                break;
            }
            case CHESTPLATE -> {
                name = Message.process("<aqua>Peitoral");
                material = Material.NETHERITE_CHESTPLATE;
                break;
            }
            case LEGGINGS -> {
                name = Message.process("<aqua>Calças");
                material = Material.NETHERITE_LEGGINGS;
                break;
            }
            case BOOTS -> {
                name = Message.process("<aqua>Botas");
                material = Material.NETHERITE_BOOTS;
                break;
            }
            default -> {
                name = null;
                material = null;
            }
        }

        List<Component> lores = new ArrayList<>();
        lores.add(Message.process("<blue>Clique-esquerdo <gray>\uD83E\uDC16 <white>Mudar padrão."));
        lores.add(Message.process("<blue>Clique-direito <gray>\uD83E\uDC16 <white>Mudar material."));
        lores.add(Message.process("<blue>Shift + Clique <gray>\uD83E\uDC16 <white>Remover enfeite."));

        ItemStack itemStack = new ItemStack(material);
        ArmorMeta armorMeta = (ArmorMeta) itemStack.getItemMeta();
        NamespacedKey example = new NamespacedKey(Sandbox.instance, "attack_speed");
        AttributeModifier attackSpeedModifier = new AttributeModifier(example, 0.0, AttributeModifier.Operation.ADD_NUMBER);
        armorMeta.addAttributeModifier(Attribute.ATTACK_SPEED, attackSpeedModifier);
        armorMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        armorMeta.setTrim(data.getTrim(type));
        itemStack.setItemMeta(armorMeta);

        return ItemBuilder.from(itemStack)
                .name(name)
                .glow()
                .lore(lores)
                .asGuiItem((player, context) -> {
                    if (context.guiClick().equals(GuiClick.SHIFT_LEFT) || context.guiClick().equals(GuiClick.SHIFT_RIGHT)) {
                        data.setTrim(type, null);
                        openGui(player, data);
                    } else if (context.guiClick().equals(GuiClick.LEFT)) {
                        switchPatternInData(type, data);
                        openGui(player, data);
                    } else if (context.guiClick().equals(GuiClick.RIGHT)) {
                        switchMaterialInData(type, data);
                        openGui(player, data);
                    }
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);;
                });
    }

    private void switchPatternInData(PlayerData.ArmorType type, PlayerData data) {
        ArmorTrim trim = data.getTrim(type);

        int actualPatternPos;
        int actualMaterialPos = getTrimMaterialPos(trim);
        if (trim == null) {
            actualPatternPos = patterns.size() - 1;
        } else { actualPatternPos = getTrimPatternPos(trim); }

        int newPatternPos;
        if (actualPatternPos == patterns.size() - 1) {
            newPatternPos = 0;
        } else {
            newPatternPos = actualPatternPos + 1;
        }

        ArmorTrim newTrim = new ArmorTrim(materials.get(actualMaterialPos), patterns.get(newPatternPos));
        data.setTrim(type, newTrim);
    }

    private void switchMaterialInData(PlayerData.ArmorType type, PlayerData data) {
        ArmorTrim trim = data.getTrim(type);

        int actualPatternPos = getTrimPatternPos(trim);
        int actualMaterialPos;
        if (trim == null) {
            actualMaterialPos = materials.size() - 1;
        } else { actualMaterialPos = getTrimMaterialPos(trim); }

        int newMaterialPos;
        if (actualMaterialPos == materials.size() - 1) {
            newMaterialPos = 0;
        } else {
            newMaterialPos = actualMaterialPos + 1;
        }

        ArmorTrim newTrim = new ArmorTrim(materials.get(newMaterialPos), patterns.get(actualPatternPos));
        data.setTrim(type, newTrim);
    }

    // clique esquerdo alterna pattern
    // clique direito alterna material
    // clique com shift = limpa

    public void openGui(Player p, PlayerData data) {
        final var gui = Gui.of(1)
                .title(Message.process("Enfeites"))
                .statelessComponent(container -> {

                    GuiItem<Player, ItemStack> helmet = getArmorGuiItem(PlayerData.ArmorType.HELMET, data);
                    GuiItem<Player, ItemStack> chestplate = getArmorGuiItem(PlayerData.ArmorType.CHESTPLATE, data);
                    GuiItem<Player, ItemStack> leggings = getArmorGuiItem(PlayerData.ArmorType.LEGGINGS, data);
                    GuiItem<Player, ItemStack> boots = getArmorGuiItem(PlayerData.ArmorType.BOOTS, data);

                    container.setItem(1, 1, helmet);
                    container.setItem(1, 2, chestplate);
                    container.setItem(1, 3, leggings);
                    container.setItem(1, 4, boots);


                })
                .build();

        new BukkitRunnable() {
            @Override
            public void run() {
                gui.open(p);
            }
        }.runTaskLater(Sandbox.instance, 0);
    }
}
