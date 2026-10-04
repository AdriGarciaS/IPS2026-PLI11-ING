package com.ips2026.pl11.model.reservation;

import java.util.Optional;

/**
 * Validation of bank card numbers, as real payment forms do:
 * <ul>
 *   <li>spaces and dashes are allowed while typing ("4111 1111 1111 1111");</li>
 *   <li>it must have between 13 and 19 digits;</li>
 *   <li>it must pass the Luhn check digit algorithm, which every real card
 *       number passes and which detects most typing mistakes.</li>
 * </ul>
 */
public final class CardValidator {

    public static final int MIN_DIGITS = 13;
    public static final int MAX_DIGITS = 19;

    private CardValidator() {
        // Utility class: not instantiated.
    }

    /** The card number without spaces or dashes. */
    public static String normalize(String cardNumber) {
        return cardNumber == null ? "" : cardNumber.replace(" ", "").replace("-", "");
    }

    /** @return the reason why the card number is not valid, or empty if it is valid */
    public static Optional<String> findProblem(String cardNumber) {
        String digits = normalize(cardNumber);
        if (digits.isEmpty()) {
            return Optional.of("Enter the bank card number.");
        }
        if (!digits.chars().allMatch(Character::isDigit)) {
            return Optional.of("The card number can only contain digits (spaces and dashes are allowed).");
        }
        if (digits.length() < MIN_DIGITS || digits.length() > MAX_DIGITS) {
            return Optional.of("A card number has between " + MIN_DIGITS + " and " + MAX_DIGITS + " digits.");
        }
        if (!passesLuhn(digits)) {
            return Optional.of("The card number is not valid. Please check the digits.");
        }
        return Optional.empty();
    }

    public static boolean isValid(String cardNumber) {
        return findProblem(cardNumber).isEmpty();
    }

    /**
     * Hides every digit except the last 4, for example
     * "**** **** **** 1111". This is what is stored and shown.
     */
    public static String mask(String cardNumber) {
        String digits = normalize(cardNumber);
        String last4 = digits.length() <= 4 ? digits : digits.substring(digits.length() - 4);
        return "**** **** **** " + last4;
    }

    /**
     * Luhn algorithm: from the right, every second digit is doubled (minus 9
     * if it is greater than 9); the sum of all the digits must be a multiple
     * of 10.
     */
    static boolean passesLuhn(String digits) {
        int sum = 0;
        boolean doubleIt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = digits.charAt(i) - '0';
            if (doubleIt) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }
}
