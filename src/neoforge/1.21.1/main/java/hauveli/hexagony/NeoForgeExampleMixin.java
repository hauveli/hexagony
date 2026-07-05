package hauveli.hexagony.mixin;

import hauveli.hexagony.Hexagony;
import net.minecraft.commands.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerInfo;
import net.minecraft.server.TickTask;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.chunk.storage.ChunkIOErrorReporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
abstract class NeoForgeExampleMixin extends ReentrantBlockableEventLoop<TickTask> implements ServerInfo, ChunkIOErrorReporter, CommandSource, AutoCloseable {
    public NeoForgeExampleMixin(String string) {
        super(string);
    }

    // common mixins will show errors in the IDE. see https://github.com/terrarium-earth/jvm-multiplatform/issues/10
    @Inject(method = "loadLevel", at = @At(value = "HEAD"))
    private void logOnWorldLoad(CallbackInfo ci) {
        Hexagony.LOGGER.info("MinecraftServer$loadLevel has started! (Fabric)");
    }
}