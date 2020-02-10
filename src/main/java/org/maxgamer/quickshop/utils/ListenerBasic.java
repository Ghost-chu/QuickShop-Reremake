package org.maxgamer.quickshop.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public final class ListenerBasic<T extends Event> {

    @NotNull
    private final Class<T> tClass;

    @NotNull
    private final Predicate<T> predicate;

    @NotNull
    private final Consumer<T> consumer;

    @NotNull
    private final EventPriority eventPriority;

    public ListenerBasic(@NotNull Class<T> tClass, @NotNull Predicate<T> predicate, @NotNull Consumer<T> consumer, @NotNull EventPriority eventPriority) {
        this.tClass = tClass;
        this.predicate = predicate;
        this.consumer = consumer;
        this.eventPriority = eventPriority;
    }

    public ListenerBasic(@NotNull Class<T> tClass, @NotNull Predicate<T> predicate, @NotNull Consumer<T> consumer) {
        this(tClass, predicate, consumer, EventPriority.NORMAL);
    }
    public ListenerBasic(@NotNull Class<T> tClass, @NotNull EventPriority eventPriority,
                         @NotNull Consumer<T> consumer) {
        this(tClass, t -> true, consumer, eventPriority);
    }
    public ListenerBasic(@NotNull Class<T> tClass, @NotNull Consumer<T> consumer) {
        this(tClass, t -> true, consumer, EventPriority.NORMAL);
    }

    @SuppressWarnings("unchecked")
    public void register(@NotNull Plugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvent(
            tClass,
            new Listener() {},
            eventPriority,
            (listener, event) -> {
                if (event.getClass().equals(tClass) && predicate.test((T) event)) {
                    consumer.accept((T) event);
                }
            },
            plugin
        );
    }

}
