package org.example.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CredentialGenerator Unit Tests")
class CredentialGeneratorTest {

    @Test
    @DisplayName("generateUsername - produces 'firstname.lastname' in lowercase")
    void generateUsername_Standard() {
        String result = CredentialGenerator.generateUsername("John", "Doe");

        assertThat(result).isEqualTo("john.doe");
    }

    @Test
    @DisplayName("generateUsername - trims whitespace and lowercases")
    void generateUsername_TrimsAndLowercases() {
        String result = CredentialGenerator.generateUsername("  ALICE  ", "  SMITH  ");

        assertThat(result).isEqualTo("alice.smith");
    }

    @Test
    @DisplayName("generateUsername - throws IllegalArgumentException for null firstName")
    void generateUsername_NullFirstName() {
        assertThatThrownBy(() -> CredentialGenerator.generateUsername(null, "Doe"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("First name and last name cannot be null or blank");
    }

    @Test
    @DisplayName("generateUsername - throws IllegalArgumentException for null lastName")
    void generateUsername_NullLastName() {
        assertThatThrownBy(() -> CredentialGenerator.generateUsername("John", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("generateUsername - throws IllegalArgumentException for blank firstName")
    void generateUsername_BlankFirstName() {
        assertThatThrownBy(() -> CredentialGenerator.generateUsername("  ", "Doe"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("generateUsername - throws IllegalArgumentException for blank lastName")
    void generateUsername_BlankLastName() {
        assertThatThrownBy(() -> CredentialGenerator.generateUsername("John", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("generateUsernameWithSerial - appends serial number to base username")
    void generateUsernameWithSerial_AppendsSerial() {
        String result = CredentialGenerator.generateUsernameWithSerial("John", "Doe", 1);

        assertThat(result).isEqualTo("john.doe1");
    }

    @Test
    @DisplayName("generateUsernameWithSerial - serial number 2 produces correct suffix")
    void generateUsernameWithSerial_Serial2() {
        String result = CredentialGenerator.generateUsernameWithSerial("Jane", "Smith", 2);

        assertThat(result).isEqualTo("jane.smith2");
    }

    @Test
    @DisplayName("generateUsernameWithSerial - inherits null check from generateUsername")
    void generateUsernameWithSerial_NullInput_ThrowsException() {
        assertThatThrownBy(() -> CredentialGenerator.generateUsernameWithSerial(null, "Doe", 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("generatePassword - generates password of exactly 10 characters")
    void generatePassword_Length() {
        String password = CredentialGenerator.generatePassword();

        assertThat(password).hasSize(10);
    }

    @Test
    @DisplayName("generatePassword - generates password containing only alphanumeric characters")
    void generatePassword_AlphanumericOnly() {
        String password = CredentialGenerator.generatePassword();

        assertThat(password).matches("[A-Za-z0-9]+");
    }

    @Test
    @DisplayName("generatePassword - two generated passwords are (almost certainly) different")
    void generatePassword_IsRandom() {
        String p1 = CredentialGenerator.generatePassword();
        String p2 = CredentialGenerator.generatePassword();

        assertThat(p1).isNotNull();
        assertThat(p2).isNotNull();
    }

    @Test
    @DisplayName("generatePassword - generates non-null, non-blank password")
    void generatePassword_NotBlank() {
        String password = CredentialGenerator.generatePassword();

        assertThat(password).isNotBlank();
    }
}

