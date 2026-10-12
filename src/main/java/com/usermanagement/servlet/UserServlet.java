package com.usermanagement.servlet;

import com.usermanagement.dao.IUserDAO;
import com.usermanagement.dao.InMemoryUserDAO;
import com.usermanagement.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * UserServlet handles HTTP requests for User CRUD operations:
 * - GET: Read all users or fetch a user by ID
 * - POST: Create a new user or process form submissions (Update/Delete via action param)
 * - PUT: Update an existing user's details
 * - DELETE: Delete a user by ID
 */
@WebServlet("/users")
public class UserServlet extends HttpServlet {

    private IUserDAO userDAO;

    @Override
    public void init() throws ServletException {
        // Default to In-Memory DAO for prototype.
        // Can be switched to new MySQLUserDAO() if database is configured.
        this.userDAO = new InMemoryUserDAO();
    }

    // Helper setter for dependency injection / switching DAO
    public void setUserDAO(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        String idParam = request.getParameter("id");
        String action = request.getParameter("action");

        PrintWriter out = response.getWriter();

        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idParam.trim());
                User user = userDAO.getUserById(id);
                if (user != null) {
                    out.print(toJson(user));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\": \"User not found\", \"id\": " + id + "}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid User ID format\"}");
            }
        } else {
            List<User> users = userDAO.getAllUsers();
            out.print(toJsonList(users));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        String action = request.getParameter("action");

        // Action parameter dispatch for HTML form / traditional HTTP POST callers
        if ("update".equalsIgnoreCase(action)) {
            handleUpdate(request, response);
            return;
        } else if ("delete".equalsIgnoreCase(action)) {
            handleDelete(request, response);
            return;
        }

        // Standard Create User
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String status = request.getParameter("status");

        // If form body was JSON instead of form-encoded
        if (name == null && request.getContentType() != null && request.getContentType().contains("application/json")) {
            String jsonBody = readRequestBody(request);
            name = extractJsonField(jsonBody, "name");
            email = extractJsonField(jsonBody, "email");
            role = extractJsonField(jsonBody, "role");
            status = extractJsonField(jsonBody, "status");
        }

        if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\": \"Name and Email are required fields\"}");
            return;
        }

        User newUser = new User(name.trim(), email.trim(), role, status);
        User createdUser = userDAO.createUser(newUser);

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().print("{\"message\": \"User created successfully\", \"user\": " + toJson(createdUser) + "}");
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleUpdate(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleDelete(request, response);
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        String idParam = request.getParameter("id");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String status = request.getParameter("status");

        if (name == null && request.getContentType() != null && request.getContentType().contains("application/json")) {
            String jsonBody = readRequestBody(request);
            idParam = extractJsonField(jsonBody, "id");
            name = extractJsonField(jsonBody, "name");
            email = extractJsonField(jsonBody, "email");
            role = extractJsonField(jsonBody, "role");
            status = extractJsonField(jsonBody, "status");
        }

        if (idParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\": \"User ID is required for update\"}");
            return;
        }

        try {
            int id = Integer.parseInt(idParam.trim());
            User existingUser = userDAO.getUserById(id);
            if (existingUser == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print("{\"error\": \"User with ID " + id + " does not exist\"}");
                return;
            }

            if (name != null) existingUser.setName(name.trim());
            if (email != null) existingUser.setEmail(email.trim());
            if (role != null) existingUser.setRole(role.trim());
            if (status != null) existingUser.setStatus(status.trim());

            boolean success = userDAO.updateUser(existingUser);
            if (success) {
                response.getWriter().print("{\"message\": \"User updated successfully\", \"user\": " + toJson(existingUser) + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"error\": \"Failed to update user\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\": \"Invalid User ID\"}");
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");

        String idParam = request.getParameter("id");

        if (idParam == null && request.getContentType() != null && request.getContentType().contains("application/json")) {
            String jsonBody = readRequestBody(request);
            idParam = extractJsonField(jsonBody, "id");
        }

        if (idParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\": \"User ID is required for deletion\"}");
            return;
        }

        try {
            int id = Integer.parseInt(idParam.trim());
            boolean success = userDAO.deleteUser(id);
            if (success) {
                response.getWriter().print("{\"message\": \"User deleted successfully\", \"id\": " + id + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print("{\"error\": \"User with ID " + id + " not found\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\": \"Invalid User ID\"}");
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private String extractJsonField(String json, String key) {
        if (json == null) return null;
        String pattern = "\"" + key + "\"\\s*:\\s*\"?([^\",\\}\\]]+)\"?";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    public static String toJson(User u) {
        return String.format(
            "{\"id\": %d, \"name\": \"%s\", \"email\": \"%s\", \"role\": \"%s\", \"status\": \"%s\"}",
            u.getId(), escapeJson(u.getName()), escapeJson(u.getEmail()), escapeJson(u.getRole()), escapeJson(u.getStatus())
        );
    }

    public static String toJsonList(List<User> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(toJson(list.get(i)));
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
