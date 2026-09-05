package com.fotolou.app.web.rest;

import com.fotolou.app.service.CoiffeurProfileQueryService;
import com.fotolou.app.service.CoiffeurProfileService;
import com.fotolou.app.service.criteria.CoiffeurProfileCriteria;
import com.fotolou.app.service.dto.CoiffeurProfileDTO;
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
 * REST controller for managing {@link com.fotolou.app.domain.CoiffeurProfile}.
 */
@Tag(name = "2. Salons & Barbiers", description = "Gestion des salons de coiffure et des barbiers")
@RestController
@RequestMapping({ "/api/coiffeur-profiles", "/api/coiffeurs" })
public class CoiffeurProfileResource {

    private static final Logger LOG = LoggerFactory.getLogger(CoiffeurProfileResource.class);

    private static final String ENTITY_NAME = "coiffeurProfile";

    @Value("${jhipster.clientApp.name:fotolouBackend}")
    private String applicationName;

    private final CoiffeurProfileService coiffeurProfileService;

    private final CoiffeurProfileQueryService coiffeurProfileQueryService;

    public CoiffeurProfileResource(CoiffeurProfileService coiffeurProfileService, CoiffeurProfileQueryService coiffeurProfileQueryService) {
        this.coiffeurProfileService = coiffeurProfileService;
        this.coiffeurProfileQueryService = coiffeurProfileQueryService;
    }

    /**
     * {@code POST  /coiffeur-profiles} : Create a new coiffeurProfile.
     *
     * @param coiffeurProfileDTO the coiffeurProfileDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new coiffeurProfileDTO, or with status {@code 400 (Bad Request)} if the coiffeurProfile has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<CoiffeurProfileDTO> createCoiffeurProfile(@Valid @RequestBody CoiffeurProfileDTO coiffeurProfileDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save CoiffeurProfile : {}", coiffeurProfileDTO);
        if (coiffeurProfileDTO.getId() != null) {
            throw new BadRequestAlertException("A new coiffeurProfile cannot already have an ID", ENTITY_NAME, "idexists");
        }
        coiffeurProfileDTO = coiffeurProfileService.save(coiffeurProfileDTO);
        return ResponseEntity.created(new URI("/api/coiffeur-profiles/" + coiffeurProfileDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, coiffeurProfileDTO.getId().toString()))
            .body(coiffeurProfileDTO);
    }

    /**
     * {@code PUT  /coiffeur-profiles/:id} : Updates an existing coiffeurProfile.
     *
     * @param id the id of the coiffeurProfileDTO to save.
     * @param coiffeurProfileDTO the coiffeurProfileDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated coiffeurProfileDTO,
     * or with status {@code 400 (Bad Request)} if the coiffeurProfileDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the coiffeurProfileDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CoiffeurProfileDTO> updateCoiffeurProfile(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CoiffeurProfileDTO coiffeurProfileDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update CoiffeurProfile : {}, {}", id, coiffeurProfileDTO);
        if (coiffeurProfileDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, coiffeurProfileDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!coiffeurProfileService.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        coiffeurProfileDTO = coiffeurProfileService.update(coiffeurProfileDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, coiffeurProfileDTO.getId().toString()))
            .body(coiffeurProfileDTO);
    }

    /**
     * {@code PATCH  /coiffeur-profiles/:id} : Partial updates given fields of an existing coiffeurProfile, field will ignore if it is null
     *
     * @param id the id of the coiffeurProfileDTO to save.
     * @param coiffeurProfileDTO the coiffeurProfileDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated coiffeurProfileDTO,
     * or with status {@code 400 (Bad Request)} if the coiffeurProfileDTO is not valid,
     * or with status {@code 404 (Not Found)} if the coiffeurProfileDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the coiffeurProfileDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CoiffeurProfileDTO> partialUpdateCoiffeurProfile(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CoiffeurProfileDTO coiffeurProfileDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update CoiffeurProfile partially : {}, {}", id, coiffeurProfileDTO);
        if (coiffeurProfileDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, coiffeurProfileDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!coiffeurProfileService.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CoiffeurProfileDTO> result = coiffeurProfileService.partialUpdate(coiffeurProfileDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, coiffeurProfileDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /coiffeur-profiles} : get all the Coiffeur Profiles.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Coiffeur Profiles in body.
     */
    @GetMapping("")
    public ResponseEntity<List<CoiffeurProfileDTO>> getAllCoiffeurProfiles(
        CoiffeurProfileCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get CoiffeurProfiles by criteria: {}", criteria);

        Page<CoiffeurProfileDTO> page = coiffeurProfileQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /coiffeur-profiles/count} : count all the coiffeurProfiles.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countCoiffeurProfiles(CoiffeurProfileCriteria criteria) {
        LOG.debug("REST request to count CoiffeurProfiles by criteria: {}", criteria);
        return ResponseEntity.ok().body(coiffeurProfileQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /coiffeur-profiles/:id} : get the "id" coiffeurProfile.
     *
     * @param id the id of the coiffeurProfileDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the coiffeurProfileDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CoiffeurProfileDTO> getCoiffeurProfile(@PathVariable("id") Long id) {
        LOG.debug("REST request to get CoiffeurProfile : {}", id);
        Optional<CoiffeurProfileDTO> coiffeurProfileDTO = coiffeurProfileService.findOne(id);
        return ResponseUtil.wrapOrNotFound(coiffeurProfileDTO);
    }

    /**
     * {@code DELETE  /coiffeur-profiles/:id} : delete the "id" coiffeurProfile.
     *
     * @param id the id of the coiffeurProfileDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoiffeurProfile(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete CoiffeurProfile : {}", id);
        coiffeurProfileService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
