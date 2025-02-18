package fr.neocle.litebansweb.api.events;

public interface EventDispatcher {
    void dispatchUserWhitelistedEvent(String userId);
}
