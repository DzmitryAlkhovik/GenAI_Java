package com.epam.training.gen.ai.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class SearchInfoDTO<T> {
    private String id;
    private T payload;
    private Float score;
}
