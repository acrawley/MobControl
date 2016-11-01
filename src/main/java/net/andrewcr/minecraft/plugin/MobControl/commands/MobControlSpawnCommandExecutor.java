package net.andrewcr.minecraft.plugin.MobControl.commands;

import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandExecutorBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.EntityUtil;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.StringUtil;
import net.andrewcr.minecraft.plugin.MobControl.Constants;
import net.andrewcr.minecraft.plugin.MobControl.model.ConfigStore;
import net.andrewcr.minecraft.plugin.MobControl.model.MobControlWorldConfig;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.RuleException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

abstract class MobControlSpawnCommandExecutor extends CommandExecutorBase {
    public MobControlSpawnCommandExecutor(String name) {
        super(name, Constants.ConfigurePermission);
    }

    protected abstract boolean isAllow();

    @Override
    protected final boolean invoke(String[] args) {
        if (args.length != 2) {
            return false;
        }

        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            this.error("Unknown world '" + args[0] + "'!");
            return false;
        }

        String prefix;

        if (StringUtil.equalsIgnoreCase(args[1], "ALL")) {
            prefix = "All mobs are";
        } else if (StringUtil.equalsIgnoreCase(args[1], "FRIENDLY")) {
            prefix = "Friendly mobs are";
        } else if (StringUtil.equalsIgnoreCase(args[1], "HOSTILE")) {
            prefix = "Hostile mobs are";
        } else {
            EntityType entityType = EntityUtil.tryGetEntityTypeByName(args[1]);
            if (entityType == null) {
                this.error("Unknown entity type '" + args[1] + "'!");
                return false;
            }

            if (!EntityUtil.isMob(entityType)) {
                this.error("Entity '" + args[1] + "' is not a mob!");
                return false;
            }

            prefix = "Mob '" + args[1] + "' is";
        }

        MobControlWorldConfig config = ConfigStore.getInstance().getWorldConfig(world.getName(), true);
        try {
            config.getSpawnRule().setCanSpawn(args[1], this.isAllow());
        } catch (RuleException ex) {
            this.error("Failed to change spawn settings: " + ex.getMessage());
            return false;
        }

        if (this.isAllow()) {
            this.sendMessage(prefix + " allowed to spawn in world '" + world.getName() + "'!");
        } else {
            this.sendMessage(prefix + " not allowed to spawn in world '" + world.getName() + "'!");
        }

        return true;
    }
}
