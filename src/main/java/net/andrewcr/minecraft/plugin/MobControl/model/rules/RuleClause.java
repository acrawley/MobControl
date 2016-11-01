package net.andrewcr.minecraft.plugin.MobControl.model.rules;

import lombok.Data;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.EntityUtil;
import org.bukkit.entity.EntityType;

import java.util.Locale;

@Data
public class RuleClause implements Comparable<RuleClause> {
    //region Private Fields

    private final String name;
    private final ClauseAction action;
    private final ClauseType type;
    private final ClauseGroupType groupType;

    //endregion

    //region Overrides

    @Override
    public String toString() {
        return (this.action == ClauseAction.Allow ? "+" : "-") + this.name.toUpperCase(Locale.ENGLISH);
    }

    //endregion

    //region Public API

    public static RuleClause fromText(String text) throws RuleException {
        if (text.length() < 2) {
            throw new RuleException("Invalid rule!");
        }

        char first = text.charAt(0);
        String mobClass = text.substring(1).toUpperCase(Locale.ENGLISH);

        ClauseAction mode;
        switch (first) {
            case '+':
                mode = ClauseAction.Allow;
                break;

            case '-':
                mode = ClauseAction.Deny;
                break;

            default:
                throw new RuleException("Rule must begin with '+' or '-'!");
        }

        ClauseType type;
        ClauseGroupType groupType = ClauseGroupType.None;
        if (mobClass.equals("ALL")) {
            type = ClauseType.All;
        } else if (mobClass.equals("FRIENDLY") || mobClass.equals("HOSTILE")) {
            type = ClauseType.Group;
            groupType = mobClass.equals("FRIENDLY") ? ClauseGroupType.FriendlyMobs : ClauseGroupType.HostileMobs;
        } else {
            EntityType entityType = EntityUtil.tryGetEntityTypeByName(mobClass);
            if (entityType == null) {
                throw new RuleException("Unknown mob type '" + mobClass + "'!");
            }

            if (!EntityUtil.isMob(entityType)) {
                throw new RuleException("Entity type '" + mobClass + "' is not a mob!");
            }

            type = ClauseType.Single;
        }

        return new RuleClause(mobClass, mode, type, groupType);
    }

    public Boolean allowsSpawn(EntityType entityType) {
        switch (this.type) {
            case All:
                return this.action == ClauseAction.Allow;

            case Group:
                if (groupType == ClauseGroupType.FriendlyMobs) {
                    if (EntityUtil.isFriendlyMob(entityType)) {
                        return this.action == ClauseAction.Allow;
                    }

                    return null;
                } else if (this.groupType == ClauseGroupType.HostileMobs) {
                    if (EntityUtil.isHostileMob(entityType)) {
                        return this.action == ClauseAction.Allow;
                    }

                    return null;
                }

            case Single:
                if (this.name.equals(entityType.name())) {
                    return this.action == ClauseAction.Allow;
                }

                return null;
        }

        return null;
    }

    //endregion

    //region Comparable Implementation

    @Override
    public int compareTo(RuleClause o) {
        // Order by type (ALL first, then groups, then singles), then by action (allow before deny), then by name
        if (this.type != o.type) {
            return this.type.compareTo(o.type);
        }

        if (this.action != o.action) {
            return this.action.compareTo(o.action);
        }

        if (!this.name.equals(o.name)) {
            return this.name.compareTo(o.name);
        }

        return 0;
    }

    //endregion
}
