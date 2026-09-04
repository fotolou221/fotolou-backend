package com.fotolou.app.web.rest;

import static com.fotolou.app.domain.PlatformSettingsAsserts.*;
import static com.fotolou.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fotolou.app.IntegrationTest;
import com.fotolou.app.domain.PlatformSettings;
import com.fotolou.app.repository.PlatformSettingsRepository;
import com.fotolou.app.service.dto.PlatformSettingsDTO;
import com.fotolou.app.service.mapper.PlatformSettingsMapper;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link PlatformSettingsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PlatformSettingsResourceIT {

    private static final String DEFAULT_APP_NAME = "AAAAAAAAAA";
    private static final String UPDATED_APP_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CONTACT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_CONTACT_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_CONTACT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_CONTACT_PHONE = "BBBBBBBBBB";

    private static final Double DEFAULT_COMMISSION_RATE = 0D;
    private static final Double UPDATED_COMMISSION_RATE = 1D;

    private static final String DEFAULT_OPENING_TIME = "AAAAAAAAAA";
    private static final String UPDATED_OPENING_TIME = "BBBBBBBBBB";

    private static final String DEFAULT_CLOSING_TIME = "AAAAAAAAAA";
    private static final String UPDATED_CLOSING_TIME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ALLOW_RELATIVE_BOOKING = false;
    private static final Boolean UPDATED_ALLOW_RELATIVE_BOOKING = true;

    private static final Boolean DEFAULT_MAINTENANCE_MODE = false;
    private static final Boolean UPDATED_MAINTENANCE_MODE = true;

    private static final String ENTITY_API_URL = "/api/platform-settings";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PlatformSettingsRepository platformSettingsRepository;

    @Autowired
    private PlatformSettingsMapper platformSettingsMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPlatformSettingsMockMvc;

    private PlatformSettings platformSettings;

    private PlatformSettings insertedPlatformSettings;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PlatformSettings createEntity() {
        return new PlatformSettings()
            .appName(DEFAULT_APP_NAME)
            .contactEmail(DEFAULT_CONTACT_EMAIL)
            .contactPhone(DEFAULT_CONTACT_PHONE)
            .commissionRate(DEFAULT_COMMISSION_RATE)
            .openingTime(DEFAULT_OPENING_TIME)
            .closingTime(DEFAULT_CLOSING_TIME)
            .allowRelativeBooking(DEFAULT_ALLOW_RELATIVE_BOOKING)
            .maintenanceMode(DEFAULT_MAINTENANCE_MODE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PlatformSettings createUpdatedEntity() {
        return new PlatformSettings()
            .appName(UPDATED_APP_NAME)
            .contactEmail(UPDATED_CONTACT_EMAIL)
            .contactPhone(UPDATED_CONTACT_PHONE)
            .commissionRate(UPDATED_COMMISSION_RATE)
            .openingTime(UPDATED_OPENING_TIME)
            .closingTime(UPDATED_CLOSING_TIME)
            .allowRelativeBooking(UPDATED_ALLOW_RELATIVE_BOOKING)
            .maintenanceMode(UPDATED_MAINTENANCE_MODE);
    }

    @BeforeEach
    void initTest() {
        platformSettings = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedPlatformSettings != null) {
            platformSettingsRepository.delete(insertedPlatformSettings);
            insertedPlatformSettings = null;
        }
    }

    @Test
    @Transactional
    void createPlatformSettings() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the PlatformSettings
        PlatformSettingsDTO platformSettingsDTO = platformSettingsMapper.toDto(platformSettings);
        var returnedPlatformSettingsDTO = om.readValue(
            restPlatformSettingsMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(platformSettingsDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PlatformSettingsDTO.class
        );

        // Validate the PlatformSettings in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPlatformSettings = platformSettingsMapper.toEntity(returnedPlatformSettingsDTO);
        assertPlatformSettingsUpdatableFieldsEquals(returnedPlatformSettings, getPersistedPlatformSettings(returnedPlatformSettings));

        insertedPlatformSettings = returnedPlatformSettings;
    }

    @Test
    @Transactional
    void createPlatformSettingsWithExistingId() throws Exception {
        // Create the PlatformSettings with an existing ID
        platformSettings.setId(1L);
        PlatformSettingsDTO platformSettingsDTO = platformSettingsMapper.toDto(platformSettings);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPlatformSettingsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(platformSettingsDTO)))
            .andExpect(status().isBadRequest());

        // Validate the PlatformSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkAppNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        platformSettings.setAppName(null);

        // Create the PlatformSettings, which fails.
        PlatformSettingsDTO platformSettingsDTO = platformSettingsMapper.toDto(platformSettings);

        restPlatformSettingsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(platformSettingsDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAllowRelativeBookingIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        platformSettings.setAllowRelativeBooking(null);

        // Create the PlatformSettings, which fails.
        PlatformSettingsDTO platformSettingsDTO = platformSettingsMapper.toDto(platformSettings);

        restPlatformSettingsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(platformSettingsDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkMaintenanceModeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        platformSettings.setMaintenanceMode(null);

        // Create the PlatformSettings, which fails.
        PlatformSettingsDTO platformSettingsDTO = platformSettingsMapper.toDto(platformSettings);

        restPlatformSettingsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(platformSettingsDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPlatformSettingses() throws Exception {
        // Initialize the database
        insertedPlatformSettings = platformSettingsRepository.saveAndFlush(platformSettings);

        // Get all the platformSettingsList
        restPlatformSettingsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(platformSettings.getId().intValue())))
            .andExpect(jsonPath("$.[*].appName").value(hasItem(DEFAULT_APP_NAME)))
            .andExpect(jsonPath("$.[*].contactEmail").value(hasItem(DEFAULT_CONTACT_EMAIL)))
            .andExpect(jsonPath("$.[*].contactPhone").value(hasItem(DEFAULT_CONTACT_PHONE)))
            .andExpect(jsonPath("$.[*].commissionRate").value(hasItem(DEFAULT_COMMISSION_RATE)))
            .andExpect(jsonPath("$.[*].openingTime").value(hasItem(DEFAULT_OPENING_TIME)))
            .andExpect(jsonPath("$.[*].closingTime").value(hasItem(DEFAULT_CLOSING_TIME)))
            .andExpect(jsonPath("$.[*].allowRelativeBooking").value(hasItem(DEFAULT_ALLOW_RELATIVE_BOOKING)))
            .andExpect(jsonPath("$.[*].maintenanceMode").value(hasItem(DEFAULT_MAINTENANCE_MODE)));
    }

    @Test
    @Transactional
    void getPlatformSettings() throws Exception {
        // Initialize the database
        insertedPlatformSettings = platformSettingsRepository.saveAndFlush(platformSettings);

        // Get the platformSettings
        restPlatformSettingsMockMvc
            .perform(get(ENTITY_API_URL_ID, platformSettings.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(platformSettings.getId().intValue()))
            .andExpect(jsonPath("$.appName").value(DEFAULT_APP_NAME))
            .andExpect(jsonPath("$.contactEmail").value(DEFAULT_CONTACT_EMAIL))
            .andExpect(jsonPath("$.contactPhone").value(DEFAULT_CONTACT_PHONE))
            .andExpect(jsonPath("$.commissionRate").value(DEFAULT_COMMISSION_RATE))
            .andExpect(jsonPath("$.openingTime").value(DEFAULT_OPENING_TIME))
            .andExpect(jsonPath("$.closingTime").value(DEFAULT_CLOSING_TIME))
            .andExpect(jsonPath("$.allowRelativeBooking").value(DEFAULT_ALLOW_RELATIVE_BOOKING))
            .andExpect(jsonPath("$.maintenanceMode").value(DEFAULT_MAINTENANCE_MODE));
    }

    @Test
    @Transactional
    void getNonExistingPlatformSettings() throws Exception {
        // Get the platformSettings
        restPlatformSettingsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPlatformSettings() throws Exception {
        // Initialize the database
        insertedPlatformSettings = platformSettingsRepository.saveAndFlush(platformSettings);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the platformSettings
        PlatformSettings updatedPlatformSettings = platformSettingsRepository.findById(platformSettings.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPlatformSettings are not directly saved in db
        em.detach(updatedPlatformSettings);
        updatedPlatformSettings
            .appName(UPDATED_APP_NAME)
            .contactEmail(UPDATED_CONTACT_EMAIL)
            .contactPhone(UPDATED_CONTACT_PHONE)
            .commissionRate(UPDATED_COMMISSION_RATE)
            .openingTime(UPDATED_OPENING_TIME)
            .closingTime(UPDATED_CLOSING_TIME)
            .allowRelativeBooking(UPDATED_ALLOW_RELATIVE_BOOKING)
            .maintenanceMode(UPDATED_MAINTENANCE_MODE);
        PlatformSettingsDTO platformSettingsDTO = platformSettingsMapper.toDto(updatedPlatformSettings);

        restPlatformSettingsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, platformSettingsDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(platformSettingsDTO))
            )
            .andExpect(status().isOk());

        // Validate the PlatformSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPlatformSettingsToMatchAllProperties(updatedPlatformSettings);
    }

    @Test
    @Transactional
    void putNonExistingPlatformSettings() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        platformSettings.setId(longCount.incrementAndGet());

        // Create the PlatformSettings
        PlatformSettingsDTO platformSettingsDTO = platformSettingsMapper.toDto(platformSettings);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPlatformSettingsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, platformSettingsDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(platformSettingsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PlatformSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPlatformSettings() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        platformSettings.setId(longCount.incrementAndGet());

        // Create the PlatformSettings
        PlatformSettingsDTO platformSettingsDTO = platformSettingsMapper.toDto(platformSettings);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPlatformSettingsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(platformSettingsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PlatformSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPlatformSettings() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        platformSettings.setId(longCount.incrementAndGet());

        // Create the PlatformSettings
        PlatformSettingsDTO platformSettingsDTO = platformSettingsMapper.toDto(platformSettings);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPlatformSettingsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(platformSettingsDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the PlatformSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePlatformSettingsWithPatch() throws Exception {
        // Initialize the database
        insertedPlatformSettings = platformSettingsRepository.saveAndFlush(platformSettings);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the platformSettings using partial update
        PlatformSettings partialUpdatedPlatformSettings = new PlatformSettings();
        partialUpdatedPlatformSettings.setId(platformSettings.getId());

        partialUpdatedPlatformSettings
            .contactEmail(UPDATED_CONTACT_EMAIL)
            .contactPhone(UPDATED_CONTACT_PHONE)
            .openingTime(UPDATED_OPENING_TIME)
            .closingTime(UPDATED_CLOSING_TIME);

        restPlatformSettingsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPlatformSettings.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPlatformSettings))
            )
            .andExpect(status().isOk());

        // Validate the PlatformSettings in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPlatformSettingsUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedPlatformSettings, platformSettings),
            getPersistedPlatformSettings(platformSettings)
        );
    }

    @Test
    @Transactional
    void fullUpdatePlatformSettingsWithPatch() throws Exception {
        // Initialize the database
        insertedPlatformSettings = platformSettingsRepository.saveAndFlush(platformSettings);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the platformSettings using partial update
        PlatformSettings partialUpdatedPlatformSettings = new PlatformSettings();
        partialUpdatedPlatformSettings.setId(platformSettings.getId());

        partialUpdatedPlatformSettings
            .appName(UPDATED_APP_NAME)
            .contactEmail(UPDATED_CONTACT_EMAIL)
            .contactPhone(UPDATED_CONTACT_PHONE)
            .commissionRate(UPDATED_COMMISSION_RATE)
            .openingTime(UPDATED_OPENING_TIME)
            .closingTime(UPDATED_CLOSING_TIME)
            .allowRelativeBooking(UPDATED_ALLOW_RELATIVE_BOOKING)
            .maintenanceMode(UPDATED_MAINTENANCE_MODE);

        restPlatformSettingsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPlatformSettings.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPlatformSettings))
            )
            .andExpect(status().isOk());

        // Validate the PlatformSettings in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPlatformSettingsUpdatableFieldsEquals(
            partialUpdatedPlatformSettings,
            getPersistedPlatformSettings(partialUpdatedPlatformSettings)
        );
    }

    @Test
    @Transactional
    void patchNonExistingPlatformSettings() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        platformSettings.setId(longCount.incrementAndGet());

        // Create the PlatformSettings
        PlatformSettingsDTO platformSettingsDTO = platformSettingsMapper.toDto(platformSettings);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPlatformSettingsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, platformSettingsDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(platformSettingsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PlatformSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPlatformSettings() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        platformSettings.setId(longCount.incrementAndGet());

        // Create the PlatformSettings
        PlatformSettingsDTO platformSettingsDTO = platformSettingsMapper.toDto(platformSettings);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPlatformSettingsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(platformSettingsDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PlatformSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPlatformSettings() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        platformSettings.setId(longCount.incrementAndGet());

        // Create the PlatformSettings
        PlatformSettingsDTO platformSettingsDTO = platformSettingsMapper.toDto(platformSettings);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPlatformSettingsMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(platformSettingsDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the PlatformSettings in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePlatformSettings() throws Exception {
        // Initialize the database
        insertedPlatformSettings = platformSettingsRepository.saveAndFlush(platformSettings);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the platformSettings
        restPlatformSettingsMockMvc
            .perform(delete(ENTITY_API_URL_ID, platformSettings.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return platformSettingsRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected PlatformSettings getPersistedPlatformSettings(PlatformSettings platformSettings) {
        return platformSettingsRepository.findById(platformSettings.getId()).orElseThrow();
    }

    protected void assertPersistedPlatformSettingsToMatchAllProperties(PlatformSettings expectedPlatformSettings) {
        assertPlatformSettingsAllPropertiesEquals(expectedPlatformSettings, getPersistedPlatformSettings(expectedPlatformSettings));
    }

    protected void assertPersistedPlatformSettingsToMatchUpdatableProperties(PlatformSettings expectedPlatformSettings) {
        assertPlatformSettingsAllUpdatablePropertiesEquals(
            expectedPlatformSettings,
            getPersistedPlatformSettings(expectedPlatformSettings)
        );
    }
}
