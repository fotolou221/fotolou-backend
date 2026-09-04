package com.fotolou.app.web.rest;

import static com.fotolou.app.domain.CoiffeurProfileAsserts.*;
import static com.fotolou.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fotolou.app.IntegrationTest;
import com.fotolou.app.domain.CoiffeurProfile;
import com.fotolou.app.domain.Salon;
import com.fotolou.app.domain.User;
import com.fotolou.app.repository.CoiffeurProfileRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.service.CoiffeurProfileService;
import com.fotolou.app.service.dto.CoiffeurProfileDTO;
import com.fotolou.app.service.mapper.CoiffeurProfileMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link CoiffeurProfileResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class CoiffeurProfileResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final String DEFAULT_SPECIALTY = "AAAAAAAAAA";
    private static final String UPDATED_SPECIALTY = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String DEFAULT_AVATAR_URL = "AAAAAAAAAA";
    private static final String UPDATED_AVATAR_URL = "BBBBBBBBBB";

    private static final Integer DEFAULT_TICKETS_SERVED_COUNT = 0;
    private static final Integer UPDATED_TICKETS_SERVED_COUNT = 1;
    private static final Integer SMALLER_TICKETS_SERVED_COUNT = 0 - 1;

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.ofEpochMilli(1787419709034L);

    private static final String ENTITY_API_URL = "/api/coiffeur-profiles";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CoiffeurProfileRepository coiffeurProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private CoiffeurProfileRepository coiffeurProfileRepositoryMock;

    @Autowired
    private CoiffeurProfileMapper coiffeurProfileMapper;

    @Mock
    private CoiffeurProfileService coiffeurProfileServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCoiffeurProfileMockMvc;

    private CoiffeurProfile coiffeurProfile;

    private CoiffeurProfile insertedCoiffeurProfile;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CoiffeurProfile createEntity(EntityManager em) {
        CoiffeurProfile coiffeurProfile = new CoiffeurProfile()
            .name(DEFAULT_NAME)
            .phone(DEFAULT_PHONE)
            .specialty(DEFAULT_SPECIALTY)
            .active(DEFAULT_ACTIVE)
            .avatarUrl(DEFAULT_AVATAR_URL)
            .ticketsServedCount(DEFAULT_TICKETS_SERVED_COUNT)
            .createdDate(DEFAULT_CREATED_DATE);
        // Add required entity
        Salon salon;
        if (TestUtil.findAll(em, Salon.class).isEmpty()) {
            salon = SalonResourceIT.createEntity();
            em.persist(salon);
            em.flush();
        } else {
            salon = TestUtil.findAll(em, Salon.class).get(0);
        }
        coiffeurProfile.setSalon(salon);
        return coiffeurProfile;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CoiffeurProfile createUpdatedEntity(EntityManager em) {
        CoiffeurProfile updatedCoiffeurProfile = new CoiffeurProfile()
            .name(UPDATED_NAME)
            .phone(UPDATED_PHONE)
            .specialty(UPDATED_SPECIALTY)
            .active(UPDATED_ACTIVE)
            .avatarUrl(UPDATED_AVATAR_URL)
            .ticketsServedCount(UPDATED_TICKETS_SERVED_COUNT)
            .createdDate(UPDATED_CREATED_DATE);
        // Add required entity
        Salon salon;
        if (TestUtil.findAll(em, Salon.class).isEmpty()) {
            salon = SalonResourceIT.createUpdatedEntity();
            em.persist(salon);
            em.flush();
        } else {
            salon = TestUtil.findAll(em, Salon.class).get(0);
        }
        updatedCoiffeurProfile.setSalon(salon);
        return updatedCoiffeurProfile;
    }

    @BeforeEach
    void initTest() {
        coiffeurProfile = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedCoiffeurProfile != null) {
            coiffeurProfileRepository.delete(insertedCoiffeurProfile);
            insertedCoiffeurProfile = null;
        }
    }

    @Test
    @Transactional
    void createCoiffeurProfile() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the CoiffeurProfile
        CoiffeurProfileDTO coiffeurProfileDTO = coiffeurProfileMapper.toDto(coiffeurProfile);
        var returnedCoiffeurProfileDTO = om.readValue(
            restCoiffeurProfileMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(coiffeurProfileDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CoiffeurProfileDTO.class
        );

        // Validate the CoiffeurProfile in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCoiffeurProfile = coiffeurProfileMapper.toEntity(returnedCoiffeurProfileDTO);
        assertCoiffeurProfileUpdatableFieldsEquals(returnedCoiffeurProfile, getPersistedCoiffeurProfile(returnedCoiffeurProfile));

        insertedCoiffeurProfile = returnedCoiffeurProfile;
    }

    @Test
    @Transactional
    void createCoiffeurProfileWithExistingId() throws Exception {
        // Create the CoiffeurProfile with an existing ID
        coiffeurProfile.setId(1L);
        CoiffeurProfileDTO coiffeurProfileDTO = coiffeurProfileMapper.toDto(coiffeurProfile);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCoiffeurProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(coiffeurProfileDTO)))
            .andExpect(status().isBadRequest());

        // Validate the CoiffeurProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        coiffeurProfile.setName(null);

        // Create the CoiffeurProfile, which fails.
        CoiffeurProfileDTO coiffeurProfileDTO = coiffeurProfileMapper.toDto(coiffeurProfile);

        restCoiffeurProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(coiffeurProfileDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPhoneIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        coiffeurProfile.setPhone(null);

        // Create the CoiffeurProfile, which fails.
        CoiffeurProfileDTO coiffeurProfileDTO = coiffeurProfileMapper.toDto(coiffeurProfile);

        restCoiffeurProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(coiffeurProfileDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        coiffeurProfile.setActive(null);

        // Create the CoiffeurProfile, which fails.
        CoiffeurProfileDTO coiffeurProfileDTO = coiffeurProfileMapper.toDto(coiffeurProfile);

        restCoiffeurProfileMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(coiffeurProfileDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfiles() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList
        restCoiffeurProfileMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(coiffeurProfile.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].specialty").value(hasItem(DEFAULT_SPECIALTY)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))
            .andExpect(jsonPath("$.[*].avatarUrl").value(hasItem(DEFAULT_AVATAR_URL)))
            .andExpect(jsonPath("$.[*].ticketsServedCount").value(hasItem(DEFAULT_TICKETS_SERVED_COUNT)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCoiffeurProfilesWithEagerRelationshipsIsEnabled() throws Exception {
        when(coiffeurProfileServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCoiffeurProfileMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(coiffeurProfileServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCoiffeurProfilesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(coiffeurProfileServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCoiffeurProfileMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(coiffeurProfileRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getCoiffeurProfile() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get the coiffeurProfile
        restCoiffeurProfileMockMvc
            .perform(get(ENTITY_API_URL_ID, coiffeurProfile.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(coiffeurProfile.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE))
            .andExpect(jsonPath("$.specialty").value(DEFAULT_SPECIALTY))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE))
            .andExpect(jsonPath("$.avatarUrl").value(DEFAULT_AVATAR_URL))
            .andExpect(jsonPath("$.ticketsServedCount").value(DEFAULT_TICKETS_SERVED_COUNT))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()));
    }

    @Test
    @Transactional
    void getCoiffeurProfilesByIdFiltering() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        Long id = coiffeurProfile.getId();

        defaultCoiffeurProfileFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultCoiffeurProfileFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultCoiffeurProfileFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where name equals to
        defaultCoiffeurProfileFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where name in
        defaultCoiffeurProfileFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where name is not null
        defaultCoiffeurProfileFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where name contains
        defaultCoiffeurProfileFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where name does not contain
        defaultCoiffeurProfileFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByPhoneIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where phone equals to
        defaultCoiffeurProfileFiltering("phone.equals=" + DEFAULT_PHONE, "phone.equals=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByPhoneIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where phone in
        defaultCoiffeurProfileFiltering("phone.in=" + DEFAULT_PHONE + "," + UPDATED_PHONE, "phone.in=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByPhoneIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where phone is not null
        defaultCoiffeurProfileFiltering("phone.specified=true", "phone.specified=false");
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByPhoneContainsSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where phone contains
        defaultCoiffeurProfileFiltering("phone.contains=" + DEFAULT_PHONE, "phone.contains=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByPhoneNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where phone does not contain
        defaultCoiffeurProfileFiltering("phone.doesNotContain=" + UPDATED_PHONE, "phone.doesNotContain=" + DEFAULT_PHONE);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesBySpecialtyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where specialty equals to
        defaultCoiffeurProfileFiltering("specialty.equals=" + DEFAULT_SPECIALTY, "specialty.equals=" + UPDATED_SPECIALTY);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesBySpecialtyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where specialty in
        defaultCoiffeurProfileFiltering("specialty.in=" + DEFAULT_SPECIALTY + "," + UPDATED_SPECIALTY, "specialty.in=" + UPDATED_SPECIALTY);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesBySpecialtyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where specialty is not null
        defaultCoiffeurProfileFiltering("specialty.specified=true", "specialty.specified=false");
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesBySpecialtyContainsSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where specialty contains
        defaultCoiffeurProfileFiltering("specialty.contains=" + DEFAULT_SPECIALTY, "specialty.contains=" + UPDATED_SPECIALTY);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesBySpecialtyNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where specialty does not contain
        defaultCoiffeurProfileFiltering("specialty.doesNotContain=" + UPDATED_SPECIALTY, "specialty.doesNotContain=" + DEFAULT_SPECIALTY);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where active equals to
        defaultCoiffeurProfileFiltering("active.equals=" + DEFAULT_ACTIVE, "active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where active in
        defaultCoiffeurProfileFiltering("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE, "active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where active is not null
        defaultCoiffeurProfileFiltering("active.specified=true", "active.specified=false");
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByAvatarUrlIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where avatarUrl equals to
        defaultCoiffeurProfileFiltering("avatarUrl.equals=" + DEFAULT_AVATAR_URL, "avatarUrl.equals=" + UPDATED_AVATAR_URL);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByAvatarUrlIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where avatarUrl in
        defaultCoiffeurProfileFiltering(
            "avatarUrl.in=" + DEFAULT_AVATAR_URL + "," + UPDATED_AVATAR_URL,
            "avatarUrl.in=" + UPDATED_AVATAR_URL
        );
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByAvatarUrlIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where avatarUrl is not null
        defaultCoiffeurProfileFiltering("avatarUrl.specified=true", "avatarUrl.specified=false");
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByAvatarUrlContainsSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where avatarUrl contains
        defaultCoiffeurProfileFiltering("avatarUrl.contains=" + DEFAULT_AVATAR_URL, "avatarUrl.contains=" + UPDATED_AVATAR_URL);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByAvatarUrlNotContainsSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where avatarUrl does not contain
        defaultCoiffeurProfileFiltering("avatarUrl.doesNotContain=" + UPDATED_AVATAR_URL, "avatarUrl.doesNotContain=" + DEFAULT_AVATAR_URL);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByTicketsServedCountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where ticketsServedCount equals to
        defaultCoiffeurProfileFiltering(
            "ticketsServedCount.equals=" + DEFAULT_TICKETS_SERVED_COUNT,
            "ticketsServedCount.equals=" + UPDATED_TICKETS_SERVED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByTicketsServedCountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where ticketsServedCount in
        defaultCoiffeurProfileFiltering(
            "ticketsServedCount.in=" + DEFAULT_TICKETS_SERVED_COUNT + "," + UPDATED_TICKETS_SERVED_COUNT,
            "ticketsServedCount.in=" + UPDATED_TICKETS_SERVED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByTicketsServedCountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where ticketsServedCount is not null
        defaultCoiffeurProfileFiltering("ticketsServedCount.specified=true", "ticketsServedCount.specified=false");
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByTicketsServedCountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where ticketsServedCount is greater than or equal to
        defaultCoiffeurProfileFiltering(
            "ticketsServedCount.greaterThanOrEqual=" + DEFAULT_TICKETS_SERVED_COUNT,
            "ticketsServedCount.greaterThanOrEqual=" + UPDATED_TICKETS_SERVED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByTicketsServedCountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where ticketsServedCount is less than or equal to
        defaultCoiffeurProfileFiltering(
            "ticketsServedCount.lessThanOrEqual=" + DEFAULT_TICKETS_SERVED_COUNT,
            "ticketsServedCount.lessThanOrEqual=" + SMALLER_TICKETS_SERVED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByTicketsServedCountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where ticketsServedCount is less than
        defaultCoiffeurProfileFiltering(
            "ticketsServedCount.lessThan=" + UPDATED_TICKETS_SERVED_COUNT,
            "ticketsServedCount.lessThan=" + DEFAULT_TICKETS_SERVED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByTicketsServedCountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where ticketsServedCount is greater than
        defaultCoiffeurProfileFiltering(
            "ticketsServedCount.greaterThan=" + SMALLER_TICKETS_SERVED_COUNT,
            "ticketsServedCount.greaterThan=" + DEFAULT_TICKETS_SERVED_COUNT
        );
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByCreatedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where createdDate equals to
        defaultCoiffeurProfileFiltering("createdDate.equals=" + DEFAULT_CREATED_DATE, "createdDate.equals=" + UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByCreatedDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where createdDate in
        defaultCoiffeurProfileFiltering(
            "createdDate.in=" + DEFAULT_CREATED_DATE + "," + UPDATED_CREATED_DATE,
            "createdDate.in=" + UPDATED_CREATED_DATE
        );
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByCreatedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        // Get all the coiffeurProfileList where createdDate is not null
        defaultCoiffeurProfileFiltering("createdDate.specified=true", "createdDate.specified=false");
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesByUserIsEqualToSomething() throws Exception {
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            coiffeurProfileRepository.saveAndFlush(coiffeurProfile);
            user = UserResourceIT.createEntity();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        coiffeurProfile.setUser(user);
        coiffeurProfileRepository.saveAndFlush(coiffeurProfile);
        Long userId = user.getId();
        // Get all the coiffeurProfileList where user equals to userId
        defaultCoiffeurProfileShouldBeFound("userId.equals=" + userId);

        // Get all the coiffeurProfileList where user equals to (userId + 1)
        defaultCoiffeurProfileShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    @Test
    @Transactional
    void getAllCoiffeurProfilesBySalonIsEqualToSomething() throws Exception {
        Salon salon;
        if (TestUtil.findAll(em, Salon.class).isEmpty()) {
            coiffeurProfileRepository.saveAndFlush(coiffeurProfile);
            salon = SalonResourceIT.createEntity();
        } else {
            salon = TestUtil.findAll(em, Salon.class).get(0);
        }
        em.persist(salon);
        em.flush();
        coiffeurProfile.setSalon(salon);
        coiffeurProfileRepository.saveAndFlush(coiffeurProfile);
        Long salonId = salon.getId();
        // Get all the coiffeurProfileList where salon equals to salonId
        defaultCoiffeurProfileShouldBeFound("salonId.equals=" + salonId);

        // Get all the coiffeurProfileList where salon equals to (salonId + 1)
        defaultCoiffeurProfileShouldNotBeFound("salonId.equals=" + (salonId + 1));
    }

    private void defaultCoiffeurProfileFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultCoiffeurProfileShouldBeFound(shouldBeFound);
        defaultCoiffeurProfileShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCoiffeurProfileShouldBeFound(String filter) throws Exception {
        restCoiffeurProfileMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(coiffeurProfile.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].specialty").value(hasItem(DEFAULT_SPECIALTY)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))
            .andExpect(jsonPath("$.[*].avatarUrl").value(hasItem(DEFAULT_AVATAR_URL)))
            .andExpect(jsonPath("$.[*].ticketsServedCount").value(hasItem(DEFAULT_TICKETS_SERVED_COUNT)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())));

        // Check, that the count call also returns 1
        restCoiffeurProfileMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCoiffeurProfileShouldNotBeFound(String filter) throws Exception {
        restCoiffeurProfileMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCoiffeurProfileMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCoiffeurProfile() throws Exception {
        // Get the coiffeurProfile
        restCoiffeurProfileMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCoiffeurProfile() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the coiffeurProfile
        CoiffeurProfile updatedCoiffeurProfile = coiffeurProfileRepository.findById(coiffeurProfile.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCoiffeurProfile are not directly saved in db
        em.detach(updatedCoiffeurProfile);
        updatedCoiffeurProfile
            .name(UPDATED_NAME)
            .phone(UPDATED_PHONE)
            .specialty(UPDATED_SPECIALTY)
            .active(UPDATED_ACTIVE)
            .avatarUrl(UPDATED_AVATAR_URL)
            .ticketsServedCount(UPDATED_TICKETS_SERVED_COUNT)
            .createdDate(UPDATED_CREATED_DATE);
        CoiffeurProfileDTO coiffeurProfileDTO = coiffeurProfileMapper.toDto(updatedCoiffeurProfile);

        restCoiffeurProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, coiffeurProfileDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(coiffeurProfileDTO))
            )
            .andExpect(status().isOk());

        // Validate the CoiffeurProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCoiffeurProfileToMatchAllProperties(updatedCoiffeurProfile);
    }

    @Test
    @Transactional
    void putNonExistingCoiffeurProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        coiffeurProfile.setId(longCount.incrementAndGet());

        // Create the CoiffeurProfile
        CoiffeurProfileDTO coiffeurProfileDTO = coiffeurProfileMapper.toDto(coiffeurProfile);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCoiffeurProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, coiffeurProfileDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(coiffeurProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CoiffeurProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCoiffeurProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        coiffeurProfile.setId(longCount.incrementAndGet());

        // Create the CoiffeurProfile
        CoiffeurProfileDTO coiffeurProfileDTO = coiffeurProfileMapper.toDto(coiffeurProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCoiffeurProfileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(coiffeurProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CoiffeurProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCoiffeurProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        coiffeurProfile.setId(longCount.incrementAndGet());

        // Create the CoiffeurProfile
        CoiffeurProfileDTO coiffeurProfileDTO = coiffeurProfileMapper.toDto(coiffeurProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCoiffeurProfileMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(coiffeurProfileDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CoiffeurProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCoiffeurProfileWithPatch() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the coiffeurProfile using partial update
        CoiffeurProfile partialUpdatedCoiffeurProfile = new CoiffeurProfile();
        partialUpdatedCoiffeurProfile.setId(coiffeurProfile.getId());

        partialUpdatedCoiffeurProfile
            .avatarUrl(UPDATED_AVATAR_URL)
            .ticketsServedCount(UPDATED_TICKETS_SERVED_COUNT)
            .createdDate(UPDATED_CREATED_DATE);

        restCoiffeurProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCoiffeurProfile.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCoiffeurProfile))
            )
            .andExpect(status().isOk());

        // Validate the CoiffeurProfile in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCoiffeurProfileUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCoiffeurProfile, coiffeurProfile),
            getPersistedCoiffeurProfile(coiffeurProfile)
        );
    }

    @Test
    @Transactional
    void fullUpdateCoiffeurProfileWithPatch() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the coiffeurProfile using partial update
        CoiffeurProfile partialUpdatedCoiffeurProfile = new CoiffeurProfile();
        partialUpdatedCoiffeurProfile.setId(coiffeurProfile.getId());

        partialUpdatedCoiffeurProfile
            .name(UPDATED_NAME)
            .phone(UPDATED_PHONE)
            .specialty(UPDATED_SPECIALTY)
            .active(UPDATED_ACTIVE)
            .avatarUrl(UPDATED_AVATAR_URL)
            .ticketsServedCount(UPDATED_TICKETS_SERVED_COUNT)
            .createdDate(UPDATED_CREATED_DATE);

        restCoiffeurProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCoiffeurProfile.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCoiffeurProfile))
            )
            .andExpect(status().isOk());

        // Validate the CoiffeurProfile in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCoiffeurProfileUpdatableFieldsEquals(
            partialUpdatedCoiffeurProfile,
            getPersistedCoiffeurProfile(partialUpdatedCoiffeurProfile)
        );
    }

    @Test
    @Transactional
    void patchNonExistingCoiffeurProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        coiffeurProfile.setId(longCount.incrementAndGet());

        // Create the CoiffeurProfile
        CoiffeurProfileDTO coiffeurProfileDTO = coiffeurProfileMapper.toDto(coiffeurProfile);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCoiffeurProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, coiffeurProfileDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(coiffeurProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CoiffeurProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCoiffeurProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        coiffeurProfile.setId(longCount.incrementAndGet());

        // Create the CoiffeurProfile
        CoiffeurProfileDTO coiffeurProfileDTO = coiffeurProfileMapper.toDto(coiffeurProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCoiffeurProfileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(coiffeurProfileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the CoiffeurProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCoiffeurProfile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        coiffeurProfile.setId(longCount.incrementAndGet());

        // Create the CoiffeurProfile
        CoiffeurProfileDTO coiffeurProfileDTO = coiffeurProfileMapper.toDto(coiffeurProfile);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCoiffeurProfileMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(coiffeurProfileDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the CoiffeurProfile in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCoiffeurProfile() throws Exception {
        // Initialize the database
        insertedCoiffeurProfile = coiffeurProfileRepository.saveAndFlush(coiffeurProfile);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the coiffeurProfile
        restCoiffeurProfileMockMvc
            .perform(delete(ENTITY_API_URL_ID, coiffeurProfile.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return coiffeurProfileRepository.count();
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

    protected CoiffeurProfile getPersistedCoiffeurProfile(CoiffeurProfile coiffeurProfile) {
        return coiffeurProfileRepository.findById(coiffeurProfile.getId()).orElseThrow();
    }

    protected void assertPersistedCoiffeurProfileToMatchAllProperties(CoiffeurProfile expectedCoiffeurProfile) {
        assertCoiffeurProfileAllPropertiesEquals(expectedCoiffeurProfile, getPersistedCoiffeurProfile(expectedCoiffeurProfile));
    }

    protected void assertPersistedCoiffeurProfileToMatchUpdatableProperties(CoiffeurProfile expectedCoiffeurProfile) {
        assertCoiffeurProfileAllUpdatablePropertiesEquals(expectedCoiffeurProfile, getPersistedCoiffeurProfile(expectedCoiffeurProfile));
    }
}
