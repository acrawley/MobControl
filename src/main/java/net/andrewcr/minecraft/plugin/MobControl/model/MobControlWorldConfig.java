package net.andrewcr.minecraft.plugin.MobControl.model;

import lombok.Getter;
import lombok.Synchronized;
import net.andrewcr.minecraft.plugin.MobControl.Plugin;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.MobSpawnRule;
import net.andrewcr.minecraft.plugin.MobControl.model.rules.RuleException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.LinkedHashMap;
import java.util.Map;

@SerializableAs("MobControlWorldConfig")
public class MobControlWorldConfig implements ConfigurationSerializable {
    //region Private Fields

    private final Object configLock = ConfigStore.getInstance().getSyncObj();

    @Getter
    private boolean isPlayerSpawnAllowed;
    @Getter
    private MobSpawnRule spawnRule;

    //endregion

    //region Constructor

    public MobControlWorldConfig(String spawnRule, boolean isPlayerSpawnAllowed) throws RuleException {
        this.isPlayerSpawnAllowed = isPlayerSpawnAllowed;
        this.spawnRule = new MobSpawnRule(spawnRule);
    }

    //endregion

    //region Serialization

    @Override
    @Synchronized("configLock")
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("allowPlayerSpawn", this.isPlayerSpawnAllowed);
        map.put("rule", this.spawnRule.getRuleText());

        return map;
    }

    public static MobControlWorldConfig deserialize(Map<String, Object> data) {
        Boolean allowPlayerSpawn = true;
        String spawnRule = null;

        if (data.containsKey("allowPlayerSpawn")) {
            allowPlayerSpawn = (Boolean) data.get("allowPlayerSpawn");
        }

        if (data.containsKey("rule")) {
            spawnRule = (String) data.get("rule");
        }

        try {
            return new MobControlWorldConfig(spawnRule, allowPlayerSpawn);
        } catch (RuleException ex) {
            Plugin.getInstance().getLogger().severe("Failed to parse spawn rule '" + spawnRule + "'!");
            Plugin.getInstance().getLogger().severe("    Error: " + ex.getMessage());
        }

        return null;
    }

    //endregion

    @Synchronized("configLock")
    public void setPlayerSpawnAllowed(boolean value) {
        this.isPlayerSpawnAllowed = value;
        ConfigStore.getInstance().notifyChanged();
    }
}
