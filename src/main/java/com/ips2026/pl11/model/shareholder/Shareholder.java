package com.ips2026.pl11.model.shareholder;

/**
 * A person that owns shares of the club (table SHAREHOLDER).
 */
public final class Shareholder {

    private final int id;
    private final String firstName;
    private final String lastName;
    private final String nationalId;
    private final String username;
    private final String password;

    public Shareholder(int id, String firstName, String lastName, String nationalId) {
        this(id, firstName, lastName, nationalId, null, null);
    }
    
    public Shareholder(int id, String firstName, String lastName, String nationalId, String username, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationalId = nationalId;
        this.username = username;
        this.password = password;
    }

    public int getId() {
        return id;
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

    public String getNationalId() {
        return nationalId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }

    /** "Name (national ID)". */
    @Override
    public String toString() {
        return getFullName() + " (" + nationalId + ")";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Shareholder shareholder && shareholder.id == id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
