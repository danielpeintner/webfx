// Generated by WebFx

module webfx.platform.client.uischeduler {

    // Direct dependencies modules
    requires java.base;
    requires webfx.platform.shared.scheduler;
    requires webfx.platform.shared.util;

    // Exported packages
    exports webfx.platform.client.services.uischeduler;
    exports webfx.platform.client.services.uischeduler.spi;
    exports webfx.platform.client.services.uischeduler.spi.impl;

    // Used services
    uses webfx.platform.client.services.uischeduler.spi.UiSchedulerProvider;

}