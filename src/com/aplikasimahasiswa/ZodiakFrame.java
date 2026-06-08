package com.aplikasimahasiswa;

import javax.swing.*;

public class ZodiakFrame extends JFrame {
    private JPanel rootPanel;
    private JTextField namaField, tanggalField, bulanField;
    private JButton ramalButton, hapusButton, exitButton;
    private JTextArea hasilArea;
    private JLabel namaLabel, tanggalLabel, bulanLabel;

    public ZodiakFrame() {
        setContentPane(rootPanel);
        setTitle("Program Penentuan Zodiak");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 400);
        setLocationRelativeTo(null);

        ramalButton.addActionListener(e -> ramalZodiak());
        hapusButton.addActionListener(e -> clearFields());
        exitButton.addActionListener(e -> {
            new MainMenuFrame().setVisible(true);
            dispose();
        });
    }

    private void ramalZodiak() {
        if (namaField.getText().trim().isEmpty() || tanggalField.getText().trim().isEmpty() || bulanField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String nama = namaField.getText();
            int tanggal = Integer.parseInt(tanggalField.getText());
            int bulan = Integer.parseInt(bulanField.getText());
            String zodiak = getZodiak(tanggal, bulan);

            if (zodiak.equals("Tidak Valid")) {
                JOptionPane.showMessageDialog(this, "Tanggal atau Bulan tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            hasilArea.setText(
                    "Nama: " + nama + "\n" +
                            "Tanggal Lahir Anda: " + tanggal + " " + getNamaBulan(bulan) + "\n" +
                            "Zodiak Anda adalah: " + zodiak
            );

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tanggal dan Bulan harus berupa angka.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        namaField.setText("");
        tanggalField.setText("");
        bulanField.setText("");
        hasilArea.setText("");
    }

    private String getNamaBulan(int bulan) {
        String[] namaBulan = {"", "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        return (bulan >= 1 && bulan <= 12) ? namaBulan[bulan] : "";
    }

    private String getZodiak(int tanggal, int bulan) {
        if ((bulan == 3 && tanggal >= 21) || (bulan == 4 && tanggal <= 19)) return "Aries";
        if ((bulan == 4 && tanggal >= 20) || (bulan == 5 && tanggal <= 20)) return "Taurus";
        if ((bulan == 5 && tanggal >= 21) || (bulan == 6 && tanggal <= 20)) return "Gemini";
        if ((bulan == 6 && tanggal >= 21) || (bulan == 7 && tanggal <= 22)) return "Cancer";
        if ((bulan == 7 && tanggal >= 23) || (bulan == 8 && tanggal <= 22)) return "Leo";
        if ((bulan == 8 && tanggal >= 23) || (bulan == 9 && tanggal <= 22)) return "Virgo";
        if ((bulan == 9 && tanggal >= 23) || (bulan == 10 && tanggal <= 22)) return "Libra";
        if ((bulan == 10 && tanggal >= 23) || (bulan == 11 && tanggal <= 21)) return "Scorpio";
        if ((bulan == 11 && tanggal >= 22) || (bulan == 12 && tanggal <= 21)) return "Sagitarius";
        if ((bulan == 12 && tanggal >= 22) || (bulan == 1 && tanggal <= 19)) return "Capricorn";
        if ((bulan == 1 && tanggal >= 20) || (bulan == 2 && tanggal <= 18)) return "Aquarius";
        if ((bulan == 2 && tanggal >= 19) || (bulan == 3 && tanggal <= 20)) return "Pisces";
        return "Tidak Valid";
    }
}