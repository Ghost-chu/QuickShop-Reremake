package org.maxgamer.quickshop.Shop;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import lombok.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.maxgamer.quickshop.QuickShop;

@Getter
public class QueuedShopManager {
    QuickShop plugin;
    Queue<QueueShopObject> shopQueue = new LinkedBlockingQueue<>();
    int maxShopLoadPerTick = 0;
    boolean useQueue = false;

    /**
     * QueuedShopManager can help you process shops by queue not in once
     * This can solve some performance issue
     * @param quickshop
     */
    public QueuedShopManager(QuickShop quickshop){
        this.plugin = quickshop;
        this.useQueue = plugin.getConfig().getBoolean("queue.enable");
        if(!useQueue)
            return;
        maxShopLoadPerTick =  plugin.getConfig().getInt("queue.shops-per-tick");
        new BukkitRunnable(){
            @Override
            public void run() {
                int loadedShopInTick = 0;
                while (true){
                    if(loadedShopInTick >= maxShopLoadPerTick) //Max loads check
                        break; //Jump out, go next tick
                    QueueShopObject queueShopObject = shopQueue.poll(); //Load QueueShopObject
                    if(queueShopObject == null) //No more queue need to do
                        break; //Jump out, go next tick
                    QueueAction[] actions = queueShopObject.getAction();
                    for (QueueAction action : actions){ //Run actions.
                        switch (action){
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
                            case CHECKDISPLAYMOVED:
                                queueShopObject.getShop().checkDisplay();
                                break;
                            case DELETE:
                                queueShopObject.getShop().delete();
                                break;
                            case CLICK:
                                queueShopObject.getShop().onClick();
                                break;
                        }
                    }

                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Add QueueShopObjects to queue
     * @param queueShopObjects target object you want add
     */
    public void add(QueueShopObject... queueShopObjects){
        for (QueueShopObject queueShopObject : queueShopObjects){
           this.shopQueue.offer(queueShopObject);
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
