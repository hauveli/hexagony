package hauveli.hexagony.fabric.xplat.cc
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob

class CCBrainswept(owner: LivingEntity?) : Component, AutoSyncedComponent {
    private val owner: LivingEntity?

    var brainswept: Boolean = false
        set(value) {
            field = value
            HexagonyCardinalComponents.BRAINSWEPT.sync(owner)
        }

    init {
        this.owner = owner
    }

    override fun applySyncPacket(buf: FriendlyByteBuf?) {
        super.applySyncPacket(buf)
        if (owner is Mob && brainswept) owner.removeFreeWill()
    }

    override fun readFromNbt(tag: CompoundTag) {
        this.brainswept = tag.getBoolean(TAG_BRAINSWEPT)
    }

    override fun writeToNbt(tag: CompoundTag) {
        tag.putBoolean(TAG_BRAINSWEPT, this.brainswept)
    }

    companion object {
        const val TAG_BRAINSWEPT: String = "brainswept"
    }
}