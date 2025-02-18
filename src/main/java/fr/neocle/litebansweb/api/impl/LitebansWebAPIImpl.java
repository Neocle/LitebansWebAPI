package fr.neocle.litebansweb.api.impl;

import fr.neocle.litebansweb.api.LitebansWebAPI;
import fr.neocle.litebansweb.api.whitelist.DiscordWhitelist;
import fr.neocle.litebansweb.api.whitelist.PlayersWhitelist;
import fr.neocle.litebansweb.api.events.EventDispatcher;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class LitebansWebAPIImpl implements LitebansWebAPI {
    private static Path configFile;
    private static Logger logger;
    private static EventDispatcher eventDispatcher;
    private final PlayersWhitelist playersWhitelist;
    private final DiscordWhitelist discordWhitelist;
    private static LitebansWebAPI instance;

    private LitebansWebAPIImpl() {
        this.playersWhitelist = new PlayersWhitelist(configFile, logger, eventDispatcher);
        this.discordWhitelist = new DiscordWhitelist(configFile, logger, eventDispatcher);
    }

    public static synchronized void initialize(Path configFile, Logger logger, EventDispatcher eventDispatcher) {
        if (instance == null) {
            LitebansWebAPIImpl.configFile = configFile;
            LitebansWebAPIImpl.logger = logger;
            LitebansWebAPIImpl.eventDispatcher = eventDispatcher;
            instance = new LitebansWebAPIImpl();
        }
    }

    public static LitebansWebAPI getSingletonInstance() {
        if (instance == null) {
            throw new IllegalStateException("LitebansWebAPI is not initialized");
        }
        return instance;
    }

    @Override
    public boolean addPlayerToWhitelist(String username) {
        return playersWhitelist.addPlayer(username);
    }

    @Override
    public boolean removePlayerFromWhitelist(String username) {
        return playersWhitelist.removePlayer(username);
    }

    @Override
    public List<String> getWhitelistedPlayers() {
        return playersWhitelist.getWhitelistedPlayers();
    }

    @Override
    public boolean isPlayerWhitelisted(String username) {
        return playersWhitelist.isPlayerWhitelisted(username);
    }

    @Override
    public PlayersWhitelist getPlayersWhitelist() {
        return playersWhitelist;
    }

    @Override
    public boolean addUserToWhitelist(String userId) {
        return discordWhitelist.addUser(userId);
    }

    @Override
    public boolean removeUserFromWhitelist(String userId) {
        return discordWhitelist.removeUser(userId);
    }

    @Override
    public List<String> getWhitelistedUsers() {
        return discordWhitelist.getWhitelistedUsers();
    }

    @Override
    public boolean isUserWhitelisted(String userId) {
        return discordWhitelist.isUserWhitelisted(userId);
    }
    
    @Override
    public DiscordWhitelist getDiscordWhitelist() {
        return discordWhitelist;
    }

}
