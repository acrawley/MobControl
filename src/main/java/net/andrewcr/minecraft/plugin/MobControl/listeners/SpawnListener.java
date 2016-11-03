package net.andrewcr.minecraft.plugin.MobControl.listeners;

import net.andrewcr.minecraft.plugin.BasePluginLib.util.EntityUtil;
import net.andrewcr.minecraft.plugin.MobControl.model.ConfigStore;
import net.andrewcr.minecraft.plugin.MobControl.model.MobControlWorldConfig;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;

public class SpawnListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!this.allowEntitySpawn(event.getEntityType(), event.getLocation().getWorld(), event.getSpawnReason())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (!this.allowEntitySpawn(event.getEntityType(), event.getLocation().getWorld(), CreatureSpawnEvent.SpawnReason.SPAWNER)) {
            event.setCancelled(true);
        }
    }

    private boolean allowEntitySpawn(EntityType entityType, World world, CreatureSpawnEvent.SpawnReason reason) {
        if (!EntityUtil.isMob(entityType)) {
            // Not a mob
            return true;
        }

        MobControlWorldConfig config = ConfigStore.getInstance().getWorldConfig(world.getName(), false);
        if (config == null) {
            // No spawn config for this world
            return true;
        }

        if (config.getAlwaysAllowTypes().contains(reason)) {
            // Config specifically allows spawns of this type
            return true;
        }

        return config.getSpawnRule().canSpawn(entityType);
    }
}
