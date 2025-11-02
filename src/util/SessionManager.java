package util;

import model.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {
        // Private constructor for singleton
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    public boolean isLibrarian() {
        return currentUser != null && "librarian".equalsIgnoreCase(currentUser.getRole());
    }

    public boolean isMember() {
        return currentUser != null && "member".equalsIgnoreCase(currentUser.getRole());
    }

    public void logout() {
        currentUser = null;
    }

    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "Guest";
    }

    public String getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : "None";
    }

    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
}