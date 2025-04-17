package eu.duckrealm.parkourGenerator.parcour;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static eu.duckrealm.parkourGenerator.ParkourGenerator.config;
import static java.util.Objects.isNull;

public class Parkour {
    private final Location startLocation;
    private Location currentLocation;
    private Location nextLocation;
    private final Color currentColor;
    private int length;
    private int currentLength = 0;
    private Player pairedPlayer;
    private Direction lastDirection;
    private final BossBar bossBar;

    public Parkour(Location startLocation, int length) {
        this.startLocation = startLocation.clone();
        this.currentLocation = startLocation.clone();
        this.length = length;
        this.currentColor = Color.values()[((int) (Math.random() * (Color.values().length - 1)))];
        this.bossBar = BossBar.bossBar(
                parseBossBarTitle((String) config.get("ui.BarTitle")),
                0,
                BossBar.Color.valueOf((String) config.get("ui.BarColor")),
                BossBar.Overlay.valueOf((String) config.get("ui.BarOverlay")));
        if(startLocation.clone().add(0, -1, 0).getBlock().getType().isAir()) {
            startLocation.clone().add(0, -1, 0).getBlock().setType(currentColor.easy);
        }
    }

    private @NotNull Component parseBossBarTitle(String s) {
        return MiniMessage.miniMessage().deserialize((String) config.get("ui.BarTitle"),
                Placeholder.component("total-jumps", Component.text(length)),
                Placeholder.component("current-jump", Component.text(currentLength)),
                Placeholder.component("player", Component.text(isPaired() ? pairedPlayer.getName() : "none")));
    }

    public void setPairedPlayer(Player pairedPlayer) {
        this.pairedPlayer = pairedPlayer;
        this.pairedPlayer.showBossBar(bossBar);
        this.pairedPlayer.getScoreboardTags().add("current_parkour");
        this.pairedPlayer.getScoreboardTags().add("activity");
    }

    public boolean isPaired() {
        return pairedPlayer != null;
    }

    public void nextJump() {
        if(!currentLocation.equals(startLocation)) currentLocation.getBlock().setType(Material.AIR);
        currentLocation = nextLocation.clone();
        currentLength++;
        bossBar.progress((float) currentLength / length);
        bossBar.name(parseBossBarTitle((String) config.get("ui.BarTitle")));
        if (currentLength >= length) {
            win();
            return;
        }
        generateNextJump();
        if (currentLength+1 == length) {
            nextLocation.getBlock().setType(Material.GOLD_BLOCK);
        }
    }

    public void generateFirstJump() {
        generateNextJump();
    }

    public void cleanUp() {
        if(nextLocation != null) nextLocation.getBlock().setType(Material.AIR);
        if(!currentLocation.equals(startLocation)) currentLocation.getBlock().setType(Material.AIR);
        if(isPaired()) {
            pairedPlayer.hideBossBar(bossBar);

            pairedPlayer.getScoreboardTags().remove("current_parkour");
            pairedPlayer.getScoreboardTags().remove("activity");
        }
        ParkourManager.removeParkour(this);
    }

    private void win() {
        if(isPaired()) {
            pairedPlayer.sendActionBar(Component.text("You win!", NamedTextColor.GREEN));
            pairedPlayer.playSound(pairedPlayer, (String) config.get("parkour.WinSound"), SoundCategory.BLOCKS, 1, 1);
            String function = parseFunction("functions.win");
            if(!function.isEmpty()) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), function);
        }
        cleanUp();
    }

    private @NotNull String parseFunction(String key) {
        String function = (String) config.get(key);
        if (function.isEmpty()) return "";
        if(isPaired()) {
            function = function.replace("<player>", pairedPlayer.getName());
        } else {
            function = function.replace("<player>", "none");
        }
        function = function.replace("<current-jump>", String.valueOf(currentLength));
        function = function.replace("<total-jumps>", String.valueOf(length));

        return function;
    }

    public void lose() {
        if(isPaired()) {
            pairedPlayer.sendActionBar(Component.text("You lost!", NamedTextColor.RED));
            pairedPlayer.playSound(pairedPlayer, (String) config.get("parkour.LoseSound"), SoundCategory.BLOCKS, 1, 1);
            String function = parseFunction("functions.lose");
            if(!function.isEmpty()) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), function);
        }
        cleanUp();
    }

    private void generateNextJump() {
        List<Direction> possibleDirections = new ArrayList<>();
        boolean isLastJumpHard = currentLocation.clone().getBlock().getType().equals(currentColor.hard);

        // Check all directions if jump is possible
        for (Direction direction : Direction.values()) {
            Location directionLocation = currentLocation.clone().add(direction.offset);
            Location firstCheckLocation = directionLocation.clone().add(-direction.radius, 0, -direction.radius);
            Location secondCheckLocation = directionLocation.clone().add(direction.radius, 4, direction.radius);
            boolean isDiagonal = !direction.isHardPossible; //only diagonal jumps have set isHardPossible to false
            if (!(isDiagonal && isLastJumpHard) && isAreaFree(firstCheckLocation, secondCheckLocation)) {
                possibleDirections.add(direction);
            }
        }

        // Select random jump direction
        if (possibleDirections.isEmpty() && (boolean) config.get("experimental.ForcePlacement")) {
            possibleDirections.add(Direction.values()[(int) (Math.random() * Direction.values().length)]);
        } else if(possibleDirections.isEmpty()) {
            if(isPaired()) pairedPlayer.sendMessage(Component.text("Not enough space!", NamedTextColor.RED));
            cleanUp();
            return;
        }

        Direction selectedDirection = possibleDirections.get((int) (Math.random() * possibleDirections.size()));

        if(isNull(lastDirection)) lastDirection = selectedDirection;
        if(selectedDirection.equals(Direction.getOppositeDirection(lastDirection))) {
            int index = possibleDirections.indexOf(selectedDirection) + 1; //Select next direction if going back
            if(index > possibleDirections.size() - 1) index = 0; //Wrap said direction
            selectedDirection = possibleDirections.get(index);
        }

        Vector randomOffset = new Vector(0, 0, 0);

        if (Math.random() < 0.5) {
            randomOffset.setY(1);
        } else if(Math.random() < 0.5) {
            randomOffset.setX(Math.random() * 2 - 1);
        } else {
            randomOffset.setZ(Math.random() * 2 - 1);
        }

        // Calculate next location
        nextLocation = currentLocation.clone().add(selectedDirection.offset).add(randomOffset);
        Material material = Math.random() < config.getDouble("parkour.HardChance") ? currentColor.easy : currentColor.hard;

        if(!selectedDirection.isHardPossible) material = currentColor.easy;

        if(isLastJumpHard) material = currentColor.easy; //Force easy (in hopes of not generating impossible jumps)

        lastDirection = selectedDirection;
        nextLocation.getBlock().setType(material);
        if(isPaired()) pairedPlayer.playSound(nextLocation, (String) config.get("parkour.NewBlockSound"), SoundCategory.BLOCKS, 1, 1);
    }

    private boolean isAreaFree(Location first, Location second) {
        if (first.getWorld() != second.getWorld()) {
            Bukkit.broadcast(Component.text("Not in the same world"));
            return false;
        }

        for (int x = Math.min(first.getBlockX(), second.getBlockX()); x <= Math.max(first.getBlockX(), second.getBlockX()); x++) {
            for (int y = Math.min(first.getBlockY(), second.getBlockY()); y <= Math.max(first.getBlockY(), second.getBlockY()); y++) {
                for (int z = Math.min(first.getBlockZ(), second.getBlockZ()); z <= Math.max(first.getBlockZ(), second.getBlockZ()); z++) {
                    if (!first.getWorld().getBlockAt(x, y, z).isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public int getCurrentLength() {
        return currentLength;
    }

    public int getLength() {
        return length;
    }

    public Location getNextLocation() {
        return nextLocation;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public Player getPairedPlayer() {
        return pairedPlayer;
    }
}
