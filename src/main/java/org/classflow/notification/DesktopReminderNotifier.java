package org.classflow.notification;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.lang.Process;
import java.lang.StringBuilder;


public final class DesktopReminderNotifier {

    private static TrayIcon trayIcon;

    private DesktopReminderNotifier() {
    }

    public static void show(String title, String message) {
        if (isWindows() && showWindowsBalloon(title, message)) {
            return;
        }

        try {
            if (GraphicsEnvironment.isHeadless() || !SystemTray.isSupported()) {
                printFallback(title, message,
                        "Desktop notifications are unavailable in this environment.");
                return;
            }

            TrayIcon icon = getOrCreateTrayIcon();
            icon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        } catch (Exception exception) {
            printFallback(title, message,
                    "Desktop notification could not be displayed: "
                            + exception.getMessage());
        }
    }

    private static boolean showWindowsBalloon(String title, String message) {
        
        String script =
                "Add-Type -AssemblyName System.Windows.Forms; " +
                "Add-Type -AssemblyName System.Drawing; " +
                "$n = New-Object System.Windows.Forms.NotifyIcon; " +
                "$n.Icon = [System.Drawing.SystemIcons]::Information; " +
                "$n.Visible = $true; " +
                "$n.BalloonTipTitle = '" + escapePowerShell(title) + "'; " +
                "$n.BalloonTipText = '" + escapePowerShell(message) + "'; " +
                "$n.BalloonTipIcon = [System.Windows.Forms.ToolTipIcon]::Info; " +
                "$n.ShowBalloonTip(10000); " +
                "Start-Sleep -Seconds 4; " +
                "$n.Dispose();";

        try {
            Process process = new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-NonInteractive",
                    "-ExecutionPolicy", "Bypass",
                    "-Command", script)
                    .redirectErrorStream(true)
                    .start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("[CLASSFLOW] Windows desktop notification requested.");
                return true;
            }

            System.out.println("[CLASSFLOW] Windows notification command failed: "
                    + output.toString().trim());
            return false;
        } catch (Exception exception) {
            System.out.println("[CLASSFLOW] Could not start Windows notification: "
                    + exception.getMessage());
            return false;
        }
    }

    private static String escapePowerShell(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''")
                .replace("\r", " ")
                .replace("\n", " ");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "")
                .toLowerCase()
                .contains("windows");
    }

    private static synchronized TrayIcon getOrCreateTrayIcon() throws Exception {
        if (trayIcon != null) {
            return trayIcon;
        }

        Image image = createIconImage();
        trayIcon = new TrayIcon(image, "CLASSFLOW Training Reminders");
        trayIcon.setImageAutoSize(true);
        SystemTray.getSystemTray().add(trayIcon);
        return trayIcon;
    }

    private static Image createIconImage() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(35, 115, 190));
        graphics.fillRoundRect(0, 0, 16, 16, 4, 4);
        graphics.setColor(Color.WHITE);
        graphics.drawString("C", 4, 12);
        graphics.dispose();
        return image;
    }

    private static void printFallback(String title, String message, String reason) {
        System.out.println("[CLASSFLOW notification fallback] " + reason);
        System.out.println(title + " - " + message);
    }
}
