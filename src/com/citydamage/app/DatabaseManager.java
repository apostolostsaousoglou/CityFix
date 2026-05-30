package com.citydamage.app;

import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton that manages the PostgreSQL connection and all DB queries.
 * Passwords are stored as SHA-256 hashes (simple, no extra libs needed).
 * Note: existing argon2-hashed passwords from the web app won't work here;
 * users registered via the Java app will use SHA-256.
 */
public class DatabaseManager {

    private static final String URL;
    private static final String USER;
    private static final String PASS;

    static {
        Map<String, String> env = loadEnv();
        URL  = env.getOrDefault("DB_URL",  "");
        USER = env.getOrDefault("DB_USER", "");
        PASS = env.getOrDefault("DB_PASS", "");
    }

    private static Map<String, String> loadEnv() {
        Map<String, String> map = new HashMap<>();
        try {
            Path envPath = Path.of(".env");
            if (!Files.exists(envPath)) envPath = Path.of("../.env");
            if (Files.exists(envPath)) {
                for (String line : Files.readAllLines(envPath)) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eq = line.indexOf('=');
                    if (eq > 0) map.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
                }
            } else {
                System.err.println("Warning: .env file not found");
            }
        } catch (Exception e) {
            System.err.println("Failed to load .env: " + e.getMessage());
        }
        return map;
    }

    private static DatabaseManager instance;
    private Connection conn;

    private DatabaseManager() {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected to PostgreSQL database");
        } catch (Exception e) {
            System.err.println("DB connection failed: " + e.getMessage());
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public boolean isConnected() {
        try { return conn != null && conn.isValid(2); } catch (Exception e) { return false; }
    }

    private void reconnectIfNeeded() {
        if (isConnected()) return;
        try {
            if (conn != null) { try { conn.close(); } catch (Exception ignored) {} }
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Reconnected to PostgreSQL database");
        } catch (Exception e) {
            System.err.println("Reconnect failed: " + e.getMessage());
        }
    }

    // ─── Password hashing ────────────────────────────────────────────────────

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ─── Auth ────────────────────────────────────────────────────────────────

    /**
     * Returns the user record if credentials match, null otherwise.
     * Checks both email and phone. Supports SHA-256 hashed passwords (Java app).
     */
    public UserRecord login(String identifier, String password) {
        reconnectIfNeeded();
        if (!isConnected()) return null;
        String sql = "SELECT user_id, user_first_name, user_last_name, user_email, user_phone, user_password, is_admin " +
                     "FROM \"User\" WHERE user_email = ? OR user_phone = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            String storedHash = rs.getString("user_password");
            // Check SHA-256 hash (Java app registered users)
            boolean ok = storedHash.equals(sha256(password));
            if (!ok) return null;

            return new UserRecord(
                rs.getInt("user_id"),
                rs.getString("user_first_name"),
                rs.getString("user_last_name"),
                rs.getString("user_email"),
                rs.getString("user_phone"),
                rs.getBoolean("is_admin")
            );
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Registers a new user. Returns the created UserRecord or null on failure.
     */
    public UserRecord register(String firstName, String lastName, String email,
                               String phone, String password, boolean isAdmin) {
        reconnectIfNeeded();
        if (!isConnected()) return null;
        // Check duplicates
        if (getUserByEmail(email) != null || getUserByPhone(phone) != null) return null;

        String countSql = "SELECT COALESCE(MAX(user_id), 0) FROM \"User\"";
        String insertSql = "INSERT INTO \"User\" (user_id, user_email, user_password, user_phone, user_first_name, user_last_name, is_admin) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            int newId;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(countSql)) {
                rs.next();
                newId = rs.getInt(1) + 1;
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, newId);
                ps.setString(2, email);
                ps.setString(3, sha256(password));
                ps.setString(4, phone);
                ps.setString(5, firstName);
                ps.setString(6, lastName);
                ps.setBoolean(7, isAdmin);
                ps.executeUpdate();
            }
            return new UserRecord(newId, firstName, lastName, email, phone, isAdmin);
        } catch (Exception e) {
            System.err.println("Register error: " + e.getMessage());
            return null;
        }
    }

    public UserRecord getUserByEmail(String email) {
        return queryUser("user_email", email);
    }

    public UserRecord getUserByPhone(String phone) {
        return queryUser("user_phone", phone);
    }

    private UserRecord queryUser(String column, String value) {
        reconnectIfNeeded();
        if (!isConnected()) return null;
        String sql = "SELECT user_id, user_first_name, user_last_name, user_email, user_phone, is_admin " +
                     "FROM \"User\" WHERE " + column + " = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            return new UserRecord(
                rs.getInt("user_id"),
                rs.getString("user_first_name"),
                rs.getString("user_last_name"),
                rs.getString("user_email"),
                rs.getString("user_phone"),
                rs.getBoolean("is_admin")
            );
        } catch (Exception e) {
            System.err.println("Query user error: " + e.getMessage());
            return null;
        }
    }

    // ─── Reports ─────────────────────────────────────────────────────────────

    /**
     * Inserts a new damage report. Returns true on success.
     */
    public boolean addReport(String type, String description, String street,
                              String streetNumber, String area, String pcode,
                              double latitude, double longitude, String userPhone,
                              byte[] photo) {
        reconnectIfNeeded();
        if (!isConnected()) return false;
        String sql = "INSERT INTO \"Report\" " +
                     "(report_type, report_description, report_date, report_street, report_street_number, " +
                     " report_area, report_pcode, report_latitude, report_longitude, report_status, user_phone, report_photo) " +
                     "VALUES (?, ?, CURRENT_DATE, ?, ?, ?, ?, ?, ?, 'received', ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, description);
            ps.setString(3, street);
            ps.setString(4, streetNumber);
            ps.setString(5, area);
            ps.setString(6, pcode);
            ps.setDouble(7, latitude);
            ps.setDouble(8, longitude);
            ps.setString(9, userPhone);
            if (photo != null) ps.setBytes(10, photo);
            else               ps.setNull(10, Types.BINARY);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("Add report error: " + e.getMessage());
            return false;
        }
    }

    // ─── Update user info ────────────────────────────────────────────────────

    /**
     * Updates user info. Pass null/empty password to keep existing one.
     * Returns updated UserRecord or null on failure.
     */
    public UserRecord updateUserInfo(int userId, String firstName, String lastName,
                                     String email, String phone, String newPassword, boolean isAdmin) {
        reconnectIfNeeded();
        if (!isConnected()) return null;
        try {
            if (newPassword != null && !newPassword.isBlank()) {
                String sql = "UPDATE \"User\" SET user_first_name=?, user_last_name=?, " +
                             "user_email=?, user_phone=?, user_password=? WHERE user_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, firstName);
                    ps.setString(2, lastName);
                    ps.setString(3, email);
                    ps.setString(4, phone);
                    ps.setString(5, sha256(newPassword));
                    ps.setInt(6, userId);
                    ps.executeUpdate();
                }
            } else {
                String sql = "UPDATE \"User\" SET user_first_name=?, user_last_name=?, " +
                             "user_email=?, user_phone=? WHERE user_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, firstName);
                    ps.setString(2, lastName);
                    ps.setString(3, email);
                    ps.setString(4, phone);
                    ps.setInt(5, userId);
                    ps.executeUpdate();
                }
            }
            return new UserRecord(userId, firstName, lastName, email, phone, isAdmin);
        } catch (Exception e) {
            System.err.println("Update user error: " + e.getMessage());
            return null;
        }
    }

    // ─── Reports queries ─────────────────────────────────────────────────────

    public java.util.List<ReportRecord> getReportsByPhone(String phone) {
        String sql = "SELECT report_id, report_type, report_description, " +
                     "to_char(report_date,'YYYY-MM-DD') as report_date, " +
                     "report_street, report_street_number, report_area, report_pcode, " +
                     "report_latitude, report_longitude, report_status, user_phone, " +
                     "(report_photo IS NOT NULL) AS has_photo " +
                     "FROM \"Report\" WHERE user_phone = ? ORDER BY report_id DESC";
        return queryReports(sql, phone);
    }

    public java.util.List<ReportRecord> getAllReports() {
        String sql = "SELECT report_id, report_type, report_description, " +
                     "to_char(report_date,'YYYY-MM-DD') as report_date, " +
                     "report_street, report_street_number, report_area, report_pcode, " +
                     "report_latitude, report_longitude, report_status, user_phone, " +
                     "(report_photo IS NOT NULL) AS has_photo " +
                     "FROM \"Report\" ORDER BY report_id DESC";
        return queryReports(sql, null);
    }

    private java.util.List<ReportRecord> queryReports(String sql, String param) {
        java.util.List<ReportRecord> list = new java.util.ArrayList<>();
        reconnectIfNeeded();
        if (!isConnected()) return list;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ReportRecord(
                    rs.getInt("report_id"),
                    rs.getString("report_type"),
                    rs.getString("report_description"),
                    rs.getString("report_date"),
                    rs.getString("report_street"),
                    rs.getString("report_street_number"),
                    rs.getString("report_area"),
                    rs.getString("report_pcode"),
                    rs.getDouble("report_latitude"),
                    rs.getDouble("report_longitude"),
                    rs.getString("report_status"),
                    rs.getString("user_phone"),
                    rs.getBoolean("has_photo")
                ));
            }
        } catch (Exception e) {
            System.err.println("Query reports error: " + e.getMessage());
        }
        return list;
    }

    public boolean updateReportStatus(int reportId, String status) {
        reconnectIfNeeded();
        if (!isConnected()) return false;
        String sql = "UPDATE \"Report\" SET report_status = ? WHERE report_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reportId);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("Update status error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteReport(int reportId) {
        reconnectIfNeeded();
        if (!isConnected()) return false;
        String sql = "DELETE FROM \"Report\" WHERE report_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reportId);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("Delete report error: " + e.getMessage());
            return false;
        }
    }

    // ─── Photo fetch ─────────────────────────────────────────────────────────

    public byte[] getReportPhoto(int reportId) {
        reconnectIfNeeded();
        if (!isConnected()) return null;
        String sql = "SELECT report_photo FROM \"Report\" WHERE report_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reportId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBytes("report_photo");
        } catch (Exception e) {
            System.err.println("Get photo error: " + e.getMessage());
        }
        return null;
    }

    // ─── ReportRecord ────────────────────────────────────────────────────────

    public static class ReportRecord {
        public final int     id;
        public final String  type;
        public final String  description;
        public final String  date;
        public final String  street;
        public final String  streetNumber;
        public final String  area;
        public final String  pcode;
        public final double  latitude;
        public final double  longitude;
        public final String  status;
        public final String  userPhone;
        public final boolean hasPhoto;

        public ReportRecord(int id, String type, String description, String date,
                            String street, String streetNumber, String area, String pcode,
                            double latitude, double longitude, String status, String userPhone,
                            boolean hasPhoto) {
            this.id           = id;
            this.type         = type;
            this.description  = description;
            this.date         = date;
            this.street       = street;
            this.streetNumber = streetNumber;
            this.area         = area;
            this.pcode        = pcode;
            this.latitude     = latitude;
            this.longitude    = longitude;
            this.status       = status;
            this.userPhone    = userPhone;
            this.hasPhoto     = hasPhoto;
        }
    }

    // ─── UserRecord ──────────────────────────────────────────────────────────

    public static class UserRecord {
        public final int     id;
        public final String  firstName;
        public final String  lastName;
        public final String  email;
        public final String  phone;
        public final boolean isAdmin;

        public UserRecord(int id, String firstName, String lastName, String email, String phone, boolean isAdmin) {
            this.id        = id;
            this.firstName = firstName;
            this.lastName  = lastName;
            this.email     = email;
            this.phone     = phone;
            this.isAdmin   = isAdmin;
        }
    }
}
