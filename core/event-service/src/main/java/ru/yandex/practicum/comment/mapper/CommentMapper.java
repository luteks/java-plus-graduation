package ru.yandex.practicum.comment.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.comment.dto.CommentDto;
import ru.yandex.practicum.comment.dto.InputCommentDto;
import ru.yandex.practicum.comment.model.Comment;
import ru.yandex.practicum.dto.UserShortDto;
import ru.yandex.practicum.event.model.Event;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "created", ignore = true)
    Comment toComment(InputCommentDto inputCommentDto, UserShortDto author, Event event);

    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "eventId", source = "comment.event.id")
    @Mapping(target = "author", source = "author")
    CommentDto toCommentDto(Comment comment, UserShortDto author);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", source = "commentDto.author.id")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    Comment toComment(CommentDto commentDto, Event event);

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "author", ignore = true)
    List<CommentDto> toCommentDtos(List<Comment> comments);
}