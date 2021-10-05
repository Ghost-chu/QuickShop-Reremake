package org.maxgamer.quickshop.localization.resources;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.localization.LocalizationType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@AllArgsConstructor
@EqualsAndHashCode
public abstract class BasicLocalizationResource {

    protected static final Logger logger = QuickShop.getInstance().getLogger();
    protected final LocalizationType type;
    protected final MemoryConfiguration textMap;

    /**
     * Get the localization text by the key given
     *
     * @param key localization key
     * @return the localization text
     */
    @NotNull
    public String getText(@NotNull String key) {
        String result = textMap.getString(key);
        return result == null ? "Missing no: " + key : result;
    }

    /**
     * Return the type of this resource
     *
     * @return The type of this resource
     * @see LocalizationType
     */
    public @NotNull LocalizationType getLocalizationType() {
        return type;
    }

    public static class LocalizationResourceProcessor {

        private final BasicLocalizationResource pendingResources;
        private final List<Runnable> pendingAction = new ArrayList<>(10);
        private final List<LocalizationResourceProcessorFilter> filters = new ArrayList<>();

        private LocalizationResourceProcessor(BasicLocalizationResource pendingResources) {
            this.pendingResources = pendingResources;
        }

        public static LocalizationResourceProcessor base(BasicLocalizationResource resource) {
            return new LocalizationResourceProcessor(resource);
        }

        public LocalizationResourceProcessor addFilter(LocalizationResourceProcessorFilter filter) {
            filters.add(filter);
            return this;
        }

        public LocalizationResourceProcessor apply(BasicLocalizationResource applyConfig) {
            return apply(applyConfig.textMap);
        }

        public LocalizationResourceProcessor apply(MemoryConfiguration applyConfig) {
            pendingAction.add(() -> {
                for (String key : applyConfig.getKeys(true)) {
                    if (pendingResources.textMap.isSet(key)) {
                        applyValue(key, applyConfig.get(key));
                    }
                }
            });
            return this;
        }

        private void applyValue(String key, Object value) {
            if (value instanceof ConfigurationSection) {
                return;
            }
            for (LocalizationResourceProcessorFilter filter : filters) {
                if (!filter.isAccept(key)) {
                    return;
                }
            }
            pendingResources.textMap.set(key, value);
        }

        public LocalizationResourceProcessor merge(BasicLocalizationResource applyConfig) {
            return merge(applyConfig.textMap);
        }

        public LocalizationResourceProcessor merge(MemoryConfiguration configuration) {
            pendingAction.add(
                    () -> {
                        for (String key : configuration.getKeys(true)) {
                            applyValue(key, configuration.get(key));
                        }
                    }
            );
            return this;
        }

        public BasicLocalizationResource compile() {
            for (Runnable runnable : pendingAction) {
                runnable.run();
            }
            return pendingResources;
        }

        public interface LocalizationResourceProcessorFilter {
            boolean isAccept(String key);
        }
    }
}
