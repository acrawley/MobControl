package net.andrewcr.minecraft.plugin.MobControl.commands;

import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandExecutorBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.ArrayUtil;
import net.andrewcr.minecraft.plugin.MobControl.Constants;
import net.andrewcr.minecraft.plugin.MobControl.model.ConfigStore;
import net.andrewcr.minecraft.plugin.MobControl.model.MobControlWorldConfig;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.InvalidSpawnTypeException;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.RuleException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

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
                case "alwaysallow":
                    return this.setAlwaysAllow(world, propertyArgs);

                case "rule":
                    return this.setRule(world, propertyArgs);
            }

            this.error("Unknown property '" + args[1] + "'!");
            return false;
        }

        private boolean setAlwaysAllow(World world, String[] args) {
            if (args.length == 0) {
                return false;
            }

            MobControlWorldConfig config = ConfigStore.getInstance().getWorldConfig(world.getName(), true);

            for (String arg : args) {
                char first = arg.charAt(0);
                String reason = arg.substring(1).toUpperCase(Locale.ENGLISH);

                try {
                    config.setAlwaysAllow(reason, first == '+');
                } catch (RuleException ex) {
                    this.error("Error setting spawn type overrides: " + ex.getMessage());

                    if (ex instanceof InvalidSpawnTypeException) {
                        this.error("Try one of: " + Arrays.stream(CreatureSpawnEvent.SpawnReason.values())
                            .map(Enum::name)
                            .collect(Collectors.joining(", ")));
                    }

                    return false;
                }
            }

            this.sendMessage("Spawn types always allowed in world '" + world.getName() + "': "
                + config.getAlwaysAllowTypes().stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(", ")));

            return true;
        }

        private boolean setRule(World world, String[] args) {
            if (args.length == 0) {
                return false;
            }

            String rule = String.join(" ", (CharSequence[]) args);

            MobControlWorldConfig config = ConfigStore.getInstance().getWorldConfig(world.getName(), true);

            try {
                config.getSpawnRule().setRuleText(rule);
            } catch (RuleException ex) {
                this.error("Error setting rule: " + ex.getMessage());
                return false;
            }

            this.sendMessage("Spawn rule for world '" + world.getName() + "' changed to '" + config.getSpawnRule().getRuleText() + "'!");

            return true;
        }
    }
}
