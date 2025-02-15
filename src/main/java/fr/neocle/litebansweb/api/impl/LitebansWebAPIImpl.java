package fr.neocle.litebansweb.api.impl;

import fr.neocle.litebansweb.api.LitebansWebAPI;
import fr.neocle.litebansweb.api.whitelist.DiscordWhitelist;
import fr.neocle.litebansweb.api.whitelist.PlayersWhitelist;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class LitebansWebAPIImpl implements LitebansWebAPI {
    private static LitebansWebAPIImpl instance;
    private static Path configFile;
    private static Logger logger;
    private final PlayersWhitelist playersWhitelist;
    private final DiscordWhitelist discordWhitelist;

    private LitebansWebAPIImpl() {
        this.playersWhitelist = new PlayersWhitelist(configFile, logger);
        this.discordWhitelist = new DiscordWhitelist(configFile, logger);
    }

    public static void initialize(Path configFile, Logger logger) {
        if (instance == null) {
            LitebansWebAPIImpl.configFile = configFile;
            LitebansWebAPIImpl.logger = logger;
            instance = new LitebansWebAPIImpl();
        }
    }

    @Override
    public LitebansWebAPIImpl getInstance() {
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
    public boolean addUserToWhitelist(String username) {
        return discordWhitelist.addUser(username);
    }

    @Override
    public boolean removeUserFromWhitelist(String username) {
        return discordWhitelist.removeUser(username);
    }

    @Override
    public List<String> getWhitelistedUsers() {
        return discordWhitelist.getWhitelistedUsers();
    }

    @Override
    public boolean isUserWhitelisted(String username) {
        return discordWhitelist.isUserWhitelisted(username);
    }
    
    @Override
    public DiscordWhitelist getDiscordWhitelist() {
        return discordWhitelist;
    }

}
