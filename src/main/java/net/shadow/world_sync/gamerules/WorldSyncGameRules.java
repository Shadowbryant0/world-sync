package net.shadow.world_sync.gamerules;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.mixin.gamerule.GameRulesAccessor;
import net.minecraft.world.GameRules;
import net.shadow.world_sync.WorldSync;

public class WorldSyncGameRules {

    public static final GameRules.Key<GameRules.BooleanRule> TOGGLE =
            GameRulesAccessor.callRegister("worldsync", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.IntRule> TUNE =
            GameRulesAccessor.callRegister("worldsync_tune", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(1));

    public static void register(){
        WorldSync.LOGGER.info("Registering gamerules for" + WorldSync.MOD_ID);
    }
}
