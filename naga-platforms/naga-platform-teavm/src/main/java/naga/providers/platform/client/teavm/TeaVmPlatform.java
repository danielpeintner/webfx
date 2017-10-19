package naga.providers.platform.client.teavm;

import naga.platform.spi.Platform;
import naga.providers.platform.abstr.web.WebPlatform;
import naga.providers.platform.client.teavm.url.history.TeaVmWindowHistory;
import naga.providers.platform.client.teavm.url.location.TeaVmWindowLocation;
import naga.providers.platform.client.teavm.websocket.TeaVmWebSocketFactory;

/**
 * @author Bruno Salmon
 */
public final class TeaVmPlatform extends WebPlatform {

    public static void register() {
        Platform.register(new TeaVmPlatform());
    }

    public TeaVmPlatform() {
        super(new TeaVmWebSocketFactory(), TeaVmWindowLocation.current(), TeaVmWindowHistory.current());
    }
}
