package com.csols.FirstFlight;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LoginSystemWithCaptcha {
    private static int failedAttempts = 0;
    private static Timer lockoutTimer;
    static MusicPlayer player = new MusicPlayer();
    static String currentCaptcha;

    public static void main(String[] args) {
        if (UserStorage.loadUsers().isEmpty()) {
            UserManager.registerUser("user123", "password123");
        }
        
        JFrame frame = new JFrame("Login System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // Background setup
        ImageIcon backgroundIcon = new ImageIcon("src/background.jpeg");
        Image backgroundImage = backgroundIcon.getImage();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setLayout(null);

        // Play background music
        player.playMusic("/fazbear.wav");

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(250, 100, 100, 25);
        usernameLabel.setForeground(Color.WHITE);
        JTextField usernameField = new JTextField();
        usernameField.setBounds(350, 100, 200, 25);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(250, 140, 100, 25);
        passwordLabel.setForeground(Color.WHITE);
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(350, 140, 200, 25);

        // CAPTCHA
        JLabel captchaLabel = new JLabel("CAPTCHA:");
        captchaLabel.setBounds(250, 180, 150, 25);
        captchaLabel.setForeground(Color.WHITE);
        JTextField captchaField = new JTextField();
        captchaField.setBounds(400, 180, 120, 25);

        // Log In button
        JButton loginButton = new JButton("Log In");
        loginButton.setBounds(330, 230, 120, 30);
        
        JButton refreshCaptcha = new JButton("↻");
        refreshCaptcha.setBounds(550, 180, 30, 25);
        refreshCaptcha.addActionListener(e -> {
            currentCaptcha = ReCaptcha.generateCaptcha();
            captchaLabel.setText("CAPTCHA: " + currentCaptcha);
            captchaField.setText("");
        });
        panel.add(refreshCaptcha);

        // Register link (styled as a hyperlink)
        JLabel registerLink = new JLabel("<html><u>Don't have an account? Register here</u></html>");
        registerLink.setBounds(300, 280, 300, 30);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.setForeground(Color.CYAN);

        registerLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showRegistrationDialog(frame);
            }
        });

        // Add components to panel
        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(captchaLabel);
        panel.add(captchaField);
        panel.add(loginButton);
        panel.add(registerLink);

        frame.add(panel);
        frame.setVisible(true);
        currentCaptcha = ReCaptcha.generateCaptcha();
        captchaLabel.setText("CAPTCHA: " + currentCaptcha);
        
        // Login button action
        loginButton.addActionListener(e -> {
            // If currently locked out
            if (!loginButton.isEnabled()) {
                JOptionPane.showMessageDialog(frame, 
                    "System temporarily locked. Please wait.",
                    "Locked", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String userInput = captchaField.getText().trim();

            if (!userInput.equals(currentCaptcha)) {
                failedAttempts++;
                
                if (failedAttempts >= 3) {
                    // Lock the system for 30 seconds
                    loginButton.setEnabled(false);
                    lockoutTimer = new Timer(30000, ev -> {
                        loginButton.setEnabled(true);
                        failedAttempts = 0;
                        currentCaptcha = ReCaptcha.generateCaptcha();
                        captchaLabel.setText("CAPTCHA: " + currentCaptcha);
                        captchaField.setText("");
                        ((Timer)ev.getSource()).stop();
                    });
                    lockoutTimer.setRepeats(false);
                    lockoutTimer.start();
                    
                    JOptionPane.showMessageDialog(frame, 
                        "❌ Too many failed attempts. System locked for 30 seconds.",
                        "Security Lock", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, 
                        "❌ CAPTCHA incorrect. Attempts left: " + (3 - failedAttempts),
                        "CAPTCHA Failed", JOptionPane.ERROR_MESSAGE);
                    currentCaptcha = ReCaptcha.generateCaptcha();
                    captchaLabel.setText("CAPTCHA: " + currentCaptcha);
                    captchaField.setText("");
                }
                return;
            }

            // Reset attempts on successful CAPTCHA
            failedAttempts = 0;
            
            if (UserManager.validateUser(username, password)) {
                JOptionPane.showMessageDialog(frame, "🎉 Welcome, " + username + "!",
                        "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                player.stopMusic();
                frame.dispose();
                SwingUtilities.invokeLater(() -> {
                    Dashboard dashboard = new Dashboard(username);
                    dashboard.setVisible(true);  // This is the key fix
                });
            } else {
                JOptionPane.showMessageDialog(frame, "❌ Incorrect username or password.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void showRegistrationDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Register New Account", true);
        dialog.setSize(350, 250);
        dialog.setLayout(new GridBagLayout());
        dialog.setLocationRelativeTo(parent);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Create New Account");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(title, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField userField = new JTextField(15);
        dialog.add(userField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passField = new JPasswordField(15);
        dialog.add(passField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        dialog.add(new JLabel("Confirm:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmField = new JPasswordField(15);
        dialog.add(confirmField, gbc);

        // Register Button
        JButton registerBtn = new JButton("Register");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        dialog.add(registerBtn, gbc);

        registerBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String confirm = new String(confirmField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                showError(dialog, "All fields are required");
                return;
            }

            if (password.length() < 6) {
                showError(dialog, "Password must be at least 6 characters");
                return;
            }

            if (!password.equals(confirm)) {
                showError(dialog, "Passwords do not match");
                return;
            }

            if (UserManager.userExists(username)) {
                showError(dialog, "Username already exists");
                return;
            }

            UserManager.registerUser(username, password);
            JOptionPane.showMessageDialog(dialog,
                    "Account created successfully!\nYou can now login.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    private static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}