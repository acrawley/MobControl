package net.andrewcr.minecraft.plugin.MobControl.commands;

import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandExecutorBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.EntityUtil;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.StringUtil;
import net.andrewcr.minecraft.plugin.MobControl.Constants;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.InvalidMobTypeException;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.RuleClause;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.RuleException;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MobControlZapCommand extends CommandBase {
    @Override
    public CommandExecutorBase getExecutor() {
        return new MobControlZapCommandExecutor();
    }

    private class MobControlZapCommandExecutor extends CommandExecutorBase {
        public MobControlZapCommandExecutor() {
            super("mobcontrol zap");
        }

        @Override
        protected boolean allowConsole() {
            return false;
        }

        @Override
        protected boolean invoke(String[] args) {
            if (args.length != 2) {
                return false;
            }

            RuleClause clause;
            try {
                clause = RuleClause.fromText("+" + args[0]);
            } catch (RuleException ex) {
                this.error("Error: " + ex.getMessage());

                if (ex instanceof InvalidMobTypeException) {
                    this.error("Try one of: " + Arrays.stream(EntityType.values())
                        .filter(EntityUtil::isMob)
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")));
                }

                return false;
            }

            int radius = 0;
            if (!StringUtil.equalsIgnoreCase("all", args[1])) {
                try {
                    radius = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex) {
                    this.error("Error: Cannot parse '" + args[1] + "' as an integer!");
                    return false;
                }
            }

            Iterable<? extends Entity> entities;
            if (radius == 0) {
                if (!this.hasPermission(Constants.ZapAllPermission)) {
                    this.error("Error: You do not have permission to zap all entities in a world!");
                    return false;
                }

                entities = this.getPlayer().getWorld().getLivingEntities();
            } else {
                if (!this.hasPermission(Constants.ZapRadiusPermission)) {
                    this.error("Error: You do not have permission to zap entities!");
                    return false;
                }

                entities = this.getPlayer().getNearbyEntities(radius, radius, radius);
            }

            radius = radius * radius;
            Location playerLoc = getPlayer().getLocation();

            ArrayList<Location> killedEntityLocations = new ArrayList<>();

            for (Entity entity : entities) {
                if (!(entity instanceof LivingEntity)) {
                    // Not a living entity
                    continue;
                }

                if (radius != 0 && entity.getLocation().distanceSquared(playerLoc) > radius) {
                    // Entity is out of range
                    continue;
                }

                LivingEntity livingEntity = (LivingEntity) entity;

                Boolean match = clause.allowsSpawn(livingEntity.getType());
                if (match == null || !match) {
                    // Doesn't match clause
                    continue;
                }

                // Kill the entity
                livingEntity.setHealth(0);
                killedEntityLocations.add(livingEntity.getLocation());
            }

            // If we didn't kill too many entities, strike them with lightning for grins
            if (killedEntityLocations.size() < 200) {
                for (Location loc : killedEntityLocations) {
                    loc.getWorld().strikeLightningEffect(loc);
                }
            }

            this.sendMessage("Killed " + killedEntityLocations.size() + " mobs!");

            return true;
        }
    }
}
