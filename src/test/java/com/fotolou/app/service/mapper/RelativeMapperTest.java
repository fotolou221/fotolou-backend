package com.fotolou.app.service.mapper;

import static com.fotolou.app.domain.RelativeAsserts.*;
import static com.fotolou.app.domain.RelativeTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RelativeMapperTest {

    private RelativeMapper relativeMapper;

    @BeforeEach
    void setUp() {
        relativeMapper = new RelativeMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getRelativeSample1();
        var actual = relativeMapper.toEntity(relativeMapper.toDto(expected));
        assertRelativeAllPropertiesEquals(expected, actual);
    }
}
