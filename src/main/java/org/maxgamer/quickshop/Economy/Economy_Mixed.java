/*
 * This file is a part of project QuickShop, the name is Economy_Mixed.java
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

package org.maxgamer.quickshop.Economy;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

public class Economy_Mixed implements EconomyCore {
  EconomyCore core;

  public Economy_Mixed() {
    core = new Economy_Vault();
  }

  @Override
  public boolean deposit(UUID name, double amount) {
    if (getBalance(name) < amount) {
      return false;
    }
    Bukkit.dispatchCommand(
        Bukkit.getConsoleSender(),
        MsgUtil.fillArgs(
            QuickShop.instance.getConfig().getString("mixedeconomy.deposit"),
            Bukkit.getOfflinePlayer(name).getName(),
            String.valueOf(amount)));
    return true;
  }

  @Override
  public String format(double balance) {
    return Util.format(balance);
  }

  @Override
  public double getBalance(UUID name) {
    return core.getBalance(name);
  }

  @Override
  public boolean transfer(UUID from, UUID to, double amount) {
    boolean result;
    result = withdraw(from, amount);
    if (!result) {
      deposit(from, amount);
    }
    result = deposit(to, amount);
    if (!result) {
      withdraw(to, amount);
    }
    return true;
  }

  @Override
  public boolean withdraw(UUID name, double amount) {
    if (getBalance(name) > amount) {
      return false;
    }
    Bukkit.dispatchCommand(
        Bukkit.getConsoleSender(),
        MsgUtil.fillArgs(
            QuickShop.instance.getConfig().getString("mixedeconomy.withdraw"),
            Bukkit.getOfflinePlayer(name).getName(),
            String.valueOf(amount)));
    return true;
  }

  @Override
  public boolean isValid() {
    return core.isValid();
  }
}
