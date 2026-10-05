package com.ips2026.pl11.model.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("CardValidator")
class CardValidatorTest {

    @ParameterizedTest(name = "\"{0}\" is valid")
    @DisplayName("real test card numbers are valid, with or without spaces and dashes")
    @ValueSource(strings = {
            "4111111111111111",        // Visa
            "4111 1111 1111 1111",
            "4111-1111-1111-1111",
            "5555555555554444",        // Mastercard
            "378282246310005",         // American Express (15 digits)
            "4222222222222",           // 13 digits
            "6011111111111117"         // Discover
    })
    void validNumbers(String number) {
        assertTrue(CardValidator.isValid(number));
    }

    @ParameterizedTest(name = "\"{0}\" is rejected")
    @DisplayName("an empty card number is rejected")
    @NullAndEmptySource
    @ValueSource(strings = { "   ", " - " })
    void emptyNumbers(String number) {
        assertEquals("Enter the bank card number.", CardValidator.findProblem(number).orElse(""));
    }

    @Test
    @DisplayName("letters or other symbols are rejected")
    void nonDigitsAreRejected() {
        assertTrue(CardValidator.findProblem("4111 1111 1111 111A").orElse("").contains("only contain digits"));
        assertTrue(CardValidator.findProblem("4111.1111.1111.1111").orElse("").contains("only contain digits"));
    }

    @Test
    @DisplayName("numbers with less than 13 or more than 19 digits are rejected")
    void lengthLimits() {
        assertTrue(CardValidator.findProblem("411111111111").orElse("").contains("between 13 and 19"));          // 12
        assertTrue(CardValidator.findProblem("41111111111111111111").orElse("").contains("between 13 and 19"));  // 20
    }

    @Test
    @DisplayName("a typing mistake in one digit is detected (Luhn check)")
    void wrongDigitIsDetected() {
        assertFalse(CardValidator.isValid("4111 1111 1111 1112"));
        assertTrue(CardValidator.findProblem("4111 1111 1111 1112").orElse("").contains("not valid"));
    }

    @Test
    @DisplayName("the masked number only shows the last 4 digits")
    void maskShowsLastFourDigits() {
        assertEquals("**** **** **** 1111", CardValidator.mask("4111 1111 1111 1111"));
        assertEquals("**** **** **** 0005", CardValidator.mask("3782-822463-10005"));
    }
}
