package org.maxgamer.quickshop.Util.Location;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LocationOf {

    @NotNull
    private static final Pattern PATTERN =
            Pattern.compile("((?<world>[^:/]+)[:/])?(?<x>[\\-0-9.]+),(?<y>[\\-0-9.]+),(?<z>[\\-0-9.]+)(:(?<yaw>[\\-0-9.]+):(?<pitch>[\\-0-9.]+))?");

    @NotNull
    private final String text;

    public LocationOf(@NotNull String text) {
        this.text = text;
    }

    @NotNull
    public Location value() {
        final Matcher match = PATTERN.matcher(text.replaceAll("_", "\\."));

        if (match.matches())
            return new Location(
                    Bukkit.getWorld(match.group("world")),
                    Double.parseDouble(match.group("x")),
                    Double.parseDouble(match.group("y")),
                    Double.parseDouble(match.group("z")),
                    match.group("yaw") != null ? Float.parseFloat(match.group("yaw")) : 0F,
                    match.group("pitch") != null ? Float.parseFloat(match.group("pitch")) : 0F
            );

        throw new IllegalStateException("Location string has wrong style!");
    }
}
