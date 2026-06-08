package com.aplikasimahasiswa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/akademik_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC PostgreSQL tidak ditemukan di dalam classpath proyek Anda.", e);
        }
    }

    public static void main(String[] args) {
        System.out.println("Mencoba menghubungkan ke database...");
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Koneksi Berhasil.");
            }
        } catch (SQLException e) {
            System.err.println("Koneksi Gagal!");
            System.err.println("Penyebab: " + e.getMessage());
            e.printStackTrace();
        }
    }
}