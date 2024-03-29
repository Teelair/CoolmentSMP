package plugins.nate.smp;

import com.google.common.base.Charsets;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import plugins.nate.smp.managers.EnchantmentManager;
import plugins.nate.smp.managers.TrustManager;
import plugins.nate.smp.utils.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SMP extends JavaPlugin {
    private static SMP plugin;
    private static CoreProtectAPI coreProtect;

    public static StateFlag WITHER_EXPLOSIONS;

    public static final Logger logger = Logger.getLogger("Minecraft");
    public final File prefixesFile = new File(getDataFolder() + "/prefixes.yml");
    public FileConfiguration prefixes;

    @Override
    public void onEnable() {
        super.onEnable();
        plugin = this;
        coreProtect = SMPUtils.loadCoreProtect();


        TrustManager.init(this.getDataFolder());

        DependencyUtils.checkDependencies();
        EventRegistration.registerEvents(this);
        CommandRegistration.registerCommands(this);
        EnchantmentManager.registerEnchants();

        getPrefixes().options().copyDefaults(true);
        saveDefaultPrefixes();

        for (Player p : Bukkit.getOnlinePlayers()) {
            NametagManager.updateNametag(p);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag witherExplosionsFlag = new StateFlag("wither-explosions", true);
            registry.register(witherExplosionsFlag);

            WITHER_EXPLOSIONS = witherExplosionsFlag;
        } catch (FlagConflictException ignored) {}
    }

    public static SMP getPlugin() {
        return plugin;
    }

    public static CoreProtectAPI getCoreProtect() {
        return coreProtect;
    }


    public FileConfiguration getPrefixes() {
        if (prefixes == null) {
            reloadPrefixes();
        }
        return prefixes;
    }

    public void reloadPrefixes() {
        prefixes = YamlConfiguration.loadConfiguration(prefixesFile);

        final InputStream defConfigStream = getResource("prefixes.yml");
        if (defConfigStream == null) {
            return;
        }

        final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8));

        prefixes.setDefaults(defConfig);
    }

    public void savePrefixes() {
        try {
            getPrefixes().save(prefixesFile);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not save prefixes to " + prefixesFile, ex);
        }
    }

    public void saveDefaultPrefixes() {
        if (!prefixesFile.exists()) {
            saveResource("prefixes.yml", false);
        }
    }
}
