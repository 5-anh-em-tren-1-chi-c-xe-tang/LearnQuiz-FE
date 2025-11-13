package com.example.learnquiz_fe.ui.adapter;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

    // Danh sách các định dạng ngày tháng mà ứng dụng của bạn hỗ trợ
    private final List<SimpleDateFormat> dateFormats = Arrays.asList(
            // Ưu tiên định dạng ISO 8601 chuẩn
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            // Thêm định dạng gây lỗi vào đây
            new SimpleDateFormat("dd-MM-yyyy", Locale.US)
    );

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String dateString = json.getAsString();

        // Thử parse bằng từng định dạng trong danh sách
        for (SimpleDateFormat format : dateFormats) {
            try {
                // Nếu parse thành công, trả về đối tượng Date
                return format.parse(dateString);
            } catch (ParseException e) {
                // Nếu không thành công, bỏ qua và thử định dạng tiếp theo
            }
        }

        // Nếu đã thử hết tất cả các định dạng mà vẫn lỗi, báo lỗi
        throw new JsonParseException("Unparseable date: \"" + dateString + "\". Supported formats: " + dateFormats);
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        // Khi gửi dữ liệu lên server, luôn dùng định dạng chuẩn nhất (ISO 8601)
        synchronized (dateFormats) {
            return new JsonPrimitive(dateFormats.get(0).format(src));
        }
    }
}
