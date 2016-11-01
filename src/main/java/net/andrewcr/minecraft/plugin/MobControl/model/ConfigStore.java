package net.andrewcr.minecraft.plugin.MobControl.model;

import net.andrewcr.minecraft.plugin.BasePluginLib.config.ConfigurationFileBase;
import net.andrewcr.minecraft.plugin.MobControl.Plugin;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.RuleException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigStore extends ConfigurationFileBase {
    //region Private Fields

    private Map<String, MobControlWorldConfig> worldConfigs;

    //endregion

    //region Constructor

    public ConfigStore() {
        super(Plugin.getInstance());

        this.worldConfigs = new HashMap<>();

        ConfigurationSerialization.registerClass(MobControlWorldConfig.class);
    }

    //endregion

    //region Singleton

    public static ConfigStore getInstance() {
        return Plugin.getInstance().getConfigStore();
    }

    //endregion

    //region Serialization

    @Override
    protected void loadCore() {
        File configFile = new File(Plugin.getInstance().getDataFolder(), "config.yml");
        if (configFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            String version = config.getString("ConfigurationVersion");

            switch (version) {
                case "1.0":
                    this.loadV1_0Config(config);
                    return;

                default:
                    Plugin.getInstance().getLogger().severe("Unknown MobControl configuration version '" + version + "'!");
            }
        }
    }

    private void loadV1_0Config(YamlConfiguration config) {
        ConfigurationSection configs = config.getConfigurationSection("spawnConfigs");
        if (configs != null) {
            for (String world : configs.getKeys(false)) {
                this.worldConfigs.put(world, (MobControlWorldConfig) configs.get(world));
            }
        }
    }

    @Override
    protected void saveCore() {
        File portalFile = new File(Plugin.getInstance().getDataFolder(), "config.yml");

        YamlConfiguration config = new YamlConfiguration();
        config.set("ConfigurationVersion", "1.0");

        config.set("spawnConfigs", this.worldConfigs);

        try {
            config.save(portalFile);
        } catch (IOException e) {
            Plugin.getInstance().getLogger().severe("Failed to save MobControl configuration: " + e.toString());
        }
    }

    //endregion

    //region Public API

    public MobControlWorldConfig getWorldConfig(String worldName, boolean shouldCreate) {
        if (this.worldConfigs.containsKey(worldName)) {
            return this.worldConfigs.get(worldName);
        }

        if (shouldCreate) {
            try {
                MobControlWorldConfig newConfig = new MobControlWorldConfig("+ALL", true);
                this.worldConfigs.put(worldName, newConfig);

                return newConfig;
            } catch (RuleException ex) {
                Plugin.getInstance().getLogger().severe("Error creating default rule!");
            }
        }

        return null;
    }

    //endregion
}
