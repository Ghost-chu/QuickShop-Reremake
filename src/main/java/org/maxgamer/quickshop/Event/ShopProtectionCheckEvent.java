package org.maxgamer.quickshop.Event;

import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
@ToString
public class ShopProtectionCheckEvent extends QSEvent {

    @Getter
    @NotNull
    private final Player player;

    @Getter
    @NotNull
    private final Location loc;

    @NotNull
    private final Event event; //Don't use getter, we have important notice need told dev in javadoc.

    @Getter
    @NotNull
    private final ProtectionCheckStatus status;

    /**
     * Will call when shop price was changed.
     *
     * @param location Target location will execute protect check.
     * @param status   The checking status
     * @param event    The event will call to check the permissions.
     * @param player   The player in was mentions in this event
     */
    public ShopProtectionCheckEvent(@NotNull Location location, @NotNull Player player,
                                    @NotNull ProtectionCheckStatus status, @NotNull Event event) {
        this.loc = location;
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
    @NotNull
    public Event getEvent() {
        return event;
    }

}

