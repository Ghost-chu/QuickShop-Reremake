/*
 * This file is a part of project QuickShop, the name is ReloadManager.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util.reload;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReloadManager controls modules to reloading while needed
 * <p>
 * Register order is reloading order preventing unexpected behavior.
 */
public class ReloadManager {
    private final List<Reloadable> registry = new ArrayList<>();

    /**
     * Register a reloadable module into reloading registery
     *
     * @param reloadable Reloadable module
     */
    public void register(@NotNull Reloadable reloadable) {
        unregister(reloadable);
        this.registry.add(reloadable);
    }

    /**
     * Unregister a reloadable module from reloading registry
     *
     * @param reloadable Reloadable module
     */
    public void unregister(@NotNull Reloadable reloadable) {
        this.registry.remove(reloadable);
    }

    /**
     * Unregister all reloadable modules that same with specific class from reloading registry
     *
     * @param clazz Class that impl reloadable
     */
    public void unregister(@NotNull Class<Reloadable> clazz) {
        this.registry.removeIf(reloadable -> reloadable.getClass().equals(clazz));
    }

    /**
     * Reload all reloadable modules
     *
     * @return Reloading results
     */
    @NotNull
    public Map<Reloadable, ReloadResult> reload() {
        return reload(null);
    }

    /**
     * Reload all reloadable modules that equals specific class
     *
     * @param clazz The class that impl reloadable
     * @return Reloading results
     */
    @NotNull
    public Map<Reloadable, ReloadResult> reload(@Nullable Class<Reloadable> clazz) {
        Map<Reloadable, ReloadResult> reloadResultMap = new HashMap<>();
        for (Reloadable reloadable : this.registry) {
            if (clazz != null && !reloadable.getClass().equals(clazz)) continue;
            ReloadResult reloadResult;
            try {
                reloadResult = reloadable.reloadModule();
                if (reloadResult.getStatus() == ReloadStatus.REDIRECT_STATIC)
                    reloadResult = (ReloadResult) reloadable.getClass().getDeclaredMethod("reloadModule").invoke(null);
            } catch (Exception exception) {
                reloadResult = new ReloadResult(ReloadStatus.EXCEPTION, "Reloading failed", exception);
            }
            reloadResultMap.put(reloadable, reloadResult);
        }
        return reloadResultMap;
    }
}
