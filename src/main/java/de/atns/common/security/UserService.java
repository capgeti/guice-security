package de.atns.common.security;

/**
 * @author tbaum
 * @since 27.11.2009
 */
public interface UserService {
// -------------------------- OTHER METHODS --------------------------

    SecurityUser findUser(String login, String pass);

    SecurityUser refreshUser(String login);
}
