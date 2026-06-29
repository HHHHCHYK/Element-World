package com.elementworld.floatingtext;

import com.elementworld.ElementWorld;
import com.elementworld.elements.Element;
import com.elementworld.util.ElementColors;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.LinkedHashSet;
import java.util.Set;

public final class FloatingTextService {
    public static final int DEFAULT_LIFETIME_TICKS = 35;
    private static final DecimalFormat DAMAGE_FORMAT = new DecimalFormat("0.#");

    private FloatingTextService() {
    }

    public static void spawnDamageNumber(LivingEntity target, float amount, @Nullable Element element) {
        if (amount <= 0 || target.getWorld().isClient()) {
            return;
        }

        Element.ElementType elementType = element == null
                ? Element.ElementType.PHYSICS
                : Element.typeOfElementClass(element.getClass());
        int color = ElementColors.colorFor(elementType);
        Random random = target.getWorld().getRandom();
        Vec3d offset = new Vec3d(
                (random.nextDouble() - 0.5D) * target.getWidth(),
                target.getHeight() + 0.35D,
                (random.nextDouble() - 0.5D) * target.getWidth()
        );
        Vec3d velocity = new Vec3d(0.0D, 0.035D, 0.0D);
        spawnAroundEntity(
                target,
                Text.literal(DAMAGE_FORMAT.format(amount)),
                color,
                DEFAULT_LIFETIME_TICKS,
                offset,
                velocity
        );
    }

    public static void spawnReactionLabel(
            LivingEntity target,
            Text text,
            Element.ElementType triggerElementType
    ) {
        if (target.getWorld().isClient()) {
            return;
        }

        spawnAroundEntity(
                target,
                text,
                ElementColors.colorFor(triggerElementType),
                DEFAULT_LIFETIME_TICKS,
                new Vec3d(0.0D, target.getHeight() + 0.7D, 0.0D),
                new Vec3d(0.0D, 0.035D, 0.0D)
        );
    }

    public static void spawnAroundEntity(
            LivingEntity entity,
            String text,
            int color,
            int lifetime,
            Vec3d offset,
            Vec3d velocity
    ) {
        spawnAroundEntity(entity, Text.literal(text), color, lifetime, offset, velocity);
    }

    public static void spawnAroundEntity(
            LivingEntity entity,
            Text text,
            int color,
            int lifetime,
            Vec3d offset,
            Vec3d velocity
    ) {
        if (entity.getWorld().isClient() || lifetime <= 0 || text.getString().isEmpty()) {
            return;
        }

        Vec3d position = entity.getPos().add(offset);
        sendToTrackingPlayers(entity, position, text, color, lifetime, velocity);
    }

    public static void spawnAt(
            ServerWorld world,
            Vec3d position,
            String text,
            int color,
            int lifetime,
            Vec3d velocity
    ) {
        spawnAt(world, position, Text.literal(text), color, lifetime, velocity);
    }

    public static void spawnAt(
            ServerWorld world,
            Vec3d position,
            Text text,
            int color,
            int lifetime,
            Vec3d velocity
    ) {
        if (lifetime <= 0 || text.getString().isEmpty()) {
            return;
        }

        for (ServerPlayerEntity player : PlayerLookup.around(world, position, 64.0D)) {
            send(player, position, text, color, lifetime, velocity);
        }
    }

    private static void sendToTrackingPlayers(
            LivingEntity entity,
            Vec3d position,
            Text text,
            int color,
            int lifetime,
            Vec3d velocity
    ) {
        Set<ServerPlayerEntity> players = new LinkedHashSet<>(PlayerLookup.tracking(entity));
        if (entity instanceof ServerPlayerEntity player) {
            players.add(player);
        }

        for (ServerPlayerEntity player : players) {
            send(player, position, text, color, lifetime, velocity);
        }
    }

    private static void send(
            ServerPlayerEntity player,
            Vec3d position,
            Text text,
            int color,
            int lifetime,
            Vec3d velocity
    ) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeDouble(position.x);
        buf.writeDouble(position.y);
        buf.writeDouble(position.z);
        buf.writeText(text);
        buf.writeInt(color);
        buf.writeInt(lifetime);
        buf.writeDouble(velocity.x);
        buf.writeDouble(velocity.y);
        buf.writeDouble(velocity.z);
        ServerPlayNetworking.send(player, ElementWorld.FLOATING_TEXT_PACKET_ID, buf);
    }
}
