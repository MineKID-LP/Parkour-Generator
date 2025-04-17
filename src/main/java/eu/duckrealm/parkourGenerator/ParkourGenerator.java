package eu.duckrealm.parkourGenerator;

import eu.duckrealm.parkourGenerator.commands.StartPacourCommand;
import eu.duckrealm.parkourGenerator.listeners.MovementListener;
import eu.duckrealm.parkourGenerator.parcour.ParkourManager;
import eu.duckrealm.parkourGenerator.parcour.Parkour;
import eu.duckrealm.parkourGenerator.util.Config;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Objects;

public final class ParkourGenerator extends JavaPlugin {
    private static ParkourGenerator instance;
    public static Config config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        config = new Config();

        initializeConfig();

        Objects.requireNonNull(getServer().getPluginCommand("start-parkour")).setExecutor(new StartPacourCommand());

        getServer().getPluginManager().registerEvents(new MovementListener(), this);
    }

    @Override
    public void onDisable() {
        config.save();
        ParkourManager.getParkourList().forEach(Parkour::cleanUp);
    }

    public static Path getPath() {
        return instance.getDataFolder().toPath();
    }

    public static ParkourGenerator getInstance() {
        return instance;
    }

    private void initializeConfig() {
        config.load();
        config.setDefault("parkour.MaxRandomLength", 40);
        config.setDefault("parkour.MinRandomLength", 20);
        config.setDefault("parkour.HardChance", 0.1);
        config.setDefault("parkour.NewBlockSound", Objects.requireNonNull(Registry.SOUNDS.getKey(Sound.BLOCK_NOTE_BLOCK_PLING)).asString());
        config.setDefault("parkour.WinSound", Objects.requireNonNull(Registry.SOUNDS.getKey(Sound.BLOCK_NOTE_BLOCK_BIT)).asString());
        config.setDefault("parkour.LoseSound", Objects.requireNonNull(Registry.SOUNDS.getKey(Sound.ITEM_MACE_SMASH_GROUND)).asString());

        config.setDefault("ui.BarTitle", "<gold>Parkour <b><current-jump>/<total-jumps></b></gold>");
        config.setDefault("ui.BarColor", BossBar.Color.YELLOW.toString());
        config.setDefault("ui.BarOverlay", BossBar.Overlay.PROGRESS.toString());

        config.setDefault("functions.win", "xp add <player> <total-jumps>0 points");
        config.setDefault("functions.lose", "");

        config.setDefault("experimental.ForcePlacement", false);

        config.save();
    }
}
