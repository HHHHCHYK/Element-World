package com.elementworld.elementComponents.reaction;

import com.elementworld.elements.Element;
import org.jetbrains.annotations.Nullable;

/**
 * 反应结算结果。伤害侧（LivingEntityMixin）只读取本结果决定是否改写伤害倍率，
 * 反应的所有副作用（剧变效果、持续反应注册、冻结生成等）已在 handler 阶段完成。
 *
 * <p>结果语义：
 * <ul>
 *   <li>{@link None}        —— 无反应（元素直接附着 / 无操作）。</li>
 *   <li>{@link ReactionOccurred} —— 有反应副作用但不改伤害（剧变 / 持续 / 冻结 / 空壳注册）。</li>
 *   <li>{@link Amplified}    —— 增幅反应（蒸发 / 融化），携带倍率与精通乘数。</li>
 *   <li>{@link Additive}     —— 加算反应（超激化 / 蔓激化），携带固定加算伤害。</li>
 * </ul>
 *
 * <p>发生反应的结果可携带 {@link Feedback}，供客户端弹出独立反应名。
 */
public sealed interface ReactionOutcome permits ReactionOutcome.None, ReactionOutcome.ReactionOccurred,
        ReactionOutcome.Amplified, ReactionOutcome.Additive {

    /** 无反应发生。元素直接附着或被忽略。 */
    None NONE = None.INSTANCE;

    /** 有反应副作用发生，但不改写触发攻击的伤害。 */
    ReactionOccurred REACTION_OCCURRED = ReactionOccurred.INSTANCE;

    @Nullable
    default Feedback feedback() {
        return null;
    }

    static ReactionOccurred occurred(String translationKey, Element.ElementType triggerElementType) {
        return ReactionOccurred.withFeedback(new Feedback(translationKey, triggerElementType));
    }

    record Feedback(String translationKey, Element.ElementType triggerElementType) {
    }

    /** 无反应。 */
    final class None implements ReactionOutcome {
        static final None INSTANCE = new None();
        private None() {}
    }

    /** 有反应副作用（剧变 / 持续反应 / 冻结 / 空壳），但不改写触发攻击的伤害。 */
    final class ReactionOccurred implements ReactionOutcome {
        static final ReactionOccurred INSTANCE = new ReactionOccurred(null);

        @Nullable
        private final Feedback feedback;

        private ReactionOccurred(@Nullable Feedback feedback) {
            this.feedback = feedback;
        }

        static ReactionOccurred withFeedback(Feedback feedback) {
            return new ReactionOccurred(feedback);
        }

        @Override
        @Nullable
        public Feedback feedback() {
            return feedback;
        }
    }

    /**
     * 增幅反应（蒸发 / 融化）。
     *
     * @param multiplier         反应倍率，已并入基础倍率与方向系数：强=2.0，弱=1.5
     *                           （等价于旧代码 caller 的 {@code (1 + getReactionBaseMul())}）。
     * @param masteryMultiplier  精通乘数（旧 {@link AmpReaction#getMasteryAmp}）。
     *                           无攻击者容器时为 1。
     */
    record Amplified(
            double multiplier,
            double masteryMultiplier,
            @Nullable Feedback feedback
    ) implements ReactionOutcome {

        public Amplified(double multiplier, double masteryMultiplier) {
            this(multiplier, masteryMultiplier, null);
        }

        /** 增幅强（方向有利），gauge 系数 2，baseMul=1 → multiplier=2。 */
        public static Amplified strong(double masteryMultiplier) {
            return new Amplified(2.0, masteryMultiplier);
        }

        /** 增幅强（方向有利），并携带反应弹字反馈。 */
        public static Amplified strong(double masteryMultiplier, Feedback feedback) {
            return new Amplified(2.0, masteryMultiplier, feedback);
        }

        /** 增幅弱（方向不利），gauge 系数 0.5，baseMul=0.5 → multiplier=1.5。 */
        public static Amplified weak(double masteryMultiplier) {
            return new Amplified(1.5, masteryMultiplier);
        }

        /** 增幅弱（方向不利），并携带反应弹字反馈。 */
        public static Amplified weak(double masteryMultiplier, Feedback feedback) {
            return new Amplified(1.5, masteryMultiplier, feedback);
        }
    }

    record Additive(
            Reaction.ReactionType reactionType,
            String label,
            double bonusDamage,
            @Nullable Feedback feedback
    ) implements ReactionOutcome {

        public Additive(Reaction.ReactionType reactionType, String label, double bonusDamage) {
            this(reactionType, label, bonusDamage, null);
        }
    }
}
