package eu.duckrealm.parkourGenerator.parcour;

import org.bukkit.util.Vector;

public enum Direction {
    NORTH(new Vector(0, 0, -3), 1, true),
    SOUTH(new Vector(0, 0, 3), 1,true),
    EAST(new Vector(3, 0, 0), 1, true),
    WEST(new Vector(-3, 0, 0), 1, true),
    NORTH_EAST(new Vector(3, 0, -3), 2, false),
    NORTH_WEST(new Vector(-3, 0, -3), 2, false),
    SOUTH_EAST(new Vector(3, 0, 3), 2, false),
    SOUTH_WEST(new Vector(-3, 0, 3), 2, false),
    ;

    public final Vector offset;
    public final int radius; // The radius to check for possible blocks, that could hinder the jump
    public final boolean isHardPossible;

    Direction(Vector offset, int radius, boolean isHardPossible) {
        this.offset = offset;
        this.radius = radius;
        this.isHardPossible = isHardPossible;
    }

    static Direction getOppositeDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
            case NORTH_EAST -> SOUTH_WEST;
            case NORTH_WEST -> SOUTH_EAST;
            case SOUTH_EAST -> NORTH_WEST;
            case SOUTH_WEST -> NORTH_EAST;
        };
    }
}
