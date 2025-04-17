package eu.duckrealm.parkourGenerator.listeners;

import eu.duckrealm.parkourGenerator.parcour.ParkourManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        try {
            ParkourManager.getParkourList().forEach(parcour -> {
                if (parcour.isPaired() && parcour.getPairedPlayer().equals(event.getPlayer())) {
                    double distance = event.getTo().distance(parcour.getNextLocation());
                    if (distance < 2) {
                        parcour.nextJump();
                    }

                    int y = event.getTo().getBlockY();
                    int minY = Math.min(parcour.getNextLocation().getBlockY(), parcour.getCurrentLocation().getBlockY()) - 3;
                    if (y < minY) {
                        parcour.lose();
                    }
                } else if(!event.getPlayer().getScoreboardTags().contains("activity")) {
                    double distance = event.getTo().distance(parcour.getStartLocation());
                    if (distance < 2) {
                        event.getPlayer().sendActionBar(Component.text("Started parkour!", NamedTextColor.GREEN));
                        parcour.setPairedPlayer(event.getPlayer());
                    }
                }
            });
        } catch (Exception ignored) {}
    }
}
