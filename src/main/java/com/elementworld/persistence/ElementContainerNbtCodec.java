package com.elementworld.persistence;

import com.elementworld.ElementContainer;
import com.elementworld.ElementWorld;
import com.elementworld.elementComponents.modifiers.Modifier;
import com.elementworld.elementComponents.modifiers.Modifiers;
import com.elementworld.elements.EP;
import com.elementworld.elements.Element;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.List;

public final class ElementContainerNbtCodec {
    private static final String ROOT_KEY = "elementworld";
    private static final int VERSION = 1;

    private ElementContainerNbtCodec() {
    }

    public static void writeToEntityNbt(ElementContainer container, NbtCompound entityNbt) {
        ElementContainerState state = container.toPersistentState();
        NbtCompound root = new NbtCompound();
        root.putInt("version", VERSION);
        root.put("elements", writeElements(state.elements()));
        root.putDouble("mastery", state.mastery());
        root.putBoolean("displayElement", state.displayElement());
        root.putDouble("shieldStrength", state.shieldStrength());
        root.put("immune", writeEpClassList(state.immuneElements()));
        root.put("bonuses", writeBonuses(state.bonuses()));
        root.put("resistances", writeResistances(state.resistances()));
        root.put("modifiers", writeModifiers(state.modifiers()));
        root.put("shields", writeShields(state.shields()));
        entityNbt.put(ROOT_KEY, root);
    }

    public static void readFromEntityNbt(ElementContainer container, NbtCompound entityNbt) {
        if (!entityNbt.contains(ROOT_KEY, NbtElement.COMPOUND_TYPE)) {
            return;
        }

        NbtCompound root = entityNbt.getCompound(ROOT_KEY);
        int version = root.getInt("version");
        if (version != VERSION) {
            ElementWorld.LOGGER.warn("Skipping unsupported ElementWorld NBT version: {}", version);
            return;
        }

        ElementContainerState state = new ElementContainerState(
                readElements(root.getList("elements", NbtElement.COMPOUND_TYPE)),
                root.getDouble("mastery"),
                !root.contains("displayElement") || root.getBoolean("displayElement"),
                root.contains("shieldStrength") ? root.getDouble("shieldStrength") : 1,
                readEpClassList(root.getList("immune", NbtElement.STRING_TYPE)),
                readBonuses(root.getList("bonuses", NbtElement.COMPOUND_TYPE)),
                readResistances(root.getList("resistances", NbtElement.COMPOUND_TYPE)),
                readModifiers(root.getList("modifiers", NbtElement.COMPOUND_TYPE)),
                readShields(root.getList("shields", NbtElement.COMPOUND_TYPE))
        );
        container.loadPersistentState(state);
    }

    private static NbtList writeElements(List<ElementContainerState.ElementEntry> elements) {
        NbtList list = new NbtList();
        for (ElementContainerState.ElementEntry element : elements) {
            NbtCompound entry = new NbtCompound();
            entry.putString("type", element.type().name());
            entry.putDouble("gauge", element.gauge());
            list.add(entry);
        }
        return list;
    }

    private static List<ElementContainerState.ElementEntry> readElements(NbtList list) {
        List<ElementContainerState.ElementEntry> elements = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            Element.ElementType type = parseElementType(entry.getString("type"));
            if (type == null || type == Element.ElementType.PHYSICS) {
                continue;
            }
            elements.add(new ElementContainerState.ElementEntry(type, entry.getDouble("gauge")));
        }
        return elements;
    }

    private static NbtList writeEpClassList(List<Class<? extends EP>> elementTypes) {
        NbtList list = new NbtList();
        for (Class<? extends EP> elementType : elementTypes) {
            Element.ElementType type = Element.typeOfEpClass(elementType);
            if (type != null) {
                list.add(NbtString.of(type.name()));
            }
        }
        return list;
    }

    private static List<Class<? extends EP>> readEpClassList(NbtList list) {
        List<Class<? extends EP>> elementTypes = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Element.ElementType type = parseElementType(list.getString(i));
            if (type != null) {
                elementTypes.add(Element.enumToClass(type));
            }
        }
        return elementTypes;
    }

    private static NbtList writeBonuses(List<ElementContainerState.BonusEntry> bonuses) {
        NbtList list = new NbtList();
        for (ElementContainerState.BonusEntry bonus : bonuses) {
            Element.ElementType type = Element.typeOfEpClass(bonus.elementType());
            if (type == null) {
                continue;
            }
            NbtCompound entry = new NbtCompound();
            entry.putString("element", type.name());
            entry.putString("name", bonus.name());
            entry.putDouble("value", bonus.value());
            list.add(entry);
        }
        return list;
    }

    private static List<ElementContainerState.BonusEntry> readBonuses(NbtList list) {
        List<ElementContainerState.BonusEntry> bonuses = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            Class<? extends EP> elementType = readEpClass(entry, "element");
            String name = entry.getString("name");
            if (elementType == null || name.isEmpty()) {
                continue;
            }
            bonuses.add(new ElementContainerState.BonusEntry(elementType, entry.getDouble("value"), name));
        }
        return bonuses;
    }

    private static NbtList writeResistances(List<ElementContainerState.ResistanceEntry> resistances) {
        NbtList list = new NbtList();
        for (ElementContainerState.ResistanceEntry resistance : resistances) {
            Element.ElementType type = Element.typeOfEpClass(resistance.elementType());
            if (type == null) {
                continue;
            }
            NbtCompound entry = new NbtCompound();
            entry.putString("element", type.name());
            entry.putString("name", resistance.name());
            entry.putDouble("value", resistance.value());
            list.add(entry);
        }
        return list;
    }

    private static List<ElementContainerState.ResistanceEntry> readResistances(NbtList list) {
        List<ElementContainerState.ResistanceEntry> resistances = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            Class<? extends EP> elementType = readEpClass(entry, "element");
            String name = entry.getString("name");
            if (elementType == null || name.isEmpty()) {
                continue;
            }
            resistances.add(new ElementContainerState.ResistanceEntry(elementType, entry.getDouble("value"), name));
        }
        return resistances;
    }

    private static NbtList writeModifiers(List<ElementContainerState.ModifierGroup> modifierGroups) {
        NbtList list = new NbtList();
        for (ElementContainerState.ModifierGroup modifierGroup : modifierGroups) {
            NbtCompound group = new NbtCompound();
            group.putString("target", modifierGroup.type().name());
            NbtList entries = new NbtList();
            for (ElementContainerState.ModifierEntry modifier : modifierGroup.entries()) {
                NbtCompound entry = new NbtCompound();
                entry.putString("name", modifier.name());
                entry.putDouble("value", modifier.value());
                entry.putInt("duration", modifier.duration());
                entry.putString("method", modifier.method().name());
                entries.add(entry);
            }
            group.put("entries", entries);
            list.add(group);
        }
        return list;
    }

    private static List<ElementContainerState.ModifierGroup> readModifiers(NbtList list) {
        List<ElementContainerState.ModifierGroup> modifierGroups = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            NbtCompound group = list.getCompound(i);
            Modifiers.ModifierType type = parseModifierType(group.getString("target"));
            if (type == null) {
                continue;
            }
            List<ElementContainerState.ModifierEntry> entries = new ArrayList<>();
            NbtList entryList = group.getList("entries", NbtElement.COMPOUND_TYPE);
            for (int j = 0; j < entryList.size(); j++) {
                NbtCompound entry = entryList.getCompound(j);
                String name = entry.getString("name");
                Modifier.ModifierMethod method = parseModifierMethod(entry.getString("method"));
                if (name.isEmpty() || method == null) {
                    continue;
                }
                entries.add(new ElementContainerState.ModifierEntry(
                        name,
                        entry.getDouble("value"),
                        entry.getInt("duration"),
                        method
                ));
            }
            modifierGroups.add(new ElementContainerState.ModifierGroup(type, entries));
        }
        return modifierGroups;
    }

    private static NbtList writeShields(List<ElementContainerState.ShieldEntry> shields) {
        NbtList list = new NbtList();
        for (ElementContainerState.ShieldEntry shield : shields) {
            NbtCompound entry = new NbtCompound();
            entry.putString("name", shield.name());
            entry.putDouble("value", shield.value());
            entry.putDouble("maxValue", shield.maxValue());
            Element.ElementType type = Element.typeOfElementClass(shield.elementType());
            if (type != null) {
                entry.putString("element", type.name());
            }
            list.add(entry);
        }
        return list;
    }

    private static List<ElementContainerState.ShieldEntry> readShields(NbtList list) {
        List<ElementContainerState.ShieldEntry> shields = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            String name = entry.getString("name");
            if (name.isEmpty()) {
                continue;
            }
            Class<? extends Element> elementType = null;
            if (entry.contains("element", NbtElement.STRING_TYPE)) {
                elementType = readElementClass(entry, "element");
                if (elementType == null) {
                    continue;
                }
            }
            shields.add(new ElementContainerState.ShieldEntry(
                    name,
                    entry.getDouble("value"),
                    entry.getDouble("maxValue"),
                    elementType
            ));
        }
        return shields;
    }

    private static Class<? extends EP> readEpClass(NbtCompound compound, String key) {
        Element.ElementType type = parseElementType(compound.getString(key));
        return type == null ? null : Element.enumToClass(type);
    }

    private static Class<? extends Element> readElementClass(NbtCompound compound, String key) {
        Element.ElementType type = parseElementType(compound.getString(key));
        if (type == null || type == Element.ElementType.PHYSICS) {
            return null;
        }
        return Element.elementClassOf(type);
    }

    private static Element.ElementType parseElementType(String value) {
        try {
            return Element.ElementType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            ElementWorld.LOGGER.warn("Skipping unknown ElementWorld element type in NBT: {}", value);
            return null;
        }
    }

    private static Modifiers.ModifierType parseModifierType(String value) {
        try {
            return Modifiers.ModifierType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            ElementWorld.LOGGER.warn("Skipping unknown ElementWorld modifier type in NBT: {}", value);
            return null;
        }
    }

    private static Modifier.ModifierMethod parseModifierMethod(String value) {
        try {
            return Modifier.ModifierMethod.valueOf(value);
        } catch (IllegalArgumentException exception) {
            ElementWorld.LOGGER.warn("Skipping unknown ElementWorld modifier method in NBT: {}", value);
            return null;
        }
    }
}
