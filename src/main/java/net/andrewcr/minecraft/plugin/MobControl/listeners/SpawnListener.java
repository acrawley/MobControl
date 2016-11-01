package net.andrewcr.minecraft.plugin.MobControl.listeners;

import net.andrewcr.minecraft.plugin.BasePluginLib.util.EntityUtil;
import net.andrewcr.minecraft.plugin.MobControl.model.ConfigStore;
import net.andrewcr.minecraft.plugin.MobControl.model.MobControlWorldConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class SpawnListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!EntityUtil.isMob(event.getEntityType())) {
            // Not a mob
            return;
        }

        MobControlWorldConfig config = ConfigStore.getInstance().getWorldConfig(event.getLocation().getWorld().getName(), false);
        if (config == null) {
            // No spawn config for this world
            return;
        }

        if (config.isPlayerSpawnAllowed()) {
            // If the entity is spawning because of something the player is doing intentionally, allow it
            switch (event.getSpawnReason()) {
                case DISPENSE_EGG:
                case EGG:
                case SPAWNER_EGG:
                case BREEDING:
                case BUILD_IRONGOLEM:
                case BUILD_SNOWMAN:
                case BUILD_WITHER:
                case CURED:
                    return;
            }
        }

        if (!config.getSpawnRule().canSpawn(event.getEntityType())) {
            // We have a spawn rule that prevents this entity from spawning
            event.setCancelled(true);
        }
    }
}
