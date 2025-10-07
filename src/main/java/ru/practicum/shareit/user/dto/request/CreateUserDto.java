package ru.practicum.shareit.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserDto {
    private String name;
    @Email
    @NotNull
    private String email;
}
