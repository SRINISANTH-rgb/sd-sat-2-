package com.usermanagement.model;
public class User {
    private int id;
    private String name;
    private String email;
    private String role;
    private String status;
    public User() {
    }
    public User(int id, String name, String email, String role, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role != null ? role : "Student";
        this.status = status != null ? status : "Active";
    }
    public User(String name, String email, String role, String status) {
        this.name = name;
        this.email = email;
        this.role = role != null ? role : "Student";
        this.status = status != null ? status : "Active";
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
