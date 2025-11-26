package net.olokw.sandbox.configs;

import java.util.Set;

public class PluginConfig {
    private Set<String> restartHours;

    public PluginConfig(Set<String> restartHours) {
        this.restartHours = restartHours;
    }

    public Set<String> getRestartHours() {
        return restartHours;
    }

    public void setRestartHours(Set<String> restartHours) {
        this.restartHours = restartHours;
    }
}
