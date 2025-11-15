package ru.practicum.shareit.itemTests.comment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.comments.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class CommentRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    private Item itemOne;
    private Item itemTwo;
    private User itemOwner;
    private Comment commentOne;
    private Comment commentTwo;

    @BeforeEach
    public void init() {
        itemOwner = userRepository.save(User.builder()
                .id(null)
                .name("ItemOwner")
                .email("owner@email.com")
                .build()
        );

        itemOne = itemRepository.save(Item.builder()
                .id(null)
                .name("ItemOne")
                .description("ItemOne Desc")
                .owner(itemOwner)
                .available(true)
                .itemRequest(null)
                .comments(null)
                .bookings(null)
                .build()
        );

        itemTwo = itemRepository.save(Item.builder()
                .id(null)
                .name("ItemTwo")
                .description("ItemTwo Desc")
                .owner(itemOwner)
                .available(true)
                .itemRequest(null)
                .comments(null)
                .bookings(null)
                .build()
        );

        commentOne = commentRepository.save(
                Comment.builder()
                        .item(itemOne)
                        .author(itemOwner)
                        .text("commentOne")
                        .created(LocalDateTime.now())
                        .build()
        );

        commentTwo = commentRepository.save(
                Comment.builder()
                        .item(itemOne)
                        .author(itemOwner)
                        .text("commentTwo")
                        .created(LocalDateTime.now())
                        .build()
        );

    }

    @AfterEach
    public void cleanUp() {
        commentRepository.delete(commentOne);
        commentRepository.delete(commentTwo);
        itemRepository.delete(itemOne);
        userRepository.delete(itemOwner);
    }

    @Test
    public void findAllByItem_IdTest() {
        List<Comment> comments = commentRepository.findAllByItem_Id(itemOne.getId());
        List<String> commentTexts = comments.stream()
                .map(Comment::getText)
                .toList();
        assertEquals(2, comments.size());
        assertTrue(commentTexts.contains("commentOne"));
        assertTrue(commentTexts.contains("commentTwo"));
    }

    @Test
    public void findByItemIdIn() {
        List<Comment> comments = commentRepository.findByItemIdIn(List.of(itemOne.getId(), itemTwo.getId()));
        List<String> commentTexts = comments.stream()
                .map(Comment::getText)
                .toList();
        assertEquals(2, comments.size());
        assertTrue(commentTexts.contains("commentOne"));
        assertTrue(commentTexts.contains("commentTwo"));
    }

}
