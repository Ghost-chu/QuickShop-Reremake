package org.maxgamer.quickshop.Util;

import lombok.*;
import org.jetbrains.annotations.*;

@AllArgsConstructor
@ToString
@Data
@EqualsAndHashCode
public class UpdateInfomation {
    private boolean isBeta;
    @Nullable private String version;

}
