package net.andrewcr.minecraft.plugin.MobControl.model.rules;

import lombok.Synchronized;
import net.andrewcr.minecraft.plugin.BasePluginLib.util.EntityUtil;
import net.andrewcr.minecraft.plugin.MobControl.model.ConfigStore;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class MobSpawnRule {
    private final Object configLock = ConfigStore.getInstance().getSyncObj();

    private List<RuleClause> clauses;

    public MobSpawnRule(String ruleText) throws RuleException {
        this.setRuleText(ruleText);
    }

    public String getRuleText() {
        return this.clauses.stream()
            .map(RuleClause::toString)
            .collect(Collectors.joining(" "));
    }

    @Synchronized("configLock")
    public void setRuleText(String ruleText) throws RuleException {
        List<RuleClause> clauses = new ArrayList<>();

        String[] ruleTextParts = ruleText.split(" ");
        for (String rulePart : ruleTextParts) {
            clauses.add(RuleClause.fromText(rulePart));
        }

        // Check for duplicate rules
        HashSet<String> dupes = new HashSet<>();
        for (RuleClause clause : clauses) {
            if (dupes.contains(clause.getName())) {
                throw new RuleException("Rule contains multiple entries for '" + clause.getName() + "'!");
            }

            dupes.add(clause.getName());
        }

        // Sort rules in descending order from most general to most specific
        clauses.sort(null);

        this.clauses = clauses;
        ConfigStore.getInstance().notifyChanged();
    }

    public boolean canSpawn(EntityType entityType) {
        boolean result = true;

        // Check all rules to see if the entity is allowed to spawn - more specific rules override less specific
        for (RuleClause clause : this.clauses) {
            Boolean ruleResult = clause.allowsSpawn(entityType);
            if (ruleResult != null) {
                result = ruleResult;
            }
        }

        return result;
    }

    public void setCanSpawn(String entityType, boolean canSpawn) throws RuleException {
        String ruleText = (canSpawn ? "+" : "-") + entityType;
        RuleClause clause = RuleClause.fromText(ruleText);

        RuleClause existingClause = this.clauses.stream()
            .filter(c -> c.getName().equals(clause.getName()))
            .findFirst()
            .orElse(null);

        switch (clause.getType()) {
            case All:
                // Adding an "all" clause - reset everything else
                this.clauses.clear();
                break;

            case Group:
                if (existingClause != null) {
                    // There's an existing clause for this group - remove it
                    this.clauses.remove(existingClause);
                }

                // Remove all "single" clauses that the new group clause would match to avoid a case where (e.g.) the user
                //  issues a command like "mobcontrol allow world friendly", but some friendly mobs would continue to not
                //  spawn due to existing single-mob "deny" clauses.
                for (int i = this.clauses.size() - 1; i >= 0; i--) {
                    RuleClause testClause = this.clauses.get(i);
                    if (testClause.getType() == ClauseType.Single) {
                        EntityType clauseEntity = EntityUtil.tryGetEntityTypeByName(testClause.getName());

                        if ((clause.getGroupType() == ClauseGroupType.FriendlyMobs && EntityUtil.isFriendlyMob(clauseEntity)) ||
                            (clause.getGroupType() == ClauseGroupType.HostileMobs && EntityUtil.isHostileMob(clauseEntity))) {
                            this.clauses.remove(i);
                        }
                    }
                }
                break;

            case Single:
                if (existingClause != null) {
                    if (existingClause.getAction() == clause.getAction()) {
                        // Existing clause is the same, do nothing
                        return;
                    }

                    EntityType clauseEntity = EntityUtil.tryGetEntityTypeByName(clause.getName());
                    if (clauseEntity != null) {
                        if (this.canSpawn(clauseEntity) == (clause.getAction() == ClauseAction.Allow)) {
                            // Rule would have no effect, do nothing
                            return;
                        }
                    }

                    // Remove conflicting clause
                    this.clauses.remove(existingClause);
                }
                break;
        }

        this.clauses.add(clause);
        this.clauses.sort(null);
        ConfigStore.getInstance().notifyChanged();
    }
}
