package com.biblioteca.utilidades;

import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_PATTERN);

    @Override
    public Object stringToValue(String text) throws ParseException {
        return dateFormatter.parse(text);
    }

    @Override
    public String valueToString(Object value) {
        if (value != null) {
            if (value instanceof java.util.Calendar) {
                java.util.Calendar calendar = (java.util.Calendar) value;
                return dateFormatter.format(calendar.getTime());
            }
            if (value instanceof java.util.Date) {
                return dateFormatter.format(value);
            }
        }
        return "";
    }
}
