package net.andrewcr.minecraft.plugin.MobControl.commands;

import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandExecutorBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.ArrayUtil;
import net.andrewcr.minecraft.plugin.MobControl.Constants;
import net.andrewcr.minecraft.plugin.MobControl.model.ConfigStore;
import net.andrewcr.minecraft.plugin.MobControl.model.MobControlWorldConfig;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.RuleException;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Locale;

public class MobControlSetCommand extends CommandBase {
    @Override
    public CommandExecutorBase getExecutor() {
        return new MobControlSetExecutor();
    }

    private class MobControlSetExecutor extends CommandExecutorBase {
        public MobControlSetExecutor() {
            super("mobcontrol set", Constants.ConfigurePermission);
        }

        @Override
        protected boolean invoke(String[] args) {
            if (args.length < 2) {
                return false;
            }

            World world = Bukkit.getWorld(args[0]);
            if (world == null) {
                this.error("Unknown world '" + args[0] + "'!");
                return false;
            }

            String property = args[1].toLowerCase(Locale.ENGLISH);
            String[] propertyArgs = ArrayUtil.removeFirst(args, 2);
            switch (property) {
                case "allowplayerspawn":
                    return this.setAllowPlayerSpawn(world, propertyArgs);

                case "rule":
                    return this.setRule(world, propertyArgs);
            }

            this.error("Unknown property '" + args[1] + "'!");
            return false;
        }

        private boolean setAllowPlayerSpawn(World world, String[] args) {
            if (args.length != 1) {
                return false;
            }

            boolean value;
            try {
                value = Boolean.parseBoolean(args[0]);
            } catch (NumberFormatException ex) {
                this.error("Cannot parse '" + args[0] + "' as a boolean!");
                return false;
            }

            MobControlWorldConfig config = ConfigStore.getInstance().getWorldConfig(world.getName(), true);
            config.setPlayerSpawnAllowed(value);

            if (value) {
                this.sendMessage("Player-spawned mobs are always allowed in world '" + world.getName() + "'!");
            } else {
                this.sendMessage("Player-spawned mobs are subject to normal rules in world '" + world.getName() + "'!");
            }

            return true;
        }

        private boolean setRule(World world, String[] args) {
            if (args.length == 0) {
                return false;
            }

            String rule = String.join(" ", args);

            MobControlWorldConfig config = ConfigStore.getInstance().getWorldConfig(world.getName(), true);

            try {
                config.getSpawnRule().setRuleText(rule);
            } catch (RuleException ex) {
                this.error(ex.getMessage());
                return false;
            }

            this.sendMessage("Spawn rule for world '" + world.getName() + "' changed to '" + config.getSpawnRule().getRuleText() + "'!");

            return true;
        }
    }
}
