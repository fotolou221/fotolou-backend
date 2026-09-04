package com.fotolou.app.web.rest;

import com.fotolou.app.repository.SalonRepository;
import com.fotolou.app.service.SalonQueryService;
import com.fotolou.app.service.SalonService;
import com.fotolou.app.service.criteria.SalonCriteria;
import com.fotolou.app.service.dto.SalonDTO;
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
 * REST controller for managing {@link com.fotolou.app.domain.Salon}.
 */
@Tag(name = "2. Salons & Barbiers", description = "Recherche et gestion des salons de coiffure")
@RestController
@RequestMapping("/api/salons")
public class SalonResource {

    private static final Logger LOG = LoggerFactory.getLogger(SalonResource.class);

    private static final String ENTITY_NAME = "salon";

    @Value("${jhipster.clientApp.name:fotolouBackend}")
    private String applicationName;

    private final SalonService salonService;

    private final SalonRepository salonRepository;

    private final SalonQueryService salonQueryService;

    public SalonResource(SalonService salonService, SalonRepository salonRepository, SalonQueryService salonQueryService) {
        this.salonService = salonService;
        this.salonRepository = salonRepository;
        this.salonQueryService = salonQueryService;
    }

    /**
     * {@code POST  /salons} : Create a new salon.
     *
     * @param salonDTO the salonDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new salonDTO, or with status {@code 400 (Bad Request)} if the salon has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<SalonDTO> createSalon(@Valid @RequestBody SalonDTO salonDTO) throws URISyntaxException {
        LOG.debug("REST request to save Salon : {}", salonDTO);
        if (salonDTO.getId() != null) {
            throw new BadRequestAlertException("A new salon cannot already have an ID", ENTITY_NAME, "idexists");
        }
        salonDTO = salonService.save(salonDTO);
        return ResponseEntity.created(new URI("/api/salons/" + salonDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, salonDTO.getId().toString()))
            .body(salonDTO);
    }

    /**
     * {@code PUT  /salons/:id} : Updates an existing salon.
     *
     * @param id the id of the salonDTO to save.
     * @param salonDTO the salonDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated salonDTO,
     * or with status {@code 400 (Bad Request)} if the salonDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the salonDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SalonDTO> updateSalon(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SalonDTO salonDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Salon : {}, {}", id, salonDTO);
        if (salonDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, salonDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!salonRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        salonDTO = salonService.update(salonDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, salonDTO.getId().toString()))
            .body(salonDTO);
    }

    /**
     * {@code PATCH  /salons/:id} : Partial updates given fields of an existing salon, field will ignore if it is null
     *
     * @param id the id of the salonDTO to save.
     * @param salonDTO the salonDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated salonDTO,
     * or with status {@code 400 (Bad Request)} if the salonDTO is not valid,
     * or with status {@code 404 (Not Found)} if the salonDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the salonDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SalonDTO> partialUpdateSalon(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody SalonDTO salonDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Salon partially : {}, {}", id, salonDTO);
        if (salonDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, salonDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!salonRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SalonDTO> result = salonService.partialUpdate(salonDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, salonDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /salons} : get all the Salons.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Salons in body.
     */
    @GetMapping("")
    public ResponseEntity<List<SalonDTO>> getAllSalons(
        SalonCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Salons by criteria: {}", criteria);

        Page<SalonDTO> page = salonQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /salons/count} : count all the salons.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countSalons(SalonCriteria criteria) {
        LOG.debug("REST request to count Salons by criteria: {}", criteria);
        return ResponseEntity.ok().body(salonQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /salons/:id} : get the "id" salon.
     *
     * @param id the id of the salonDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the salonDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SalonDTO> getSalon(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Salon : {}", id);
        Optional<SalonDTO> salonDTO = salonService.findOne(id);
        return ResponseUtil.wrapOrNotFound(salonDTO);
    }

    /**
     * {@code DELETE  /salons/:id} : delete the "id" salon.
     *
     * @param id the id of the salonDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalon(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Salon : {}", id);
        salonService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
