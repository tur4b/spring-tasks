package org.example.util;

import java.security.SecureRandom;

/**
 * Utility class for credential generation
 */
public class CredentialGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a username according to firstName and lastName
     *
     * @param firstName user first name
     * @param lastName user last name
     * @return generated username
     */
    public static String generateUsername(String firstName, String lastName) {
        if(firstName == null || lastName == null || firstName.isBlank() || lastName.isBlank()) {
            throw new IllegalArgumentException("First name and last name cannot be null or blank");
        }
        return String.format("%s.%s", firstName.trim().toLowerCase(), lastName.trim().toLowerCase());
    }

    /**
     * Generates a username according to first name, last name and serial number
     *
     * @param firstName user's first name
     * @param lastName user's last name
     * @param serialNumber serial number suffix to handle duplicate usernames
     * @return generated username with serial number
     */
    public static String generateUsernameWithSerial(String firstName, String lastName, int serialNumber) {
        String username = generateUsername(firstName, lastName);
        return username + serialNumber;
    }

    /**
     * Generates random password with fixed length of 10 characters.
     * Including uppercase, lowercase, and numeric characters.
     *
     * @return randomly generated password
     */
    public static String generatePassword() {
        StringBuilder password = new StringBuilder();
        for(int i = 0; i < DEFAULT_LENGTH; i++) {
            password.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }
}
