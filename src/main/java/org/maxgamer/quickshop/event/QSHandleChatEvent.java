package org.maxgamer.quickshop.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class QSHandleChatEvent extends AbstractQSEvent{
    private Player sender;
    private String message;
}
