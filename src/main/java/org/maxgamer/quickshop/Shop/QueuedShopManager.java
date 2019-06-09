package org.maxgamer.quickshop.Shop;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import lombok.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;
import org.maxgamer.quickshop.Util.WarningSender;

/**
 * QueuedShopManager can help you process shops by queue not in once
 * This can solve some performance issue
 */
@Getter
public class QueuedShopManager {
    QuickShop plugin;
    final Queue<QueueShopObject> shopQueue = new LinkedBlockingQueue<>();
    int maxShopLoadPerTick = 0;
    boolean useQueue = false;
    private BukkitTask task;
    private WarningSender warningSender;

    /**
     * QueuedShopManager can help you process shops by queue not in once
     * This can solve some performance issue
     *
     * @param quickshop plugin main class
     */
    public QueuedShopManager(@NotNull QuickShop quickshop) {
        this.plugin = quickshop;
        this.warningSender = new WarningSender(this.plugin, 60000);
        this.useQueue = plugin.getConfig().getBoolean("queue.enable");
        if (!useQueue)
            return;
        maxShopLoadPerTick = plugin.getConfig().getInt("queue.shops-per-tick");
        task = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getQueuedShopManager().runTask(false);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Unload the QueuedShopManager.
     */
    public void uninit() {
        if ((task != null) && !task.isCancelled())
            task.cancel();
        plugin.getLogger().info("Please wait for the shop queue to finish...");
        runTask(true);
    }

    /**
     * Run tasks in queue now.
     *
     * @param shuttingDown Is running when plugin disabling.
     */
    private void runTask(boolean shuttingDown) {
        int loadedShopInTick = 0;
        while (true) {
            if (!shuttingDown)
                if (loadedShopInTick >= maxShopLoadPerTick) //Max loads check
                    return; //Jump out, go next tick
            QueueShopObject queueShopObject = shopQueue.poll(); //Load QueueShopObject
            if (queueShopObject == null) //No more queue need to do
                return; //Jump out, go next tick
            this.doTask(queueShopObject);
            loadedShopInTick++;
            if (shuttingDown) {
                if (shopQueue.size() > 20) {
                    Util.debugLog("Have " + shopQueue.size() + " tasks remaining");
                    if (String.valueOf(shopQueue.size()).endsWith("0"))
                        plugin.getLogger().info("Have " + shopQueue
                                .size() + " tasks remaining in the queue waiting to finish, this may need take while...");
                }
            }
        }
    }

    /**
     * Run a queuedShopObject.
     * @param queueShopObject queueShopObject
     */
    private void doTask(@NotNull QueueShopObject queueShopObject) {
        // if (queueShopObject == null) //No more queue need to do
        //     return; //Jump out, go next tick
        QueueAction[] actions = queueShopObject.getAction();
        for (QueueAction action : actions) { //Run actions.
            Util.debugLog("Execute action " + action.name() + " for shop at " + queueShopObject.getShop().getLocation()
                    .toString());
            switch (action) {
                case LOAD:
                    queueShopObject.getShop().onLoad();
                    break;
                case UNLOAD:
                    queueShopObject.getShop().onUnload();
                    break;
                case UPDATE:
                    queueShopObject.getShop().update();
                    break;
                case SETSIGNTEXT:
                    queueShopObject.getShop().setSignText();
                    break;
                case REMOVEDISPLAYITEM:
                    DisplayItem displayItem = ((ContainerShop) queueShopObject.getShop()).getDisplayItem();
                    if (displayItem != null)
                        displayItem.remove();
                    break;
                case DELETE:
                    queueShopObject.getShop().delete();
                    break;
                case CLICK:
                    queueShopObject.getShop().onClick();
                    break;
                case CHECKDISPLAYITEM:
                    queueShopObject.getShop().checkDisplay();
                    break;
            }
        }
    }

    /**
     * Add QueueShopObjects to queue
     *
     * @param queueShopObjects target object you want add
     */
    public void add(@NotNull QueueShopObject... queueShopObjects) {
        for (QueueShopObject queueShopObject : queueShopObjects) {
            if (useQueue) {
                this.shopQueue.offer(queueShopObject);
                if (this.shopQueue.size() > 50)
                    this.warningSender
                            .sendWarn("There are too many task in queue and they can't be executed in a timely manner, please raise the number of shops per tick via queue.shops-per-tick in config.yml");
            } else {
                this.doTask(queueShopObject); //Direct do actions when turn off queue
            }
        }
    }
    // /**
    //  * Add QueueShopObject to queue
    //  * @param queueShopObject
    //  */
    // public void add(QueueShopObject queueShopObject){
    //     this.shopQueue.offer(queueShopObject);
    // }

}
