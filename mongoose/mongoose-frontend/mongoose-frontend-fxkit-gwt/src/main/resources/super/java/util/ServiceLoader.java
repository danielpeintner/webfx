package java.util;

import java.util.Iterator;
import webfx.platforms.core.util.function.Factory;

public class ServiceLoader<S> implements Iterable<S> {

    public static <S> ServiceLoader<S> load(Class<S> serviceClass) {
        if (serviceClass.equals(webfx.platforms.core.services.storage.spi.LocalStorageProvider.class)) return new ServiceLoader<S>(webfx.platform.gwt.services.storage.GwtLocalStorageProviderImpl::new);
        if (serviceClass.equals(webfx.platforms.core.services.storage.spi.SessionStorageProvider.class)) return new ServiceLoader<S>(webfx.platform.gwt.services.storage.GwtSessionStorageProviderImpl::new);
        if (serviceClass.equals(webfx.platforms.core.services.windowhistory.spi.WindowHistoryProvider.class)) return new ServiceLoader<S>(webfx.platforms.web.services.windowhistory.WebWindowHistoryProvider::new);
        if (serviceClass.equals(webfx.platforms.core.services.resource.spi.ResourceServiceProvider.class)) return new ServiceLoader<S>(webfx.platform.gwt.services.resource.GwtResourceServiceProviderImpl::new);
        if (serviceClass.equals(webfx.fxkits.core.spi.FxKitProvider.class)) return new ServiceLoader<S>(webfx.fxkit.gwt.GwtFxKitProvider::new);
        if (serviceClass.equals(webfx.platforms.core.services.appcontainer.spi.ApplicationContainerProvider.class)) return new ServiceLoader<S>(webfx.platform.gwt.services.appcontainer.GwtApplicationContainerProvider::new);
        if (serviceClass.equals(webfx.platforms.core.services.appcontainer.spi.ApplicationModuleInitializer.class)) return new ServiceLoader<S>(mongooses.web.activities.sharedends.MongooseWebApplicationModuleInitializer::new, mongooses.frontend.MongooseFrontendApplicationModuleInitializer::new, webfx.platforms.core.services.update.UpdateModuleInitializer::new, webfx.fxkits.core.FxKitModuleInitializer::new, webfx.platforms.core.services.buscall.BusCallModuleInitializer::new, webfx.platforms.core.services.query.QueryModuleInitializer::new, webfx.platforms.core.services.querypush.QueryPushModuleInitializer::new);
        if (serviceClass.equals(webfx.platforms.core.services.shutdown.spi.ShutdownProvider.class)) return new ServiceLoader<S>(webfx.platform.gwt.services.shutdown.GwtShutdownProviderImpl::new);
        if (serviceClass.equals(webfx.platforms.core.services.log.spi.LoggerProvider.class)) return new ServiceLoader<S>(webfx.platform.gwt.services.log.GwtLoggerProviderImpl::new);
        if (serviceClass.equals(webfx.platforms.core.util.numbers.spi.NumbersProvider.class)) return new ServiceLoader<S>(webfx.platforms.core.util.numbers.providers.StandardNumbersProviderImpl::new);
        if (serviceClass.equals(webfx.platforms.core.services.bus.spi.BusServiceProvider.class)) return new ServiceLoader<S>(webfx.platforms.web.services.clientbus.WebClientBusServiceProvider::new, webfx.platforms.core.services.bus.client.ClientBusServiceProviderImpl::new);
        if (serviceClass.equals(webfx.platforms.core.services.windowlocation.spi.WindowLocationProvider.class)) return new ServiceLoader<S>(webfx.platform.gwt.services.windowlocation.GwtWindowLocationProvider::new);
        if (serviceClass.equals(webfx.platforms.core.services.json.spi.JsonProvider.class)) return new ServiceLoader<S>(webfx.platform.gwt.services.json.GwtJsonObject::create);
        if (serviceClass.equals(webfx.platforms.core.services.uischeduler.spi.UiSchedulerProvider.class)) return new ServiceLoader<S>(webfx.platform.gwt.services.scheduler.GwtSchedulerProviderImpl::new);
        if (serviceClass.equals(webfx.platforms.core.services.scheduler.spi.SchedulerProvider.class)) return new ServiceLoader<S>(webfx.platform.gwt.services.scheduler.GwtSchedulerProviderImpl::new);
        if (serviceClass.equals(webfx.platforms.web.services.windowhistory.JsWindowHistory.class)) return new ServiceLoader<S>(webfx.platform.gwt.services.windowhistory.GwtJsWindowHistory::new);
        if (serviceClass.equals(webfx.platforms.core.services.websocket.spi.WebSocketServiceProvider.class)) return new ServiceLoader<S>(webfx.platform.gwt.services.websocket.GwtWebSocketServiceProviderImpl::new);
        return new ServiceLoader<S>();
    }

    private final Factory[] factories;

    public ServiceLoader(Factory... factories) {
        this.factories = factories;
    }

    public Iterator<S> iterator() {
        return new Iterator<S>() {
            int index = 0;
            @Override
            public boolean hasNext() {
                return index < factories.length;
            }

            @Override
            public S next() {
                return (S) factories[index++].create();
            }
        };
    }
}