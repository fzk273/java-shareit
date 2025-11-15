package ru.practicum.shareit.user.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {
    private String name;
    @Email
    private String email;

    public boolean hasEmail() {
        return email != null && !email.isBlank();
    }
}
