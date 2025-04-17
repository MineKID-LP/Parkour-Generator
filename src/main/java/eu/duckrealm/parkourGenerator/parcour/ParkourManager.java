package eu.duckrealm.parkourGenerator.parcour;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class ParkourManager {
    static List<Parkour> parkourList = new ArrayList<>();

    public static Parkour newParkour(Location location, int length) {
        Parkour parkour = new Parkour(location, length);
        parkourList.add(parkour);
        return parkour;
    }

    public static void removeParkour(Parkour parkour) {
        parkourList.remove(parkour);
    }

    public static List<Parkour> getParkourList() {
        return parkourList;
    }
}
