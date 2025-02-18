package fr.neocle.litebansweb.api.events.bukkit;

import fr.neocle.litebansweb.api.events.EventDispatcher;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class BukkitEventDispatcher implements EventDispatcher {
    private final Plugin plugin;

    public BukkitEventDispatcher(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void dispatchUserWhitelistedEvent(String userId) {
        BukkitUserWhitelistedEvent event = new BukkitUserWhitelistedEvent(userId);
        System.out.println("Dispatching event bukkit");
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(event));
    }
}
