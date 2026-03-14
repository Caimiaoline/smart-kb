package com.example.smartkb.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 将 List&lt;Long&gt; 与数据库 text 列中的 JSON 数组互转，如 "[1,2,3]"。
 */
@Converter
public class LongListJsonConverter implements AttributeConverter<List<Long>, String> {

    private static final Pattern SPLIT = Pattern.compile("\\s*,\\s*");

    @Override
    public String convertToDatabaseColumn(List<Long> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < attribute.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(attribute.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }
        String s = dbData.trim();
        if (s.equals("[]")) {
            return Collections.emptyList();
        }
        s = s.replace("[", "").replace("]", "").trim();
        if (s.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> list = new ArrayList<>();
        for (String part : SPLIT.split(s)) {
            try {
                list.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return list;
    }
}
