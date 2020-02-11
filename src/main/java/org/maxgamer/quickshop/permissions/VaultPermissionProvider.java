/*
 * MIT License
 *
 * Copyright © 2020 Bukkit Commons Studio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.maxgamer.quickshop.permissions;

import lombok.AllArgsConstructor;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.permissions.exceptions.DoesntSupportOfflinePlayerException;
import org.maxgamer.quickshop.permissions.exceptions.DoesntSupportPermissionControllerException;

@AllArgsConstructor
public class VaultPermissionProvider implements PermissionProvider {
    private Permission permission;
    @Override
    public boolean has(@NotNull CommandSender sender,
                       @NotNull String permission) {
        return this.permission.has(sender,permission);
    }

    @Override
    public boolean has(@NotNull Player player,
                       @NotNull String permission) {
        return this.permission.has(player,permission);
    }

    @Override
    public boolean hasOfflinePlayerSupport() {
        return true;
    }

    @Override
    public boolean has(@NotNull OfflinePlayer offlinePlayer,
                       @NotNull String permission) throws DoesntSupportOfflinePlayerException {
        return this.permission.playerHas(null,offlinePlayer,permission);
    }

    @Override
    public boolean add(@NotNull Player player,
                       @NotNull String permission) throws DoesntSupportPermissionControllerException {
        return this.permission.playerAdd(null,player,permission);
    }

    @Override
    public boolean remove(@NotNull Player player,
                          @NotNull String permission) throws DoesntSupportPermissionControllerException {
        return this.permission.playerRemove(null,player,permission);
    }
}
