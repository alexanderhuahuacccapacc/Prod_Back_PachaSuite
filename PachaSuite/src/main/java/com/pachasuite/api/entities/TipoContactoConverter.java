package com.pachasuite.api.entities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoContactoConverter implements AttributeConverter<VerificacionContacto.TipoContacto, String> {

    @Override
    public String convertToDatabaseColumn(VerificacionContacto.TipoContacto attribute) {
        if (attribute == null) return null;
        return attribute.name();
    }

    @Override
    public VerificacionContacto.TipoContacto convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return VerificacionContacto.TipoContacto.valueOf(dbData);
    }
}