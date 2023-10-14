package plugins.nate.smp.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TrustManager {
    private static final HashMap<UUID, HashSet<UUID>> trustRelations = new HashMap<>();
    private static File file;
    private static FileConfiguration config;

    public static void init(File dataFolder) {
        file = new File(dataFolder, "trusts.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public static boolean trustPlayer(Player owner, Player trusted) {
        HashSet<UUID> trustedPlayers = trustRelations.computeIfAbsent(owner.getUniqueId(), k -> new HashSet<>());
        boolean added = trustedPlayers.add(trusted.getUniqueId());
        if (added) save();
        return added;
    }

    public static boolean untrustPlayer(Player owner, Player untrusted) {
        HashSet<UUID> trustedPlayers = trustRelations.get(owner.getUniqueId());
        if (trustedPlayers != null) {
            boolean removed = trustedPlayers.remove(untrusted.getUniqueId());
            if (removed) save();
            return removed;
        }
        return false;
    }

    public static Set<UUID> getTrustedPlayers(UUID ownerUUID) {
        return trustRelations.getOrDefault(ownerUUID, new HashSet<>());
    }

    public static Set<String> getTrustedPlayerNames(UUID ownerUuid) {
        Set<UUID> trustedPlayerUUIDs = trustRelations.getOrDefault(ownerUuid, new HashSet<>());
        return trustedPlayerUUIDs.stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .collect(Collectors.toSet());
    }

    private static void save() {
        for (UUID ownerUUID : trustRelations.keySet()) {
            config.set(ownerUUID.toString(), new ArrayList<>(convertSetToUUIDStrings(trustRelations.get(ownerUUID))));
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void load() {
        for (String ownerUUIDString : config.getKeys(false)) {
            Set<String> trustedUUIDStrings = new HashSet<>(config.getStringList(ownerUUIDString));
            trustRelations.put(UUID.fromString(ownerUUIDString), convertStringsToUUIDSet(trustedUUIDStrings));
        }
    }

    private static Set<String> convertSetToUUIDStrings(Set<UUID> uuids) {
        Set<String> uuidStrings = new HashSet<>();
        for (UUID uuid : uuids) {
            uuidStrings.add(uuid.toString());
        }
        return uuidStrings;
    }

    private static HashSet<UUID> convertStringsToUUIDSet(Set<String> uuidStrings) {
        HashSet<UUID> uuids = new HashSet<>();
        for (String uuidString : uuidStrings) {
            uuids.add(UUID.fromString(uuidString));
        }
        return uuids;
    }
}

