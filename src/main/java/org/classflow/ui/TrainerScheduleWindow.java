
package org.classflow.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TrainerScheduleWindow extends JFrame {

    private static final String API_URL =
            "http://localhost:8082/training-schedules";

    private final JTextField courseNameField =
            new JTextField("Java Full Stack");

    private final JTextField courseIdField =
            new JTextField("1");

    private final JTextField topicNameField =
            new JTextField("Spring Boot Introduction");

    private final JTextField dateField =
            new JTextField(LocalDate.now().toString());

    private final JTextField startTimeField =
            new JTextField("1:00 PM");

    private final JTextField endTimeField =
            new JTextField("2:00 PM");

    private final JTextField meetLinkField =
            new JTextField("https://meet.google.com/azs-wqqx-xra");

    private final JTextArea resultArea = new JTextArea(4, 35);

    private final JButton scheduleButton =
            new JButton("Schedule Class");

    private final JButton refreshButton =
            new JButton("Refresh Records");

    private final JLabel statusLabel = new JLabel("Ready");

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                            "ID",
                            "Course ID",
                            "Topic",
                            "Scheduled Start",
                            "Scheduled End",
                            "Actual Start",
                            "Actual End",
                            "Duration",
                            "Status",
                            "Reminder Sent"
                    }, 0) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable recordsTable = new JTable(tableModel);

    public TrainerScheduleWindow() {

        setTitle("CLASSFLOW - Trainer Dashboard");
        setSize(1250, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel heading = new JLabel("CLASSFLOW");
        heading.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel subHeading =
                new JLabel("Trainer Class Scheduling and Tracking");

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 5, 5));
        titlePanel.add(heading);
        titlePanel.add(subHeading);

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 8));
        formPanel.setBorder(
                BorderFactory.createTitledBorder("Schedule a Class"));

        formPanel.add(new JLabel("Course Name:"));
        formPanel.add(courseNameField);

        formPanel.add(new JLabel("Course ID:"));
        formPanel.add(courseIdField);

        formPanel.add(new JLabel("Topic Name:"));
        formPanel.add(topicNameField);

        formPanel.add(new JLabel("Class Date (yyyy-MM-dd):"));
        formPanel.add(dateField);

        formPanel.add(new JLabel("Start Time (e.g. 1:00 PM):"));
        formPanel.add(startTimeField);

        formPanel.add(new JLabel("End Time (e.g. 2:00 PM):"));
        formPanel.add(endTimeField);

        formPanel.add(new JLabel("Google Meet Link:"));
        formPanel.add(meetLinkField);

        scheduleButton.setFont(new Font("Arial", Font.BOLD, 14));
        refreshButton.setFont(new Font("Arial", Font.BOLD, 14));

        scheduleButton.addActionListener(event -> scheduleClass());
        refreshButton.addActionListener(event -> refreshRecords());

        JPanel buttonPanel =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        buttonPanel.add(scheduleButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(statusLabel);

        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(
                BorderFactory.createTitledBorder("Latest Activity"));

        recordsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        recordsTable.setRowHeight(24);
        recordsTable.setFillsViewportHeight(true);
        recordsTable.setAutoCreateRowSorter(true);

        recordsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        recordsTable.getColumnModel().getColumn(1).setPreferredWidth(75);
        recordsTable.getColumnModel().getColumn(2).setPreferredWidth(190);
        recordsTable.getColumnModel().getColumn(3).setPreferredWidth(145);
        recordsTable.getColumnModel().getColumn(4).setPreferredWidth(145);
        recordsTable.getColumnModel().getColumn(5).setPreferredWidth(175);
        recordsTable.getColumnModel().getColumn(6).setPreferredWidth(175);
        recordsTable.getColumnModel().getColumn(7).setPreferredWidth(110);
        recordsTable.getColumnModel().getColumn(8).setPreferredWidth(120);
        recordsTable.getColumnModel().getColumn(9).setPreferredWidth(110);

        JScrollPane tableScroll = new JScrollPane(recordsTable);
        tableScroll.setBorder(
                BorderFactory.createTitledBorder("Class Records"));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(resultScroll, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);

        refreshRecords();
    }

    private void scheduleClass() {

        scheduleButton.setEnabled(false);
        statusLabel.setText("Saving...");
        resultArea.setText("Saving class details...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {

            @Override
            protected String doInBackground() throws Exception {

                int courseId = Integer.parseInt(
                        courseIdField.getText().trim());

                String courseName = courseNameField.getText().trim();
                String topicName = topicNameField.getText().trim();

                LocalDate sessionDate =
                        LocalDate.parse(dateField.getText().trim());

                LocalTime startTime = parseTime(startTimeField.getText());
                LocalTime endTime = parseTime(endTimeField.getText());

                String meetCode =
                        extractMeetingCode(meetLinkField.getText());

                if (courseId <= 0) {
                    throw new IllegalArgumentException(
                            "Course ID must be greater than zero.");
                }

                if (courseName.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Please enter the course name.");
                }

                if (topicName.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Please enter the topic name.");
                }

                if (!endTime.isAfter(startTime)) {
                    throw new IllegalArgumentException(
                            "End time must be later than start time.");
                }

                if (meetCode.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Please enter a valid Google Meet link.");
                }

                String json = "{"
                        + "\"courseId\":" + courseId + ","
                        + "\"meetingCode\":\"" + escapeJson(meetCode) + "\","
                        + "\"sessionDate\":\"" + sessionDate + "\","
                        + "\"scheduledStartTime\":\"" + startTime + "\","
                        + "\"scheduledEndTime\":\"" + endTime + "\","
                        + "\"topicName\":\"" + escapeJson(topicName) + "\","
                        + "\"topicCompleted\":false,"
                        + "\"topicNotes\":\"Created from CLASSFLOW Swing UI\""
                        + "}";

                HttpURLConnection connection =
                        (HttpURLConnection) URI.create(API_URL)
                                .toURL().openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty(
                        "Content-Type", "application/json");
                connection.setRequestProperty(
                        "Accept", "application/json");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);
                connection.setDoOutput(true);

                try {
                    try (OutputStream output = connection.getOutputStream()) {
                        output.write(
                                json.getBytes(StandardCharsets.UTF_8));
                    }

                    int statusCode = connection.getResponseCode();

                    String responseBody =
                            readResponse(connection, statusCode);

                    if (statusCode >= 200 && statusCode < 300) {
                        return "SUCCESS\n"
                                + "Class scheduled successfully!\n\n"
                                + "Course: " + courseName + "\n"
                                + "Topic: " + topicName + "\n"
                                + "Date: " + sessionDate + "\n"
                                + "Scheduled Start: " + startTime + "\n"
                                + "Scheduled End: " + endTime + "\n"
                                + "Meeting Code: " + meetCode + "\n\n"
                                + "Backend Response:\n" + responseBody;
                    }

                    return "ERROR\nHTTP " + statusCode + "\n\n"
                            + responseBody;

                } finally {
                    connection.disconnect();
                }
            }

            @Override
            protected void done() {

                scheduleButton.setEnabled(true);

                try {
                    String result = get();
                    resultArea.setText(result);

                    if (result.startsWith("SUCCESS")) {
                        statusLabel.setText("Class saved");

                        JOptionPane.showMessageDialog(
                                TrainerScheduleWindow.this,
                                "Class scheduled successfully!",
                                "CLASSFLOW",
                                JOptionPane.INFORMATION_MESSAGE);

                        refreshRecords();

                    } else {
                        statusLabel.setText("Save failed");

                        JOptionPane.showMessageDialog(
                                TrainerScheduleWindow.this,
                                result,
                                "Scheduling Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception exception) {

                    String message = getErrorMessage(exception);

                    resultArea.setText(
                            "Could not schedule the class.\n\n"
                                    + message
                                    + "\n\nCheck whether the backend is "
                                    + "running on port 8082.");

                    statusLabel.setText("Connection error");

                    JOptionPane.showMessageDialog(
                            TrainerScheduleWindow.this,
                            resultArea.getText(),
                            "Connection or Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void refreshRecords() {

        refreshButton.setEnabled(false);
        statusLabel.setText("Loading records...");

        SwingWorker<List<String[]>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<String[]> doInBackground() throws Exception {

                HttpURLConnection connection =
                        (HttpURLConnection) URI.create(API_URL)
                                .toURL().openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);

                try {
                    int statusCode = connection.getResponseCode();

                    String responseBody =
                            readResponse(connection, statusCode);

                    if (statusCode < 200 || statusCode >= 300) {
                        throw new IllegalStateException(
                                "Backend returned HTTP " + statusCode
                                        + "\n" + responseBody);
                    }

                    return parseScheduleRecords(responseBody);

                } finally {
                    connection.disconnect();
                }
            }

            @Override
            protected void done() {

                refreshButton.setEnabled(true);

                try {
                    List<String[]> records = get();

                    tableModel.setRowCount(0);

                    for (String[] record : records) {
                        tableModel.addRow(record);
                    }

                    statusLabel.setText(
                            records.size() + " record(s) loaded");

                    resultArea.setText(
                            "Records refreshed successfully.\n"
                                    + "Total records: " + records.size()
                                    + "\n\n"
                                    + "Actual times and status are loaded "
                                    + "from the backend.");

                } catch (Exception exception) {

                    String message = getErrorMessage(exception);

                    statusLabel.setText("Refresh failed");
                    resultArea.setText(
                            "Could not load class records.\n\n" + message);

                    JOptionPane.showMessageDialog(
                            TrainerScheduleWindow.this,
                            "Could not load class records.\n\n" + message,
                            "Refresh Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private List<String[]> parseScheduleRecords(String json) {

        List<String[]> records = new ArrayList<>();

        Pattern objectPattern = Pattern.compile("\\{([^{}]*)\\}");
        Matcher objectMatcher = objectPattern.matcher(json);

        while (objectMatcher.find()) {

            String object = objectMatcher.group(1);

            String id = getJsonValue(object, "id");
            String courseId = getJsonValue(object, "courseId");
            String topic = getJsonValue(object, "topicName");

            String scheduledStart =
                    getJsonValue(object, "scheduledStartTime");

            String scheduledEnd =
                    getJsonValue(object, "scheduledEndTime");

            String actualStart =
                    getJsonValue(object, "actualStartTime");

            String actualEnd =
                    getJsonValue(object, "actualEndTime");

            String duration =
                    formatDuration(getJsonValue(
                            object, "actualDurationMinutes"));

            String status = getJsonValue(object, "status");

            String reminder =
                    getJsonValue(object, "startReminderSent");

            records.add(new String[]{
                    display(id),
                    display(courseId),
                    display(topic),
                    display(scheduledStart),
                    display(scheduledEnd),
                    display(actualStart),
                    display(actualEnd),
                    duration,
                    display(status),
                    reminder.equalsIgnoreCase("true") ? "Yes"
                            : reminder.equalsIgnoreCase("false") ? "No"
                            : "N/A"
            });
        }

        return records;
    }

    private String formatDuration(String value) {

        if (value == null || value.isBlank()
                || value.equals("N/A")) {
            return "-";
        }

        try {
            int totalMinutes = Integer.parseInt(value);

            if (totalMinutes < 0) {
                return "-";
            }

            if (totalMinutes < 60) {
                return totalMinutes + " min";
            }

            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;

            if (minutes == 0) {
                return hours + " hr";
            }

            return hours + " hr " + minutes + " min";

        } catch (NumberFormatException exception) {
            return value;
        }
    }

    private String getJsonValue(String object, String key) {

        Pattern valuePattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                        + "\"\\s*:\\s*(\"(?:\\\\.|[^\"\\\\])*\"|-?\\d+(?:\\.\\d+)?|true|false|null)",
                Pattern.CASE_INSENSITIVE);

        Matcher matcher = valuePattern.matcher(object);

        if (!matcher.find()) {
            return "N/A";
        }

        String value = matcher.group(1);

        if ("null".equals(value)) {
            return "N/A";
        }

        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t");
        }

        return value;
    }

    private String display(String value) {

        if (value == null || value.isBlank()
                || value.equals("N/A")) {
            return "-";
        }

        return value;
    }

    private String readResponse(
            HttpURLConnection connection, int statusCode) throws Exception {

        InputStream stream = statusCode >= 200 && statusCode < 400
                ? connection.getInputStream()
                : connection.getErrorStream();

        if (stream == null) {
            return "";
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private LocalTime parseTime(String input) {

        String value = input.trim().toUpperCase(Locale.ENGLISH);

        DateTimeFormatter twelveHour =
                DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

        DateTimeFormatter twentyFourHour =
                DateTimeFormatter.ofPattern("HH:mm");

        try {
            return LocalTime.parse(value, twelveHour);
        } catch (java.time.format.DateTimeParseException exception) {
            return LocalTime.parse(value, twentyFourHour);
        }
    }

    private String extractMeetingCode(String input) {

        String value = input.trim();

        if (value.isEmpty()) {
            return "";
        }

        try {
            String path = URI.create(value).getPath();

            if (path != null) {
                String[] parts = path.split("/");

                for (int i = parts.length - 1; i >= 0; i--) {
                    if (!parts[i].isBlank()) {
                        return parts[i];
                    }
                }
            }
        } catch (IllegalArgumentException exception) {
           
        }

        return value;
    }

    private String escapeJson(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String getErrorMessage(Exception exception) {

        Throwable cause = exception;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        return cause.getMessage() != null
                ? cause.getMessage()
                : cause.toString();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            TrainerScheduleWindow window =
                    new TrainerScheduleWindow();

            window.setVisible(true);
        });
    }
}
