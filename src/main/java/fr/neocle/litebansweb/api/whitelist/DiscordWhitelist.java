package fr.neocle.litebansweb.api.whitelist;

import org.bukkit.Bukkit;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Representer;

import com.velocitypowered.api.proxy.ProxyServer;

import fr.neocle.litebansweb.velocity.api.events.VelocityUserWhitelistedEvent;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DiscordWhitelist {
    private final Path configFile;
    private final Logger logger;
    private final Object platformInstance;
    private final Yaml yaml;

    public DiscordWhitelist(Path configFile, Logger logger, Object platformInstance) {
        this.configFile = configFile;
        this.logger = logger;
        this.platformInstance = platformInstance;
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setProcessComments(true);
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setProcessComments(true);
        this.yaml = new Yaml(new Constructor(loaderOptions), new Representer(dumperOptions), dumperOptions, loaderOptions);
    }

    public synchronized boolean addUser(String userId) {
        if (!isValidDiscordId(userId)) return false;

        boolean added = modifyWhitelist(userId, true);

        if (added) {
            fireUserWhitelistedEvent(userId);
        }

        return added;
    }

    public synchronized boolean removeUser(String userId) {
        if (!isValidDiscordId(userId)) return false;

        boolean removed = modifyWhitelist(userId, false);

        return removed;
    }

    private void fireUserWhitelistedEvent(String userId) {
        if (platformInstance instanceof ProxyServer velocity) {
            velocity.getEventManager().fire(new VelocityUserWhitelistedEvent(userId));
        } else {
            return;
        } /*else if (platformInstance instanceof Plugin bungee) {
            bungee.getProxy().getPluginManager().callEvent(new BungeeUserWhitelistedEvent(userId));
        } else if (platformInstance instanceof org.bukkit.plugin.Plugin bukkit) {
            Bukkit.getPluginManager().callEvent(new BukkitUserWhitelistedEvent(userId));
        }*/
    }

    public List<String> getWhitelistedUsers() {
        if (!Files.exists(configFile)) {
            logger.warning("Config file not found: " + configFile);
            return List.of();
        }
        
        Node root = readYamlRoot();
        if (!(root instanceof MappingNode mappingNode)) return List.of();

        for (NodeTuple tuple : mappingNode.getValue()) {
            if (isDiscordOAuthNode(tuple)) {
                return extractAllowedUsers(tuple);
            }
        }
        return List.of();
    }

    public boolean isUserWhitelisted(String userId) {
        return getWhitelistedUsers().contains(userId);
    }

    private Node readYamlRoot() {
        try (FileReader reader = new FileReader(configFile.toFile())) {
            return yaml.compose(reader);
        } catch (IOException e) {
            logger.severe("Failed to read config file: " + e.getMessage());
            return null;
        }
    }

    private boolean isDiscordOAuthNode(NodeTuple tuple) {
        return tuple.getKeyNode() instanceof ScalarNode keyNode && "discord_oauth".equals(keyNode.getValue());
    }

    private List<String> extractAllowedUsers(NodeTuple tuple) {
        if (tuple.getValueNode() instanceof MappingNode discordOAuthNode) {
            for (NodeTuple innerTuple : discordOAuthNode.getValue()) {
                if (innerTuple.getKeyNode() instanceof ScalarNode keyNode && "allowed_users".equals(keyNode.getValue())) {
                    if (innerTuple.getValueNode() instanceof SequenceNode sequenceNode) {
                        return sequenceNode.getValue().stream()
                                .filter(node -> node instanceof ScalarNode)
                                .map(node -> ((ScalarNode) node).getValue())
                                .collect(Collectors.toList());
                    }
                }
            }
        }
        return List.of();
    }

    private boolean modifyWhitelist(String userId, boolean isAdding) {
        if (!Files.exists(configFile)) {
            logger.warning("Config file not found: " + configFile);
            return false;
        }

        Node root = readYamlRoot();
        if (root == null || !(root instanceof MappingNode mappingNode)) {
            logger.warning("Invalid or empty config file.");
            return false;
        }

        boolean updated = isAdding ? updateAllowedUsers(mappingNode, userId) : removeAllowedUser(mappingNode, userId);

        if (updated) {
            try (FileWriter writer = new FileWriter(configFile.toFile())) {
                yaml.serialize(root, writer);
            } catch (IOException e) {
                logger.severe("Failed to write config file: " + e.getMessage());
                return false;
            }
        }

        return updated;
    }

    private boolean updateAllowedUsers(MappingNode root, String userId) {
        return modifyAllowedUsersList(root, userId, true);
    }

    private boolean removeAllowedUser(MappingNode root, String userId) {
        return modifyAllowedUsersList(root, userId, false);
    }

    private boolean modifyAllowedUsersList(MappingNode root, String userId, boolean isAdding) {
        for (NodeTuple tuple : root.getValue()) {
            if (tuple.getKeyNode() instanceof ScalarNode keyNode && "discord_oauth".equals(keyNode.getValue())) {
                if (tuple.getValueNode() instanceof MappingNode discordOAuthNode) {
                    return modifyUserList(discordOAuthNode, userId, isAdding);
                }
            }
        }
        return false;
    }

    private boolean modifyUserList(MappingNode discordOAuthNode, String userId, boolean isAdding) {
        for (NodeTuple tuple : discordOAuthNode.getValue()) {
            if (tuple.getKeyNode() instanceof ScalarNode keyNode && "allowed_users".equals(keyNode.getValue())) {
                if (tuple.getValueNode() instanceof SequenceNode sequenceNode) {
                    List<Node> userNodes = sequenceNode.getValue();

                    if (isAdding) {
                        for (Node node : userNodes) {
                            if (node instanceof ScalarNode scalar && scalar.getValue().equals(userId)) {
                                return false; // User already exists
                            }
                        }
                        userNodes.add(new ScalarNode(Tag.STR, userId, null, null, DumperOptions.ScalarStyle.PLAIN));
                    } else {
                        List<Node> updatedNodes = userNodes.stream()
                                .filter(node -> !(node instanceof ScalarNode scalar && scalar.getValue().equals(userId)))
                                .collect(Collectors.toList());

                        if (updatedNodes.size() == userNodes.size()) {
                            return false; // User was not found
                        }

                        sequenceNode.getValue().clear();
                        sequenceNode.getValue().addAll(updatedNodes);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isValidDiscordId(String userId) {
        if (userId == null || userId.length() < 17 || userId.length() > 20) {
            return false;
        }
        try {
            long id = Long.parseLong(userId);
            return id > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
