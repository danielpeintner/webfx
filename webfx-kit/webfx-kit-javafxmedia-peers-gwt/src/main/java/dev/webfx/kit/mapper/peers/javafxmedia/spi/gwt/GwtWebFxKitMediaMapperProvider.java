package dev.webfx.kit.mapper.peers.javafxmedia.spi.gwt;

import dev.webfx.kit.mapper.peers.javafxmedia.MediaPlayerPeer;
import dev.webfx.kit.mapper.peers.javafxmedia.spi.WebFxKitMediaMapperProvider;

/**
 * @author Bruno Salmon
 */
public final class GwtWebFxKitMediaMapperProvider implements WebFxKitMediaMapperProvider {

    @Override
    public MediaPlayerPeer createMediaPlayerPeer(String mediaUrl) {
        return new GwtMediaPlayerPeer(mediaUrl);
    }

}
