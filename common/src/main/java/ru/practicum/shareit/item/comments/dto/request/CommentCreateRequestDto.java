package ru.practicum.shareit.item.comments.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateRequestDto {
    @NotBlank
    private String text;

    public boolean hasText() {
        return text != null && !text.isBlank();
    }
}
