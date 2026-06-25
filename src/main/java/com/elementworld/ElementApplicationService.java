package com.elementworld;

import com.elementworld.elementComponents.reaction.ReactionOutcome;
import com.elementworld.elements.Element;
import com.elementworld.interfaces.LivingEntityHolder;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * 无伤害的元素施加入口。
 *
 * <p>用于在不触发 Damage 流程的情况下给目标附着元素 / 结算反应
 * （如 {@code /element add} 命令）。绕过了旧代码「damage(0.1f) 骗附着」的 hack：
 * 该入口直接调用容器侧的 {@link ElementContainer#resolveReaction}，
 * 不再产生任何伤害记录。
 */
public final class ElementApplicationService {

    private ElementApplicationService() {
    }

    /**
     * 给目标施加元素并结算反应，不造成伤害。
     *
     * @param target   被附着目标
     * @param trigger  施加的元素
     * @param attacker 触发者，可为 {@code null}
     * @return 反应结果（供调用方了解是否触发了反应）
     */
    public static ReactionOutcome applyElement(LivingEntity target, Element trigger, @Nullable LivingEntity attacker) {
        ElementContainer container = ((LivingEntityHolder) target).getElementContainer$EW();
        return container.resolveReaction(trigger, attacker, null);
    }
}
