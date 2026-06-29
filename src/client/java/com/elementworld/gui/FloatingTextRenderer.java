package com.elementworld.gui;

import com.elementworld.ElementWorld;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class FloatingTextRenderer {
    private static final List<FloatingText> FLOATING_TEXTS = new ArrayList<>();
    private static final float TEXT_SCALE = 0.025F;

    private FloatingTextRenderer() {
    }

    public static void register() {
        registerReceiver();
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
        WorldRenderEvents.AFTER_ENTITIES.register(FloatingTextRenderer::render);
    }

    private static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(
                ElementWorld.FLOATING_TEXT_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    double x = buf.readDouble();
                    double y = buf.readDouble();
                    double z = buf.readDouble();
                    Text text = buf.readText();
                    int color = buf.readInt();
                    int lifetime = buf.readInt();
                    double velocityX = buf.readDouble();
                    double velocityY = buf.readDouble();
                    double velocityZ = buf.readDouble();

                    client.execute(() -> FLOATING_TEXTS.add(new FloatingText(
                            new Vec3d(x, y, z),
                            new Vec3d(velocityX, velocityY, velocityZ),
                            text,
                            color,
                            lifetime
                    )));
                }
        );
    }

    private static void tick() {
        Iterator<FloatingText> iterator = FLOATING_TEXTS.iterator();
        while (iterator.hasNext()) {
            FloatingText floatingText = iterator.next();
            floatingText.tick();
            if (floatingText.isDead()) {
                iterator.remove();
            }
        }
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        VertexConsumerProvider consumers = context.consumers();
        if (client.world == null || client.player == null || consumers == null || FLOATING_TEXTS.isEmpty()) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = context.camera().getPos();
        TextRenderer textRenderer = client.textRenderer;

        for (FloatingText floatingText : List.copyOf(FLOATING_TEXTS)) {
            floatingText.render(matrices, consumers, textRenderer, cameraPos, context.camera().getRotation());
        }
    }

    private static final class FloatingText {
        private Vec3d position;
        private final Vec3d velocity;
        private final Text text;
        private final int color;
        private final int lifetime;
        private int age;

        private FloatingText(Vec3d position, Vec3d velocity, Text text, int color, int lifetime) {
            this.position = position;
            this.velocity = velocity;
            this.text = text;
            this.color = color;
            this.lifetime = Math.max(lifetime, 1);
        }

        private void tick() {
            age++;
            position = position.add(velocity);
        }

        private boolean isDead() {
            return age >= lifetime;
        }

        private void render(
                MatrixStack matrices,
                VertexConsumerProvider consumers,
                TextRenderer textRenderer,
                Vec3d cameraPos,
                Quaternionf cameraRotation
        ) {
            float alpha = 1.0F - (float) age / (float) lifetime;
            int textColor = withAlpha(color, Math.max(0, Math.min(255, (int) (alpha * 255.0F))));
            float x = (float) -textRenderer.getWidth(text) / 2.0F;

            matrices.push();
            matrices.translate(position.x - cameraPos.x, position.y - cameraPos.y, position.z - cameraPos.z);
            matrices.multiply(cameraRotation);
            matrices.scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
            textRenderer.draw(
                    text,
                    x,
                    0.0F,
                    textColor,
                    false,
                    matrices.peek().getPositionMatrix(),
                    consumers,
                    TextRenderer.TextLayerType.SEE_THROUGH,
                    0,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE
            );
            matrices.pop();
        }

        private static int withAlpha(int color, int alpha) {
            return (alpha << 24) | (color & 0x00FFFFFF);
        }
    }
}
