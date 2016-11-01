package net.andrewcr.minecraft.plugin.MobControl.commands;

import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandExecutorBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.EntityUtil;
import net.andrewcr.minecraft.plugin.MobControl.Constants;
import net.andrewcr.minecraft.plugin.MobControl.model.ConfigStore;
import net.andrewcr.minecraft.plugin.MobControl.model.MobControlWorldConfig;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.MobSpawnRule;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MobControlInfoCommand extends CommandBase {
    @Override
    public CommandExecutorBase getExecutor() {
        return new MobControlInfoCommandExecutor();
    }

    private class MobControlInfoCommandExecutor extends CommandExecutorBase {
        public MobControlInfoCommandExecutor() {
            super("mobcontrol info", Constants.InfoPermission);
        }

        @Override
        protected boolean invoke(String[] args) {
            if (args.length != 1) {
                return false;
            }

            World world = Bukkit.getWorld(args[0]);
            if (world == null) {
                this.error("Unknown world '" + args[0] + "'!");
                return false;
            }

            MobControlWorldConfig config = ConfigStore.getInstance().getWorldConfig(world.getName(), false);
            if (config == null) {
                this.sendMessage("No spawn rules configured for world '" + world.getName() + "'!");
            }

            this.sendMessage("World '" + world.getName() + "' configuration:");
            this.sendMessage("  Player-initiated spawns override rules: " + (config.isPlayerSpawnAllowed() ? "yes" : "no"));
            this.sendMessage("  Spawn rule: " + config.getSpawnRule().getRuleText());
            this.sendMessage("  Allowed mobs: " + this.getMobs(config.getSpawnRule(), true));
            this.sendMessage("  Denied mobs: " + this.getMobs(config.getSpawnRule(), false));

            return true;
        }

        private String getMobs(MobSpawnRule rule, boolean allowed) {
            return Arrays.stream(EntityType.values())
                .filter(e -> EntityUtil.isMob(e) && rule.canSpawn(e) == allowed)
                .map(e -> e.name())
                .collect(Collectors.joining(", "));
        }
    }
}
