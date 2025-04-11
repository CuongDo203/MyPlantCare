package com.example.myplantcare.utils;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp; // Sử dụng Timestamp của Firebase

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
public final class DateUtils {

    private DateUtils() {}

    // Định dạng ngày tháng phổ biến
    public static final String DATE_FORMAT_DISPLAY = "dd/MM/yyyy"; // Ví dụ: 11/04/2025
    public static final String DATETIME_FORMAT_DISPLAY = "dd/MM/yyyy HH:mm"; // Ví dụ: 11/04/2025 14:30
    public static final String TIME_FORMAT_DISPLAY = "HH:mm"; // Ví dụ: 14:30
    public static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"; // Dùng cho múi giờ UTC

    /**
     * Định dạng một đối tượng Date thành chuỗi để hiển thị.
     * @param date Đối tượng Date.
     * @param formatPattern Mẫu định dạng (ví dụ: DATE_FORMAT_DISPLAY).
     * @return Chuỗi đã định dạng hoặc chuỗi rỗng nếu date là null.
     */
    public static String formatDate(@Nullable Date date, String formatPattern) {
        if (date == null) {
            return "";
        }
        // Sử dụng Locale của Việt Nam và múi giờ địa phương
        SimpleDateFormat sdf = new SimpleDateFormat(formatPattern, new Locale("vi", "VN"));
        // Bạn có thể muốn set TimeZone cụ thể nếu cần
        // sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return sdf.format(date);
    }

    /**
     * Định dạng một đối tượng Timestamp của Firebase thành chuỗi để hiển thị.
     * @param timestamp Đối tượng Timestamp.
     * @param formatPattern Mẫu định dạng.
     * @return Chuỗi đã định dạng hoặc chuỗi rỗng nếu timestamp là null.
     */
    public static String formatTimestamp(@Nullable Timestamp timestamp, String formatPattern) {
        if (timestamp == null) {
            return "";
        }
        return formatDate(timestamp.toDate(), formatPattern);
    }

    /**
     * Chuyển đổi Timestamp thành chuỗi ngày tháng hiển thị mặc định (dd/MM/yyyy).
     */
    public static String formatTimestampToDateString(@Nullable Timestamp timestamp) {
        return formatTimestamp(timestamp, DATE_FORMAT_DISPLAY);
    }

    /**
     * Chuyển đổi Timestamp thành chuỗi ngày giờ hiển thị mặc định (dd/MM/yyyy HH:mm).
     */
    public static String formatTimestampToDateTimeString(@Nullable Timestamp timestamp) {
        return formatTimestamp(timestamp, DATETIME_FORMAT_DISPLAY);
    }

    /**
     * Chuyển đổi Timestamp thành chuỗi giờ hiển thị mặc định (HH:mm).
     */
    public static String formatTimestampToTimeString(@Nullable Timestamp timestamp) {
        return formatTimestamp(timestamp, TIME_FORMAT_DISPLAY);
    }


    /**
     * Lấy Timestamp hiện tại của Firebase.
     * @return Timestamp hiện tại.
     */
    public static Timestamp getCurrentTimestamp() {
        return Timestamp.now();
    }

}
