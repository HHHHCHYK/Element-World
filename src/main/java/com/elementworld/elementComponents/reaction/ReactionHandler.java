package com.elementworld.elementComponents.reaction;

/**
 * 反应处理器。一个 handler 负责一种「后手元素 × 附着元素」组合的全部副作用：
 * gauge 扣减 / 元素增删 / 剧变效果 / 持续反应注册，并返回 {@link ReactionOutcome}。
 *
 * <p>副作用与伤害逻辑彻底解耦：handler 内不依赖 Damage 流程，
 * 伤害侧（LivingEntityMixin）只读 outcome 决定倍率。
 */
@FunctionalInterface
public interface ReactionHandler {
    /**
     * 执行该组合的反应副作用。
     *
     * @param ctx 反应上下文（trigger / aura / owner / attacker / 容器等）
     * @return 反应结果（NONE / REACTION_OCCURRED / AMPLIFIED）
     */
    ReactionOutcome handle(ReactionContext ctx);
}
