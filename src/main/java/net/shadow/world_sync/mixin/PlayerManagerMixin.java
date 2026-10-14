package net.shadow.world_sync.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.WorldSaveHandler;
import net.shadow.world_sync.WorldSync;
import net.shadow.world_sync.gamerules.WorldSyncGameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    private final MinecraftServer server;

    private final WorldSaveHandler saveHandler;
    protected PlayerManagerMixin(MinecraftServer server, WorldSaveHandler saveHandler) {
        this.server = server;
        this.saveHandler = saveHandler;
    }

    @Inject(at = @At("TAIL"), method = "loadPlayerData", cancellable = true)
	private void init(ServerPlayerEntity player, CallbackInfoReturnable<NbtCompound> cir) {
        FabricLoader fabric = FabricLoader.getInstance();
        File folder = fabric.getGameDirectory();

        String path = fabric.getGameDirectory().getAbsolutePath();
        NbtCompound nbtCompound3 = null;

        try {
            File file = new File(folder, player.getUuidAsString() + "-" + player.getWorld().getGameRules().getInt(WorldSyncGameRules.TUNE) + ".dat_old");
            if (file.exists() && file.isFile()) {
                nbtCompound3 = NbtIo.readCompressed(file);
            }
        } catch (Exception exception) {
            WorldSync.LOGGER.warn("Failed to load player data for {}", player.getName().getString());
        }

        if(nbtCompound3 != null) {


            NbtCompound nbtCompound = this.server.getSaveProperties().getPlayerData();
            NbtCompound nbtCompound2;
            if (this.server.isHost(player.getGameProfile()) && nbtCompound != null) {
                nbtCompound2 = nbtCompound;
                nbtCompound2.put("Inventory", nbtCompound3.get("Inventory"));



                player.readNbt(nbtCompound2);

                WorldSync.LOGGER.debug("loading synced single player");
            } else {
                nbtCompound2 = this.saveHandler.loadPlayerData(player);
            }

            cir.setReturnValue(nbtCompound2);
            return;
        }
	}
}