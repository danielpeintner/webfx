package naga.providers.platform.abstr.java.services.resource;

import naga.platform.services.resource.spi.ResourceServiceProvider;
import naga.util.async.Future;

import java.io.InputStream;
import java.util.Scanner;

/**
 * @author Bruno Salmon
 */
public final class JavaResourceServiceProvider implements ResourceServiceProvider {

    @Override
    public Future<String> getText(String resourcePath) {
        try (Scanner scanner = createScanner(getClass().getClassLoader().getResourceAsStream(resourcePath))) {
            return Future.succeededFuture(scanner == null ? null : scanner.useDelimiter("\\A").next());
        }
    }

    private static Scanner createScanner(InputStream inputStream) {
        return inputStream == null ? null : new Scanner(inputStream, "UTF-8");
    }
}