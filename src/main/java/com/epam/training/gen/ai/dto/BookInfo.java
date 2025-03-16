package com.epam.training.gen.ai.dto;

import java.util.List;

public record BookInfo(String name, String author, String year, List<String> genres, String annotation) {
}
