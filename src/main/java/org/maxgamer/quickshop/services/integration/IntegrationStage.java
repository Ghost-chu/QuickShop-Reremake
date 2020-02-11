package org.maxgamer.quickshop.services.integration;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface IntegrationStage {
  IntegrateStage[] registeredStage();
}
