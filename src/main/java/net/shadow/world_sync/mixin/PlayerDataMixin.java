package net.shadow.world_sync.mixin;

import com.mojang.datafixers.DataFixer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.level.storage.LevelStorage;
import net.shadow.world_sync.WorldSync;
import net.shadow.world_sync.gamerules.WorldSyncGameRules;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(WorldSaveHandler.class)
public class PlayerDataMixin{
    @Unique
    private final File playerDataDir;
    @Mutable
    @Shadow
    @Final
    protected DataFixer dataFixer;
    public PlayerDataMixin(LevelStorage.Session session, DataFixer dataFixer) {
        super();
        this.dataFixer = dataFixer;
        this.playerDataDir = session.getDirectory(WorldSavePath.PLAYERDATA).toFile();
    }
    @Inject(at = @At("HEAD"), method = "savePlayerData")
	private void savecustomdata(PlayerEntity player, CallbackInfo ci) {
        if (player.getWorld().getGameRules().getBoolean(WorldSyncGameRules.TOGGLE)) {
            FabricLoader fabric = FabricLoader.getInstance();
            File folder = fabric.getGameDirectory();

            String path = fabric.getGameDirectory().getAbsolutePath();
            WorldSync.LOGGER.info("FilePath for data is {}", path);
            WorldSync.LOGGER.info("FilePath for data is {}", player.getInventory().toString());
            try {
                NbtCompound nbtCompound = player.writeNbt(new NbtCompound());
                nbtCompound.remove("Pos");
                NbtCompound nbtCompound2 = nbtCompound;
                File file = File.createTempFile(player.getUuidAsString() + "-" + player.getWorld().getGameRules().getInt(WorldSyncGameRules.TUNE), ".dat", folder);
                NbtIo.writeCompressed(nbtCompound2, file);
                File file2 = new File(folder, player.getUuidAsString() + "-" + player.getWorld().getGameRules().getInt(WorldSyncGameRules.TUNE) + ".dat");
                File file3 = new File(folder, player.getUuidAsString() + "-" + player.getWorld().getGameRules().getInt(WorldSyncGameRules.TUNE) + ".dat_old");
                Util.backupAndReplace(file2, file, file3);
            } catch (Exception exception) {
                WorldSync.LOGGER.warn("Failed to save player data for {}", player.getName().getString());
            }
            // This code is injected into the start of MinecraftServer.loadWorld()V
        }
    }

    @Inject(at = @At("HEAD"), method = "loadPlayerData", cancellable = true)
    private void loadcustomdata(PlayerEntity player, CallbackInfoReturnable<NbtCompound> cir) {
        if (player.getWorld().getGameRules().getBoolean(WorldSyncGameRules.TOGGLE)) {

            FabricLoader fabric = FabricLoader.getInstance();
            File folder = fabric.getGameDirectory();

            String path = fabric.getGameDirectory().getAbsolutePath();
            NbtCompound nbtCompound = null;
            NbtCompound nbtCompound2 = null;

            try {
                File file = new File(folder, player.getUuidAsString() + "-" + player.getWorld().getGameRules().getInt(WorldSyncGameRules.TUNE) + ".dat_old");
                if (file.exists() && file.isFile()) {
                    nbtCompound = NbtIo.readCompressed(file);
                }
            } catch (Exception exception) {
                WorldSync.LOGGER.warn("Failed to load player data for {}", player.getName().getString());
            }
            if (nbtCompound != null) {
                try {
                    File file2 = new File(this.playerDataDir, player.getUuidAsString() + ".dat");
                    if (file2.exists() && file2.isFile()) {
                        nbtCompound2 = NbtIo.readCompressed(file2);
                    }
                } catch (Exception exception) {
                    WorldSync.LOGGER.warn("Failed to load world player data for {}", player.getName().getString());
                }

                if (nbtCompound2 != null) {
                    nbtCompound2.put("Inventory", nbtCompound.get("Inventory"));

                    int i = NbtHelper.getDataVersion(nbtCompound2, -1);

                    player.readNbt(DataFixTypes.PLAYER.update(this.dataFixer, nbtCompound2, i));
                    cir.setReturnValue(nbtCompound2);

                    if ((player.getX() >= -0.5 || player.getX() <= -0.5) && (player.getZ() >= -0.5 || player.getZ() <= -0.5) && (player.getY() <= 0.5)) {
                        player.setPosition(player.getWorld().getSpawnPos().getX(), player.getWorld().getSpawnPos().getY(), player.getWorld().getSpawnPos().getZ());
                    }
                    return;
                }
                int i = NbtHelper.getDataVersion(nbtCompound, -1);

                player.readNbt(DataFixTypes.PLAYER.update(this.dataFixer, nbtCompound, i));

                if ((player.getX() >= -0.5 || player.getX() <= -0.5) && (player.getZ() >= -0.5 || player.getZ() <= -0.5) && (player.getY() <= 0.5)) {
                    player.setPosition(player.getWorld().getSpawnPos().getX(), player.getWorld().getSpawnPos().getY(), player.getWorld().getSpawnPos().getZ());
                }
                cir.setReturnValue(nbtCompound);
            }
        }
    }
}