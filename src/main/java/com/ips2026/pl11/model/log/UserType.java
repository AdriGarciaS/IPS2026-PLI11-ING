package com.ips2026.pl11.model.log;

/**
 * US 36717: who is behind a session of the application.
 */
public enum UserType {

    /** An employee who has logged in (the role is their position). */
    EMPLOYEE,
    /** A shareholder who has logged in. */
    SHAREHOLDER,
    /** Nobody has logged in yet (the login window, and the purchases of external users). */
    GUEST
}
