package net.andrewcr.minecraft.plugin.MobControl;

import lombok.Getter;
import net.andrewcr.minecraft.plugin.BasePluginLib.plugin.PluginBase;
import net.andrewcr.minecraft.plugin.MobControl.commands.*;
import net.andrewcr.minecraft.plugin.MobControl.listeners.SpawnListener;
import net.andrewcr.minecraft.plugin.MobControl.model.ConfigStore;

public class Plugin extends PluginBase {
    @Getter
    private static Plugin instance;
    @Getter
    private ConfigStore configStore;

    @Override
    public void onEnable() {
        super.onEnable();
        Plugin.instance = this;

        this.configStore = new ConfigStore();

        this.registerCommand(new MobControlCommand());
        this.registerCommand(new MobControlSetCommand());
        this.registerCommand(new MobControlAllowSpawnCommand());
        this.registerCommand(new MobControlDenySpawnCommand());
        this.registerCommand(new MobControlInfoCommand());
        this.registerCommand(new MobControlZapCommand());

        this.registerListener(new SpawnListener());

        this.configStore.load();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        this.configStore.save();

        Plugin.instance = null;
    }
}
