package naga.platform.services.auth.spi.authz;

/**
 * @author Bruno Salmon
 */
public interface AuthorityParser {

    Object parseAuthority(String authority);

}
