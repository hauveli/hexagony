package hauveli.hexagony.common.recipe.ingredient.brainsweep
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.BrainsweepeeIngredient
import com.google.gson.JsonObject
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.GsonHelper
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.entity.npc.VillagerProfession
import net.minecraft.world.entity.npc.VillagerType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import java.util.*
import kotlin.math.min


/**
 * Special case for villagers so we can have biome/profession/level reqs
 */
class EntityIngredient (
    entity: LivingEntity?,
    minLevel: Int
) : BrainsweepeeIngredient() {
    val entity: LivingEntity?
    val minLevel: Int

    init {
        this.entity = entity
        this.minLevel = minLevel
    }

    override fun test(entity: Entity?, level: ServerLevel?): Boolean {
        if (entity !is LivingEntity) return false

        // TODO: uhhh check if entity has experience or levels?
        return this.entity == null
    }

    override fun getTooltip(advanced: Boolean): MutableList<Component?> {
        val tooltip: MutableList<Component?> = ArrayList<Component?>()
        tooltip.add(this.getName())

        if (advanced) {
            if (minLevel >= 5) {
                tooltip.add(
                    Component.translatable("hexcasting.tooltip.brainsweep.level", 5)
                        .withStyle(ChatFormatting.DARK_GRAY)
                )
            } else if (minLevel > 1) {
                tooltip.add(
                    Component.translatable("hexcasting.tooltip.brainsweep.min_level", minLevel)
                        .withStyle(ChatFormatting.DARK_GRAY)
                )
            }
        }

        tooltip.add(
            getModNameComponent("minecraft")
        )

        return tooltip
    }

    override fun getName(): Component {
        val component = Component.literal("")

        var addedAny = false

        if (minLevel >= 5) {
            component.append(Component.translatable("merchant.level.5"))
            addedAny = true
        } else {
            component.append(Component.translatable("merchant.level." + minLevel))
            addedAny = true
        }
        component.append(" ")
        component.append(Component.translatable("entity.minecraft.player"))
        component.append(" ")
        component.append(EntityType.PLAYER.getDescription())

        return component
    }

    override fun serialize(): JsonObject {
        val obj = JsonObject()
        obj.addProperty("type", EntityType.PLAYER.toString())
        obj.addProperty("minLevel", this.minLevel)
        return obj
    }

    override fun write(buf: FriendlyByteBuf) {
        if (this.profession != null) {
            buf.writeVarInt(1)
            buf.writeVarInt(BuiltInRegistries.VILLAGER_PROFESSION.getId(this.profession))
        } else {
            buf.writeVarInt(0)
        }
        if (this.biome != null) {
            buf.writeVarInt(1)
            buf.writeVarInt(BuiltInRegistries.VILLAGER_TYPE.getId(this.biome))
        } else {
            buf.writeVarInt(0)
        }
        buf.writeInt(this.minLevel)
    }

    override fun ingrType(): Type {
        return Type.VILLAGER
    }

    override fun getSomeKindOfReasonableIDForEmi(): String {
        val bob = StringBuilder()
        if (this.profession != null) {
            val profLoc = BuiltInRegistries.VILLAGER_PROFESSION.getKey(this.profession)
            bob.append(profLoc.getNamespace())
                .append("//")
                .append(profLoc.getPath())
        } else {
            bob.append("null")
        }
        bob.append("_")
        if (this.biome != null) {
            val biomeLoc = BuiltInRegistries.VILLAGER_TYPE.getKey(this.biome)
            bob.append(biomeLoc.getNamespace())
                .append("//")
                .append(biomeLoc.getPath())
        } else {
            bob.append("null")
        }

        bob.append(this.minLevel)
        return bob.toString()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as VillagerIngredient
        return minLevel == that.minLevel && profession == that.profession && biome == that.biome
    }

    override fun hashCode(): Int {
        return Objects.hash(profession, biome, minLevel)
    }

    companion object {
        fun deserialize(json: JsonObject): VillagerIngredient {
            var profession: VillagerProfession? = null
            if (json.has("profession") && !json.get("profession").isJsonNull()) {
                profession = BuiltInRegistries.VILLAGER_PROFESSION.get(
                    ResourceLocation(
                        GsonHelper.getAsString(
                            json,
                            "profession"
                        )
                    )
                )
            }
            var biome: VillagerType? = null
            if (json.has("biome") && !json.get("biome").isJsonNull()) {
                biome = BuiltInRegistries.VILLAGER_TYPE.get(ResourceLocation(GsonHelper.getAsString(json, "biome")))
            }
            val minLevel = GsonHelper.getAsInt(json, "minLevel")
            return VillagerIngredient(profession, biome, minLevel)
        }

        fun read(buf: FriendlyByteBuf): VillagerIngredient {
            var profession: VillagerProfession? = null
            val hasProfession = buf.readVarInt()
            if (hasProfession != 0) {
                profession = BuiltInRegistries.VILLAGER_PROFESSION.byId(buf.readVarInt())
            }
            var biome: VillagerType? = null
            val hasBiome = buf.readVarInt()
            if (hasBiome != 0) {
                biome = BuiltInRegistries.VILLAGER_TYPE.byId(buf.readVarInt())
            }
            val minLevel = buf.readInt()
            return VillagerIngredient(profession, biome, minLevel)
        }
    }
}