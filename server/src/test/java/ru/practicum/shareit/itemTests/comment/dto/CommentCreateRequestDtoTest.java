package ru.practicum.shareit.item.comments.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CommentCreateRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validComment_passesValidation() {
        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setText("Nice item!");

        Set<ConstraintViolation<CommentCreateRequestDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("Nice item!", dto.getText());
    }

    @Test
    void blankComment_failsValidation() {
        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setText("   "); //

        Set<ConstraintViolation<CommentCreateRequestDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());

        String msg = violations.iterator().next().getMessage();
        assertTrue(msg.toLowerCase().contains("blank"));
    }

    @Test
    void nullComment_failsValidation() {
        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setText(null);

        Set<ConstraintViolation<CommentCreateRequestDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }
}
