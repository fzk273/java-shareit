package ru.practicum.shareit.userTests.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.request.UpdateUserDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UpdateUserDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validEmail_passesValidation() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setName("John");
        dto.setEmail("john@example.com");

        Set<ConstraintViolation<UpdateUserDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("John", dto.getName());  // покрываем getter/setter
        assertEquals("john@example.com", dto.getEmail());
    }

    @Test
    void nullEmail_passesValidationBecauseEmailIsOptional() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setName("John");
        dto.setEmail(null);

        Set<ConstraintViolation<UpdateUserDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidEmail_failsValidation() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setName("John");
        dto.setEmail("not-an-email");

        Set<ConstraintViolation<UpdateUserDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().toLowerCase().contains("email"));
    }

    @Test
    void emptyName_isAllowed() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setName("");
        dto.setEmail("valid@mail.com");

        Set<ConstraintViolation<UpdateUserDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
}
