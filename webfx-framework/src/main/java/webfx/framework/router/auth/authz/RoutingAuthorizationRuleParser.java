package webfx.framework.router.auth.authz;

import webfx.framework.services.authz.spi.impl.inmemory.AuthorizationRuleType;
import webfx.framework.services.authz.spi.impl.inmemory.InMemoryAuthorizationRule;
import webfx.framework.services.authz.spi.impl.inmemory.parser.SimpleInMemoryAuthorizationRuleParserBase;

/**
 * @author Bruno Salmon
 */
public class RoutingAuthorizationRuleParser extends SimpleInMemoryAuthorizationRuleParserBase {

    @Override
    protected InMemoryAuthorizationRule parseAuthorization(AuthorizationRuleType type, String argument) {
        if (argument.startsWith("route:")) {
            String route = argument.substring(6).trim();
            boolean includeSubRoutes = route.endsWith("*");
            if (includeSubRoutes)
                route = route.substring(0, route.length() - 1);
            return new RoutingAuthorizationRule(type, route, includeSubRoutes);
        }
        return null;
    }

}