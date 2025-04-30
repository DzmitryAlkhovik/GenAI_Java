package com.epam.training.gen.ai.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class SearchBookInfoDTO {
    private String id;
    private BookInfo bookInfo;
    private Float score;
}
