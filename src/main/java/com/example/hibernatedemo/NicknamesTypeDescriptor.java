package com.example.hibernatedemo;

import java.util.Arrays;
import java.util.List;

import javax.persistence.AttributeConverter;

public class NicknamesTypeDescriptor implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) {
            return null;
        }
        return String.join(",", attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Arrays.asList(dbData.split(","));
    }
}
