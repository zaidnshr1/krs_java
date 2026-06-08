package com.aplikasimahasiswa;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;

public class KrsFrame extends JFrame {
    private JPanel rootPanel;
    private JComboBox<Mahasiswa> mahasiswaComboBox;
    private JTextField dosenPaField;
    private JComboBox<Integer> semesterComboBox;
    private JTable availableTable;
    private JTable selectedTable;
    private JButton ambilButton;
    private JButton ajukanButton;
    private JButton batalButton;
    private JButton refreshButton;
    private JButton exitButton;
    private JButton exportTxtButton;
    private JButton exportPdfButton;

    private DefaultTableModel availableModel;
    private DefaultTableModel selectedModel;
    private boolean isAlreadySubmitted = false;

    public KrsFrame() {
        setContentPane(rootPanel);
        setTitle("Pengajuan Kartu Rencana Studi (KRS)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);

        dosenPaField.setText("Yohanes Eka Wibawa, S.T., M.Eng.");
        dosenPaField.setEditable(false);

        setupTableModels();
        loadMahasiswaToComboBox();
        populateSemesterComboBox();

        mahasiswaComboBox.addActionListener(e -> periksaStatusKrsMahasiswa());

        semesterComboBox.addActionListener(e -> loadAvailableCourses());
        ambilButton.addActionListener(e -> ambilMataKuliah());
        ajukanButton.addActionListener(e -> ajukanKrsKeDatabase());
        batalButton.addActionListener(e -> batalkanKrsDariDatabase());
        refreshButton.addActionListener(e -> resetFormKrs());
        exportTxtButton.addActionListener(e -> eksporKrsKeTxt());
        exportPdfButton.addActionListener(e -> cetakKrsKePdf());

        exitButton.addActionListener(e -> {
            new MainMenuFrame().setVisible(true);
            dispose();
        });

        periksaStatusKrsMahasiswa();
    }

    private void setupTableModels() {
        String[] columnNames = {"Kode MK", "Nama Mata Kuliah", "SKS"};

        availableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        availableTable.setModel(availableModel);

        selectedModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        selectedTable.setModel(selectedModel);
    }

    private void loadMahasiswaToComboBox() {
        mahasiswaComboBox.removeAllItems();
        String query = "SELECT nim, nama_lengkap, nomor_telepon FROM mahasiswa ORDER BY nama_lengkap ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                mahasiswaComboBox.addItem(new Mahasiswa(rs.getString("nim"), rs.getString("nama_lengkap"), rs.getString("nomor_telepon")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat mahasiswa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateSemesterComboBox() {
        semesterComboBox.removeAllItems();
        for (int i = 1; i <= 7; i++) semesterComboBox.addItem(i);
    }

    private void loadAvailableCourses() {
        availableModel.setRowCount(0);
        Integer selectedSemester = (Integer) semesterComboBox.getSelectedItem();
        if (selectedSemester == null) return;

        String query = "SELECT kode_mk, nama_mk, sks FROM mata_kuliah WHERE semester = ? ORDER BY kode_mk ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, selectedSemester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    availableModel.addRow(new Object[]{rs.getString("kode_mk"), rs.getString("nama_mk"), rs.getInt("sks")});
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat mata kuliah: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void periksaStatusKrsMahasiswa() {
        Mahasiswa mhs = (Mahasiswa) mahasiswaComboBox.getSelectedItem();
        if (mhs == null) return;

        selectedModel.setRowCount(0); // Kosongkan tabel pilihan terlebih dahulu

        String query = "SELECT kp.kode_mk, mk.nama_mk, mk.sks " +
                "FROM krs_pengajuan kp " +
                "JOIN mata_kuliah mk ON kp.kode_mk = mk.kode_mk " +
                "WHERE kp.nim = ? " +
                "ORDER BY kp.id_pengajuan ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, mhs.getNim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.isBeforeFirst()) {
                    isAlreadySubmitted = true;
                    while (rs.next()) {
                        selectedModel.addRow(new Object[]{
                                rs.getString("kode_mk"),
                                rs.getString("nama_mk"),
                                rs.getInt("sks")
                        });
                    }
                    ambilButton.setEnabled(false);
                    ajukanButton.setEnabled(false);
                    batalButton.setEnabled(true);
                    setTitle("KRS Mahasiswa (Status: SUDAH DIAJUKAN)");
                } else {
                    isAlreadySubmitted = false;
                    ambilButton.setEnabled(true);
                    ajukanButton.setEnabled(true);
                    batalButton.setEnabled(false);
                    setTitle("KRS Mahasiswa (Status: BELUM MENGISI)");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memvalidasi status KRS: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ambilMataKuliah() {
        if (isAlreadySubmitted) {
            JOptionPane.showMessageDialog(this, "KRS mahasiswa ini sudah diajukan dan terkunci.\nBatalkan terlebih dahulu untuk mengubah.", "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = availableTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih mata kuliah terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String kode = (String) availableModel.getValueAt(selectedRow, 0);
        String nama = (String) availableModel.getValueAt(selectedRow, 1);
        int sks = (int) availableModel.getValueAt(selectedRow, 2);

        for (int i = 0; i < selectedModel.getRowCount(); i++) {
            if (kode.equals(selectedModel.getValueAt(i, 0))) {
                JOptionPane.showMessageDialog(this, "Mata kuliah sudah diambil.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        selectedModel.addRow(new Object[]{kode, nama, sks});
    }

    private void ajukanKrsKeDatabase() {
        Mahasiswa mhsTerpilih = (Mahasiswa) mahasiswaComboBox.getSelectedItem();
        if (mhsTerpilih == null) return;

        if (isAlreadySubmitted) {
            JOptionPane.showMessageDialog(this, "Mahasiswa ini sudah mengajukan KRS sebelumnya.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int jumlahMk = selectedModel.getRowCount();
        if (jumlahMk == 0) {
            JOptionPane.showMessageDialog(this, "Anda belum mengambil mata kuliah.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Simpan pengajuan KRS ke database?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String queryInsert = "INSERT INTO krs_pengajuan (nim, kode_mk, dosen_pa) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(queryInsert)) {
                for (int i = 0; i < jumlahMk; i++) {
                    ps.setString(1, mhsTerpilih.getNim());
                    ps.setString(2, (String) selectedModel.getValueAt(i, 0));
                    ps.setString(3, dosenPaField.getText());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                JOptionPane.showMessageDialog(this, "KRS berhasil diajukan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                periksaStatusKrsMahasiswa(); // Update status form
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan pengajuan KRS: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void batalkanKrsDariDatabase() {
        Mahasiswa mhs = (Mahasiswa) mahasiswaComboBox.getSelectedItem();
        if (mhs == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin membatalkan/menghapus seluruh pengajuan KRS mahasiswa:\n" +
                        mhs.getNamaLengkap() + " (" + mhs.getNim() + ")?",
                "Konfirmasi Pembatalan KRS",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String query = "DELETE FROM krs_pengajuan WHERE nim = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setString(1, mhs.getNim());
                int rowsDeleted = ps.executeUpdate();

                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Seluruh pengajuan KRS berhasil dibatalkan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Tidak ada pengajuan KRS yang perlu dibatalkan.", "Peringatan", JOptionPane.INFORMATION_MESSAGE);
                }
                periksaStatusKrsMahasiswa();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal membatalkan KRS: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetFormKrs() {
        if (mahasiswaComboBox.getItemCount() > 0) mahasiswaComboBox.setSelectedIndex(0);
        if (semesterComboBox.getItemCount() > 0) semesterComboBox.setSelectedIndex(0);
        selectedModel.setRowCount(0);
        periksaStatusKrsMahasiswa();
        loadAvailableCourses();
    }

    private void eksporKrsKeTxt() {
        Mahasiswa mhs = (Mahasiswa) mahasiswaComboBox.getSelectedItem();
        if (mhs == null) return;

        String fileName = "KRS_" + mhs.getNim() + ".txt";
        String query = "SELECT kp.kode_mk, mk.nama_mk, mk.sks, mk.semester " +
                "FROM krs_pengajuan kp " +
                "JOIN mata_kuliah mk ON kp.kode_mk = mk.kode_mk " +
                "WHERE kp.nim = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, mhs.getNim());
            try (ResultSet rs = ps.executeQuery()) {
                File file = new File(fileName);
                try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
                    out.println("=================================================");
                    out.println("          KARTU RENCANA STUDI (KRS)              ");
                    out.println("=================================================");
                    out.println("NIM              : " + mhs.getNim());
                    out.println("Nama Mahasiswa   : " + mhs.getNamaLengkap());
                    out.println("Dosen PA         : " + dosenPaField.getText());
                    out.println("=================================================");
                    out.printf("%-10s | %-25s | %-5s | %-8s\n", "Kode", "Nama Mata Kuliah", "SKS", "Semester");
                    out.println("-------------------------------------------------");

                    int totalSks = 0;
                    while (rs.next()) {
                        totalSks += rs.getInt("sks");
                        out.printf("%-10s | %-25s | %-5d | %-8d\n",
                                rs.getString("kode_mk"),
                                rs.getString("nama_mk"),
                                rs.getInt("sks"),
                                rs.getInt("semester"));
                    }

                    out.println("-------------------------------------------------");
                    out.println("Total SKS diambil: " + totalSks);
                    out.println("=================================================");
                    out.println("\nMengetahui,");
                    out.println("Dosen Wali / Pembimbing Akademik\n\n\n");
                    out.println("( " + dosenPaField.getText() + " )");

                    JOptionPane.showMessageDialog(this, "KRS berhasil diekspor ke TXT.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cetakKrsKePdf() {
        Mahasiswa mhs = (Mahasiswa) mahasiswaComboBox.getSelectedItem();
        if (mhs == null) return;

        String fileName = "KRS_" + mhs.getNim() + ".pdf";
        String query = "SELECT kp.kode_mk, mk.nama_mk, mk.sks, mk.semester " +
                "FROM krs_pengajuan kp " +
                "JOIN mata_kuliah mk ON kp.kode_mk = mk.kode_mk " +
                "WHERE kp.nim = ?";

        Document document = new Document(PageSize.A4, 45, 45, 45, 45);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, mhs.getNim());
            try (ResultSet rs = ps.executeQuery()) {
                PdfWriter.getInstance(document, new FileOutputStream(fileName));
                document.open();

                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD, Color.BLACK);
                Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);

                PdfPTable kopTable = new PdfPTable(2);
                kopTable.setWidthPercentage(100);
                kopTable.setWidths(new float[]{15f, 85f});

                String logoPath = "logo.png";
                File logoFile = new File(logoPath);
                PdfPCell logoCell;

                if (logoFile.exists()) {
                    Image logo = Image.getInstance(logoPath);
                    logo.scaleToFit(60, 60);
                    logoCell = new PdfPCell(logo);
                    logoCell.setBorder(Rectangle.NO_BORDER);
                } else {
                    logoCell = new PdfPCell(new Paragraph("[ LOGO ]", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.BOLD, Color.GRAY)));
                    logoCell.setBackgroundColor(new Color(235, 235, 235));
                    logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    logoCell.setPadding(10);
                    logoCell.setBorderWidth(1);
                    logoCell.setBorderColor(Color.LIGHT_GRAY);
                }

                logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                kopTable.addCell(logoCell);

                PdfPCell textCell = new PdfPCell();
                textCell.setBorder(Rectangle.NO_BORDER);
                textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                textCell.setPaddingLeft(10);
                textCell.addElement(new Paragraph("TANRI ABENG UNIVERSITY", titleFont));
                textCell.addElement(new Paragraph("Fakultas Teknik dan Teknologi  - Program Studi Teknik Informatika\nJl. Swadarma Raya No.58, Ulujami, Kec. Pesanggrahan, Kota Jakarta Selatan, Daerah Khusus Ibukota Jakarta 12250, Telp +6221 2932 4031 | Email: admission@tau.ac.id", subTitleFont));
                kopTable.addCell(textCell);

                document.add(kopTable);

                Paragraph lineSeparator = new Paragraph("___________________________________________________________________________\n\n",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, Color.GRAY));
                lineSeparator.setAlignment(Element.ALIGN_CENTER);
                document.add(lineSeparator);

                Paragraph docTitle = new Paragraph("KARTU RENCANA STUDI (KRS)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD, Color.BLACK));
                docTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(docTitle);
                document.add(new Paragraph(" "));

                Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.BLACK);
                Paragraph info = new Paragraph();
                info.setFont(infoFont);
                info.add("NIM                     : " + mhs.getNim() + "\n");
                info.add("Nama Lengkap   : " + mhs.getNamaLengkap() + "\n");
                info.add("Dosen PA            : " + dosenPaField.getText() + "\n");
                document.add(info);
                document.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{15f, 50f, 15f, 20f});

                Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, Color.WHITE);
                String[] headers = {"Kode MK", "Nama Mata Kuliah", "SKS", "Semester"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Paragraph(header, tableHeaderFont));
                    cell.setBackgroundColor(new Color(0, 51, 102)); // Warna Biru Gelap
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(6);
                    table.addCell(cell);
                }

                Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.BLACK);
                int totalSks = 0;
                while (rs.next()) {
                    totalSks += rs.getInt("sks");
                    table.addCell(new PdfPCell(new Paragraph(rs.getString("kode_mk"), dataFont)));
                    table.addCell(new PdfPCell(new Paragraph(rs.getString("nama_mk"), dataFont)));
                    table.addCell(new PdfPCell(new Paragraph(String.valueOf(rs.getInt("sks")), dataFont)));
                    table.addCell(new PdfPCell(new Paragraph(String.valueOf(rs.getInt("semester")), dataFont)));
                }
                document.add(table);

                Paragraph totalPara = new Paragraph("\nTotal SKS Diambil: " + totalSks + " SKS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD));
                totalPara.setAlignment(Element.ALIGN_RIGHT);
                document.add(totalPara);
                document.add(new Paragraph(" "));

                PdfPTable sigTable = new PdfPTable(2);
                sigTable.setWidthPercentage(100);
                sigTable.setWidths(new float[]{50f, 50f});

                PdfPCell leftCell = new PdfPCell(new Paragraph("Mahasiswa,\n\n\n\n( " + mhs.getNamaLengkap() + " )", dataFont));
                leftCell.setBorder(Rectangle.NO_BORDER);
                leftCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                PdfPCell rightCell = new PdfPCell(new Paragraph("Mengetahui,\nDosen Pembimbing Akademik\n\n\n\n( " + dosenPaField.getText() + " )", dataFont));
                rightCell.setBorder(Rectangle.NO_BORDER);
                rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                sigTable.addCell(leftCell);
                sigTable.addCell(rightCell);
                document.add(sigTable);

                document.close();
                JOptionPane.showMessageDialog(this, "Cetak PDF KRS Berhasil! File tersimpan: " + fileName, "Sukses", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuat PDF: " + ex.getMessage(), "Error PDF", JOptionPane.ERROR_MESSAGE);
        }
    }
}