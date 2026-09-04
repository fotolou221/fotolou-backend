package com.fotolou.app.service.mapper;

import static com.fotolou.app.domain.PlatformSettingsAsserts.*;
import static com.fotolou.app.domain.PlatformSettingsTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlatformSettingsMapperTest {

    private PlatformSettingsMapper platformSettingsMapper;

    @BeforeEach
    void setUp() {
        platformSettingsMapper = new PlatformSettingsMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getPlatformSettingsSample1();
        var actual = platformSettingsMapper.toEntity(platformSettingsMapper.toDto(expected));
        assertPlatformSettingsAllPropertiesEquals(expected, actual);
    }
}
