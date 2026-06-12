package com.pachasuite.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String message;

    public static MessageDTO of(String message) {
        return new MessageDTO(message);
    }
}