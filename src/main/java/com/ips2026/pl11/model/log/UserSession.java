package com.ips2026.pl11.model.log;

import java.time.LocalDateTime;

/**
 * US 36717: a session of the application (table USER_SESSION). Every record
 * of the system log belongs to one session, which says who did it.
 */
public class UserSession {

    public static final String GUEST_ROLE = "Guest";

    private final int id;
    private final UserType userType;
    private final Integer userId;
    private final String username;
    private final String role;
    private final LocalDateTime startedAt;

    /**
     * @param userId   id of the employee or shareholder, or null for a guest
     * @param username username, or null for a guest
     */
    public UserSession(int id, UserType userType, Integer userId, String username, String role,
            LocalDateTime startedAt) {
        this.id = id;
        this.userType = userType;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.startedAt = startedAt;
    }

    public int getId() {
        return id;
    }

    public UserType getUserType() {
        return userType;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    /** How the user is shown: "general_manager (General Manager)", or "Guest" when nobody has logged in. */
    public String getUserLabel() {
        return userLabel(username, role);
    }

    /** See {@link #getUserLabel()}. */
    public static String userLabel(String username, String role) {
        return username == null ? GUEST_ROLE : username + " (" + role + ")";
    }
}
