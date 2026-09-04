package com.fotolou.app.service.mapper;

import static com.fotolou.app.domain.AppNotificationAsserts.*;
import static com.fotolou.app.domain.AppNotificationTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppNotificationMapperTest {

    private AppNotificationMapper appNotificationMapper;

    @BeforeEach
    void setUp() {
        appNotificationMapper = new AppNotificationMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAppNotificationSample1();
        var actual = appNotificationMapper.toEntity(appNotificationMapper.toDto(expected));
        assertAppNotificationAllPropertiesEquals(expected, actual);
    }
}
