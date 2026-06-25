package com.elementworld.elementComponents.reaction;

/**
 * 反应结算结果。伤害侧（LivingEntityMixin）只读取本结果决定是否改写伤害倍率，
 * 反应的所有副作用（剧变效果、持续反应注册、冻结生成等）已在 handler 阶段完成。
 *
 * <p>三态语义：
 * <ul>
 *   <li>{@link None}        —— 无反应（元素直接附着 / 无操作）。</li>
 *   <li>{@link ReactionOccurred} —— 有反应副作用但不改伤害（剧变 / 持续 / 冻结 / 空壳注册）。</li>
 *   <li>{@link Amplified}    —— 增幅反应（蒸发 / 融化），携带倍率与精通乘数。</li>
 * </ul>
 */
public sealed interface ReactionOutcome permits ReactionOutcome.None, ReactionOutcome.ReactionOccurred, ReactionOutcome.Amplified {

    /** 无反应发生。元素直接附着或被忽略。 */
    None NONE = None.INSTANCE;

    /** 有反应副作用发生，但不改写触发攻击的伤害。 */
    ReactionOccurred REACTION_OCCURRED = ReactionOccurred.INSTANCE;

    /** 无反应。 */
    final class None implements ReactionOutcome {
        static final None INSTANCE = new None();
        private None() {}
    }

    /** 有反应副作用（剧变 / 持续反应 / 冻结 / 空壳），但不改写触发攻击的伤害。 */
    final class ReactionOccurred implements ReactionOutcome {
        static final ReactionOccurred INSTANCE = new ReactionOccurred();
        private ReactionOccurred() {}
    }

    /**
     * 增幅反应（蒸发 / 融化）。
     *
     * @param multiplier         反应倍率，已并入基础倍率与方向系数：强=2.0，弱=1.5
     *                           （等价于旧代码 caller 的 {@code (1 + getReactionBaseMul())}）。
     * @param masteryMultiplier  精通乘数（旧 {@link AmpReaction#getMasteryAmp}）。
     *                           无攻击者容器时为 1。
     */
    record Amplified(double multiplier, double masteryMultiplier) implements ReactionOutcome {

        /** 增幅强（方向有利），gauge 系数 2，baseMul=1 → multiplier=2。 */
        public static Amplified strong(double masteryMultiplier) {
            return new Amplified(2.0, masteryMultiplier);
        }

        /** 增幅弱（方向不利），gauge 系数 0.5，baseMul=0.5 → multiplier=1.5。 */
        public static Amplified weak(double masteryMultiplier) {
            return new Amplified(1.5, masteryMultiplier);
        }
    }
}
