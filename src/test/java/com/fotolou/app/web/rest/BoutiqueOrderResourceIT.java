package com.fotolou.app.web.rest;

import static com.fotolou.app.domain.BoutiqueOrderAsserts.*;
import static com.fotolou.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fotolou.app.IntegrationTest;
import com.fotolou.app.domain.BoutiqueOrder;
import com.fotolou.app.domain.User;
import com.fotolou.app.domain.enumeration.OrderStatus;
import com.fotolou.app.domain.enumeration.OrderType;
import com.fotolou.app.repository.BoutiqueOrderRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.security.AuthoritiesConstants;
import com.fotolou.app.service.dto.BoutiqueOrderDTO;
import com.fotolou.app.service.mapper.BoutiqueOrderMapper;
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
 * Integration tests for the {@link BoutiqueOrderResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class BoutiqueOrderResourceIT {

    private static final String DEFAULT_ORDER_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_ORDER_NUMBER = "BBBBBBBBBB";

    private static final Long DEFAULT_SUBTOTAL = 0L;
    private static final Long UPDATED_SUBTOTAL = 1L;
    private static final Long SMALLER_SUBTOTAL = 0L - 1L;

    private static final Long DEFAULT_DELIVERY_FEE = 0L;
    private static final Long UPDATED_DELIVERY_FEE = 1L;
    private static final Long SMALLER_DELIVERY_FEE = 0L - 1L;

    private static final Long DEFAULT_TOTAL_PRICE = 0L;
    private static final Long UPDATED_TOTAL_PRICE = 1L;
    private static final Long SMALLER_TOTAL_PRICE = 0L - 1L;

    private static final OrderStatus DEFAULT_STATUS = OrderStatus.EN_COURS;
    private static final OrderStatus UPDATED_STATUS = OrderStatus.LIVRE;

    private static final OrderType DEFAULT_ORDER_TYPE = OrderType.WHATSAPP;
    private static final OrderType UPDATED_ORDER_TYPE = OrderType.CALL;

    private static final String DEFAULT_DELIVERY_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_DELIVERY_ADDRESS = "BBBBBBBBBB";

    private static final String DEFAULT_DELIVERY_DISTRICT = "AAAAAAAAAA";
    private static final String UPDATED_DELIVERY_DISTRICT = "BBBBBBBBBB";

    private static final String DEFAULT_CUSTOMER_NAME = "AAAAAAAAAA";
    private static final String UPDATED_CUSTOMER_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CUSTOMER_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_CUSTOMER_PHONE = "BBBBBBBBBB";

    private static final String DEFAULT_NOTES = "AAAAAAAAAA";
    private static final String UPDATED_NOTES = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.ofEpochMilli(1787419709034L);

    private static final Instant DEFAULT_LAST_MODIFIED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_MODIFIED_DATE = Instant.ofEpochMilli(1787419709034L);

    private static final String ENTITY_API_URL = "/api/boutique-orders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BoutiqueOrderRepository boutiqueOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoutiqueOrderMapper boutiqueOrderMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBoutiqueOrderMockMvc;

    private BoutiqueOrder boutiqueOrder;

    private BoutiqueOrder insertedBoutiqueOrder;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BoutiqueOrder createEntity() {
        return new BoutiqueOrder()
            .orderNumber(DEFAULT_ORDER_NUMBER)
            .subtotal(DEFAULT_SUBTOTAL)
            .deliveryFee(DEFAULT_DELIVERY_FEE)
            .totalPrice(DEFAULT_TOTAL_PRICE)
            .status(DEFAULT_STATUS)
            .orderType(DEFAULT_ORDER_TYPE)
            .deliveryAddress(DEFAULT_DELIVERY_ADDRESS)
            .deliveryDistrict(DEFAULT_DELIVERY_DISTRICT)
            .customerName(DEFAULT_CUSTOMER_NAME)
            .customerPhone(DEFAULT_CUSTOMER_PHONE)
            .notes(DEFAULT_NOTES)
            .createdDate(DEFAULT_CREATED_DATE)
            .lastModifiedDate(DEFAULT_LAST_MODIFIED_DATE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BoutiqueOrder createUpdatedEntity() {
        return new BoutiqueOrder()
            .orderNumber(UPDATED_ORDER_NUMBER)
            .subtotal(UPDATED_SUBTOTAL)
            .deliveryFee(UPDATED_DELIVERY_FEE)
            .totalPrice(UPDATED_TOTAL_PRICE)
            .status(UPDATED_STATUS)
            .orderType(UPDATED_ORDER_TYPE)
            .deliveryAddress(UPDATED_DELIVERY_ADDRESS)
            .deliveryDistrict(UPDATED_DELIVERY_DISTRICT)
            .customerName(UPDATED_CUSTOMER_NAME)
            .customerPhone(UPDATED_CUSTOMER_PHONE)
            .notes(UPDATED_NOTES)
            .createdDate(UPDATED_CREATED_DATE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE);
    }

    @BeforeEach
    void initTest() {
        boutiqueOrder = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedBoutiqueOrder != null) {
            boutiqueOrderRepository.delete(insertedBoutiqueOrder);
            insertedBoutiqueOrder = null;
        }
    }

    @Test
    @Transactional
    void createBoutiqueOrder() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the BoutiqueOrder
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);
        var returnedBoutiqueOrderDTO = om.readValue(
            restBoutiqueOrderMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(boutiqueOrderDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            BoutiqueOrderDTO.class
        );

        // Validate the BoutiqueOrder in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBoutiqueOrder = boutiqueOrderMapper.toEntity(returnedBoutiqueOrderDTO);
        assertBoutiqueOrderUpdatableFieldsEquals(returnedBoutiqueOrder, getPersistedBoutiqueOrder(returnedBoutiqueOrder));

        insertedBoutiqueOrder = returnedBoutiqueOrder;
    }

    @Test
    @Transactional
    void createBoutiqueOrderWithExistingId() throws Exception {
        // Create the BoutiqueOrder with an existing ID
        boutiqueOrder.setId(1L);
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBoutiqueOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(boutiqueOrderDTO)))
            .andExpect(status().isBadRequest());

        // Validate the BoutiqueOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkOrderNumberIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        boutiqueOrder.setOrderNumber(null);

        // Create the BoutiqueOrder, which fails.
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);

        restBoutiqueOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(boutiqueOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSubtotalIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        boutiqueOrder.setSubtotal(null);

        // Create the BoutiqueOrder, which fails.
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);

        restBoutiqueOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(boutiqueOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDeliveryFeeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        boutiqueOrder.setDeliveryFee(null);

        // Create the BoutiqueOrder, which fails.
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);

        restBoutiqueOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(boutiqueOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTotalPriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        boutiqueOrder.setTotalPrice(null);

        // Create the BoutiqueOrder, which fails.
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);

        restBoutiqueOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(boutiqueOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        boutiqueOrder.setStatus(null);

        // Create the BoutiqueOrder, which fails.
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);

        restBoutiqueOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(boutiqueOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOrderTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        boutiqueOrder.setOrderType(null);

        // Create the BoutiqueOrder, which fails.
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);

        restBoutiqueOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(boutiqueOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        boutiqueOrder.setCreatedDate(null);

        // Create the BoutiqueOrder, which fails.
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);

        restBoutiqueOrderMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(boutiqueOrderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrders() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList
        restBoutiqueOrderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(boutiqueOrder.getId().intValue())))
            .andExpect(jsonPath("$.[*].orderNumber").value(hasItem(DEFAULT_ORDER_NUMBER)))
            .andExpect(jsonPath("$.[*].subtotal").value(hasItem(DEFAULT_SUBTOTAL.intValue())))
            .andExpect(jsonPath("$.[*].deliveryFee").value(hasItem(DEFAULT_DELIVERY_FEE.intValue())))
            .andExpect(jsonPath("$.[*].totalPrice").value(hasItem(DEFAULT_TOTAL_PRICE.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].orderType").value(hasItem(DEFAULT_ORDER_TYPE.toString())))
            .andExpect(jsonPath("$.[*].deliveryAddress").value(hasItem(DEFAULT_DELIVERY_ADDRESS)))
            .andExpect(jsonPath("$.[*].deliveryDistrict").value(hasItem(DEFAULT_DELIVERY_DISTRICT)))
            .andExpect(jsonPath("$.[*].customerName").value(hasItem(DEFAULT_CUSTOMER_NAME)))
            .andExpect(jsonPath("$.[*].customerPhone").value(hasItem(DEFAULT_CUSTOMER_PHONE)))
            .andExpect(jsonPath("$.[*].notes").value(hasItem(DEFAULT_NOTES)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(hasItem(DEFAULT_LAST_MODIFIED_DATE.toString())));
    }

    @Test
    @Transactional
    void getBoutiqueOrder() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get the boutiqueOrder
        restBoutiqueOrderMockMvc
            .perform(get(ENTITY_API_URL_ID, boutiqueOrder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(boutiqueOrder.getId().intValue()))
            .andExpect(jsonPath("$.orderNumber").value(DEFAULT_ORDER_NUMBER))
            .andExpect(jsonPath("$.subtotal").value(DEFAULT_SUBTOTAL.intValue()))
            .andExpect(jsonPath("$.deliveryFee").value(DEFAULT_DELIVERY_FEE.intValue()))
            .andExpect(jsonPath("$.totalPrice").value(DEFAULT_TOTAL_PRICE.intValue()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.orderType").value(DEFAULT_ORDER_TYPE.toString()))
            .andExpect(jsonPath("$.deliveryAddress").value(DEFAULT_DELIVERY_ADDRESS))
            .andExpect(jsonPath("$.deliveryDistrict").value(DEFAULT_DELIVERY_DISTRICT))
            .andExpect(jsonPath("$.customerName").value(DEFAULT_CUSTOMER_NAME))
            .andExpect(jsonPath("$.customerPhone").value(DEFAULT_CUSTOMER_PHONE))
            .andExpect(jsonPath("$.notes").value(DEFAULT_NOTES))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()))
            .andExpect(jsonPath("$.lastModifiedDate").value(DEFAULT_LAST_MODIFIED_DATE.toString()));
    }

    @Test
    @Transactional
    void getBoutiqueOrdersByIdFiltering() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        Long id = boutiqueOrder.getId();

        defaultBoutiqueOrderFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultBoutiqueOrderFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultBoutiqueOrderFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByOrderNumberIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where orderNumber equals to
        defaultBoutiqueOrderFiltering("orderNumber.equals=" + DEFAULT_ORDER_NUMBER, "orderNumber.equals=" + UPDATED_ORDER_NUMBER);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByOrderNumberIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where orderNumber in
        defaultBoutiqueOrderFiltering(
            "orderNumber.in=" + DEFAULT_ORDER_NUMBER + "," + UPDATED_ORDER_NUMBER,
            "orderNumber.in=" + UPDATED_ORDER_NUMBER
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByOrderNumberIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where orderNumber is not null
        defaultBoutiqueOrderFiltering("orderNumber.specified=true", "orderNumber.specified=false");
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByOrderNumberContainsSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where orderNumber contains
        defaultBoutiqueOrderFiltering("orderNumber.contains=" + DEFAULT_ORDER_NUMBER, "orderNumber.contains=" + UPDATED_ORDER_NUMBER);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByOrderNumberNotContainsSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where orderNumber does not contain
        defaultBoutiqueOrderFiltering(
            "orderNumber.doesNotContain=" + UPDATED_ORDER_NUMBER,
            "orderNumber.doesNotContain=" + DEFAULT_ORDER_NUMBER
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersBySubtotalIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where subtotal equals to
        defaultBoutiqueOrderFiltering("subtotal.equals=" + DEFAULT_SUBTOTAL, "subtotal.equals=" + UPDATED_SUBTOTAL);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersBySubtotalIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where subtotal in
        defaultBoutiqueOrderFiltering("subtotal.in=" + DEFAULT_SUBTOTAL + "," + UPDATED_SUBTOTAL, "subtotal.in=" + UPDATED_SUBTOTAL);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersBySubtotalIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where subtotal is not null
        defaultBoutiqueOrderFiltering("subtotal.specified=true", "subtotal.specified=false");
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersBySubtotalIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where subtotal is greater than or equal to
        defaultBoutiqueOrderFiltering("subtotal.greaterThanOrEqual=" + DEFAULT_SUBTOTAL, "subtotal.greaterThanOrEqual=" + UPDATED_SUBTOTAL);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersBySubtotalIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where subtotal is less than or equal to
        defaultBoutiqueOrderFiltering("subtotal.lessThanOrEqual=" + DEFAULT_SUBTOTAL, "subtotal.lessThanOrEqual=" + SMALLER_SUBTOTAL);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersBySubtotalIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where subtotal is less than
        defaultBoutiqueOrderFiltering("subtotal.lessThan=" + UPDATED_SUBTOTAL, "subtotal.lessThan=" + DEFAULT_SUBTOTAL);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersBySubtotalIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where subtotal is greater than
        defaultBoutiqueOrderFiltering("subtotal.greaterThan=" + SMALLER_SUBTOTAL, "subtotal.greaterThan=" + DEFAULT_SUBTOTAL);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryFeeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryFee equals to
        defaultBoutiqueOrderFiltering("deliveryFee.equals=" + DEFAULT_DELIVERY_FEE, "deliveryFee.equals=" + UPDATED_DELIVERY_FEE);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryFeeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryFee in
        defaultBoutiqueOrderFiltering(
            "deliveryFee.in=" + DEFAULT_DELIVERY_FEE + "," + UPDATED_DELIVERY_FEE,
            "deliveryFee.in=" + UPDATED_DELIVERY_FEE
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryFeeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryFee is not null
        defaultBoutiqueOrderFiltering("deliveryFee.specified=true", "deliveryFee.specified=false");
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryFeeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryFee is greater than or equal to
        defaultBoutiqueOrderFiltering(
            "deliveryFee.greaterThanOrEqual=" + DEFAULT_DELIVERY_FEE,
            "deliveryFee.greaterThanOrEqual=" + UPDATED_DELIVERY_FEE
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryFeeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryFee is less than or equal to
        defaultBoutiqueOrderFiltering(
            "deliveryFee.lessThanOrEqual=" + DEFAULT_DELIVERY_FEE,
            "deliveryFee.lessThanOrEqual=" + SMALLER_DELIVERY_FEE
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryFeeIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryFee is less than
        defaultBoutiqueOrderFiltering("deliveryFee.lessThan=" + UPDATED_DELIVERY_FEE, "deliveryFee.lessThan=" + DEFAULT_DELIVERY_FEE);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryFeeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryFee is greater than
        defaultBoutiqueOrderFiltering("deliveryFee.greaterThan=" + SMALLER_DELIVERY_FEE, "deliveryFee.greaterThan=" + DEFAULT_DELIVERY_FEE);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByTotalPriceIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where totalPrice equals to
        defaultBoutiqueOrderFiltering("totalPrice.equals=" + DEFAULT_TOTAL_PRICE, "totalPrice.equals=" + UPDATED_TOTAL_PRICE);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByTotalPriceIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where totalPrice in
        defaultBoutiqueOrderFiltering(
            "totalPrice.in=" + DEFAULT_TOTAL_PRICE + "," + UPDATED_TOTAL_PRICE,
            "totalPrice.in=" + UPDATED_TOTAL_PRICE
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByTotalPriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where totalPrice is not null
        defaultBoutiqueOrderFiltering("totalPrice.specified=true", "totalPrice.specified=false");
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByTotalPriceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where totalPrice is greater than or equal to
        defaultBoutiqueOrderFiltering(
            "totalPrice.greaterThanOrEqual=" + DEFAULT_TOTAL_PRICE,
            "totalPrice.greaterThanOrEqual=" + UPDATED_TOTAL_PRICE
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByTotalPriceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where totalPrice is less than or equal to
        defaultBoutiqueOrderFiltering(
            "totalPrice.lessThanOrEqual=" + DEFAULT_TOTAL_PRICE,
            "totalPrice.lessThanOrEqual=" + SMALLER_TOTAL_PRICE
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByTotalPriceIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where totalPrice is less than
        defaultBoutiqueOrderFiltering("totalPrice.lessThan=" + UPDATED_TOTAL_PRICE, "totalPrice.lessThan=" + DEFAULT_TOTAL_PRICE);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByTotalPriceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where totalPrice is greater than
        defaultBoutiqueOrderFiltering("totalPrice.greaterThan=" + SMALLER_TOTAL_PRICE, "totalPrice.greaterThan=" + DEFAULT_TOTAL_PRICE);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where status equals to
        defaultBoutiqueOrderFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where status in
        defaultBoutiqueOrderFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where status is not null
        defaultBoutiqueOrderFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByOrderTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where orderType equals to
        defaultBoutiqueOrderFiltering("orderType.equals=" + DEFAULT_ORDER_TYPE, "orderType.equals=" + UPDATED_ORDER_TYPE);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByOrderTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where orderType in
        defaultBoutiqueOrderFiltering(
            "orderType.in=" + DEFAULT_ORDER_TYPE + "," + UPDATED_ORDER_TYPE,
            "orderType.in=" + UPDATED_ORDER_TYPE
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByOrderTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where orderType is not null
        defaultBoutiqueOrderFiltering("orderType.specified=true", "orderType.specified=false");
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryAddressIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryAddress equals to
        defaultBoutiqueOrderFiltering(
            "deliveryAddress.equals=" + DEFAULT_DELIVERY_ADDRESS,
            "deliveryAddress.equals=" + UPDATED_DELIVERY_ADDRESS
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryAddressIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryAddress in
        defaultBoutiqueOrderFiltering(
            "deliveryAddress.in=" + DEFAULT_DELIVERY_ADDRESS + "," + UPDATED_DELIVERY_ADDRESS,
            "deliveryAddress.in=" + UPDATED_DELIVERY_ADDRESS
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryAddressIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryAddress is not null
        defaultBoutiqueOrderFiltering("deliveryAddress.specified=true", "deliveryAddress.specified=false");
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryAddressContainsSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryAddress contains
        defaultBoutiqueOrderFiltering(
            "deliveryAddress.contains=" + DEFAULT_DELIVERY_ADDRESS,
            "deliveryAddress.contains=" + UPDATED_DELIVERY_ADDRESS
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryAddressNotContainsSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryAddress does not contain
        defaultBoutiqueOrderFiltering(
            "deliveryAddress.doesNotContain=" + UPDATED_DELIVERY_ADDRESS,
            "deliveryAddress.doesNotContain=" + DEFAULT_DELIVERY_ADDRESS
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryDistrictIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryDistrict equals to
        defaultBoutiqueOrderFiltering(
            "deliveryDistrict.equals=" + DEFAULT_DELIVERY_DISTRICT,
            "deliveryDistrict.equals=" + UPDATED_DELIVERY_DISTRICT
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryDistrictIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryDistrict in
        defaultBoutiqueOrderFiltering(
            "deliveryDistrict.in=" + DEFAULT_DELIVERY_DISTRICT + "," + UPDATED_DELIVERY_DISTRICT,
            "deliveryDistrict.in=" + UPDATED_DELIVERY_DISTRICT
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryDistrictIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryDistrict is not null
        defaultBoutiqueOrderFiltering("deliveryDistrict.specified=true", "deliveryDistrict.specified=false");
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryDistrictContainsSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryDistrict contains
        defaultBoutiqueOrderFiltering(
            "deliveryDistrict.contains=" + DEFAULT_DELIVERY_DISTRICT,
            "deliveryDistrict.contains=" + UPDATED_DELIVERY_DISTRICT
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByDeliveryDistrictNotContainsSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where deliveryDistrict does not contain
        defaultBoutiqueOrderFiltering(
            "deliveryDistrict.doesNotContain=" + UPDATED_DELIVERY_DISTRICT,
            "deliveryDistrict.doesNotContain=" + DEFAULT_DELIVERY_DISTRICT
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByCustomerNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where customerName equals to
        defaultBoutiqueOrderFiltering("customerName.equals=" + DEFAULT_CUSTOMER_NAME, "customerName.equals=" + UPDATED_CUSTOMER_NAME);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByCustomerNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where customerName in
        defaultBoutiqueOrderFiltering(
            "customerName.in=" + DEFAULT_CUSTOMER_NAME + "," + UPDATED_CUSTOMER_NAME,
            "customerName.in=" + UPDATED_CUSTOMER_NAME
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByCustomerNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where customerName is not null
        defaultBoutiqueOrderFiltering("customerName.specified=true", "customerName.specified=false");
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByCustomerNameContainsSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where customerName contains
        defaultBoutiqueOrderFiltering("customerName.contains=" + DEFAULT_CUSTOMER_NAME, "customerName.contains=" + UPDATED_CUSTOMER_NAME);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByCustomerNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where customerName does not contain
        defaultBoutiqueOrderFiltering(
            "customerName.doesNotContain=" + UPDATED_CUSTOMER_NAME,
            "customerName.doesNotContain=" + DEFAULT_CUSTOMER_NAME
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByCustomerPhoneIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where customerPhone equals to
        defaultBoutiqueOrderFiltering("customerPhone.equals=" + DEFAULT_CUSTOMER_PHONE, "customerPhone.equals=" + UPDATED_CUSTOMER_PHONE);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByCustomerPhoneIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where customerPhone in
        defaultBoutiqueOrderFiltering(
            "customerPhone.in=" + DEFAULT_CUSTOMER_PHONE + "," + UPDATED_CUSTOMER_PHONE,
            "customerPhone.in=" + UPDATED_CUSTOMER_PHONE
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByCustomerPhoneIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where customerPhone is not null
        defaultBoutiqueOrderFiltering("customerPhone.specified=true", "customerPhone.specified=false");
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByCustomerPhoneContainsSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where customerPhone contains
        defaultBoutiqueOrderFiltering(
            "customerPhone.contains=" + DEFAULT_CUSTOMER_PHONE,
            "customerPhone.contains=" + UPDATED_CUSTOMER_PHONE
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByCustomerPhoneNotContainsSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where customerPhone does not contain
        defaultBoutiqueOrderFiltering(
            "customerPhone.doesNotContain=" + UPDATED_CUSTOMER_PHONE,
            "customerPhone.doesNotContain=" + DEFAULT_CUSTOMER_PHONE
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByCreatedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where createdDate equals to
        defaultBoutiqueOrderFiltering("createdDate.equals=" + DEFAULT_CREATED_DATE, "createdDate.equals=" + UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByCreatedDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where createdDate in
        defaultBoutiqueOrderFiltering(
            "createdDate.in=" + DEFAULT_CREATED_DATE + "," + UPDATED_CREATED_DATE,
            "createdDate.in=" + UPDATED_CREATED_DATE
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByCreatedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where createdDate is not null
        defaultBoutiqueOrderFiltering("createdDate.specified=true", "createdDate.specified=false");
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByLastModifiedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where lastModifiedDate equals to
        defaultBoutiqueOrderFiltering(
            "lastModifiedDate.equals=" + DEFAULT_LAST_MODIFIED_DATE,
            "lastModifiedDate.equals=" + UPDATED_LAST_MODIFIED_DATE
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByLastModifiedDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where lastModifiedDate in
        defaultBoutiqueOrderFiltering(
            "lastModifiedDate.in=" + DEFAULT_LAST_MODIFIED_DATE + "," + UPDATED_LAST_MODIFIED_DATE,
            "lastModifiedDate.in=" + UPDATED_LAST_MODIFIED_DATE
        );
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByLastModifiedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        // Get all the boutiqueOrderList where lastModifiedDate is not null
        defaultBoutiqueOrderFiltering("lastModifiedDate.specified=true", "lastModifiedDate.specified=false");
    }

    @Test
    @Transactional
    void getAllBoutiqueOrdersByUserIsEqualToSomething() throws Exception {
        User user;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            boutiqueOrderRepository.saveAndFlush(boutiqueOrder);
            user = UserResourceIT.createEntity();
        } else {
            user = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(user);
        em.flush();
        boutiqueOrder.setUser(user);
        boutiqueOrderRepository.saveAndFlush(boutiqueOrder);
        Long userId = user.getId();
        // Get all the boutiqueOrderList where user equals to userId
        defaultBoutiqueOrderShouldBeFound("userId.equals=" + userId);

        // Get all the boutiqueOrderList where user equals to (userId + 1)
        defaultBoutiqueOrderShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    private void defaultBoutiqueOrderFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultBoutiqueOrderShouldBeFound(shouldBeFound);
        defaultBoutiqueOrderShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultBoutiqueOrderShouldBeFound(String filter) throws Exception {
        restBoutiqueOrderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(boutiqueOrder.getId().intValue())))
            .andExpect(jsonPath("$.[*].orderNumber").value(hasItem(DEFAULT_ORDER_NUMBER)))
            .andExpect(jsonPath("$.[*].subtotal").value(hasItem(DEFAULT_SUBTOTAL.intValue())))
            .andExpect(jsonPath("$.[*].deliveryFee").value(hasItem(DEFAULT_DELIVERY_FEE.intValue())))
            .andExpect(jsonPath("$.[*].totalPrice").value(hasItem(DEFAULT_TOTAL_PRICE.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].orderType").value(hasItem(DEFAULT_ORDER_TYPE.toString())))
            .andExpect(jsonPath("$.[*].deliveryAddress").value(hasItem(DEFAULT_DELIVERY_ADDRESS)))
            .andExpect(jsonPath("$.[*].deliveryDistrict").value(hasItem(DEFAULT_DELIVERY_DISTRICT)))
            .andExpect(jsonPath("$.[*].customerName").value(hasItem(DEFAULT_CUSTOMER_NAME)))
            .andExpect(jsonPath("$.[*].customerPhone").value(hasItem(DEFAULT_CUSTOMER_PHONE)))
            .andExpect(jsonPath("$.[*].notes").value(hasItem(DEFAULT_NOTES)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(hasItem(DEFAULT_LAST_MODIFIED_DATE.toString())));

        // Check, that the count call also returns 1
        restBoutiqueOrderMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultBoutiqueOrderShouldNotBeFound(String filter) throws Exception {
        restBoutiqueOrderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restBoutiqueOrderMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingBoutiqueOrder() throws Exception {
        // Get the boutiqueOrder
        restBoutiqueOrderMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBoutiqueOrder() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the boutiqueOrder
        BoutiqueOrder updatedBoutiqueOrder = boutiqueOrderRepository.findById(boutiqueOrder.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedBoutiqueOrder are not directly saved in db
        em.detach(updatedBoutiqueOrder);
        updatedBoutiqueOrder
            .orderNumber(UPDATED_ORDER_NUMBER)
            .subtotal(UPDATED_SUBTOTAL)
            .deliveryFee(UPDATED_DELIVERY_FEE)
            .totalPrice(UPDATED_TOTAL_PRICE)
            .status(UPDATED_STATUS)
            .orderType(UPDATED_ORDER_TYPE)
            .deliveryAddress(UPDATED_DELIVERY_ADDRESS)
            .deliveryDistrict(UPDATED_DELIVERY_DISTRICT)
            .customerName(UPDATED_CUSTOMER_NAME)
            .customerPhone(UPDATED_CUSTOMER_PHONE)
            .notes(UPDATED_NOTES)
            .createdDate(UPDATED_CREATED_DATE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE);
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(updatedBoutiqueOrder);

        restBoutiqueOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, boutiqueOrderDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(boutiqueOrderDTO))
            )
            .andExpect(status().isOk());

        // Validate the BoutiqueOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBoutiqueOrderToMatchAllProperties(updatedBoutiqueOrder);
    }

    @Test
    @Transactional
    void putNonExistingBoutiqueOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        boutiqueOrder.setId(longCount.incrementAndGet());

        // Create the BoutiqueOrder
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBoutiqueOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, boutiqueOrderDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(boutiqueOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BoutiqueOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBoutiqueOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        boutiqueOrder.setId(longCount.incrementAndGet());

        // Create the BoutiqueOrder
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBoutiqueOrderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(boutiqueOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BoutiqueOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBoutiqueOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        boutiqueOrder.setId(longCount.incrementAndGet());

        // Create the BoutiqueOrder
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBoutiqueOrderMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(boutiqueOrderDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the BoutiqueOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBoutiqueOrderWithPatch() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the boutiqueOrder using partial update
        BoutiqueOrder partialUpdatedBoutiqueOrder = new BoutiqueOrder();
        partialUpdatedBoutiqueOrder.setId(boutiqueOrder.getId());

        partialUpdatedBoutiqueOrder
            .orderNumber(UPDATED_ORDER_NUMBER)
            .subtotal(UPDATED_SUBTOTAL)
            .deliveryFee(UPDATED_DELIVERY_FEE)
            .deliveryAddress(UPDATED_DELIVERY_ADDRESS)
            .deliveryDistrict(UPDATED_DELIVERY_DISTRICT)
            .customerName(UPDATED_CUSTOMER_NAME)
            .notes(UPDATED_NOTES)
            .createdDate(UPDATED_CREATED_DATE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE);

        restBoutiqueOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBoutiqueOrder.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBoutiqueOrder))
            )
            .andExpect(status().isOk());

        // Validate the BoutiqueOrder in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBoutiqueOrderUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedBoutiqueOrder, boutiqueOrder),
            getPersistedBoutiqueOrder(boutiqueOrder)
        );
    }

    @Test
    @Transactional
    void fullUpdateBoutiqueOrderWithPatch() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the boutiqueOrder using partial update
        BoutiqueOrder partialUpdatedBoutiqueOrder = new BoutiqueOrder();
        partialUpdatedBoutiqueOrder.setId(boutiqueOrder.getId());

        partialUpdatedBoutiqueOrder
            .orderNumber(UPDATED_ORDER_NUMBER)
            .subtotal(UPDATED_SUBTOTAL)
            .deliveryFee(UPDATED_DELIVERY_FEE)
            .totalPrice(UPDATED_TOTAL_PRICE)
            .status(UPDATED_STATUS)
            .orderType(UPDATED_ORDER_TYPE)
            .deliveryAddress(UPDATED_DELIVERY_ADDRESS)
            .deliveryDistrict(UPDATED_DELIVERY_DISTRICT)
            .customerName(UPDATED_CUSTOMER_NAME)
            .customerPhone(UPDATED_CUSTOMER_PHONE)
            .notes(UPDATED_NOTES)
            .createdDate(UPDATED_CREATED_DATE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE);

        restBoutiqueOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBoutiqueOrder.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBoutiqueOrder))
            )
            .andExpect(status().isOk());

        // Validate the BoutiqueOrder in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBoutiqueOrderUpdatableFieldsEquals(partialUpdatedBoutiqueOrder, getPersistedBoutiqueOrder(partialUpdatedBoutiqueOrder));
    }

    @Test
    @Transactional
    void patchNonExistingBoutiqueOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        boutiqueOrder.setId(longCount.incrementAndGet());

        // Create the BoutiqueOrder
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBoutiqueOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, boutiqueOrderDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(boutiqueOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BoutiqueOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBoutiqueOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        boutiqueOrder.setId(longCount.incrementAndGet());

        // Create the BoutiqueOrder
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBoutiqueOrderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(boutiqueOrderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BoutiqueOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBoutiqueOrder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        boutiqueOrder.setId(longCount.incrementAndGet());

        // Create the BoutiqueOrder
        BoutiqueOrderDTO boutiqueOrderDTO = boutiqueOrderMapper.toDto(boutiqueOrder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBoutiqueOrderMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(boutiqueOrderDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the BoutiqueOrder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBoutiqueOrder() throws Exception {
        // Initialize the database
        insertedBoutiqueOrder = boutiqueOrderRepository.saveAndFlush(boutiqueOrder);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the boutiqueOrder
        restBoutiqueOrderMockMvc
            .perform(delete(ENTITY_API_URL_ID, boutiqueOrder.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return boutiqueOrderRepository.count();
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

    protected BoutiqueOrder getPersistedBoutiqueOrder(BoutiqueOrder boutiqueOrder) {
        return boutiqueOrderRepository.findById(boutiqueOrder.getId()).orElseThrow();
    }

    protected void assertPersistedBoutiqueOrderToMatchAllProperties(BoutiqueOrder expectedBoutiqueOrder) {
        assertBoutiqueOrderAllPropertiesEquals(expectedBoutiqueOrder, getPersistedBoutiqueOrder(expectedBoutiqueOrder));
    }

    protected void assertPersistedBoutiqueOrderToMatchUpdatableProperties(BoutiqueOrder expectedBoutiqueOrder) {
        assertBoutiqueOrderAllUpdatablePropertiesEquals(expectedBoutiqueOrder, getPersistedBoutiqueOrder(expectedBoutiqueOrder));
    }
}
