package com.fotolou.app.web.rest;

import static com.fotolou.app.domain.RelativeAsserts.*;
import static com.fotolou.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fotolou.app.IntegrationTest;
import com.fotolou.app.domain.Relative;
import com.fotolou.app.domain.User;
import com.fotolou.app.domain.enumeration.RelativeRelation;
import com.fotolou.app.repository.RelativeRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.service.dto.RelativeDTO;
import com.fotolou.app.service.mapper.RelativeMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
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
 * Integration tests for the {@link RelativeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class RelativeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final RelativeRelation DEFAULT_RELATION = RelativeRelation.MERE;
    private static final RelativeRelation UPDATED_RELATION = RelativeRelation.PERE;

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.ofEpochMilli(1787419709034L);

    private static final String ENTITY_API_URL = "/api/relatives";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private RelativeRepository relativeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RelativeMapper relativeMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRelativeMockMvc;

    private Relative relative;

    private Relative insertedRelative;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Relative createEntity(EntityManager em) {
        Relative relative = new Relative()
            .name(DEFAULT_NAME)
            .relation(DEFAULT_RELATION)
            .phone(DEFAULT_PHONE)
            .createdDate(DEFAULT_CREATED_DATE);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        relative.setUser(user);
        return relative;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Relative createUpdatedEntity(EntityManager em) {
        Relative updatedRelative = new Relative()
            .name(UPDATED_NAME)
            .relation(UPDATED_RELATION)
            .phone(UPDATED_PHONE)
            .createdDate(UPDATED_CREATED_DATE);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedRelative.setUser(user);
        return updatedRelative;
    }

    @BeforeEach
    void initTest() {
        relative = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedRelative != null) {
            relativeRepository.delete(insertedRelative);
            insertedRelative = null;
        }
    }

    @Test
    @Transactional
    void createRelative() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Relative
        RelativeDTO relativeDTO = relativeMapper.toDto(relative);
        var returnedRelativeDTO = om.readValue(
            restRelativeMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(relativeDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            RelativeDTO.class
        );

        // Validate the Relative in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedRelative = relativeMapper.toEntity(returnedRelativeDTO);
        assertRelativeUpdatableFieldsEquals(returnedRelative, getPersistedRelative(returnedRelative));

        insertedRelative = returnedRelative;
    }

    @Test
    @Transactional
    void createRelativeWithExistingId() throws Exception {
        // Create the Relative with an existing ID
        relative.setId(1L);
        RelativeDTO relativeDTO = relativeMapper.toDto(relative);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRelativeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(relativeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Relative in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        relative.setName(null);

        // Create the Relative, which fails.
        RelativeDTO relativeDTO = relativeMapper.toDto(relative);

        restRelativeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(relativeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRelationIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        relative.setRelation(null);

        // Create the Relative, which fails.
        RelativeDTO relativeDTO = relativeMapper.toDto(relative);

        restRelativeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(relativeDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllRelatives() throws Exception {
        // Initialize the database
        insertedRelative = relativeRepository.saveAndFlush(relative);

        // Get all the relativeList
        restRelativeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(relative.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].relation").value(hasItem(DEFAULT_RELATION.toString())))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())));
    }

    @Test
    @Transactional
    void getRelative() throws Exception {
        // Initialize the database
        insertedRelative = relativeRepository.saveAndFlush(relative);

        // Get the relative
        restRelativeMockMvc
            .perform(get(ENTITY_API_URL_ID, relative.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(relative.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.relation").value(DEFAULT_RELATION.toString()))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingRelative() throws Exception {
        // Get the relative
        restRelativeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingRelative() throws Exception {
        // Initialize the database
        insertedRelative = relativeRepository.saveAndFlush(relative);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the relative
        Relative updatedRelative = relativeRepository.findById(relative.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedRelative are not directly saved in db
        em.detach(updatedRelative);
        updatedRelative.name(UPDATED_NAME).relation(UPDATED_RELATION).phone(UPDATED_PHONE).createdDate(UPDATED_CREATED_DATE);
        RelativeDTO relativeDTO = relativeMapper.toDto(updatedRelative);

        restRelativeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, relativeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(relativeDTO))
            )
            .andExpect(status().isOk());

        // Validate the Relative in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedRelativeToMatchAllProperties(updatedRelative);
    }

    @Test
    @Transactional
    void putNonExistingRelative() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        relative.setId(longCount.incrementAndGet());

        // Create the Relative
        RelativeDTO relativeDTO = relativeMapper.toDto(relative);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRelativeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, relativeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(relativeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Relative in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchRelative() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        relative.setId(longCount.incrementAndGet());

        // Create the Relative
        RelativeDTO relativeDTO = relativeMapper.toDto(relative);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRelativeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(relativeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Relative in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRelative() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        relative.setId(longCount.incrementAndGet());

        // Create the Relative
        RelativeDTO relativeDTO = relativeMapper.toDto(relative);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRelativeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(relativeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Relative in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateRelativeWithPatch() throws Exception {
        // Initialize the database
        insertedRelative = relativeRepository.saveAndFlush(relative);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the relative using partial update
        Relative partialUpdatedRelative = new Relative();
        partialUpdatedRelative.setId(relative.getId());

        partialUpdatedRelative.name(UPDATED_NAME).createdDate(UPDATED_CREATED_DATE);

        restRelativeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRelative.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedRelative))
            )
            .andExpect(status().isOk());

        // Validate the Relative in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRelativeUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedRelative, relative), getPersistedRelative(relative));
    }

    @Test
    @Transactional
    void fullUpdateRelativeWithPatch() throws Exception {
        // Initialize the database
        insertedRelative = relativeRepository.saveAndFlush(relative);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the relative using partial update
        Relative partialUpdatedRelative = new Relative();
        partialUpdatedRelative.setId(relative.getId());

        partialUpdatedRelative.name(UPDATED_NAME).relation(UPDATED_RELATION).phone(UPDATED_PHONE).createdDate(UPDATED_CREATED_DATE);

        restRelativeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRelative.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedRelative))
            )
            .andExpect(status().isOk());

        // Validate the Relative in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRelativeUpdatableFieldsEquals(partialUpdatedRelative, getPersistedRelative(partialUpdatedRelative));
    }

    @Test
    @Transactional
    void patchNonExistingRelative() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        relative.setId(longCount.incrementAndGet());

        // Create the Relative
        RelativeDTO relativeDTO = relativeMapper.toDto(relative);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRelativeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, relativeDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(relativeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Relative in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRelative() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        relative.setId(longCount.incrementAndGet());

        // Create the Relative
        RelativeDTO relativeDTO = relativeMapper.toDto(relative);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRelativeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(relativeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Relative in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRelative() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        relative.setId(longCount.incrementAndGet());

        // Create the Relative
        RelativeDTO relativeDTO = relativeMapper.toDto(relative);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRelativeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(relativeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Relative in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteRelative() throws Exception {
        // Initialize the database
        insertedRelative = relativeRepository.saveAndFlush(relative);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the relative
        restRelativeMockMvc
            .perform(delete(ENTITY_API_URL_ID, relative.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return relativeRepository.count();
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

    protected Relative getPersistedRelative(Relative relative) {
        return relativeRepository.findById(relative.getId()).orElseThrow();
    }

    protected void assertPersistedRelativeToMatchAllProperties(Relative expectedRelative) {
        assertRelativeAllPropertiesEquals(expectedRelative, getPersistedRelative(expectedRelative));
    }

    protected void assertPersistedRelativeToMatchUpdatableProperties(Relative expectedRelative) {
        assertRelativeAllUpdatablePropertiesEquals(expectedRelative, getPersistedRelative(expectedRelative));
    }
}
