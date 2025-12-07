package com.busapi.modules.voyage.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class DayOfWeekSetConverter implements AttributeConverter<Set<DayOfWeek>, String> {

    private static final String SPLIT_CHAR = ",";

    @Override
    public String convertToDatabaseColumn(Set<DayOfWeek> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        // Veritabanında okunaklı ve tutarlı olsun diye sıralı kaydediyoruz
        return attribute.stream()
                .sorted()
                .map(DayOfWeek::name)
                .collect(Collectors.joining(SPLIT_CHAR));
    }

    @Override
    public Set<DayOfWeek> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(dbData.split(SPLIT_CHAR))
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());
    }
}