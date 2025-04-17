package eu.duckrealm.parkourGenerator.commands;

import eu.duckrealm.parkourGenerator.parcour.Parkour;
import eu.duckrealm.parkourGenerator.parcour.ParkourManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static eu.duckrealm.parkourGenerator.ParkourGenerator.config;

public class StartPacourCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        Location location = locationFromCommandSender(commandSender);
        if (location == null) {
            commandSender.sendMessage("You must be in World");
            return true;
        }

        if(commandSender instanceof Entity entity) {
            if(entity.getScoreboardTags().contains("activity")) {
                if(entity.getScoreboardTags().contains("current_parkour")) {
                    commandSender.sendMessage(Component.text("You are already in a parkour!", NamedTextColor.RED));
                } else {
                    commandSender.sendMessage(Component.text("You are already in another activity!", NamedTextColor.RED));
                }
                return true;
            }
        }

        //parse 0, 1, 2 as position
        String x = "";
        String y = "";
        String z = "";

        if(strings.length > 0) x = strings[0];
        if(strings.length > 1) y = strings[1];
        if(strings.length > 2) z = strings[2];

        location = parsePosition(location, x, y, z);

        int MaxLength = config.getInt("parkour.MaxRandomLength");
        int MinLength = config.getInt("parkour.MinRandomLength");
        int length = strings.length > 3 ? Integer.parseInt(strings[3]) : (int) (Math.floor(Math.random() * (MaxLength - MinLength + 1)) + MinLength);

        Parkour parkour = ParkourManager.newParkour(location, length);
        if(commandSender instanceof Player player) parkour.setPairedPlayer(player);
        parkour.generateFirstJump();
        return true;
    }

    private Location parsePosition(Location location, String x, String y, String z) {
        if(x.isEmpty() || y.isEmpty() || z.isEmpty()) return location.clone();
        Location newLocation = location.clone();

        if(x.startsWith("~") && !x.equals("~")) {
            newLocation.setX(newLocation.getX() + Double.parseDouble(x.replace("~", "")));
        } else if(!x.equals("~")) {
            newLocation.setX(Double.parseDouble(x));
        }

        if(y.startsWith("~") && !y.equals("~")) {
            newLocation.setY(newLocation.getY() + Double.parseDouble(y.replace("~", "")));
        } else if(!y.equals("~")) {
            newLocation.setY(Double.parseDouble(y));
        }

        if(z.startsWith("~") && !z.equals("~")) {
            newLocation.setZ(newLocation.getZ() + Double.parseDouble(z.replace("~", "")));
        } else if(!z.equals("~")) {
            newLocation.setZ(Double.parseDouble(z));
        }
        return newLocation;
    }

    public static Location locationFromCommandSender(CommandSender cs) {
        return cs instanceof BlockCommandSender bcs ? bcs.getBlock().getLocation()
                : cs instanceof Entity ecs ? ecs.getLocation() : null;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(strings.length < 3) return List.of("~", "~ ~", "~ ~ ~");
        int MaxLength = config.getInt("parkour.MaxRandomLength");
        int MinLength = config.getInt("parkour.MinRandomLength");
        if(strings.length == 4) return List.of(String.valueOf((int) (Math.floor(Math.random() * (MaxLength - MinLength + 1)) + MinLength)));
        return List.of();
    }
}
