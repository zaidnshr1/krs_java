package com.aplikasimahasiswa;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JPanel rootPanel;
    private JTextField idUserField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JTextField passcodeField;
    private JButton button1, button2, button3, button4, button5, button6, button7, button8, button9, button0;
    private JLabel titleLabel, idUserLabel, passwordLabel, passcodeLabel;
    private JPanel keypadPanel;

    private final String VALID_USER_ID = "admin";
    private final String VALID_PASSWORD = "123";
    private final String VALID_PASSCODE = "456789";

    public LoginFrame() {
        setContentPane(rootPanel);
        setTitle("Login - Aplikasi Daftar Mahasiswa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 600);
        setLocationRelativeTo(null);

        loginButton.addActionListener(e -> handleLogin());

        ActionListener keypadListener = e -> {
            JButton source = (JButton) e.getSource();
            passcodeField.setText(passcodeField.getText() + source.getText());
            if (passcodeField.getText().length() == 6) {
                handlePasscodeLogin();
            }
        };

        button1.addActionListener(keypadListener);
        button2.addActionListener(keypadListener);
        button3.addActionListener(keypadListener);
        button4.addActionListener(keypadListener);
        button5.addActionListener(keypadListener);
        button6.addActionListener(keypadListener);
        button7.addActionListener(keypadListener);
        button8.addActionListener(keypadListener);
        button9.addActionListener(keypadListener);
        button0.addActionListener(keypadListener);
    }

    private void handleLogin() {
        String userId = idUserField.getText();
        String password = new String(passwordField.getPassword());

        if (userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "User dan Password tidak boleh kosong.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userId.equals(VALID_USER_ID) && password.equals(VALID_PASSWORD)) {
            openMainMenu();
        } else {
            JOptionPane.showMessageDialog(this, "User atau Password salah.", "Login Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePasscodeLogin() {
        String passcode = passcodeField.getText();
        if (passcode.equals(VALID_PASSCODE)) {
            openMainMenu();
        } else {
            JOptionPane.showMessageDialog(this, "Passcode salah.", "Login Gagal", JOptionPane.ERROR_MESSAGE);
            passcodeField.setText("");
        }
    }

    private void openMainMenu() {
        MainMenuFrame mainMenu = new MainMenuFrame();
        mainMenu.setVisible(true);
        dispose();
    }
}