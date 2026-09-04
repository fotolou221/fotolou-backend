package com.fotolou.app.service.mapper;

import static com.fotolou.app.domain.SalonAsserts.*;
import static com.fotolou.app.domain.SalonTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SalonMapperTest {

    private SalonMapper salonMapper;

    @BeforeEach
    void setUp() {
        salonMapper = new SalonMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getSalonSample1();
        var actual = salonMapper.toEntity(salonMapper.toDto(expected));
        assertSalonAllPropertiesEquals(expected, actual);
    }
}
