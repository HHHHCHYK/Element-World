package com.elementworld.gui;

import com.elementworld.ElementWorld;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public final class ElementRender {
    private static final int ICON_SIZE = 16;
    private static final List<String> elements = new ArrayList<>();

    private ElementRender() {
    }

    public static void renderElement() {
        registerElementReceiver();
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || elements.isEmpty()) {
                return;
            }

            List<String> snapshot = List.copyOf(elements);
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();
            int x = (screenWidth - snapshot.size() * ICON_SIZE) / 2;
            int y = screenHeight - 54;

            for (String element : snapshot) {
                drawContext.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, colorFor(element));
                drawContext.drawBorder(x, y, ICON_SIZE, ICON_SIZE, 0xAA000000);
                x += ICON_SIZE;
            }
        });
    }

    private static void registerElementReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(
                ElementWorld.ELEMENT_TYPES_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    int size = buf.readInt();
                    List<String> receivedElements = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        receivedElements.add(buf.readString());
                    }
                    client.execute(() -> {
                        elements.clear();
                        elements.addAll(receivedElements);
                    });
                }
        );
    }

    private static int colorFor(String element) {
        return switch (element) {
            case "Anemo" -> 0xFF63E6BE;
            case "Geo" -> 0xFFE3B341;
            case "Electro" -> 0xFFB783FF;
            case "Hydro" -> 0xFF48C6FF;
            case "Pyro" -> 0xFFFF5A3C;
            case "Cryo" -> 0xFF9DEBFF;
            case "Dendro" -> 0xFF6BD65A;
            case "Frozen" -> 0xFFB7F4FF;
            case "Catalyze" -> 0xFFB6F15F;
            default -> 0xFFFFFFFF;
        };
    }
}
