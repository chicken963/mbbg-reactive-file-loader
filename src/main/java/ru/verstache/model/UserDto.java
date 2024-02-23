package ru.verstache.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private final String username;
}
