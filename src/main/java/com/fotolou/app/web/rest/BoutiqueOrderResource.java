package com.fotolou.app.web.rest;

import com.fotolou.app.service.BoutiqueOrderQueryService;
import com.fotolou.app.service.BoutiqueOrderService;
import com.fotolou.app.service.criteria.BoutiqueOrderCriteria;
import com.fotolou.app.service.dto.BoutiqueOrderDTO;
import com.fotolou.app.web.rest.errors.BadRequestAlertException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.fotolou.app.domain.BoutiqueOrder}.
 */
@Tag(name = "6. Boutique & Commandes", description = "Commandes boutique")
@RestController
@RequestMapping({ "/api/boutique-orders", "/api/orders" })
public class BoutiqueOrderResource {

    private static final Logger LOG = LoggerFactory.getLogger(BoutiqueOrderResource.class);

    private static final String ENTITY_NAME = "boutiqueOrder";

    @Value("${jhipster.clientApp.name:fotolouBackend}")
    private String applicationName;

    private final BoutiqueOrderService boutiqueOrderService;

    private final BoutiqueOrderQueryService boutiqueOrderQueryService;

    public BoutiqueOrderResource(BoutiqueOrderService boutiqueOrderService, BoutiqueOrderQueryService boutiqueOrderQueryService) {
        this.boutiqueOrderService = boutiqueOrderService;
        this.boutiqueOrderQueryService = boutiqueOrderQueryService;
    }

    /**
     * {@code POST  /boutique-orders} : Create a new boutiqueOrder.
     *
     * @param boutiqueOrderDTO the boutiqueOrderDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new boutiqueOrderDTO, or with status {@code 400 (Bad Request)} if the boutiqueOrder has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<BoutiqueOrderDTO> createBoutiqueOrder(@Valid @RequestBody BoutiqueOrderDTO boutiqueOrderDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save BoutiqueOrder : {}", boutiqueOrderDTO);
        if (boutiqueOrderDTO.getId() != null) {
            throw new BadRequestAlertException("A new boutiqueOrder cannot already have an ID", ENTITY_NAME, "idexists");
        }
        boutiqueOrderDTO = boutiqueOrderService.save(boutiqueOrderDTO);
        return ResponseEntity.created(new URI("/api/boutique-orders/" + boutiqueOrderDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, boutiqueOrderDTO.getId().toString()))
            .body(boutiqueOrderDTO);
    }

    /**
     * {@code PUT  /boutique-orders/:id} : Updates an existing boutiqueOrder.
     *
     * @param id the id of the boutiqueOrderDTO to save.
     * @param boutiqueOrderDTO the boutiqueOrderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated boutiqueOrderDTO,
     * or with status {@code 400 (Bad Request)} if the boutiqueOrderDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the boutiqueOrderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BoutiqueOrderDTO> updateBoutiqueOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody BoutiqueOrderDTO boutiqueOrderDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update BoutiqueOrder : {}, {}", id, boutiqueOrderDTO);
        if (boutiqueOrderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, boutiqueOrderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!boutiqueOrderService.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        boutiqueOrderDTO = boutiqueOrderService.update(boutiqueOrderDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, boutiqueOrderDTO.getId().toString()))
            .body(boutiqueOrderDTO);
    }

    /**
     * {@code PATCH  /boutique-orders/:id} : Partial updates given fields of an existing boutiqueOrder, field will ignore if it is null
     *
     * @param id the id of the boutiqueOrderDTO to save.
     * @param boutiqueOrderDTO the boutiqueOrderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated boutiqueOrderDTO,
     * or with status {@code 400 (Bad Request)} if the boutiqueOrderDTO is not valid,
     * or with status {@code 404 (Not Found)} if the boutiqueOrderDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the boutiqueOrderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<BoutiqueOrderDTO> partialUpdateBoutiqueOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody BoutiqueOrderDTO boutiqueOrderDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update BoutiqueOrder partially : {}, {}", id, boutiqueOrderDTO);
        if (boutiqueOrderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, boutiqueOrderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!boutiqueOrderService.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<BoutiqueOrderDTO> result = boutiqueOrderService.partialUpdate(boutiqueOrderDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, boutiqueOrderDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /boutique-orders} : get all the Boutique Orders.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Boutique Orders in body.
     */
    @GetMapping("")
    public ResponseEntity<List<BoutiqueOrderDTO>> getAllBoutiqueOrders(
        BoutiqueOrderCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get BoutiqueOrders by criteria: {}", criteria);

        Page<BoutiqueOrderDTO> page = boutiqueOrderQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /boutique-orders/count} : count all the boutiqueOrders.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countBoutiqueOrders(BoutiqueOrderCriteria criteria) {
        LOG.debug("REST request to count BoutiqueOrders by criteria: {}", criteria);
        return ResponseEntity.ok().body(boutiqueOrderQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /boutique-orders/:id} : get the "id" boutiqueOrder.
     *
     * @param id the id of the boutiqueOrderDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the boutiqueOrderDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BoutiqueOrderDTO> getBoutiqueOrder(@PathVariable("id") Long id) {
        LOG.debug("REST request to get BoutiqueOrder : {}", id);
        Optional<BoutiqueOrderDTO> boutiqueOrderDTO = boutiqueOrderService.findOne(id);
        return ResponseUtil.wrapOrNotFound(boutiqueOrderDTO);
    }

    /**
     * {@code DELETE  /boutique-orders/:id} : delete the "id" boutiqueOrder.
     *
     * @param id the id of the boutiqueOrderDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoutiqueOrder(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete BoutiqueOrder : {}", id);
        boutiqueOrderService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
