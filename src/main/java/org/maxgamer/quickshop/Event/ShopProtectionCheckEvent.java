package org.maxgamer.quickshop.Event;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ShopProtectionCheckEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    @NotNull
    private Player player;
    @Getter
    @NotNull
    private Location loc;
    @NotNull
    private Event event; //Don't use getter, we have important notice need told dev in javadoc.
    @Getter
    @NotNull
    private ProtectionCheckStatus status;

    /**
     * Will call when shop price was changed.
     *
     * @param loc    Target location will execute protect check.
     * @param status The checking status
     * @param event  The event will call to check the permissions.
     */
    public ShopProtectionCheckEvent(@NotNull Location loc, @NotNull Player player, @NotNull ProtectionCheckStatus status, @NotNull Event event) {
        this.loc = loc;
        this.player = player;
        this.status = status;
        this.event = event;
    }

    /**
     * Get the event will used for permission check.
     * WARN: This might not only BlockBreakEvent, you should check the event type before casting.
     *
     * @return The protection check event.
     */
    public @NotNull Event getEvent() {
        return event;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}

