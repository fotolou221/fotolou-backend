package com.fotolou.app.web.rest;

import static com.fotolou.app.domain.AppNotificationAsserts.*;
import static com.fotolou.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fotolou.app.IntegrationTest;
import com.fotolou.app.domain.AppNotification;
import com.fotolou.app.domain.User;
import com.fotolou.app.domain.enumeration.NotificationType;
import com.fotolou.app.domain.enumeration.RecipientRole;
import com.fotolou.app.repository.AppNotificationRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.service.dto.AppNotificationDTO;
import com.fotolou.app.service.mapper.AppNotificationMapper;
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
 * Integration tests for the {@link AppNotificationResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AppNotificationResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_MESSAGE = "BBBBBBBBBB";

    private static final NotificationType DEFAULT_TYPE = NotificationType.TICKET;
    private static final NotificationType UPDATED_TYPE = NotificationType.ORDER;

    private static final RecipientRole DEFAULT_RECIPIENT_ROLE = RecipientRole.CLIENT;
    private static final RecipientRole UPDATED_RECIPIENT_ROLE = RecipientRole.COIFFEUR;

    private static final Boolean DEFAULT_IS_READ = false;
    private static final Boolean UPDATED_IS_READ = true;

    private static final String DEFAULT_TARGET_ROUTE = "AAAAAAAAAA";
    private static final String UPDATED_TARGET_ROUTE = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.ofEpochMilli(1787419709034L);

    private static final String ENTITY_API_URL = "/api/app-notifications";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AppNotificationRepository appNotificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppNotificationMapper appNotificationMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAppNotificationMockMvc;

    private AppNotification appNotification;

    private AppNotification insertedAppNotification;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AppNotification createEntity() {
        return new AppNotification()
            .title(DEFAULT_TITLE)
            .message(DEFAULT_MESSAGE)
            .type(DEFAULT_TYPE)
            .recipientRole(DEFAULT_RECIPIENT_ROLE)
            .isRead(DEFAULT_IS_READ)
            .targetRoute(DEFAULT_TARGET_ROUTE)
            .createdDate(DEFAULT_CREATED_DATE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AppNotification createUpdatedEntity() {
        return new AppNotification()
            .title(UPDATED_TITLE)
            .message(UPDATED_MESSAGE)
            .type(UPDATED_TYPE)
            .recipientRole(UPDATED_RECIPIENT_ROLE)
            .isRead(UPDATED_IS_READ)
            .targetRoute(UPDATED_TARGET_ROUTE)
            .createdDate(UPDATED_CREATED_DATE);
    }

    @BeforeEach
    void initTest() {
        appNotification = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAppNotification != null) {
            appNotificationRepository.delete(insertedAppNotification);
            insertedAppNotification = null;
        }
    }

    @Test
    @Transactional
    void createAppNotification() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the AppNotification
        AppNotificationDTO appNotificationDTO = appNotificationMapper.toDto(appNotification);
        var returnedAppNotificationDTO = om.readValue(
            restAppNotificationMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appNotificationDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AppNotificationDTO.class
        );

        // Validate the AppNotification in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAppNotification = appNotificationMapper.toEntity(returnedAppNotificationDTO);
        assertAppNotificationUpdatableFieldsEquals(returnedAppNotification, getPersistedAppNotification(returnedAppNotification));

        insertedAppNotification = returnedAppNotification;
    }

    @Test
    @Transactional
    void createAppNotificationWithExistingId() throws Exception {
        // Create the AppNotification with an existing ID
        appNotification.setId(1L);
        AppNotificationDTO appNotificationDTO = appNotificationMapper.toDto(appNotification);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAppNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appNotificationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AppNotification in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        appNotification.setTitle(null);

        // Create the AppNotification, which fails.
        AppNotificationDTO appNotificationDTO = appNotificationMapper.toDto(appNotification);

        restAppNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appNotificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        appNotification.setType(null);

        // Create the AppNotification, which fails.
        AppNotificationDTO appNotificationDTO = appNotificationMapper.toDto(appNotification);

        restAppNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appNotificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRecipientRoleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        appNotification.setRecipientRole(null);

        // Create the AppNotification, which fails.
        AppNotificationDTO appNotificationDTO = appNotificationMapper.toDto(appNotification);

        restAppNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appNotificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkIsReadIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        appNotification.setIsRead(null);

        // Create the AppNotification, which fails.
        AppNotificationDTO appNotificationDTO = appNotificationMapper.toDto(appNotification);

        restAppNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appNotificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        appNotification.setCreatedDate(null);

        // Create the AppNotification, which fails.
        AppNotificationDTO appNotificationDTO = appNotificationMapper.toDto(appNotification);

        restAppNotificationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appNotificationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAppNotifications() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList
        restAppNotificationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appNotification.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].recipientRole").value(hasItem(DEFAULT_RECIPIENT_ROLE.toString())))
            .andExpect(jsonPath("$.[*].isRead").value(hasItem(DEFAULT_IS_READ)))
            .andExpect(jsonPath("$.[*].targetRoute").value(hasItem(DEFAULT_TARGET_ROUTE)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())));
    }

    @Test
    @Transactional
    void getAppNotification() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get the appNotification
        restAppNotificationMockMvc
            .perform(get(ENTITY_API_URL_ID, appNotification.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(appNotification.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.message").value(DEFAULT_MESSAGE))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.recipientRole").value(DEFAULT_RECIPIENT_ROLE.toString()))
            .andExpect(jsonPath("$.isRead").value(DEFAULT_IS_READ))
            .andExpect(jsonPath("$.targetRoute").value(DEFAULT_TARGET_ROUTE))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()));
    }

    @Test
    @Transactional
    void getAppNotificationsByIdFiltering() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        Long id = appNotification.getId();

        defaultAppNotificationFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultAppNotificationFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultAppNotificationFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAppNotificationsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where title equals to
        defaultAppNotificationFiltering("title.equals=" + DEFAULT_TITLE, "title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllAppNotificationsByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where title in
        defaultAppNotificationFiltering("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE, "title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllAppNotificationsByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where title is not null
        defaultAppNotificationFiltering("title.specified=true", "title.specified=false");
    }

    @Test
    @Transactional
    void getAllAppNotificationsByTitleContainsSomething() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where title contains
        defaultAppNotificationFiltering("title.contains=" + DEFAULT_TITLE, "title.contains=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllAppNotificationsByTitleNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where title does not contain
        defaultAppNotificationFiltering("title.doesNotContain=" + UPDATED_TITLE, "title.doesNotContain=" + DEFAULT_TITLE);
    }

    @Test
    @Transactional
    void getAllAppNotificationsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where type equals to
        defaultAppNotificationFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllAppNotificationsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where type in
        defaultAppNotificationFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllAppNotificationsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where type is not null
        defaultAppNotificationFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    @Transactional
    void getAllAppNotificationsByRecipientRoleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where recipientRole equals to
        defaultAppNotificationFiltering("recipientRole.equals=" + DEFAULT_RECIPIENT_ROLE, "recipientRole.equals=" + UPDATED_RECIPIENT_ROLE);
    }

    @Test
    @Transactional
    void getAllAppNotificationsByRecipientRoleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where recipientRole in
        defaultAppNotificationFiltering(
            "recipientRole.in=" + DEFAULT_RECIPIENT_ROLE + "," + UPDATED_RECIPIENT_ROLE,
            "recipientRole.in=" + UPDATED_RECIPIENT_ROLE
        );
    }

    @Test
    @Transactional
    void getAllAppNotificationsByRecipientRoleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where recipientRole is not null
        defaultAppNotificationFiltering("recipientRole.specified=true", "recipientRole.specified=false");
    }

    @Test
    @Transactional
    void getAllAppNotificationsByIsReadIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where isRead equals to
        defaultAppNotificationFiltering("isRead.equals=" + DEFAULT_IS_READ, "isRead.equals=" + UPDATED_IS_READ);
    }

    @Test
    @Transactional
    void getAllAppNotificationsByIsReadIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where isRead in
        defaultAppNotificationFiltering("isRead.in=" + DEFAULT_IS_READ + "," + UPDATED_IS_READ, "isRead.in=" + UPDATED_IS_READ);
    }

    @Test
    @Transactional
    void getAllAppNotificationsByIsReadIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where isRead is not null
        defaultAppNotificationFiltering("isRead.specified=true", "isRead.specified=false");
    }

    @Test
    @Transactional
    void getAllAppNotificationsByTargetRouteIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where targetRoute equals to
        defaultAppNotificationFiltering("targetRoute.equals=" + DEFAULT_TARGET_ROUTE, "targetRoute.equals=" + UPDATED_TARGET_ROUTE);
    }

    @Test
    @Transactional
    void getAllAppNotificationsByTargetRouteIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where targetRoute in
        defaultAppNotificationFiltering(
            "targetRoute.in=" + DEFAULT_TARGET_ROUTE + "," + UPDATED_TARGET_ROUTE,
            "targetRoute.in=" + UPDATED_TARGET_ROUTE
        );
    }

    @Test
    @Transactional
    void getAllAppNotificationsByTargetRouteIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where targetRoute is not null
        defaultAppNotificationFiltering("targetRoute.specified=true", "targetRoute.specified=false");
    }

    @Test
    @Transactional
    void getAllAppNotificationsByTargetRouteContainsSomething() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where targetRoute contains
        defaultAppNotificationFiltering("targetRoute.contains=" + DEFAULT_TARGET_ROUTE, "targetRoute.contains=" + UPDATED_TARGET_ROUTE);
    }

    @Test
    @Transactional
    void getAllAppNotificationsByTargetRouteNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where targetRoute does not contain
        defaultAppNotificationFiltering(
            "targetRoute.doesNotContain=" + UPDATED_TARGET_ROUTE,
            "targetRoute.doesNotContain=" + DEFAULT_TARGET_ROUTE
        );
    }

    @Test
    @Transactional
    void getAllAppNotificationsByCreatedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where createdDate equals to
        defaultAppNotificationFiltering("createdDate.equals=" + DEFAULT_CREATED_DATE, "createdDate.equals=" + UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void getAllAppNotificationsByCreatedDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where createdDate in
        defaultAppNotificationFiltering(
            "createdDate.in=" + DEFAULT_CREATED_DATE + "," + UPDATED_CREATED_DATE,
            "createdDate.in=" + UPDATED_CREATED_DATE
        );
    }

    @Test
    @Transactional
    void getAllAppNotificationsByCreatedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        // Get all the appNotificationList where createdDate is not null
        defaultAppNotificationFiltering("createdDate.specified=true", "createdDate.specified=false");
    }

    @Test
    @Transactional
    void getAllAppNotificationsByUserIsEqualToSomething() throws Exception {
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            appNotificationRepository.saveAndFlush(appNotification);
            user = UserResourceIT.createEntity();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        appNotification.setUser(user);
        appNotificationRepository.saveAndFlush(appNotification);
        Long userId = user.getId();
        // Get all the appNotificationList where user equals to userId
        defaultAppNotificationShouldBeFound("userId.equals=" + userId);

        // Get all the appNotificationList where user equals to (userId + 1)
        defaultAppNotificationShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    private void defaultAppNotificationFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultAppNotificationShouldBeFound(shouldBeFound);
        defaultAppNotificationShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAppNotificationShouldBeFound(String filter) throws Exception {
        restAppNotificationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(appNotification.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].recipientRole").value(hasItem(DEFAULT_RECIPIENT_ROLE.toString())))
            .andExpect(jsonPath("$.[*].isRead").value(hasItem(DEFAULT_IS_READ)))
            .andExpect(jsonPath("$.[*].targetRoute").value(hasItem(DEFAULT_TARGET_ROUTE)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())));

        // Check, that the count call also returns 1
        restAppNotificationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAppNotificationShouldNotBeFound(String filter) throws Exception {
        restAppNotificationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAppNotificationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAppNotification() throws Exception {
        // Get the appNotification
        restAppNotificationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAppNotification() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the appNotification
        AppNotification updatedAppNotification = appNotificationRepository.findById(appNotification.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAppNotification are not directly saved in db
        em.detach(updatedAppNotification);
        updatedAppNotification
            .title(UPDATED_TITLE)
            .message(UPDATED_MESSAGE)
            .type(UPDATED_TYPE)
            .recipientRole(UPDATED_RECIPIENT_ROLE)
            .isRead(UPDATED_IS_READ)
            .targetRoute(UPDATED_TARGET_ROUTE)
            .createdDate(UPDATED_CREATED_DATE);
        AppNotificationDTO appNotificationDTO = appNotificationMapper.toDto(updatedAppNotification);

        restAppNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, appNotificationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(appNotificationDTO))
            )
            .andExpect(status().isOk());

        // Validate the AppNotification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAppNotificationToMatchAllProperties(updatedAppNotification);
    }

    @Test
    @Transactional
    void putNonExistingAppNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        appNotification.setId(longCount.incrementAndGet());

        // Create the AppNotification
        AppNotificationDTO appNotificationDTO = appNotificationMapper.toDto(appNotification);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAppNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, appNotificationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(appNotificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AppNotification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAppNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        appNotification.setId(longCount.incrementAndGet());

        // Create the AppNotification
        AppNotificationDTO appNotificationDTO = appNotificationMapper.toDto(appNotification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppNotificationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(appNotificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AppNotification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAppNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        appNotification.setId(longCount.incrementAndGet());

        // Create the AppNotification
        AppNotificationDTO appNotificationDTO = appNotificationMapper.toDto(appNotification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppNotificationMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(appNotificationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AppNotification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAppNotificationWithPatch() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the appNotification using partial update
        AppNotification partialUpdatedAppNotification = new AppNotification();
        partialUpdatedAppNotification.setId(appNotification.getId());

        partialUpdatedAppNotification
            .message(UPDATED_MESSAGE)
            .type(UPDATED_TYPE)
            .recipientRole(UPDATED_RECIPIENT_ROLE)
            .createdDate(UPDATED_CREATED_DATE);

        restAppNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAppNotification.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAppNotification))
            )
            .andExpect(status().isOk());

        // Validate the AppNotification in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAppNotificationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAppNotification, appNotification),
            getPersistedAppNotification(appNotification)
        );
    }

    @Test
    @Transactional
    void fullUpdateAppNotificationWithPatch() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the appNotification using partial update
        AppNotification partialUpdatedAppNotification = new AppNotification();
        partialUpdatedAppNotification.setId(appNotification.getId());

        partialUpdatedAppNotification
            .title(UPDATED_TITLE)
            .message(UPDATED_MESSAGE)
            .type(UPDATED_TYPE)
            .recipientRole(UPDATED_RECIPIENT_ROLE)
            .isRead(UPDATED_IS_READ)
            .targetRoute(UPDATED_TARGET_ROUTE)
            .createdDate(UPDATED_CREATED_DATE);

        restAppNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAppNotification.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAppNotification))
            )
            .andExpect(status().isOk());

        // Validate the AppNotification in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAppNotificationUpdatableFieldsEquals(
            partialUpdatedAppNotification,
            getPersistedAppNotification(partialUpdatedAppNotification)
        );
    }

    @Test
    @Transactional
    void patchNonExistingAppNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        appNotification.setId(longCount.incrementAndGet());

        // Create the AppNotification
        AppNotificationDTO appNotificationDTO = appNotificationMapper.toDto(appNotification);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAppNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, appNotificationDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(appNotificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AppNotification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAppNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        appNotification.setId(longCount.incrementAndGet());

        // Create the AppNotification
        AppNotificationDTO appNotificationDTO = appNotificationMapper.toDto(appNotification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppNotificationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(appNotificationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AppNotification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAppNotification() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        appNotification.setId(longCount.incrementAndGet());

        // Create the AppNotification
        AppNotificationDTO appNotificationDTO = appNotificationMapper.toDto(appNotification);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAppNotificationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(appNotificationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AppNotification in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAppNotification() throws Exception {
        // Initialize the database
        insertedAppNotification = appNotificationRepository.saveAndFlush(appNotification);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the appNotification
        restAppNotificationMockMvc
            .perform(delete(ENTITY_API_URL_ID, appNotification.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return appNotificationRepository.count();
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

    protected AppNotification getPersistedAppNotification(AppNotification appNotification) {
        return appNotificationRepository.findById(appNotification.getId()).orElseThrow();
    }

    protected void assertPersistedAppNotificationToMatchAllProperties(AppNotification expectedAppNotification) {
        assertAppNotificationAllPropertiesEquals(expectedAppNotification, getPersistedAppNotification(expectedAppNotification));
    }

    protected void assertPersistedAppNotificationToMatchUpdatableProperties(AppNotification expectedAppNotification) {
        assertAppNotificationAllUpdatablePropertiesEquals(expectedAppNotification, getPersistedAppNotification(expectedAppNotification));
    }
}
