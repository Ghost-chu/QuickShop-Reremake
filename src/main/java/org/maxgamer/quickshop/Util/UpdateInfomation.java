package org.maxgamer.quickshop.Util;

import lombok.*;
import org.jetbrains.annotations.*;

@AllArgsConstructor
@ToString
@Getter
@Setter
public class UpdateInfomation {
    @Nullable private String version;
    private boolean isBeta;
}
