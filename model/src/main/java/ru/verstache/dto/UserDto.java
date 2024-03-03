package ru.verstache.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private final String username;
}
