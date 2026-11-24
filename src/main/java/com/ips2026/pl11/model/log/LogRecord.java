package com.ips2026.pl11.model.log;

import java.time.LocalDateTime;

/**
 * US 36717: one record of the system log (table SYSTEM_LOG), with the user
 * of its session.
 */
public class LogRecord {

    private final int id;
    private final LocalDateTime loggedAt;
    private final int sessionId;
    private final String username;
    private final String role;
    private final LogAction action;
    private final String description;

    /** @param username username of the session, or null for a guest */
    public LogRecord(int id, LocalDateTime loggedAt, int sessionId, String username, String role, LogAction action,
            String description) {
        this.id = id;
        this.loggedAt = loggedAt;
        this.sessionId = sessionId;
        this.username = username;
        this.role = role;
        this.action = action;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    public int getSessionId() {
        return sessionId;
    }

    /** Username, or null if nobody had logged in (guest). */
    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    /** "general_manager (General Manager)", or "Guest". */
    public String getUserLabel() {
        return UserSession.userLabel(username, role);
    }

    public LogAction getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }
}
