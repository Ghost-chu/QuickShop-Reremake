/*
 * This file is a part of project QuickShop, the name is ListenerHelper.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Listeners;

import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class ListenerHelper {
  private static Set<Class> disabledListener = new HashSet<>();

  /**
   * Make QuickShop ignore the specify event
   *
   * @param eventClass The event class, E.g BlockBreakEvent.class
   */
  public static void disableEvent(@NotNull Class eventClass) {
    disabledListener.add(eventClass);
  }

  /**
   * Make QuickShop nolonger ignore the specify event
   *
   * @param eventClass The event class, E.g BlockBreakEvent.class
   */
  public static void enableEvent(@NotNull Class eventClass) {
    disabledListener.remove(eventClass);
  }

  /**
   * Check the specify event is disabled
   *
   * @param eventClass The event class, E.g BlockBreakEvent.class
   * @return The status for target class
   */
  public static boolean isDisabled(Class eventClass) {
    return disabledListener.contains(eventClass);
  }
}
