package com.ips2026.pl11.model.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests of the format checks of the data an external user types to become a
 * shareholder (US 35828).
 */
@DisplayName("ShareholderRegistration")
class ShareholderRegistrationTest {

    private static ShareholderRegistration valid() {
        return new ShareholderRegistration("Ana", "López Vidal", "ana.lopez@example.com", "12345678Z", "ana_lopez",
                "secret1");
    }

    @Test
    @DisplayName("valid data has no problems, and the texts are trimmed and the national ID in upper case")
    void validData() {
        ShareholderRegistration registration = new ShareholderRegistration("  Ana ", " López Vidal ",
                " ana.lopez@example.com ", " x1234567l ", " ana_lopez ", " secret ");

        assertTrue(registration.findFormatProblems().isEmpty());
        assertEquals("Ana López Vidal", registration.getFullName());
        assertEquals("ana.lopez@example.com", registration.getEmail());
        assertEquals("X1234567L", registration.getNationalId());
        assertEquals("ana_lopez", registration.getUsername());
        assertEquals(" secret ", registration.getPassword()); // the password is kept as typed
        assertTrue(valid().findFormatProblems().isEmpty());
    }

    @Test
    @DisplayName("every field is required, and each one gives its own message in the order of the form")
    void requiredFields() {
        ShareholderRegistration empty = new ShareholderRegistration(" ", null, "", "  ", "", null);

        assertEquals(List.of("The first name is required.", "The last name is required.", "The email is required.",
                "The national ID is required.", "The username is required.", "The password is required."),
                empty.findFormatProblems());
    }

    @ParameterizedTest
    @ValueSource(strings = { "ana", "ana@example", "@example.com", "ana@@example.com", "ana lopez@example.com" })
    @DisplayName("an email without the form name@domain.ext is not valid")
    void wrongEmail(String email) {
        ShareholderRegistration registration = new ShareholderRegistration("Ana", "López", email, "12345678Z",
                "ana_lopez", "secret1");
        assertEquals(List.of("The email is not valid, for example name@example.com."), registration.findFormatProblems());
    }

    @ParameterizedTest
    @ValueSource(strings = { "12345678-Z", "1234 5678Z", "Ñ1234567" })
    @DisplayName("the national ID can only have letters and digits (its control letter is not checked)")
    void wrongNationalId(String nationalId) {
        ShareholderRegistration registration = new ShareholderRegistration("Ana", "López", "ana@example.com",
                nationalId, "ana_lopez", "secret1");
        assertEquals(List.of("The national ID can only have letters and digits."), registration.findFormatProblems());
    }

    @Test
    @DisplayName("the username cannot have spaces and the password needs at least 6 characters")
    void usernameAndPassword() {
        ShareholderRegistration registration = new ShareholderRegistration("Ana", "López", "ana@example.com",
                "12345678Z", "ana lopez", "12345");
        assertEquals(List.of("The username cannot have spaces.", "The password must have at least 6 characters."),
                registration.findFormatProblems());
    }
}
