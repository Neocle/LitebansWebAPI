package fr.neocle.litebansweb.api.events.bungee;

import fr.neocle.litebansweb.api.events.EventDispatcher;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeEventDispatcher implements EventDispatcher {
    private final Plugin plugin;

    public BungeeEventDispatcher(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void dispatchUserWhitelistedEvent(String userId) {
        System.out.println("Dispatching event bungee");
        BungeeUserWhitelistedEvent event = new BungeeUserWhitelistedEvent(userId);
        plugin.getProxy().getPluginManager().callEvent(event);
    }
}
