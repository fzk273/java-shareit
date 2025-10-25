package ru.practicum.shareit.item.comments.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.comments.dto.CommentMapper;
import ru.practicum.shareit.item.comments.dto.request.CommentCreateRequestDto;
import ru.practicum.shareit.item.comments.dto.response.CommentResponseDto;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.comments.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
public class CommentDbService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    public CommentDbService(CommentRepository commentRepository, UserRepository userRepository, ItemRepository itemRepository, BookingRepository bookingRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public CommentResponseDto createComment(Long userId, Long itemId, CommentCreateRequestDto dto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("There is no item with id: " + itemId));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("There is no user with id: " + userId));

        List<Booking> bookings = bookingRepository.getCompletedBookings(itemId, userId);
        if (bookings.isEmpty()) {
            throw new BadRequestException("User is not renting this item");
        }
        Comment comment = CommentMapper.toEntity(dto, item, author);

        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Transactional
    public List<CommentResponseDto> getCommentsForItem(Long itemId) {
        List<Comment> comments = commentRepository.findAllByItem_Id(itemId);
        return comments
                .stream()
                .map(CommentMapper::toDto)
                .toList();
    }

}
