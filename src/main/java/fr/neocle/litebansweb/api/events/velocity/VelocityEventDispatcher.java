package fr.neocle.litebansweb.api.events.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import fr.neocle.litebansweb.api.events.EventDispatcher;

public class VelocityEventDispatcher implements EventDispatcher {
    private final ProxyServer proxyServer;

    public VelocityEventDispatcher(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public void dispatchUserWhitelistedEvent(String userId) {
        VelocityUserWhitelistedEvent event = new VelocityUserWhitelistedEvent(userId);
        proxyServer.getEventManager().fire(event);
    }
}
