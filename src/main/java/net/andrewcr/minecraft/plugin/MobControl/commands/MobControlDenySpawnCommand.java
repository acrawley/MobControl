package net.andrewcr.minecraft.plugin.MobControl.commands;

import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandExecutorBase;

public class MobControlDenySpawnCommand extends CommandBase {
    @Override
    public CommandExecutorBase getExecutor() {
        return new MobControlDenySpawnCommandExecutor();
    }

    private class MobControlDenySpawnCommandExecutor extends MobControlSpawnCommandExecutor {
        public MobControlDenySpawnCommandExecutor() {
            super("mobcontrol deny");
        }

        @Override
        protected boolean isAllow() {
            return false;
        }
    }
}
