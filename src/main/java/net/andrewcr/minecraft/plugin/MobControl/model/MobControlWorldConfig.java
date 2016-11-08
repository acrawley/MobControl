package net.andrewcr.minecraft.plugin.MobControl.model;

import lombok.Getter;
import lombok.Synchronized;
import net.andrewcr.minecraft.plugin.MobControl.Plugin;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.InvalidSpawnTypeException;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.MobSpawnRule;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.RuleException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MobControlWorldConfig {
    //region Private Fields

    private static final String ALWAYS_ALLOW_KEY = "AlwaysAllow";
    private static final String RULE_KEY = "Rule";

    private final Object configLock = ConfigStore.getInstance().getSyncObj();

    @Getter private List<CreatureSpawnEvent.SpawnReason> alwaysAllowTypes;
    @Getter private MobSpawnRule spawnRule;
    private final String worldName;

    //endregion

    //region Constructor

    MobControlWorldConfig(String worldName) throws RuleException {
        this(worldName, "+ALL", null);
    }

    private MobControlWorldConfig(String worldName, String spawnRule, List<CreatureSpawnEvent.SpawnReason> alwaysAllowTypes) throws RuleException {
        this.worldName = worldName;

        if (alwaysAllowTypes != null) {
            this.alwaysAllowTypes = alwaysAllowTypes;
        } else {
            this.alwaysAllowTypes = new ArrayList<>();
        }

        this.spawnRule = new MobSpawnRule(spawnRule);
    }

    //endregion

    //region Serialization

    public static MobControlWorldConfig loadFrom(ConfigurationSection spawnConfig) {
        String spawnRule = null;
        List<CreatureSpawnEvent.SpawnReason> alwaysAllowTypes = null;

        if (spawnConfig.contains(RULE_KEY)) {
            spawnRule = spawnConfig.getString(RULE_KEY);
        }

        if (spawnConfig.contains(ALWAYS_ALLOW_KEY)) {
            String alwaysAllowRaw = spawnConfig.getString(ALWAYS_ALLOW_KEY);
            alwaysAllowTypes = Arrays.stream(alwaysAllowRaw.split(","))
                .map(type -> MobControlWorldConfig.getSpawnReason(type.trim()))
                .filter(r -> r != null)
                .collect(Collectors.toList());
        }

        try {
            return new MobControlWorldConfig(spawnConfig.getName(), spawnRule, alwaysAllowTypes);
        } catch (RuleException ex) {
            Plugin.getInstance().getLogger().severe("Failed to parse spawn rule '" + spawnRule + "'!");
            Plugin.getInstance().getLogger().severe("    Error: " + ex.getMessage());
        }

        return null;
    }

    public void save(ConfigurationSection spawnConfigs) {
        ConfigurationSection spawnConfig = spawnConfigs.createSection(this.worldName);

        if (this.alwaysAllowTypes.size() > 0) {
            spawnConfig.set(ALWAYS_ALLOW_KEY, this.alwaysAllowTypes.stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(", ")));
        }

        spawnConfig.set(RULE_KEY, this.spawnRule.getRuleText());
    }

    //endregion

    @Synchronized("configLock")
    public void setAlwaysAllow(String spawnType, boolean value) throws RuleException {
        CreatureSpawnEvent.SpawnReason reason = MobControlWorldConfig.getSpawnReason(spawnType);
        if (reason == null) {
            throw new InvalidSpawnTypeException("Unknown spawn type '" + spawnType + "'!");
        }

        if (value) {
            if (!this.alwaysAllowTypes.contains(reason)) {
                this.alwaysAllowTypes.add(reason);
            }
        } else {
            this.alwaysAllowTypes.remove(reason);
        }

        ConfigStore.getInstance().notifyChanged();
    }

    private static CreatureSpawnEvent.SpawnReason getSpawnReason(String spawnReason) {
        return Arrays.stream(CreatureSpawnEvent.SpawnReason.values())
            .filter(e -> e.name().equalsIgnoreCase(spawnReason))
            .findAny()
            .orElse(null);
    }
}
