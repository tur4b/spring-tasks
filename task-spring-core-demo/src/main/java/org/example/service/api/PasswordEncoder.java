package org.example.service.api;

public interface PasswordEncoder {

    /**
     * Encode a rawPassword
     *
     * @param rawPassword plaintext password
     * @return encoded version of rawPassword
     */
    String encode(String rawPassword);

    /**
     * Matches a raw password against an existing encoded format.
     *
     * @param rawPassword plaintext password
     * @param encodedPassword encoded version of any password
     * @return true if matches, otherwise false
     */
    boolean matches(String rawPassword, String encodedPassword);

}
