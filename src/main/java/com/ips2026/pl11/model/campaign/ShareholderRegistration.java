package com.ips2026.pl11.model.campaign;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * US 35828: personal data that an external user types to become a
 * shareholder when buying shares in phase 3. The texts are kept trimmed.
 * {@link #findFormatProblems()} checks what can be checked without the
 * database (that the email, national ID and username are not registered yet
 * is checked by the controller).
 */
public class ShareholderRegistration {

    public static final int MIN_PASSWORD_LENGTH = 6;

    private final String firstName;
    private final String lastName;
    private final String email;
    private final String nationalId;
    private final String username;
    private final String password;

    public ShareholderRegistration(String firstName, String lastName, String email, String nationalId,
            String username, String password) {
        this.firstName = strip(firstName);
        this.lastName = strip(lastName);
        this.email = strip(email);
        this.nationalId = strip(nationalId).toUpperCase(Locale.ROOT);
        this.username = strip(username);
        this.password = password == null ? "" : password; // spaces in a password are kept
    }

    private static String strip(String text) {
        return text == null ? "" : text.strip();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    /** The national ID in upper case. */
    public String getNationalId() {
        return nationalId;
    }

    public String getUsername() {
        return username;
    }

    /** The password as typed (it is stored hashed, never as it is). */
    public String getPassword() {
        return password;
    }

    /**
     * Problems of format, one message per problem, in the order of the form:
     * every field is required, the email must look like
     * {@code name@domain.ext}, the national ID can only have letters and
     * digits, the username cannot have spaces and the password needs at
     * least {@value #MIN_PASSWORD_LENGTH} characters.
     */
    public List<String> findFormatProblems() {
        List<String> problems = new ArrayList<>();
        if (firstName.isEmpty()) {
            problems.add("The first name is required.");
        }
        if (lastName.isEmpty()) {
            problems.add("The last name is required.");
        }
        if (email.isEmpty()) {
            problems.add("The email is required.");
        } else if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            problems.add("The email is not valid, for example name@example.com.");
        }
        if (nationalId.isEmpty()) {
            problems.add("The national ID is required.");
        } else if (!nationalId.matches("[A-Z0-9]+")) {
            problems.add("The national ID can only have letters and digits.");
        }
        if (username.isEmpty()) {
            problems.add("The username is required.");
        } else if (!username.matches("\\S+")) {
            problems.add("The username cannot have spaces.");
        }
        if (password.isEmpty()) {
            problems.add("The password is required.");
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            problems.add("The password must have at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        return problems;
    }
}
