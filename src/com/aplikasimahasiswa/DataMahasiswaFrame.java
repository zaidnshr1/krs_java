package com.aplikasimahasiswa;

import javax.swing.*;
import java.sql.*;

public class DataMahasiswaFrame extends JFrame {
    private JPanel rootPanel;
    private JTextField nimField, namaField, teleponField;
    private JButton refreshButton;
    private JButton hapusButton;
    private JButton simpanButton;
    private JButton updateButton;
    private JButton exitButton;
    private JList<Mahasiswa> mahasiswaList;
    private JTextArea detailArea;
    private JLabel nimLabel, namaLabel, teleponLabel;

    private DefaultListModel<Mahasiswa> listModel;

    public DataMahasiswaFrame() {
        setContentPane(rootPanel);
        setTitle("Data Mahasiswa - Database Mode");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        listModel = new DefaultListModel<>();
        mahasiswaList.setModel(listModel);

        loadDataDariDatabase();

        simpanButton.addActionListener(e -> tambahDataKeDatabase());
        updateButton.addActionListener(e -> updateDataKeDatabase());
        hapusButton.addActionListener(e -> hapusDataDariDatabase());
        refreshButton.addActionListener(e -> refreshForm());
        exitButton.addActionListener(e -> {
            new MainMenuFrame().setVisible(true);
            dispose();
        });

        mahasiswaList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Mahasiswa selected = mahasiswaList.getSelectedValue();
                if (selected != null) {
                    nimField.setText(selected.getNim());
                    nimField.setEditable(false);
                    namaField.setText(selected.getNamaLengkap());
                    teleponField.setText(selected.getNomorTelepon());

                    detailArea.setText(
                            "ID NIM       : " + selected.getNim() + "\n" +
                                    "Nama Lengkap : " + selected.getNamaLengkap() + "\n" +
                                    "No Telepon   : " + selected.getNomorTelepon()
                    );
                }
            }
        });
    }

    private void loadDataDariDatabase() {
        listModel.clear();
        String query = "SELECT nim, nama_lengkap, nomor_telepon FROM mahasiswa ORDER BY nim ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String nim = rs.getString("nim");
                String nama = rs.getString("nama_lengkap");
                String telepon = rs.getString("nomor_telepon");
                listModel.addElement(new Mahasiswa(nim, nama, telepon));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal memuat data dari database!\nPesan Error: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validasiInput() {
        if (nimField.getText().trim().isEmpty() ||
                namaField.getText().trim().isEmpty() ||
                teleponField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            Long.parseLong(nimField.getText().trim());
            Long.parseLong(teleponField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "NIM dan Nomor Telepon harus berupa angka.", "Validasi Input", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void tambahDataKeDatabase() {
        if (!validasiInput()) return;

        String query = "INSERT INTO mahasiswa (nim, nama_lengkap, nomor_telepon) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, nimField.getText().trim());
            ps.setString(2, namaField.getText().trim());
            ps.setString(3, teleponField.getText().trim());

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Data mahasiswa berhasil ditambahkan ke database.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                refreshForm();
            }

        } catch (SQLException e) {
            // Penanganan error dinamis dari database (Misal: duplikasi Primary Key NIM)
            if ("23505".equals(e.getSQLState())) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan! NIM '" + nimField.getText() + "' sudah terdaftar di database.", "Duplikasi Data", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan data ke database!\nKode SQLState: " + e.getSQLState() + "\nPesan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateDataKeDatabase() {
        if (!validasiInput()) return;

        int selectedIndex = mahasiswaList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data mahasiswa pada list terlebih dahulu untuk diubah.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String query = "UPDATE mahasiswa SET nama_lengkap = ?, nomor_telepon = ? WHERE nim = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, namaField.getText().trim());
            ps.setString(2, teleponField.getText().trim());
            ps.setString(3, nimField.getText().trim());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Data mahasiswa berhasil diperbarui.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                refreshForm();
            } else {
                JOptionPane.showMessageDialog(this, "Data mahasiswa gagal diperbarui. NIM tidak ditemukan.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengubah data!\nPesan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusDataDariDatabase() {
        Mahasiswa selected = mahasiswaList.getSelectedValue();

        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Pilih mahasiswa yang akan dihapus dari daftar terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin menghapus mahasiswa: " + selected.getNamaLengkap() + " dari database?\nSemua pengajuan KRS mahasiswa ini juga akan terhapus.",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String query = "DELETE FROM mahasiswa WHERE nim = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setString(1, selected.getNim());
                int rowsDeleted = ps.executeUpdate();

                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Data " + selected.getNamaLengkap() + " berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    refreshForm();
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data dari database!\nPesan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshForm() {
        nimField.setText("");
        nimField.setEditable(true); // Membuka kembali editabilitas NIM untuk input baru
        namaField.setText("");
        teleponField.setText("");
        detailArea.setText("");
        mahasiswaList.clearSelection();
        loadDataDariDatabase(); // Muat ulang data terbaru dari database
    }
}