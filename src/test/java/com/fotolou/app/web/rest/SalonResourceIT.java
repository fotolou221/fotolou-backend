package com.fotolou.app.web.rest;

import static com.fotolou.app.domain.SalonAsserts.*;
import static com.fotolou.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fotolou.app.IntegrationTest;
import com.fotolou.app.domain.Salon;
import com.fotolou.app.domain.enumeration.SalonStatus;
import com.fotolou.app.repository.SalonRepository;
import com.fotolou.app.service.dto.SalonDTO;
import com.fotolou.app.service.mapper.SalonMapper;
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
 * Integration tests for the {@link SalonResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SalonResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_SLUG = "AAAAAAAAAA";
    private static final String UPDATED_SLUG = "BBBBBBBBBB";

    private static final String DEFAULT_LOCATION = "AAAAAAAAAA";
    private static final String UPDATED_LOCATION = "BBBBBBBBBB";

    private static final String DEFAULT_DISTRICT = "AAAAAAAAAA";
    private static final String UPDATED_DISTRICT = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final SalonStatus DEFAULT_STATUS = SalonStatus.OPEN;
    private static final SalonStatus UPDATED_STATUS = SalonStatus.CLOSED;

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final String DEFAULT_OPENING_HOURS = "AAAAAAAAAA";
    private static final String UPDATED_OPENING_HOURS = "BBBBBBBBBB";

    private static final Integer DEFAULT_ESTIMATED_WAIT_MINUTES = 0;
    private static final Integer UPDATED_ESTIMATED_WAIT_MINUTES = 1;
    private static final Integer SMALLER_ESTIMATED_WAIT_MINUTES = 0 - 1;

    private static final Integer DEFAULT_PEOPLE_WAITING = 0;
    private static final Integer UPDATED_PEOPLE_WAITING = 1;
    private static final Integer SMALLER_PEOPLE_WAITING = 0 - 1;

    private static final String DEFAULT_AVATAR_URL = "AAAAAAAAAA";
    private static final String UPDATED_AVATAR_URL = "BBBBBBBBBB";

    private static final String DEFAULT_COVER_URL = "AAAAAAAAAA";
    private static final String UPDATED_COVER_URL = "BBBBBBBBBB";

    private static final Double DEFAULT_LATITUDE = 1D;
    private static final Double UPDATED_LATITUDE = 2D;
    private static final Double SMALLER_LATITUDE = 1D - 1D;

    private static final Double DEFAULT_LONGITUDE = 1D;
    private static final Double UPDATED_LONGITUDE = 2D;
    private static final Double SMALLER_LONGITUDE = 1D - 1D;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.ofEpochMilli(1787419709034L);

    private static final Instant DEFAULT_LAST_MODIFIED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_MODIFIED_DATE = Instant.ofEpochMilli(1787419709034L);

    private static final String ENTITY_API_URL = "/api/salons";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private SalonMapper salonMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSalonMockMvc;

    private Salon salon;

    private Salon insertedSalon;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Salon createEntity() {
        return new Salon()
            .name(DEFAULT_NAME)
            .slug(DEFAULT_SLUG)
            .location(DEFAULT_LOCATION)
            .district(DEFAULT_DISTRICT)
            .address(DEFAULT_ADDRESS)
            .status(DEFAULT_STATUS)
            .phone(DEFAULT_PHONE)
            .openingHours(DEFAULT_OPENING_HOURS)
            .estimatedWaitMinutes(DEFAULT_ESTIMATED_WAIT_MINUTES)
            .peopleWaiting(DEFAULT_PEOPLE_WAITING)
            .avatarUrl(DEFAULT_AVATAR_URL)
            .coverUrl(DEFAULT_COVER_URL)
            .latitude(DEFAULT_LATITUDE)
            .longitude(DEFAULT_LONGITUDE)
            .active(DEFAULT_ACTIVE)
            .createdDate(DEFAULT_CREATED_DATE)
            .lastModifiedDate(DEFAULT_LAST_MODIFIED_DATE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Salon createUpdatedEntity() {
        return new Salon()
            .name(UPDATED_NAME)
            .slug(UPDATED_SLUG)
            .location(UPDATED_LOCATION)
            .district(UPDATED_DISTRICT)
            .address(UPDATED_ADDRESS)
            .status(UPDATED_STATUS)
            .phone(UPDATED_PHONE)
            .openingHours(UPDATED_OPENING_HOURS)
            .estimatedWaitMinutes(UPDATED_ESTIMATED_WAIT_MINUTES)
            .peopleWaiting(UPDATED_PEOPLE_WAITING)
            .avatarUrl(UPDATED_AVATAR_URL)
            .coverUrl(UPDATED_COVER_URL)
            .latitude(UPDATED_LATITUDE)
            .longitude(UPDATED_LONGITUDE)
            .active(UPDATED_ACTIVE)
            .createdDate(UPDATED_CREATED_DATE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE);
    }

    @BeforeEach
    void initTest() {
        salon = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedSalon != null) {
            salonRepository.delete(insertedSalon);
            insertedSalon = null;
        }
    }

    @Test
    @Transactional
    void createSalon() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Salon
        SalonDTO salonDTO = salonMapper.toDto(salon);
        var returnedSalonDTO = om.readValue(
            restSalonMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(salonDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SalonDTO.class
        );

        // Validate the Salon in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedSalon = salonMapper.toEntity(returnedSalonDTO);
        assertSalonUpdatableFieldsEquals(returnedSalon, getPersistedSalon(returnedSalon));

        insertedSalon = returnedSalon;
    }

    @Test
    @Transactional
    void createSalonWithExistingId() throws Exception {
        // Create the Salon with an existing ID
        salon.setId(1L);
        SalonDTO salonDTO = salonMapper.toDto(salon);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSalonMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(salonDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Salon in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        salon.setName(null);

        // Create the Salon, which fails.
        SalonDTO salonDTO = salonMapper.toDto(salon);

        restSalonMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(salonDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSlugIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        salon.setSlug(null);

        // Create the Salon, which fails.
        SalonDTO salonDTO = salonMapper.toDto(salon);

        restSalonMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(salonDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLocationIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        salon.setLocation(null);

        // Create the Salon, which fails.
        SalonDTO salonDTO = salonMapper.toDto(salon);

        restSalonMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(salonDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDistrictIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        salon.setDistrict(null);

        // Create the Salon, which fails.
        SalonDTO salonDTO = salonMapper.toDto(salon);

        restSalonMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(salonDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        salon.setStatus(null);

        // Create the Salon, which fails.
        SalonDTO salonDTO = salonMapper.toDto(salon);

        restSalonMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(salonDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        salon.setActive(null);

        // Create the Salon, which fails.
        SalonDTO salonDTO = salonMapper.toDto(salon);

        restSalonMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(salonDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSalons() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList
        restSalonMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(salon.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].slug").value(hasItem(DEFAULT_SLUG)))
            .andExpect(jsonPath("$.[*].location").value(hasItem(DEFAULT_LOCATION)))
            .andExpect(jsonPath("$.[*].district").value(hasItem(DEFAULT_DISTRICT)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].openingHours").value(hasItem(DEFAULT_OPENING_HOURS)))
            .andExpect(jsonPath("$.[*].estimatedWaitMinutes").value(hasItem(DEFAULT_ESTIMATED_WAIT_MINUTES)))
            .andExpect(jsonPath("$.[*].peopleWaiting").value(hasItem(DEFAULT_PEOPLE_WAITING)))
            .andExpect(jsonPath("$.[*].avatarUrl").value(hasItem(DEFAULT_AVATAR_URL)))
            .andExpect(jsonPath("$.[*].coverUrl").value(hasItem(DEFAULT_COVER_URL)))
            .andExpect(jsonPath("$.[*].latitude").value(hasItem(DEFAULT_LATITUDE)))
            .andExpect(jsonPath("$.[*].longitude").value(hasItem(DEFAULT_LONGITUDE)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(hasItem(DEFAULT_LAST_MODIFIED_DATE.toString())));
    }

    @Test
    @Transactional
    void getSalon() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get the salon
        restSalonMockMvc
            .perform(get(ENTITY_API_URL_ID, salon.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(salon.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.slug").value(DEFAULT_SLUG))
            .andExpect(jsonPath("$.location").value(DEFAULT_LOCATION))
            .andExpect(jsonPath("$.district").value(DEFAULT_DISTRICT))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE))
            .andExpect(jsonPath("$.openingHours").value(DEFAULT_OPENING_HOURS))
            .andExpect(jsonPath("$.estimatedWaitMinutes").value(DEFAULT_ESTIMATED_WAIT_MINUTES))
            .andExpect(jsonPath("$.peopleWaiting").value(DEFAULT_PEOPLE_WAITING))
            .andExpect(jsonPath("$.avatarUrl").value(DEFAULT_AVATAR_URL))
            .andExpect(jsonPath("$.coverUrl").value(DEFAULT_COVER_URL))
            .andExpect(jsonPath("$.latitude").value(DEFAULT_LATITUDE))
            .andExpect(jsonPath("$.longitude").value(DEFAULT_LONGITUDE))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()))
            .andExpect(jsonPath("$.lastModifiedDate").value(DEFAULT_LAST_MODIFIED_DATE.toString()));
    }

    @Test
    @Transactional
    void getSalonsByIdFiltering() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        Long id = salon.getId();

        defaultSalonFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultSalonFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultSalonFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllSalonsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where name equals to
        defaultSalonFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSalonsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where name in
        defaultSalonFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSalonsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where name is not null
        defaultSalonFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where name contains
        defaultSalonFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSalonsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where name does not contain
        defaultSalonFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    @Transactional
    void getAllSalonsBySlugIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where slug equals to
        defaultSalonFiltering("slug.equals=" + DEFAULT_SLUG, "slug.equals=" + UPDATED_SLUG);
    }

    @Test
    @Transactional
    void getAllSalonsBySlugIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where slug in
        defaultSalonFiltering("slug.in=" + DEFAULT_SLUG + "," + UPDATED_SLUG, "slug.in=" + UPDATED_SLUG);
    }

    @Test
    @Transactional
    void getAllSalonsBySlugIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where slug is not null
        defaultSalonFiltering("slug.specified=true", "slug.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsBySlugContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where slug contains
        defaultSalonFiltering("slug.contains=" + DEFAULT_SLUG, "slug.contains=" + UPDATED_SLUG);
    }

    @Test
    @Transactional
    void getAllSalonsBySlugNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where slug does not contain
        defaultSalonFiltering("slug.doesNotContain=" + UPDATED_SLUG, "slug.doesNotContain=" + DEFAULT_SLUG);
    }

    @Test
    @Transactional
    void getAllSalonsByLocationIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where location equals to
        defaultSalonFiltering("location.equals=" + DEFAULT_LOCATION, "location.equals=" + UPDATED_LOCATION);
    }

    @Test
    @Transactional
    void getAllSalonsByLocationIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where location in
        defaultSalonFiltering("location.in=" + DEFAULT_LOCATION + "," + UPDATED_LOCATION, "location.in=" + UPDATED_LOCATION);
    }

    @Test
    @Transactional
    void getAllSalonsByLocationIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where location is not null
        defaultSalonFiltering("location.specified=true", "location.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByLocationContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where location contains
        defaultSalonFiltering("location.contains=" + DEFAULT_LOCATION, "location.contains=" + UPDATED_LOCATION);
    }

    @Test
    @Transactional
    void getAllSalonsByLocationNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where location does not contain
        defaultSalonFiltering("location.doesNotContain=" + UPDATED_LOCATION, "location.doesNotContain=" + DEFAULT_LOCATION);
    }

    @Test
    @Transactional
    void getAllSalonsByDistrictIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where district equals to
        defaultSalonFiltering("district.equals=" + DEFAULT_DISTRICT, "district.equals=" + UPDATED_DISTRICT);
    }

    @Test
    @Transactional
    void getAllSalonsByDistrictIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where district in
        defaultSalonFiltering("district.in=" + DEFAULT_DISTRICT + "," + UPDATED_DISTRICT, "district.in=" + UPDATED_DISTRICT);
    }

    @Test
    @Transactional
    void getAllSalonsByDistrictIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where district is not null
        defaultSalonFiltering("district.specified=true", "district.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByDistrictContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where district contains
        defaultSalonFiltering("district.contains=" + DEFAULT_DISTRICT, "district.contains=" + UPDATED_DISTRICT);
    }

    @Test
    @Transactional
    void getAllSalonsByDistrictNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where district does not contain
        defaultSalonFiltering("district.doesNotContain=" + UPDATED_DISTRICT, "district.doesNotContain=" + DEFAULT_DISTRICT);
    }

    @Test
    @Transactional
    void getAllSalonsByAddressIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where address equals to
        defaultSalonFiltering("address.equals=" + DEFAULT_ADDRESS, "address.equals=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllSalonsByAddressIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where address in
        defaultSalonFiltering("address.in=" + DEFAULT_ADDRESS + "," + UPDATED_ADDRESS, "address.in=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllSalonsByAddressIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where address is not null
        defaultSalonFiltering("address.specified=true", "address.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByAddressContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where address contains
        defaultSalonFiltering("address.contains=" + DEFAULT_ADDRESS, "address.contains=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllSalonsByAddressNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where address does not contain
        defaultSalonFiltering("address.doesNotContain=" + UPDATED_ADDRESS, "address.doesNotContain=" + DEFAULT_ADDRESS);
    }

    @Test
    @Transactional
    void getAllSalonsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where status equals to
        defaultSalonFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllSalonsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where status in
        defaultSalonFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllSalonsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where status is not null
        defaultSalonFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByPhoneIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where phone equals to
        defaultSalonFiltering("phone.equals=" + DEFAULT_PHONE, "phone.equals=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllSalonsByPhoneIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where phone in
        defaultSalonFiltering("phone.in=" + DEFAULT_PHONE + "," + UPDATED_PHONE, "phone.in=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllSalonsByPhoneIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where phone is not null
        defaultSalonFiltering("phone.specified=true", "phone.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByPhoneContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where phone contains
        defaultSalonFiltering("phone.contains=" + DEFAULT_PHONE, "phone.contains=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllSalonsByPhoneNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where phone does not contain
        defaultSalonFiltering("phone.doesNotContain=" + UPDATED_PHONE, "phone.doesNotContain=" + DEFAULT_PHONE);
    }

    @Test
    @Transactional
    void getAllSalonsByOpeningHoursIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where openingHours equals to
        defaultSalonFiltering("openingHours.equals=" + DEFAULT_OPENING_HOURS, "openingHours.equals=" + UPDATED_OPENING_HOURS);
    }

    @Test
    @Transactional
    void getAllSalonsByOpeningHoursIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where openingHours in
        defaultSalonFiltering(
            "openingHours.in=" + DEFAULT_OPENING_HOURS + "," + UPDATED_OPENING_HOURS,
            "openingHours.in=" + UPDATED_OPENING_HOURS
        );
    }

    @Test
    @Transactional
    void getAllSalonsByOpeningHoursIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where openingHours is not null
        defaultSalonFiltering("openingHours.specified=true", "openingHours.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByOpeningHoursContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where openingHours contains
        defaultSalonFiltering("openingHours.contains=" + DEFAULT_OPENING_HOURS, "openingHours.contains=" + UPDATED_OPENING_HOURS);
    }

    @Test
    @Transactional
    void getAllSalonsByOpeningHoursNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where openingHours does not contain
        defaultSalonFiltering(
            "openingHours.doesNotContain=" + UPDATED_OPENING_HOURS,
            "openingHours.doesNotContain=" + DEFAULT_OPENING_HOURS
        );
    }

    @Test
    @Transactional
    void getAllSalonsByEstimatedWaitMinutesIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where estimatedWaitMinutes equals to
        defaultSalonFiltering(
            "estimatedWaitMinutes.equals=" + DEFAULT_ESTIMATED_WAIT_MINUTES,
            "estimatedWaitMinutes.equals=" + UPDATED_ESTIMATED_WAIT_MINUTES
        );
    }

    @Test
    @Transactional
    void getAllSalonsByEstimatedWaitMinutesIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where estimatedWaitMinutes in
        defaultSalonFiltering(
            "estimatedWaitMinutes.in=" + DEFAULT_ESTIMATED_WAIT_MINUTES + "," + UPDATED_ESTIMATED_WAIT_MINUTES,
            "estimatedWaitMinutes.in=" + UPDATED_ESTIMATED_WAIT_MINUTES
        );
    }

    @Test
    @Transactional
    void getAllSalonsByEstimatedWaitMinutesIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where estimatedWaitMinutes is not null
        defaultSalonFiltering("estimatedWaitMinutes.specified=true", "estimatedWaitMinutes.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByEstimatedWaitMinutesIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where estimatedWaitMinutes is greater than or equal to
        defaultSalonFiltering(
            "estimatedWaitMinutes.greaterThanOrEqual=" + DEFAULT_ESTIMATED_WAIT_MINUTES,
            "estimatedWaitMinutes.greaterThanOrEqual=" + UPDATED_ESTIMATED_WAIT_MINUTES
        );
    }

    @Test
    @Transactional
    void getAllSalonsByEstimatedWaitMinutesIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where estimatedWaitMinutes is less than or equal to
        defaultSalonFiltering(
            "estimatedWaitMinutes.lessThanOrEqual=" + DEFAULT_ESTIMATED_WAIT_MINUTES,
            "estimatedWaitMinutes.lessThanOrEqual=" + SMALLER_ESTIMATED_WAIT_MINUTES
        );
    }

    @Test
    @Transactional
    void getAllSalonsByEstimatedWaitMinutesIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where estimatedWaitMinutes is less than
        defaultSalonFiltering(
            "estimatedWaitMinutes.lessThan=" + UPDATED_ESTIMATED_WAIT_MINUTES,
            "estimatedWaitMinutes.lessThan=" + DEFAULT_ESTIMATED_WAIT_MINUTES
        );
    }

    @Test
    @Transactional
    void getAllSalonsByEstimatedWaitMinutesIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where estimatedWaitMinutes is greater than
        defaultSalonFiltering(
            "estimatedWaitMinutes.greaterThan=" + SMALLER_ESTIMATED_WAIT_MINUTES,
            "estimatedWaitMinutes.greaterThan=" + DEFAULT_ESTIMATED_WAIT_MINUTES
        );
    }

    @Test
    @Transactional
    void getAllSalonsByPeopleWaitingIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where peopleWaiting equals to
        defaultSalonFiltering("peopleWaiting.equals=" + DEFAULT_PEOPLE_WAITING, "peopleWaiting.equals=" + UPDATED_PEOPLE_WAITING);
    }

    @Test
    @Transactional
    void getAllSalonsByPeopleWaitingIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where peopleWaiting in
        defaultSalonFiltering(
            "peopleWaiting.in=" + DEFAULT_PEOPLE_WAITING + "," + UPDATED_PEOPLE_WAITING,
            "peopleWaiting.in=" + UPDATED_PEOPLE_WAITING
        );
    }

    @Test
    @Transactional
    void getAllSalonsByPeopleWaitingIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where peopleWaiting is not null
        defaultSalonFiltering("peopleWaiting.specified=true", "peopleWaiting.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByPeopleWaitingIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where peopleWaiting is greater than or equal to
        defaultSalonFiltering(
            "peopleWaiting.greaterThanOrEqual=" + DEFAULT_PEOPLE_WAITING,
            "peopleWaiting.greaterThanOrEqual=" + UPDATED_PEOPLE_WAITING
        );
    }

    @Test
    @Transactional
    void getAllSalonsByPeopleWaitingIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where peopleWaiting is less than or equal to
        defaultSalonFiltering(
            "peopleWaiting.lessThanOrEqual=" + DEFAULT_PEOPLE_WAITING,
            "peopleWaiting.lessThanOrEqual=" + SMALLER_PEOPLE_WAITING
        );
    }

    @Test
    @Transactional
    void getAllSalonsByPeopleWaitingIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where peopleWaiting is less than
        defaultSalonFiltering("peopleWaiting.lessThan=" + UPDATED_PEOPLE_WAITING, "peopleWaiting.lessThan=" + DEFAULT_PEOPLE_WAITING);
    }

    @Test
    @Transactional
    void getAllSalonsByPeopleWaitingIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where peopleWaiting is greater than
        defaultSalonFiltering("peopleWaiting.greaterThan=" + SMALLER_PEOPLE_WAITING, "peopleWaiting.greaterThan=" + DEFAULT_PEOPLE_WAITING);
    }

    @Test
    @Transactional
    void getAllSalonsByAvatarUrlIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where avatarUrl equals to
        defaultSalonFiltering("avatarUrl.equals=" + DEFAULT_AVATAR_URL, "avatarUrl.equals=" + UPDATED_AVATAR_URL);
    }

    @Test
    @Transactional
    void getAllSalonsByAvatarUrlIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where avatarUrl in
        defaultSalonFiltering("avatarUrl.in=" + DEFAULT_AVATAR_URL + "," + UPDATED_AVATAR_URL, "avatarUrl.in=" + UPDATED_AVATAR_URL);
    }

    @Test
    @Transactional
    void getAllSalonsByAvatarUrlIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where avatarUrl is not null
        defaultSalonFiltering("avatarUrl.specified=true", "avatarUrl.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByAvatarUrlContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where avatarUrl contains
        defaultSalonFiltering("avatarUrl.contains=" + DEFAULT_AVATAR_URL, "avatarUrl.contains=" + UPDATED_AVATAR_URL);
    }

    @Test
    @Transactional
    void getAllSalonsByAvatarUrlNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where avatarUrl does not contain
        defaultSalonFiltering("avatarUrl.doesNotContain=" + UPDATED_AVATAR_URL, "avatarUrl.doesNotContain=" + DEFAULT_AVATAR_URL);
    }

    @Test
    @Transactional
    void getAllSalonsByCoverUrlIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where coverUrl equals to
        defaultSalonFiltering("coverUrl.equals=" + DEFAULT_COVER_URL, "coverUrl.equals=" + UPDATED_COVER_URL);
    }

    @Test
    @Transactional
    void getAllSalonsByCoverUrlIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where coverUrl in
        defaultSalonFiltering("coverUrl.in=" + DEFAULT_COVER_URL + "," + UPDATED_COVER_URL, "coverUrl.in=" + UPDATED_COVER_URL);
    }

    @Test
    @Transactional
    void getAllSalonsByCoverUrlIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where coverUrl is not null
        defaultSalonFiltering("coverUrl.specified=true", "coverUrl.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByCoverUrlContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where coverUrl contains
        defaultSalonFiltering("coverUrl.contains=" + DEFAULT_COVER_URL, "coverUrl.contains=" + UPDATED_COVER_URL);
    }

    @Test
    @Transactional
    void getAllSalonsByCoverUrlNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where coverUrl does not contain
        defaultSalonFiltering("coverUrl.doesNotContain=" + UPDATED_COVER_URL, "coverUrl.doesNotContain=" + DEFAULT_COVER_URL);
    }

    @Test
    @Transactional
    void getAllSalonsByLatitudeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where latitude equals to
        defaultSalonFiltering("latitude.equals=" + DEFAULT_LATITUDE, "latitude.equals=" + UPDATED_LATITUDE);
    }

    @Test
    @Transactional
    void getAllSalonsByLatitudeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where latitude in
        defaultSalonFiltering("latitude.in=" + DEFAULT_LATITUDE + "," + UPDATED_LATITUDE, "latitude.in=" + UPDATED_LATITUDE);
    }

    @Test
    @Transactional
    void getAllSalonsByLatitudeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where latitude is not null
        defaultSalonFiltering("latitude.specified=true", "latitude.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByLatitudeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where latitude is greater than or equal to
        defaultSalonFiltering("latitude.greaterThanOrEqual=" + DEFAULT_LATITUDE, "latitude.greaterThanOrEqual=" + UPDATED_LATITUDE);
    }

    @Test
    @Transactional
    void getAllSalonsByLatitudeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where latitude is less than or equal to
        defaultSalonFiltering("latitude.lessThanOrEqual=" + DEFAULT_LATITUDE, "latitude.lessThanOrEqual=" + SMALLER_LATITUDE);
    }

    @Test
    @Transactional
    void getAllSalonsByLatitudeIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where latitude is less than
        defaultSalonFiltering("latitude.lessThan=" + UPDATED_LATITUDE, "latitude.lessThan=" + DEFAULT_LATITUDE);
    }

    @Test
    @Transactional
    void getAllSalonsByLatitudeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where latitude is greater than
        defaultSalonFiltering("latitude.greaterThan=" + SMALLER_LATITUDE, "latitude.greaterThan=" + DEFAULT_LATITUDE);
    }

    @Test
    @Transactional
    void getAllSalonsByLongitudeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where longitude equals to
        defaultSalonFiltering("longitude.equals=" + DEFAULT_LONGITUDE, "longitude.equals=" + UPDATED_LONGITUDE);
    }

    @Test
    @Transactional
    void getAllSalonsByLongitudeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where longitude in
        defaultSalonFiltering("longitude.in=" + DEFAULT_LONGITUDE + "," + UPDATED_LONGITUDE, "longitude.in=" + UPDATED_LONGITUDE);
    }

    @Test
    @Transactional
    void getAllSalonsByLongitudeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where longitude is not null
        defaultSalonFiltering("longitude.specified=true", "longitude.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByLongitudeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where longitude is greater than or equal to
        defaultSalonFiltering("longitude.greaterThanOrEqual=" + DEFAULT_LONGITUDE, "longitude.greaterThanOrEqual=" + UPDATED_LONGITUDE);
    }

    @Test
    @Transactional
    void getAllSalonsByLongitudeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where longitude is less than or equal to
        defaultSalonFiltering("longitude.lessThanOrEqual=" + DEFAULT_LONGITUDE, "longitude.lessThanOrEqual=" + SMALLER_LONGITUDE);
    }

    @Test
    @Transactional
    void getAllSalonsByLongitudeIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where longitude is less than
        defaultSalonFiltering("longitude.lessThan=" + UPDATED_LONGITUDE, "longitude.lessThan=" + DEFAULT_LONGITUDE);
    }

    @Test
    @Transactional
    void getAllSalonsByLongitudeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where longitude is greater than
        defaultSalonFiltering("longitude.greaterThan=" + SMALLER_LONGITUDE, "longitude.greaterThan=" + DEFAULT_LONGITUDE);
    }

    @Test
    @Transactional
    void getAllSalonsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where active equals to
        defaultSalonFiltering("active.equals=" + DEFAULT_ACTIVE, "active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllSalonsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where active in
        defaultSalonFiltering("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE, "active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllSalonsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where active is not null
        defaultSalonFiltering("active.specified=true", "active.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByCreatedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where createdDate equals to
        defaultSalonFiltering("createdDate.equals=" + DEFAULT_CREATED_DATE, "createdDate.equals=" + UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void getAllSalonsByCreatedDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where createdDate in
        defaultSalonFiltering(
            "createdDate.in=" + DEFAULT_CREATED_DATE + "," + UPDATED_CREATED_DATE,
            "createdDate.in=" + UPDATED_CREATED_DATE
        );
    }

    @Test
    @Transactional
    void getAllSalonsByCreatedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where createdDate is not null
        defaultSalonFiltering("createdDate.specified=true", "createdDate.specified=false");
    }

    @Test
    @Transactional
    void getAllSalonsByLastModifiedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where lastModifiedDate equals to
        defaultSalonFiltering(
            "lastModifiedDate.equals=" + DEFAULT_LAST_MODIFIED_DATE,
            "lastModifiedDate.equals=" + UPDATED_LAST_MODIFIED_DATE
        );
    }

    @Test
    @Transactional
    void getAllSalonsByLastModifiedDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where lastModifiedDate in
        defaultSalonFiltering(
            "lastModifiedDate.in=" + DEFAULT_LAST_MODIFIED_DATE + "," + UPDATED_LAST_MODIFIED_DATE,
            "lastModifiedDate.in=" + UPDATED_LAST_MODIFIED_DATE
        );
    }

    @Test
    @Transactional
    void getAllSalonsByLastModifiedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        // Get all the salonList where lastModifiedDate is not null
        defaultSalonFiltering("lastModifiedDate.specified=true", "lastModifiedDate.specified=false");
    }

    private void defaultSalonFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultSalonShouldBeFound(shouldBeFound);
        defaultSalonShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSalonShouldBeFound(String filter) throws Exception {
        restSalonMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(salon.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].slug").value(hasItem(DEFAULT_SLUG)))
            .andExpect(jsonPath("$.[*].location").value(hasItem(DEFAULT_LOCATION)))
            .andExpect(jsonPath("$.[*].district").value(hasItem(DEFAULT_DISTRICT)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].openingHours").value(hasItem(DEFAULT_OPENING_HOURS)))
            .andExpect(jsonPath("$.[*].estimatedWaitMinutes").value(hasItem(DEFAULT_ESTIMATED_WAIT_MINUTES)))
            .andExpect(jsonPath("$.[*].peopleWaiting").value(hasItem(DEFAULT_PEOPLE_WAITING)))
            .andExpect(jsonPath("$.[*].avatarUrl").value(hasItem(DEFAULT_AVATAR_URL)))
            .andExpect(jsonPath("$.[*].coverUrl").value(hasItem(DEFAULT_COVER_URL)))
            .andExpect(jsonPath("$.[*].latitude").value(hasItem(DEFAULT_LATITUDE)))
            .andExpect(jsonPath("$.[*].longitude").value(hasItem(DEFAULT_LONGITUDE)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(hasItem(DEFAULT_LAST_MODIFIED_DATE.toString())));

        // Check, that the count call also returns 1
        restSalonMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSalonShouldNotBeFound(String filter) throws Exception {
        restSalonMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSalonMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingSalon() throws Exception {
        // Get the salon
        restSalonMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSalon() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the salon
        Salon updatedSalon = salonRepository.findById(salon.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSalon are not directly saved in db
        em.detach(updatedSalon);
        updatedSalon
            .name(UPDATED_NAME)
            .slug(UPDATED_SLUG)
            .location(UPDATED_LOCATION)
            .district(UPDATED_DISTRICT)
            .address(UPDATED_ADDRESS)
            .status(UPDATED_STATUS)
            .phone(UPDATED_PHONE)
            .openingHours(UPDATED_OPENING_HOURS)
            .estimatedWaitMinutes(UPDATED_ESTIMATED_WAIT_MINUTES)
            .peopleWaiting(UPDATED_PEOPLE_WAITING)
            .avatarUrl(UPDATED_AVATAR_URL)
            .coverUrl(UPDATED_COVER_URL)
            .latitude(UPDATED_LATITUDE)
            .longitude(UPDATED_LONGITUDE)
            .active(UPDATED_ACTIVE)
            .createdDate(UPDATED_CREATED_DATE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE);
        SalonDTO salonDTO = salonMapper.toDto(updatedSalon);

        restSalonMockMvc
            .perform(
                put(ENTITY_API_URL_ID, salonDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(salonDTO))
            )
            .andExpect(status().isOk());

        // Validate the Salon in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSalonToMatchAllProperties(updatedSalon);
    }

    @Test
    @Transactional
    void putNonExistingSalon() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        salon.setId(longCount.incrementAndGet());

        // Create the Salon
        SalonDTO salonDTO = salonMapper.toDto(salon);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSalonMockMvc
            .perform(
                put(ENTITY_API_URL_ID, salonDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(salonDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Salon in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSalon() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        salon.setId(longCount.incrementAndGet());

        // Create the Salon
        SalonDTO salonDTO = salonMapper.toDto(salon);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSalonMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(salonDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Salon in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSalon() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        salon.setId(longCount.incrementAndGet());

        // Create the Salon
        SalonDTO salonDTO = salonMapper.toDto(salon);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSalonMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(salonDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Salon in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSalonWithPatch() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the salon using partial update
        Salon partialUpdatedSalon = new Salon();
        partialUpdatedSalon.setId(salon.getId());

        partialUpdatedSalon
            .slug(UPDATED_SLUG)
            .location(UPDATED_LOCATION)
            .estimatedWaitMinutes(UPDATED_ESTIMATED_WAIT_MINUTES)
            .avatarUrl(UPDATED_AVATAR_URL)
            .latitude(UPDATED_LATITUDE)
            .longitude(UPDATED_LONGITUDE)
            .active(UPDATED_ACTIVE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE);

        restSalonMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSalon.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSalon))
            )
            .andExpect(status().isOk());

        // Validate the Salon in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSalonUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedSalon, salon), getPersistedSalon(salon));
    }

    @Test
    @Transactional
    void fullUpdateSalonWithPatch() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the salon using partial update
        Salon partialUpdatedSalon = new Salon();
        partialUpdatedSalon.setId(salon.getId());

        partialUpdatedSalon
            .name(UPDATED_NAME)
            .slug(UPDATED_SLUG)
            .location(UPDATED_LOCATION)
            .district(UPDATED_DISTRICT)
            .address(UPDATED_ADDRESS)
            .status(UPDATED_STATUS)
            .phone(UPDATED_PHONE)
            .openingHours(UPDATED_OPENING_HOURS)
            .estimatedWaitMinutes(UPDATED_ESTIMATED_WAIT_MINUTES)
            .peopleWaiting(UPDATED_PEOPLE_WAITING)
            .avatarUrl(UPDATED_AVATAR_URL)
            .coverUrl(UPDATED_COVER_URL)
            .latitude(UPDATED_LATITUDE)
            .longitude(UPDATED_LONGITUDE)
            .active(UPDATED_ACTIVE)
            .createdDate(UPDATED_CREATED_DATE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE);

        restSalonMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSalon.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSalon))
            )
            .andExpect(status().isOk());

        // Validate the Salon in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSalonUpdatableFieldsEquals(partialUpdatedSalon, getPersistedSalon(partialUpdatedSalon));
    }

    @Test
    @Transactional
    void patchNonExistingSalon() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        salon.setId(longCount.incrementAndGet());

        // Create the Salon
        SalonDTO salonDTO = salonMapper.toDto(salon);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSalonMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, salonDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(salonDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Salon in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSalon() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        salon.setId(longCount.incrementAndGet());

        // Create the Salon
        SalonDTO salonDTO = salonMapper.toDto(salon);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSalonMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(salonDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Salon in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSalon() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        salon.setId(longCount.incrementAndGet());

        // Create the Salon
        SalonDTO salonDTO = salonMapper.toDto(salon);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSalonMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(salonDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Salon in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSalon() throws Exception {
        // Initialize the database
        insertedSalon = salonRepository.saveAndFlush(salon);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the salon
        restSalonMockMvc
            .perform(delete(ENTITY_API_URL_ID, salon.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return salonRepository.count();
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

    protected Salon getPersistedSalon(Salon salon) {
        return salonRepository.findById(salon.getId()).orElseThrow();
    }

    protected void assertPersistedSalonToMatchAllProperties(Salon expectedSalon) {
        assertSalonAllPropertiesEquals(expectedSalon, getPersistedSalon(expectedSalon));
    }

    protected void assertPersistedSalonToMatchUpdatableProperties(Salon expectedSalon) {
        assertSalonAllUpdatablePropertiesEquals(expectedSalon, getPersistedSalon(expectedSalon));
    }
}
