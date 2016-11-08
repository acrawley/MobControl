package net.andrewcr.minecraft.plugin.MobControl.model;

import net.andrewcr.minecraft.plugin.BasePluginLib.config.ConfigurationFileBase;
import net.andrewcr.minecraft.plugin.MobControl.Plugin;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.RuleException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigStore extends ConfigurationFileBase {
    //region Private Fields

    private static final String CONFIGURATION_VERSION_KEY = "ConfigurationVersion";
    private static final String SPAWN_CONFIGS_KEY = "SpawnConfigs";

    private final Map<String, MobControlWorldConfig> worldConfigs;

    //endregion

    //region Constructor

    public ConfigStore() {
        super(Plugin.getInstance());

        this.worldConfigs = new HashMap<>();
    }

    //endregion

    //region Singleton

    public static ConfigStore getInstance() {
        return Plugin.getInstance().getConfigStore();
    }

    //endregion

    //region Serialization


    @Override
    protected String getFileName() {
        return "config.yml";
    }

    @Override
    protected void loadCore(YamlConfiguration configuration) {
        String version = configuration.getString(CONFIGURATION_VERSION_KEY);
        switch (version) {
            case "1.0":
                this.loadV1_0Config(configuration);
                return;

            default:
                Plugin.getInstance().getLogger().severe("Unknown MobControl configuration version '" + version + "'!");
        }
    }

    private void loadV1_0Config(YamlConfiguration config) {
        ConfigurationSection spawnConfigs = config.getConfigurationSection(SPAWN_CONFIGS_KEY);
        if (spawnConfigs != null) {
            for (String world : spawnConfigs.getKeys(false)) {
                this.worldConfigs.put(world, MobControlWorldConfig.loadFrom(spawnConfigs.getConfigurationSection(world)));
            }
        }

        Plugin.getInstance().getLogger().info("Loaded mob control rules for " + this.worldConfigs.size() + " world(s)!");
    }

    @Override
    protected void saveCore(YamlConfiguration configuration) {
        configuration.set(CONFIGURATION_VERSION_KEY, "1.0");

        ConfigurationSection spawnConfigs = configuration.createSection(SPAWN_CONFIGS_KEY);
        for (MobControlWorldConfig worldConfig : this.worldConfigs.values()) {
            worldConfig.save(spawnConfigs);
        }

        Plugin.getInstance().getLogger().info("Saved mob control rules for " + this.worldConfigs.size() + " world(s)!");
    }

    //endregion

    //region Public API

    public MobControlWorldConfig getWorldConfig(String worldName, boolean shouldCreate) {
        if (this.worldConfigs.containsKey(worldName)) {
            return this.worldConfigs.get(worldName);
        }

        if (shouldCreate) {
            try {
                MobControlWorldConfig newConfig = new MobControlWorldConfig(worldName);
                this.worldConfigs.put(worldName, newConfig);

                this.notifyChanged();

                return newConfig;
            } catch (RuleException ex) {
                Plugin.getInstance().getLogger().severe("Error creating default rule!");
            }
        }

        return null;
    }

    //endregion
}
