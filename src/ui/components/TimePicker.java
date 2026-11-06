package ui.components;

import ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;

// a twelve hour time picker with visible am and pm
public class TimePicker extends JPanel {

    private final RoundedComboBox<String> hourBox = new RoundedComboBox<String>();
    private final RoundedComboBox<String> minuteBox = new RoundedComboBox<String>();
    private final RoundedComboBox<String> ampmBox = new RoundedComboBox<String>(new String[] { "AM", "PM" });

    public TimePicker(LocalTime initial) {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));

        for (int hour = 1; hour <= 12; hour++) {
            hourBox.addItem(String.valueOf(hour));
        }
        for (int minute = 0; minute < 60; minute++) {
            minuteBox.addItem(String.format("%02d", minute));
        }

        hourBox.setBorder(BorderFactory.createEmptyBorder(5, 9, 5, 4));
        minuteBox.setBorder(BorderFactory.createEmptyBorder(5, 9, 5, 4));
        ampmBox.setBorder(BorderFactory.createEmptyBorder(5, 9, 5, 4));
        hourBox.setPreferredSize(new Dimension(78, 34));
        minuteBox.setPreferredSize(new Dimension(78, 34));
        ampmBox.setPreferredSize(new Dimension(88, 34));

        JLabel colon = new JLabel(":");
        colon.setFont(Theme.BODY);
        colon.setForeground(Theme.TEXT);

        add(hourBox);
        add(colon);
        add(minuteBox);
        add(ampmBox);
        setPreferredSize(new Dimension(270, 40));
        setMinimumSize(new Dimension(270, 40));

        setTime(initial == null ? LocalTime.of(9, 0) : initial);
    }

    public LocalTime getTime() {
        int hour = Integer.parseInt((String) hourBox.getSelectedItem());
        int minute = Integer.parseInt((String) minuteBox.getSelectedItem());
        boolean pm = "PM".equals(ampmBox.getSelectedItem());
        if (hour == 12) {
            hour = 0;
        }
        if (pm) {
            hour = hour + 12;
        }
        return LocalTime.of(hour, minute);
    }

    public void setTime(LocalTime time) {
        int hour = time.getHour();
        String ampm = "AM";
        if (hour >= 12) {
            ampm = "PM";
        }
        int display = hour % 12;
        if (display == 0) {
            display = 12;
        }
        hourBox.setSelectedItem(String.valueOf(display));
        minuteBox.setSelectedItem(String.format("%02d", time.getMinute()));
        ampmBox.setSelectedItem(ampm);
    }
}
