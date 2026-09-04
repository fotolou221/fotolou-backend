package com.fotolou.app.web.rest;

import static com.fotolou.app.domain.TicketAsserts.*;
import static com.fotolou.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fotolou.app.IntegrationTest;
import com.fotolou.app.domain.Salon;
import com.fotolou.app.domain.Ticket;
import com.fotolou.app.domain.User;
import com.fotolou.app.domain.enumeration.TicketCategory;
import com.fotolou.app.domain.enumeration.TicketOwnerType;
import com.fotolou.app.domain.enumeration.TicketStatus;
import com.fotolou.app.repository.TicketRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.service.TicketService;
import com.fotolou.app.service.dto.TicketDTO;
import com.fotolou.app.service.mapper.TicketMapper;
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
 * Integration tests for the {@link TicketResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class TicketResourceIT {

    private static final Integer DEFAULT_TICKET_NUMBER = 1;
    private static final Integer UPDATED_TICKET_NUMBER = 2;
    private static final Integer SMALLER_TICKET_NUMBER = 1 - 1;

    private static final String DEFAULT_OWNER_NAME = "AAAAAAAAAA";
    private static final String UPDATED_OWNER_NAME = "BBBBBBBBBB";

    private static final TicketOwnerType DEFAULT_OWNER_TYPE = TicketOwnerType.SELF;
    private static final TicketOwnerType UPDATED_OWNER_TYPE = TicketOwnerType.RELATIVE;

    private static final TicketStatus DEFAULT_STATUS = TicketStatus.WAITING;
    private static final TicketStatus UPDATED_STATUS = TicketStatus.YOUR_TURN;

    private static final TicketCategory DEFAULT_CATEGORY = TicketCategory.ACTIVE;
    private static final TicketCategory UPDATED_CATEGORY = TicketCategory.HISTORY;

    private static final Integer DEFAULT_PEOPLE_AHEAD = 0;
    private static final Integer UPDATED_PEOPLE_AHEAD = 1;
    private static final Integer SMALLER_PEOPLE_AHEAD = 0 - 1;

    private static final Integer DEFAULT_ESTIMATED_WAIT_MINUTES = 0;
    private static final Integer UPDATED_ESTIMATED_WAIT_MINUTES = 1;
    private static final Integer SMALLER_ESTIMATED_WAIT_MINUTES = 0 - 1;

    private static final Integer DEFAULT_ITEM_COUNT = 1;
    private static final Integer UPDATED_ITEM_COUNT = 2;
    private static final Integer SMALLER_ITEM_COUNT = 1 - 1;

    private static final Instant DEFAULT_SERVED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_SERVED_AT = Instant.ofEpochMilli(1787419709034L);

    private static final Instant DEFAULT_CANCELLED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CANCELLED_AT = Instant.ofEpochMilli(1787419709034L);

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.ofEpochMilli(1787419709034L);

    private static final Instant DEFAULT_LAST_MODIFIED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_MODIFIED_DATE = Instant.ofEpochMilli(1787419709034L);

    private static final String ENTITY_API_URL = "/api/tickets";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepositoryMock;

    @Autowired
    private TicketMapper ticketMapper;

    @Mock
    private TicketService ticketServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTicketMockMvc;

    private Ticket ticket;

    private Ticket insertedTicket;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ticket createEntity(EntityManager em) {
        Ticket ticket = new Ticket()
            .ticketNumber(DEFAULT_TICKET_NUMBER)
            .ownerName(DEFAULT_OWNER_NAME)
            .ownerType(DEFAULT_OWNER_TYPE)
            .status(DEFAULT_STATUS)
            .category(DEFAULT_CATEGORY)
            .peopleAhead(DEFAULT_PEOPLE_AHEAD)
            .estimatedWaitMinutes(DEFAULT_ESTIMATED_WAIT_MINUTES)
            .itemCount(DEFAULT_ITEM_COUNT)
            .servedAt(DEFAULT_SERVED_AT)
            .cancelledAt(DEFAULT_CANCELLED_AT)
            .createdDate(DEFAULT_CREATED_DATE)
            .lastModifiedDate(DEFAULT_LAST_MODIFIED_DATE);
        // Add required entity
        Salon salon;
        if (TestUtil.findAll(em, Salon.class).isEmpty()) {
            salon = SalonResourceIT.createEntity();
            em.persist(salon);
            em.flush();
        } else {
            salon = TestUtil.findAll(em, Salon.class).get(0);
        }
        ticket.setSalon(salon);
        return ticket;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ticket createUpdatedEntity(EntityManager em) {
        Ticket updatedTicket = new Ticket()
            .ticketNumber(UPDATED_TICKET_NUMBER)
            .ownerName(UPDATED_OWNER_NAME)
            .ownerType(UPDATED_OWNER_TYPE)
            .status(UPDATED_STATUS)
            .category(UPDATED_CATEGORY)
            .peopleAhead(UPDATED_PEOPLE_AHEAD)
            .estimatedWaitMinutes(UPDATED_ESTIMATED_WAIT_MINUTES)
            .itemCount(UPDATED_ITEM_COUNT)
            .servedAt(UPDATED_SERVED_AT)
            .cancelledAt(UPDATED_CANCELLED_AT)
            .createdDate(UPDATED_CREATED_DATE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE);
        // Add required entity
        Salon salon;
        if (TestUtil.findAll(em, Salon.class).isEmpty()) {
            salon = SalonResourceIT.createUpdatedEntity();
            em.persist(salon);
            em.flush();
        } else {
            salon = TestUtil.findAll(em, Salon.class).get(0);
        }
        updatedTicket.setSalon(salon);
        return updatedTicket;
    }

    @BeforeEach
    void initTest() {
        ticket = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedTicket != null) {
            ticketRepository.delete(insertedTicket);
            insertedTicket = null;
        }
    }

    @Test
    @Transactional
    void createTicket() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Ticket
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);
        var returnedTicketDTO = om.readValue(
            restTicketMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TicketDTO.class
        );

        // Validate the Ticket in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTicket = ticketMapper.toEntity(returnedTicketDTO);
        assertTicketUpdatableFieldsEquals(returnedTicket, getPersistedTicket(returnedTicket));

        insertedTicket = returnedTicket;
    }

    @Test
    @Transactional
    void createTicketWithExistingId() throws Exception {
        // Create the Ticket with an existing ID
        ticket.setId(1L);
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTicketMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTicketNumberIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ticket.setTicketNumber(null);

        // Create the Ticket, which fails.
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        restTicketMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOwnerNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ticket.setOwnerName(null);

        // Create the Ticket, which fails.
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        restTicketMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOwnerTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ticket.setOwnerType(null);

        // Create the Ticket, which fails.
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        restTicketMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ticket.setStatus(null);

        // Create the Ticket, which fails.
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        restTicketMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCategoryIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ticket.setCategory(null);

        // Create the Ticket, which fails.
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        restTicketMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        ticket.setCreatedDate(null);

        // Create the Ticket, which fails.
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        restTicketMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTickets() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList
        restTicketMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ticket.getId().intValue())))
            .andExpect(jsonPath("$.[*].ticketNumber").value(hasItem(DEFAULT_TICKET_NUMBER)))
            .andExpect(jsonPath("$.[*].ownerName").value(hasItem(DEFAULT_OWNER_NAME)))
            .andExpect(jsonPath("$.[*].ownerType").value(hasItem(DEFAULT_OWNER_TYPE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY.toString())))
            .andExpect(jsonPath("$.[*].peopleAhead").value(hasItem(DEFAULT_PEOPLE_AHEAD)))
            .andExpect(jsonPath("$.[*].estimatedWaitMinutes").value(hasItem(DEFAULT_ESTIMATED_WAIT_MINUTES)))
            .andExpect(jsonPath("$.[*].itemCount").value(hasItem(DEFAULT_ITEM_COUNT)))
            .andExpect(jsonPath("$.[*].servedAt").value(hasItem(DEFAULT_SERVED_AT.toString())))
            .andExpect(jsonPath("$.[*].cancelledAt").value(hasItem(DEFAULT_CANCELLED_AT.toString())))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(hasItem(DEFAULT_LAST_MODIFIED_DATE.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTicketsWithEagerRelationshipsIsEnabled() throws Exception {
        when(ticketServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTicketMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(ticketServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTicketsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(ticketServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTicketMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(ticketRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getTicket() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get the ticket
        restTicketMockMvc
            .perform(get(ENTITY_API_URL_ID, ticket.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(ticket.getId().intValue()))
            .andExpect(jsonPath("$.ticketNumber").value(DEFAULT_TICKET_NUMBER))
            .andExpect(jsonPath("$.ownerName").value(DEFAULT_OWNER_NAME))
            .andExpect(jsonPath("$.ownerType").value(DEFAULT_OWNER_TYPE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY.toString()))
            .andExpect(jsonPath("$.peopleAhead").value(DEFAULT_PEOPLE_AHEAD))
            .andExpect(jsonPath("$.estimatedWaitMinutes").value(DEFAULT_ESTIMATED_WAIT_MINUTES))
            .andExpect(jsonPath("$.itemCount").value(DEFAULT_ITEM_COUNT))
            .andExpect(jsonPath("$.servedAt").value(DEFAULT_SERVED_AT.toString()))
            .andExpect(jsonPath("$.cancelledAt").value(DEFAULT_CANCELLED_AT.toString()))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()))
            .andExpect(jsonPath("$.lastModifiedDate").value(DEFAULT_LAST_MODIFIED_DATE.toString()));
    }

    @Test
    @Transactional
    void getTicketsByIdFiltering() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        Long id = ticket.getId();

        defaultTicketFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultTicketFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultTicketFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllTicketsByTicketNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ticketNumber equals to
        defaultTicketFiltering("ticketNumber.equals=" + DEFAULT_TICKET_NUMBER, "ticketNumber.equals=" + UPDATED_TICKET_NUMBER);
    }

    @Test
    @Transactional
    void getAllTicketsByTicketNumberIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ticketNumber in
        defaultTicketFiltering(
            "ticketNumber.in=" + DEFAULT_TICKET_NUMBER + "," + UPDATED_TICKET_NUMBER,
            "ticketNumber.in=" + UPDATED_TICKET_NUMBER
        );
    }

    @Test
    @Transactional
    void getAllTicketsByTicketNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ticketNumber is not null
        defaultTicketFiltering("ticketNumber.specified=true", "ticketNumber.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByTicketNumberIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ticketNumber is greater than or equal to
        defaultTicketFiltering(
            "ticketNumber.greaterThanOrEqual=" + DEFAULT_TICKET_NUMBER,
            "ticketNumber.greaterThanOrEqual=" + UPDATED_TICKET_NUMBER
        );
    }

    @Test
    @Transactional
    void getAllTicketsByTicketNumberIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ticketNumber is less than or equal to
        defaultTicketFiltering(
            "ticketNumber.lessThanOrEqual=" + DEFAULT_TICKET_NUMBER,
            "ticketNumber.lessThanOrEqual=" + SMALLER_TICKET_NUMBER
        );
    }

    @Test
    @Transactional
    void getAllTicketsByTicketNumberIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ticketNumber is less than
        defaultTicketFiltering("ticketNumber.lessThan=" + UPDATED_TICKET_NUMBER, "ticketNumber.lessThan=" + DEFAULT_TICKET_NUMBER);
    }

    @Test
    @Transactional
    void getAllTicketsByTicketNumberIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ticketNumber is greater than
        defaultTicketFiltering("ticketNumber.greaterThan=" + SMALLER_TICKET_NUMBER, "ticketNumber.greaterThan=" + DEFAULT_TICKET_NUMBER);
    }

    @Test
    @Transactional
    void getAllTicketsByOwnerNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ownerName equals to
        defaultTicketFiltering("ownerName.equals=" + DEFAULT_OWNER_NAME, "ownerName.equals=" + UPDATED_OWNER_NAME);
    }

    @Test
    @Transactional
    void getAllTicketsByOwnerNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ownerName in
        defaultTicketFiltering("ownerName.in=" + DEFAULT_OWNER_NAME + "," + UPDATED_OWNER_NAME, "ownerName.in=" + UPDATED_OWNER_NAME);
    }

    @Test
    @Transactional
    void getAllTicketsByOwnerNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ownerName is not null
        defaultTicketFiltering("ownerName.specified=true", "ownerName.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByOwnerNameContainsSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ownerName contains
        defaultTicketFiltering("ownerName.contains=" + DEFAULT_OWNER_NAME, "ownerName.contains=" + UPDATED_OWNER_NAME);
    }

    @Test
    @Transactional
    void getAllTicketsByOwnerNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ownerName does not contain
        defaultTicketFiltering("ownerName.doesNotContain=" + UPDATED_OWNER_NAME, "ownerName.doesNotContain=" + DEFAULT_OWNER_NAME);
    }

    @Test
    @Transactional
    void getAllTicketsByOwnerTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ownerType equals to
        defaultTicketFiltering("ownerType.equals=" + DEFAULT_OWNER_TYPE, "ownerType.equals=" + UPDATED_OWNER_TYPE);
    }

    @Test
    @Transactional
    void getAllTicketsByOwnerTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ownerType in
        defaultTicketFiltering("ownerType.in=" + DEFAULT_OWNER_TYPE + "," + UPDATED_OWNER_TYPE, "ownerType.in=" + UPDATED_OWNER_TYPE);
    }

    @Test
    @Transactional
    void getAllTicketsByOwnerTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where ownerType is not null
        defaultTicketFiltering("ownerType.specified=true", "ownerType.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where status equals to
        defaultTicketFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllTicketsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where status in
        defaultTicketFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllTicketsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where status is not null
        defaultTicketFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByCategoryIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where category equals to
        defaultTicketFiltering("category.equals=" + DEFAULT_CATEGORY, "category.equals=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    void getAllTicketsByCategoryIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where category in
        defaultTicketFiltering("category.in=" + DEFAULT_CATEGORY + "," + UPDATED_CATEGORY, "category.in=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    void getAllTicketsByCategoryIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where category is not null
        defaultTicketFiltering("category.specified=true", "category.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByPeopleAheadIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where peopleAhead equals to
        defaultTicketFiltering("peopleAhead.equals=" + DEFAULT_PEOPLE_AHEAD, "peopleAhead.equals=" + UPDATED_PEOPLE_AHEAD);
    }

    @Test
    @Transactional
    void getAllTicketsByPeopleAheadIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where peopleAhead in
        defaultTicketFiltering(
            "peopleAhead.in=" + DEFAULT_PEOPLE_AHEAD + "," + UPDATED_PEOPLE_AHEAD,
            "peopleAhead.in=" + UPDATED_PEOPLE_AHEAD
        );
    }

    @Test
    @Transactional
    void getAllTicketsByPeopleAheadIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where peopleAhead is not null
        defaultTicketFiltering("peopleAhead.specified=true", "peopleAhead.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByPeopleAheadIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where peopleAhead is greater than or equal to
        defaultTicketFiltering(
            "peopleAhead.greaterThanOrEqual=" + DEFAULT_PEOPLE_AHEAD,
            "peopleAhead.greaterThanOrEqual=" + UPDATED_PEOPLE_AHEAD
        );
    }

    @Test
    @Transactional
    void getAllTicketsByPeopleAheadIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where peopleAhead is less than or equal to
        defaultTicketFiltering(
            "peopleAhead.lessThanOrEqual=" + DEFAULT_PEOPLE_AHEAD,
            "peopleAhead.lessThanOrEqual=" + SMALLER_PEOPLE_AHEAD
        );
    }

    @Test
    @Transactional
    void getAllTicketsByPeopleAheadIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where peopleAhead is less than
        defaultTicketFiltering("peopleAhead.lessThan=" + UPDATED_PEOPLE_AHEAD, "peopleAhead.lessThan=" + DEFAULT_PEOPLE_AHEAD);
    }

    @Test
    @Transactional
    void getAllTicketsByPeopleAheadIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where peopleAhead is greater than
        defaultTicketFiltering("peopleAhead.greaterThan=" + SMALLER_PEOPLE_AHEAD, "peopleAhead.greaterThan=" + DEFAULT_PEOPLE_AHEAD);
    }

    @Test
    @Transactional
    void getAllTicketsByEstimatedWaitMinutesIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where estimatedWaitMinutes equals to
        defaultTicketFiltering(
            "estimatedWaitMinutes.equals=" + DEFAULT_ESTIMATED_WAIT_MINUTES,
            "estimatedWaitMinutes.equals=" + UPDATED_ESTIMATED_WAIT_MINUTES
        );
    }

    @Test
    @Transactional
    void getAllTicketsByEstimatedWaitMinutesIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where estimatedWaitMinutes in
        defaultTicketFiltering(
            "estimatedWaitMinutes.in=" + DEFAULT_ESTIMATED_WAIT_MINUTES + "," + UPDATED_ESTIMATED_WAIT_MINUTES,
            "estimatedWaitMinutes.in=" + UPDATED_ESTIMATED_WAIT_MINUTES
        );
    }

    @Test
    @Transactional
    void getAllTicketsByEstimatedWaitMinutesIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where estimatedWaitMinutes is not null
        defaultTicketFiltering("estimatedWaitMinutes.specified=true", "estimatedWaitMinutes.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByEstimatedWaitMinutesIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where estimatedWaitMinutes is greater than or equal to
        defaultTicketFiltering(
            "estimatedWaitMinutes.greaterThanOrEqual=" + DEFAULT_ESTIMATED_WAIT_MINUTES,
            "estimatedWaitMinutes.greaterThanOrEqual=" + UPDATED_ESTIMATED_WAIT_MINUTES
        );
    }

    @Test
    @Transactional
    void getAllTicketsByEstimatedWaitMinutesIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where estimatedWaitMinutes is less than or equal to
        defaultTicketFiltering(
            "estimatedWaitMinutes.lessThanOrEqual=" + DEFAULT_ESTIMATED_WAIT_MINUTES,
            "estimatedWaitMinutes.lessThanOrEqual=" + SMALLER_ESTIMATED_WAIT_MINUTES
        );
    }

    @Test
    @Transactional
    void getAllTicketsByEstimatedWaitMinutesIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where estimatedWaitMinutes is less than
        defaultTicketFiltering(
            "estimatedWaitMinutes.lessThan=" + UPDATED_ESTIMATED_WAIT_MINUTES,
            "estimatedWaitMinutes.lessThan=" + DEFAULT_ESTIMATED_WAIT_MINUTES
        );
    }

    @Test
    @Transactional
    void getAllTicketsByEstimatedWaitMinutesIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where estimatedWaitMinutes is greater than
        defaultTicketFiltering(
            "estimatedWaitMinutes.greaterThan=" + SMALLER_ESTIMATED_WAIT_MINUTES,
            "estimatedWaitMinutes.greaterThan=" + DEFAULT_ESTIMATED_WAIT_MINUTES
        );
    }

    @Test
    @Transactional
    void getAllTicketsByItemCountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where itemCount equals to
        defaultTicketFiltering("itemCount.equals=" + DEFAULT_ITEM_COUNT, "itemCount.equals=" + UPDATED_ITEM_COUNT);
    }

    @Test
    @Transactional
    void getAllTicketsByItemCountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where itemCount in
        defaultTicketFiltering("itemCount.in=" + DEFAULT_ITEM_COUNT + "," + UPDATED_ITEM_COUNT, "itemCount.in=" + UPDATED_ITEM_COUNT);
    }

    @Test
    @Transactional
    void getAllTicketsByItemCountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where itemCount is not null
        defaultTicketFiltering("itemCount.specified=true", "itemCount.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByItemCountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where itemCount is greater than or equal to
        defaultTicketFiltering("itemCount.greaterThanOrEqual=" + DEFAULT_ITEM_COUNT, "itemCount.greaterThanOrEqual=" + UPDATED_ITEM_COUNT);
    }

    @Test
    @Transactional
    void getAllTicketsByItemCountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where itemCount is less than or equal to
        defaultTicketFiltering("itemCount.lessThanOrEqual=" + DEFAULT_ITEM_COUNT, "itemCount.lessThanOrEqual=" + SMALLER_ITEM_COUNT);
    }

    @Test
    @Transactional
    void getAllTicketsByItemCountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where itemCount is less than
        defaultTicketFiltering("itemCount.lessThan=" + UPDATED_ITEM_COUNT, "itemCount.lessThan=" + DEFAULT_ITEM_COUNT);
    }

    @Test
    @Transactional
    void getAllTicketsByItemCountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where itemCount is greater than
        defaultTicketFiltering("itemCount.greaterThan=" + SMALLER_ITEM_COUNT, "itemCount.greaterThan=" + DEFAULT_ITEM_COUNT);
    }

    @Test
    @Transactional
    void getAllTicketsByServedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where servedAt equals to
        defaultTicketFiltering("servedAt.equals=" + DEFAULT_SERVED_AT, "servedAt.equals=" + UPDATED_SERVED_AT);
    }

    @Test
    @Transactional
    void getAllTicketsByServedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where servedAt in
        defaultTicketFiltering("servedAt.in=" + DEFAULT_SERVED_AT + "," + UPDATED_SERVED_AT, "servedAt.in=" + UPDATED_SERVED_AT);
    }

    @Test
    @Transactional
    void getAllTicketsByServedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where servedAt is not null
        defaultTicketFiltering("servedAt.specified=true", "servedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByCancelledAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where cancelledAt equals to
        defaultTicketFiltering("cancelledAt.equals=" + DEFAULT_CANCELLED_AT, "cancelledAt.equals=" + UPDATED_CANCELLED_AT);
    }

    @Test
    @Transactional
    void getAllTicketsByCancelledAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where cancelledAt in
        defaultTicketFiltering(
            "cancelledAt.in=" + DEFAULT_CANCELLED_AT + "," + UPDATED_CANCELLED_AT,
            "cancelledAt.in=" + UPDATED_CANCELLED_AT
        );
    }

    @Test
    @Transactional
    void getAllTicketsByCancelledAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where cancelledAt is not null
        defaultTicketFiltering("cancelledAt.specified=true", "cancelledAt.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByCreatedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where createdDate equals to
        defaultTicketFiltering("createdDate.equals=" + DEFAULT_CREATED_DATE, "createdDate.equals=" + UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void getAllTicketsByCreatedDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where createdDate in
        defaultTicketFiltering(
            "createdDate.in=" + DEFAULT_CREATED_DATE + "," + UPDATED_CREATED_DATE,
            "createdDate.in=" + UPDATED_CREATED_DATE
        );
    }

    @Test
    @Transactional
    void getAllTicketsByCreatedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where createdDate is not null
        defaultTicketFiltering("createdDate.specified=true", "createdDate.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByLastModifiedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where lastModifiedDate equals to
        defaultTicketFiltering(
            "lastModifiedDate.equals=" + DEFAULT_LAST_MODIFIED_DATE,
            "lastModifiedDate.equals=" + UPDATED_LAST_MODIFIED_DATE
        );
    }

    @Test
    @Transactional
    void getAllTicketsByLastModifiedDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where lastModifiedDate in
        defaultTicketFiltering(
            "lastModifiedDate.in=" + DEFAULT_LAST_MODIFIED_DATE + "," + UPDATED_LAST_MODIFIED_DATE,
            "lastModifiedDate.in=" + UPDATED_LAST_MODIFIED_DATE
        );
    }

    @Test
    @Transactional
    void getAllTicketsByLastModifiedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        // Get all the ticketList where lastModifiedDate is not null
        defaultTicketFiltering("lastModifiedDate.specified=true", "lastModifiedDate.specified=false");
    }

    @Test
    @Transactional
    void getAllTicketsByUserIsEqualToSomething() throws Exception {
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            ticketRepository.saveAndFlush(ticket);
            user = UserResourceIT.createEntity();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        ticket.setUser(user);
        ticketRepository.saveAndFlush(ticket);
        Long userId = user.getId();
        // Get all the ticketList where user equals to userId
        defaultTicketShouldBeFound("userId.equals=" + userId);

        // Get all the ticketList where user equals to (userId + 1)
        defaultTicketShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    @Test
    @Transactional
    void getAllTicketsBySalonIsEqualToSomething() throws Exception {
        Salon salon;
        if (TestUtil.findAll(em, Salon.class).isEmpty()) {
            ticketRepository.saveAndFlush(ticket);
            salon = SalonResourceIT.createEntity();
        } else {
            salon = TestUtil.findAll(em, Salon.class).get(0);
        }
        em.persist(salon);
        em.flush();
        ticket.setSalon(salon);
        ticketRepository.saveAndFlush(ticket);
        Long salonId = salon.getId();
        // Get all the ticketList where salon equals to salonId
        defaultTicketShouldBeFound("salonId.equals=" + salonId);

        // Get all the ticketList where salon equals to (salonId + 1)
        defaultTicketShouldNotBeFound("salonId.equals=" + (salonId + 1));
    }

    private void defaultTicketFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultTicketShouldBeFound(shouldBeFound);
        defaultTicketShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultTicketShouldBeFound(String filter) throws Exception {
        restTicketMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ticket.getId().intValue())))
            .andExpect(jsonPath("$.[*].ticketNumber").value(hasItem(DEFAULT_TICKET_NUMBER)))
            .andExpect(jsonPath("$.[*].ownerName").value(hasItem(DEFAULT_OWNER_NAME)))
            .andExpect(jsonPath("$.[*].ownerType").value(hasItem(DEFAULT_OWNER_TYPE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY.toString())))
            .andExpect(jsonPath("$.[*].peopleAhead").value(hasItem(DEFAULT_PEOPLE_AHEAD)))
            .andExpect(jsonPath("$.[*].estimatedWaitMinutes").value(hasItem(DEFAULT_ESTIMATED_WAIT_MINUTES)))
            .andExpect(jsonPath("$.[*].itemCount").value(hasItem(DEFAULT_ITEM_COUNT)))
            .andExpect(jsonPath("$.[*].servedAt").value(hasItem(DEFAULT_SERVED_AT.toString())))
            .andExpect(jsonPath("$.[*].cancelledAt").value(hasItem(DEFAULT_CANCELLED_AT.toString())))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(hasItem(DEFAULT_LAST_MODIFIED_DATE.toString())));

        // Check, that the count call also returns 1
        restTicketMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultTicketShouldNotBeFound(String filter) throws Exception {
        restTicketMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restTicketMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingTicket() throws Exception {
        // Get the ticket
        restTicketMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTicket() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ticket
        Ticket updatedTicket = ticketRepository.findById(ticket.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTicket are not directly saved in db
        em.detach(updatedTicket);
        updatedTicket
            .ticketNumber(UPDATED_TICKET_NUMBER)
            .ownerName(UPDATED_OWNER_NAME)
            .ownerType(UPDATED_OWNER_TYPE)
            .status(UPDATED_STATUS)
            .category(UPDATED_CATEGORY)
            .peopleAhead(UPDATED_PEOPLE_AHEAD)
            .estimatedWaitMinutes(UPDATED_ESTIMATED_WAIT_MINUTES)
            .itemCount(UPDATED_ITEM_COUNT)
            .servedAt(UPDATED_SERVED_AT)
            .cancelledAt(UPDATED_CANCELLED_AT)
            .createdDate(UPDATED_CREATED_DATE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE);
        TicketDTO ticketDTO = ticketMapper.toDto(updatedTicket);

        restTicketMockMvc
            .perform(
                put(ENTITY_API_URL_ID, ticketDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO))
            )
            .andExpect(status().isOk());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTicketToMatchAllProperties(updatedTicket);
    }

    @Test
    @Transactional
    void putNonExistingTicket() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ticket.setId(longCount.incrementAndGet());

        // Create the Ticket
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTicketMockMvc
            .perform(
                put(ENTITY_API_URL_ID, ticketDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTicket() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ticket.setId(longCount.incrementAndGet());

        // Create the Ticket
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTicketMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(ticketDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTicket() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ticket.setId(longCount.incrementAndGet());

        // Create the Ticket
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTicketMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTicketWithPatch() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ticket using partial update
        Ticket partialUpdatedTicket = new Ticket();
        partialUpdatedTicket.setId(ticket.getId());

        partialUpdatedTicket
            .ownerName(UPDATED_OWNER_NAME)
            .ownerType(UPDATED_OWNER_TYPE)
            .status(UPDATED_STATUS)
            .itemCount(UPDATED_ITEM_COUNT)
            .servedAt(UPDATED_SERVED_AT)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE);

        restTicketMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTicket.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTicket))
            )
            .andExpect(status().isOk());

        // Validate the Ticket in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTicketUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedTicket, ticket), getPersistedTicket(ticket));
    }

    @Test
    @Transactional
    void fullUpdateTicketWithPatch() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ticket using partial update
        Ticket partialUpdatedTicket = new Ticket();
        partialUpdatedTicket.setId(ticket.getId());

        partialUpdatedTicket
            .ticketNumber(UPDATED_TICKET_NUMBER)
            .ownerName(UPDATED_OWNER_NAME)
            .ownerType(UPDATED_OWNER_TYPE)
            .status(UPDATED_STATUS)
            .category(UPDATED_CATEGORY)
            .peopleAhead(UPDATED_PEOPLE_AHEAD)
            .estimatedWaitMinutes(UPDATED_ESTIMATED_WAIT_MINUTES)
            .itemCount(UPDATED_ITEM_COUNT)
            .servedAt(UPDATED_SERVED_AT)
            .cancelledAt(UPDATED_CANCELLED_AT)
            .createdDate(UPDATED_CREATED_DATE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE);

        restTicketMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTicket.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTicket))
            )
            .andExpect(status().isOk());

        // Validate the Ticket in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTicketUpdatableFieldsEquals(partialUpdatedTicket, getPersistedTicket(partialUpdatedTicket));
    }

    @Test
    @Transactional
    void patchNonExistingTicket() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ticket.setId(longCount.incrementAndGet());

        // Create the Ticket
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTicketMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, ticketDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(ticketDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTicket() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ticket.setId(longCount.incrementAndGet());

        // Create the Ticket
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTicketMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(ticketDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTicket() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        ticket.setId(longCount.incrementAndGet());

        // Create the Ticket
        TicketDTO ticketDTO = ticketMapper.toDto(ticket);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTicketMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(ticketDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Ticket in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTicket() throws Exception {
        // Initialize the database
        insertedTicket = ticketRepository.saveAndFlush(ticket);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the ticket
        restTicketMockMvc
            .perform(delete(ENTITY_API_URL_ID, ticket.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return ticketRepository.count();
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

    protected Ticket getPersistedTicket(Ticket ticket) {
        return ticketRepository.findById(ticket.getId()).orElseThrow();
    }

    protected void assertPersistedTicketToMatchAllProperties(Ticket expectedTicket) {
        assertTicketAllPropertiesEquals(expectedTicket, getPersistedTicket(expectedTicket));
    }

    protected void assertPersistedTicketToMatchUpdatableProperties(Ticket expectedTicket) {
        assertTicketAllUpdatablePropertiesEquals(expectedTicket, getPersistedTicket(expectedTicket));
    }
}
