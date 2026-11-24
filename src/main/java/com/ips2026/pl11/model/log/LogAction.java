package com.ips2026.pl11.model.log;

/**
 * US 36717: kinds of action stored in the system log.
 */
public enum LogAction {

    /** A user logged in. */
    LOGIN,
    /** Someone tried to log in with a wrong username or password. */
    LOGIN_FAILED,
    /** A user looked at data (opened a window that shows data, or changed what it shows). */
    QUERY,
    /** Something new was stored in the database. */
    INSERT,
    /** Something stored in the database was changed. */
    UPDATE,
    /** Something was removed from the database. */
    DELETE
}
