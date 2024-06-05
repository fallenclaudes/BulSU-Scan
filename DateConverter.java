package com.example.qrcodes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConverter {

    public static String convertDate(String inputDate) {
        try {
            // Parse the input date
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
            Date date = inputFormat.parse(inputDate);

            // Format the date to the desired output format
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return outputFormat.format(date);
        } catch (ParseException e) {
            // Handle the exception or log an error
            e.printStackTrace();
            return null;
        }
    }
}
