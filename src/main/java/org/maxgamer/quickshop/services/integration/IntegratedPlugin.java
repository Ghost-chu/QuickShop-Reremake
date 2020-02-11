package org.maxgamer.quickshop.services.integration;

import org.jetbrains.annotations.NotNull;

public interface IntegratedPlugin {
  @NotNull
  String getName();

  void call(@NotNull IntegrateStage stage);
}
