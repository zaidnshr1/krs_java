package com.aplikasimahasiswa;

import javax.swing.*;

public class MainMenuFrame extends JFrame {
    private JPanel rootPanel;
    private JButton dataMahasiswaButton;
    private JButton zodiakButton;
    private JButton krsButton;
    private JButton exitButton;
    private JLabel titleLabel;

    public MainMenuFrame() {
        setContentPane(rootPanel);
        setTitle("Menu Utama - Aplikasi Daftar Mahasiswa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);

        dataMahasiswaButton.addActionListener(e -> {
            DataMahasiswaFrame dataFrame = new DataMahasiswaFrame();
            dataFrame.setVisible(true);
            dispose();
        });

        zodiakButton.addActionListener(e -> {
            ZodiakFrame zodiakFrame = new ZodiakFrame();
            zodiakFrame.setVisible(true);
            dispose();
        });

        krsButton.addActionListener(e -> {
            KrsFrame krsFrame = new KrsFrame();
            krsFrame.setVisible(true);
            dispose();
        });

        exitButton.addActionListener(e -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose();
        });
    }
}