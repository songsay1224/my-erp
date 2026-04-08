package com.example.sayy.DTO;

public record OrgUnitRowDTO(Long id, String name, Long parentId, int depth, boolean hasChildren, int sortOrder) {
}

