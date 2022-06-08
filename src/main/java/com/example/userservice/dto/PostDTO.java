package com.example.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Тело поста")
@Validated
public class PostDTO {

    @Schema(description = "id поста", required = true)
    @JsonProperty("id")
    Long id;

    @Schema(description = "текст поста", required = true)
    @JsonProperty("text")
    String text;

    @Schema(description = "дата создания", required = true)
    @JsonProperty("date")
    LocalDateTime date;

    @Schema(description = "url к изображениями", required = true)
    @JsonProperty("content")
    List<String> content;
}
