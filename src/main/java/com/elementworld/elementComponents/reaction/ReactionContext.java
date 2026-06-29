package com.elementworld.elementComponents.reaction;

import com.elementworld.ElementContainer;
import com.elementworld.elements.Element;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.Nullable;

/**
 * 反应结算的不可变上下文，由 {@code ElementContainer.resolveReaction} 构造后传给 handler。
 *
 * @param trigger              后手元素（触发元素）。
 * @param aura                 附着元素（先手元素）。
 * @param owner                被附着者（容器拥有者）。
 * @param attacker             反应触发者；可为 {@code null}（如命令施加）。
 * @param source               触发反应的 DamageSource；可为 {@code null}（非伤害入口）。
 * @param ownerContainer       被附着者容器，供 handler 注册持续反应 / 增删元素 / 取精通。
 * @param attackerContainer    攻击者容器；可为 {@code null}。增幅反应的精通乘数取自此容器。
 * @param previousOutcome      本次攻击中上一个反应结算后的结果，由当前反应决定如何合并。
 */
public record ReactionContext(
        Element trigger,
        Element aura,
        LivingEntity owner,
        @Nullable LivingEntity attacker,
        @Nullable DamageSource source,
        ElementContainer ownerContainer,
        @Nullable ElementContainer attackerContainer,
        ReactionOutcome previousOutcome
) {
}
