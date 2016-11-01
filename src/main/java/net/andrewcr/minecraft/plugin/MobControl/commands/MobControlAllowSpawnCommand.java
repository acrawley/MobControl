package net.andrewcr.minecraft.plugin.MobControl.commands;

import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandExecutorBase;

public class MobControlAllowSpawnCommand extends CommandBase {
    @Override
    public CommandExecutorBase getExecutor() {
        return new MobControlAllowCommandExecutor();
    }

    private class MobControlAllowCommandExecutor extends MobControlSpawnCommandExecutor {
        public MobControlAllowCommandExecutor() {
            super("mobcontrol allow");
        }

        @Override
        protected boolean isAllow() {
            return true;
        }
    }
}
