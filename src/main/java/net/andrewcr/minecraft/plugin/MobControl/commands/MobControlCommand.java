package net.andrewcr.minecraft.plugin.MobControl.commands;

import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.command.CommandExecutorBase;
import net.andrewcr.minecraft.plugin.BasePluginLib.command.GroupCommandExecutorBase;

public class MobControlCommand extends CommandBase {

    @Override
    public CommandExecutorBase getExecutor() {
        return new MobControlCommandExecutor();
    }

    private class MobControlCommandExecutor extends GroupCommandExecutorBase {
        public MobControlCommandExecutor() {
            super("mobcontrol");
        }
    }
}
