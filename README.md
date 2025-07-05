//If you just want the file just download it in the releases to the right, the runnablefile folder is a mistake.
// FirstFlight Login System

A secure Java Swing application with CAPTCHA verification and user authentication.

// Features

- Secure login with CAPTCHA verification
- User registration system
- Account lockout after 3 failed attempts
- Custom UI with glow effects
- Background music support
- Persistent user data storage
- Interactive dashboard after login

// Technologies Used

- Core: Java 17+
- GUI: Java Swing
- Build: Maven
- Audio: Java Sound API
- Persistence: Custom serialization

// Installation

Prerequisites:
- JDK 17 or later
- Maven 3.6+

Steps:
1. Clone the repository:
   git clone https://github.com/yourusername/FirstFlight.git
2. Navigate to project directory:
   cd FirstFlight
3. Build with Maven:
   mvn clean install
4. Run the application:
   mvn exec:java

// Configuration

Edit src/main/resources/config.properties to customize:
- Default credentials
- Lockout duration
- Music file path
- UI colors

// Code Structure

src/
  main/
    java/com/csols/FirstFlight/
      LoginSystemWithCaptcha.java     // Main application
      GlowLabel.java                 // Custom glowing label
      UserManager.java               // Authentication logic
      UserStorage.java               // Data persistence
      ReCaptcha.java                 // CAPTCHA generator
      MusicPlayer.java               // Audio handler
      Dashboard.java                 // Post-login interface
    resources/
      background.jpeg               // Login background
      fazbear.wav                   // Background music
      config.properties             // Configuration

// Customization

To change glow effects, modify GlowLabel.java:
setGlowColor(new Color(255, 100, 100, 100)); // Red glow
setGlowWidth(8); // More intense glow

Potential feature additions:
1. Password strength meter
2. "Remember Me" functionality
3. Database integration

// Troubleshooting

Issue: NullPointerException on startup
Solution: Verify all resource files exist in correct paths

Issue: CAPTCHA not displaying
Solution: Check ReCaptcha class string generation

// License

This project is licensed under the MIT License - see the LICENSE file for details.
