package com.fotolou.app.service.mapper;

import static com.fotolou.app.domain.CoiffeurProfileAsserts.*;
import static com.fotolou.app.domain.CoiffeurProfileTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CoiffeurProfileMapperTest {

    private CoiffeurProfileMapper coiffeurProfileMapper;

    @BeforeEach
    void setUp() {
        coiffeurProfileMapper = new CoiffeurProfileMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCoiffeurProfileSample1();
        var actual = coiffeurProfileMapper.toEntity(coiffeurProfileMapper.toDto(expected));
        assertCoiffeurProfileAllPropertiesEquals(expected, actual);
    }
}
