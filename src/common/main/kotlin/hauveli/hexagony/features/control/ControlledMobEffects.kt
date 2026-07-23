package hauveli.hexagony.features.control

import hauveli.hexagony.Hexagony
import hauveli.hexagony.registry.HexagonyMobEffects.make
import hauveli.hexagony.registry.HexagonyMobEffects.register
import hauveli.hexagony.registry.HexagonyRegistrar
import net.minecraft.util.datafix.fixes.MobEffectIdFix
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity

// note to future confused self:
// These are registered by HexagonyMobEffects, (import hauveli.hexagony.registry.HexagonyMobEffects.make)
object ControlledMobEffects {
    fun init() {}

    data class ControlledMobEffectPair(
        val real: HexagonyRegistrar<MobEffect>.Entry<ControlledMobEffect>,
        val fake: HexagonyRegistrar<MobEffect>.Entry<ControlledMobEffect>
    )

    // a real connected player will need to have a different effect applied. this was the simplest
    // most performant option. If I think of a better option later I can just rename the list name
    @JvmField
    val REAL_PLAYER_EFFECTS = mutableListOf<HexagonyRegistrar<MobEffect>.Entry<out MobEffect>>()
    @JvmField
    val FAKE_PLAYER_EFFECTS = mutableListOf<HexagonyRegistrar<MobEffect>.Entry<out MobEffect>>()

    val WALK_FORWARD = makeControlEffect(
        "forward",
        FakePlayerActions::walkForward, // todo: change to RealPlayerAction::walkForward
        FakePlayerActions::walkForward,
        FakePlayerActions::stopWalkingForwardsBackwards,
        FakePlayerActions::stopWalkingForwardsBackwards
    )

    val WALK_BACKWARD = makeControlEffect(
        "backward",
        FakePlayerActions::walkBackward,
        FakePlayerActions::walkBackward,
        FakePlayerActions::stopWalkingForwardsBackwards,
        FakePlayerActions::stopWalkingForwardsBackwards
    )

    val WALK_LEFT = makeControlEffect(
        "left",
        FakePlayerActions::walkLeft,
        FakePlayerActions::walkLeft,
        FakePlayerActions::stopWalkingLeftRight,
        FakePlayerActions::stopWalkingLeftRight
    )

    val WALK_RIGHT = makeControlEffect(
        "right",
        FakePlayerActions::walkRight,
        FakePlayerActions::walkRight,
        FakePlayerActions::stopWalkingLeftRight,
        FakePlayerActions::stopWalkingLeftRight
    )

    val SPRINT = makeControlEffect(
        "sprint",
        FakePlayerActions::sprint,
        FakePlayerActions::sprint,
        FakePlayerActions::stopSprinting,
        FakePlayerActions::stopSprinting
    )

    val SNEAK = makeControlEffect(
        "sneak",
        FakePlayerActions::sneak,
        FakePlayerActions::sneak,
        FakePlayerActions::stopSneaking,
        FakePlayerActions::stopSneaking
    )

    val JUMP = makeControlEffect(
        "jump",
        FakePlayerActions::jump,
        FakePlayerActions::jump,
        FakePlayerActions::stopJumping,
        FakePlayerActions::stopJumping
    )

    val SWAP_HANDS = makeControlEffect(
        "swap_hands",
        FakePlayerActions::swapHands,
        FakePlayerActions::swapHands
    )

    val ATTACK = makeControlEffect(
        "attack",
        FakePlayerActions::attack,
        FakePlayerActions::attack
    )

    val USE = makeControlEffect(
        "use",
        FakePlayerActions::use,
        FakePlayerActions::use
    )

    // where do I store the x and y rot?
    // can I store more information in the effect itself?
    // if so, I would need to pass additional arguments into the effect, somehow...
    val LOOK = makeControlEffect(
        "look",
        FakePlayerActions::look,
        FakePlayerActions::look,
        amplifierIsNotInterval = true
    )

    val HOTBAR_SLOT = makeControlEffect(
        "hotbar_slot",
        FakePlayerActions::hotbarSlot,
        FakePlayerActions::hotbarSlot,
        amplifierIsNotInterval = true
    )

    val DROP = makeControlEffect(
        "drop",
        FakePlayerActions::drop,
        FakePlayerActions::drop,
        amplifierIsNotInterval = true
    )

    fun makeControlEffect(
        name: String,
        realAction: (LivingEntity, Int) -> Unit,
        fakeAction: (LivingEntity, Int) -> Unit,
        realAbort: (LivingEntity) -> Unit = { },
        fakeAbort: (LivingEntity) -> Unit = { },
        amplifierIsNotInterval: Boolean = false
    ): ControlledMobEffectPair {
        val real = make("control/real/$name") {
            ControlledMobEffect(realAction, realAbort, amplifierIsNotInterval, 0)
        }

        val fake = make("control/fake/$name") {
            ControlledMobEffect(fakeAction, fakeAbort, amplifierIsNotInterval, 0)
        }

        REAL_PLAYER_EFFECTS += real
        FAKE_PLAYER_EFFECTS += fake

        return ControlledMobEffectPair(real, fake)
    }

}