package org.maxgamer.quickshop.Util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@ToString
@Data
@EqualsAndHashCode
public class UpdateInfomation {
    private boolean isBeta;
    @Nullable private String version;

}
