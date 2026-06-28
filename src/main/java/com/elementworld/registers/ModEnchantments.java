package com.elementworld.registers;

import com.elementworld.ElementWorld;
import com.elementworld.enchantment.ElementalInfusionEnchantment;
import com.elementworld.elements.Element;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ModEnchantments {
    public static final Enchantment PYRO_INFUSION = register("pyro_infusion", new ElementalInfusionEnchantment());
    public static final Enchantment HYDRO_INFUSION = register("hydro_infusion", new ElementalInfusionEnchantment());
    public static final Enchantment CRYO_INFUSION = register("cryo_infusion", new ElementalInfusionEnchantment());
    public static final Enchantment ELECTRO_INFUSION = register("electro_infusion", new ElementalInfusionEnchantment());
    public static final Enchantment ANEMO_INFUSION = register("anemo_infusion", new ElementalInfusionEnchantment());
    public static final Enchantment GEO_INFUSION = register("geo_infusion", new ElementalInfusionEnchantment());
    public static final Enchantment DENDRO_INFUSION = register("dendro_infusion", new ElementalInfusionEnchantment());

    private static final InfusionEntry[] INFUSIONS = {
            new InfusionEntry(PYRO_INFUSION, Element.ElementType.PYRO),
            new InfusionEntry(HYDRO_INFUSION, Element.ElementType.HYDRO),
            new InfusionEntry(CRYO_INFUSION, Element.ElementType.CRYO),
            new InfusionEntry(ELECTRO_INFUSION, Element.ElementType.ELECTRO),
            new InfusionEntry(ANEMO_INFUSION, Element.ElementType.ANEMO),
            new InfusionEntry(GEO_INFUSION, Element.ElementType.GEO),
            new InfusionEntry(DENDRO_INFUSION, Element.ElementType.DENDRO)
    };

    private ModEnchantments() {
    }

    public static void register() {
        ElementWorld.LOGGER.info("Registering ElementWorld enchantments");
    }

    @Nullable
    public static Element getInfusionElement(ItemStack stack) {
        Element.ElementType selectedType = null;
        int selectedLevel = 0;

        for (InfusionEntry entry : INFUSIONS) {
            int level = EnchantmentHelper.getLevel(entry.enchantment(), stack);
            if (level > selectedLevel) {
                selectedType = entry.type();
                selectedLevel = level;
            }
        }

        if (selectedType == null) {
            return null;
        }
        return Element.create(selectedType, gaugeForLevel(selectedLevel));
    }

    private static double gaugeForLevel(int level) {
        if (level >= 3) {
            return 4.0;
        }
        if (level == 2) {
            return 2.0;
        }
        return 1.0;
    }

    private static Enchantment register(String name, Enchantment enchantment) {
        return Registry.register(Registries.ENCHANTMENT, new Identifier(ElementWorld.MOD_ID, name), enchantment);
    }

    private record InfusionEntry(Enchantment enchantment, Element.ElementType type) {
    }
}
