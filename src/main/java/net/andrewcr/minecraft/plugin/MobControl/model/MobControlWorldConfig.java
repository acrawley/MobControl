package net.andrewcr.minecraft.plugin.MobControl.model;

import lombok.Getter;
import lombok.Synchronized;
import net.andrewcr.minecraft.plugin.MobControl.Plugin;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.MobSpawnRule;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.RuleException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.*;
import java.util.stream.Collectors;

@SerializableAs("MobControlWorldConfig")
public class MobControlWorldConfig implements ConfigurationSerializable {
    //region Private Fields

    private final Object configLock = ConfigStore.getInstance().getSyncObj();

    @Getter
    private List<CreatureSpawnEvent.SpawnReason> alwaysAllowTypes;
    @Getter
    private MobSpawnRule spawnRule;

    //endregion

    //region Constructor

    public MobControlWorldConfig(String spawnRule, List<CreatureSpawnEvent.SpawnReason> alwaysAllowTypes) throws RuleException {
        if (alwaysAllowTypes != null) {
            this.alwaysAllowTypes = alwaysAllowTypes;
        } else {
            this.alwaysAllowTypes = new ArrayList<>();
        }

        this.spawnRule = new MobSpawnRule(spawnRule);
    }

    //endregion

    //region Serialization

    @Override
    @Synchronized("configLock")
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();

        if (this.alwaysAllowTypes.size() > 0) {
            map.put("alwaysAllow", this.alwaysAllowTypes.stream()
                .map(t -> t.name())
                .sorted()
                .collect(Collectors.joining(", ")));
        }

        map.put("rule", this.spawnRule.getRuleText());

        return map;
    }

    public static MobControlWorldConfig deserialize(Map<String, Object> data) {
        String spawnRule = null;
        List<CreatureSpawnEvent.SpawnReason> alwaysAllowTypes = null;

        if (data.containsKey("rule")) {
            spawnRule = (String) data.get("rule");
        }

        if (data.containsKey("alwaysAllow")) {
            String alwaysAllowRaw = (String) data.get("alwaysAllow");
            alwaysAllowTypes = Arrays.stream(alwaysAllowRaw.split(","))
                .map(type -> MobControlWorldConfig.getSpawnReason(type.trim()))
                .filter(r -> r != null)
                .collect(Collectors.toList());
        }

        try {
            return new MobControlWorldConfig(spawnRule, alwaysAllowTypes);
        } catch (RuleException ex) {
            Plugin.getInstance().getLogger().severe("Failed to parse spawn rule '" + spawnRule + "'!");
            Plugin.getInstance().getLogger().severe("    Error: " + ex.getMessage());
        }

        return null;
    }

    //endregion

    private static CreatureSpawnEvent.SpawnReason getSpawnReason(String spawnReason) {
        return Arrays.stream(CreatureSpawnEvent.SpawnReason.values())
            .filter(e -> e.name().equalsIgnoreCase(spawnReason))
            .findAny()
            .orElse(null);
    }

    @Synchronized("configLock")
    public void setAlwaysAllow(String spawnType, boolean value) throws RuleException {
        CreatureSpawnEvent.SpawnReason reason = MobControlWorldConfig.getSpawnReason(spawnType);
        if (reason == null) {
            throw new RuleException("Unknown spawn type '" + spawnType + "'!");
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
}
