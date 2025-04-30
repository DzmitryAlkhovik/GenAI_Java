package com.epam.training.gen.ai.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class BookChunk {
    String source;
    String chunk;
}
