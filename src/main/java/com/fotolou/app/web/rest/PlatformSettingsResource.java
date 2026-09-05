package com.fotolou.app.web.rest;

import com.fotolou.app.service.PlatformSettingsService;
import com.fotolou.app.service.dto.PlatformSettingsDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.fotolou.app.domain.PlatformSettings}.
 */
@Tag(name = "8. Administration & Statistiques", description = "Paramètres de la plateforme")
@RestController
@RequestMapping("/api/platform-settings")
public class PlatformSettingsResource {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformSettingsResource.class);

    private static final String ENTITY_NAME = "platformSettings";

    @Value("${jhipster.clientApp.name:fotolouBackend}")
    private String applicationName;

    private final PlatformSettingsService platformSettingsService;

    public PlatformSettingsResource(PlatformSettingsService platformSettingsService) {
        this.platformSettingsService = platformSettingsService;
    }

    /**
     * {@code POST  /platform-settings} : Create a new platformSettings.
     *
     * @param platformSettingsDTO the platformSettingsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new platformSettingsDTO, or with status {@code 400 (Bad Request)} if the platformSettings has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<PlatformSettingsDTO> createPlatformSettings(@Valid @RequestBody PlatformSettingsDTO platformSettingsDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save PlatformSettings : {}", platformSettingsDTO);
        if (platformSettingsDTO.getId() != null) {
            throw new BadRequestAlertException("A new platformSettings cannot already have an ID", ENTITY_NAME, "idexists");
        }
        platformSettingsDTO = platformSettingsService.save(platformSettingsDTO);
        return ResponseEntity.created(new URI("/api/platform-settings/" + platformSettingsDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, platformSettingsDTO.getId().toString()))
            .body(platformSettingsDTO);
    }

    /**
     * {@code PUT  /platform-settings/:id} : Updates an existing platformSettings.
     *
     * @param id the id of the platformSettingsDTO to save.
     * @param platformSettingsDTO the platformSettingsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated platformSettingsDTO,
     * or with status {@code 400 (Bad Request)} if the platformSettingsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the platformSettingsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlatformSettingsDTO> updatePlatformSettings(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody PlatformSettingsDTO platformSettingsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update PlatformSettings : {}, {}", id, platformSettingsDTO);
        if (platformSettingsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, platformSettingsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!platformSettingsService.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        platformSettingsDTO = platformSettingsService.update(platformSettingsDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, platformSettingsDTO.getId().toString()))
            .body(platformSettingsDTO);
    }

    /**
     * {@code PATCH  /platform-settings/:id} : Partial updates given fields of an existing platformSettings, field will ignore if it is null
     *
     * @param id the id of the platformSettingsDTO to save.
     * @param platformSettingsDTO the platformSettingsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated platformSettingsDTO,
     * or with status {@code 400 (Bad Request)} if the platformSettingsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the platformSettingsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the platformSettingsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<PlatformSettingsDTO> partialUpdatePlatformSettings(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody PlatformSettingsDTO platformSettingsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update PlatformSettings partially : {}, {}", id, platformSettingsDTO);
        if (platformSettingsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, platformSettingsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!platformSettingsService.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<PlatformSettingsDTO> result = platformSettingsService.partialUpdate(platformSettingsDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, platformSettingsDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /platform-settings} : get all the Platform Settings.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Platform Settings in body.
     */
    @GetMapping("")
    public List<PlatformSettingsDTO> getAllPlatformSettingses() {
        LOG.debug("REST request to get all PlatformSettingses");
        return platformSettingsService.findAll();
    }

    /**
     * {@code GET  /platform-settings/:id} : get the "id" platformSettings.
     *
     * @param id the id of the platformSettingsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the platformSettingsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlatformSettingsDTO> getPlatformSettings(@PathVariable("id") Long id) {
        LOG.debug("REST request to get PlatformSettings : {}", id);
        Optional<PlatformSettingsDTO> platformSettingsDTO = platformSettingsService.findOne(id);
        if (platformSettingsDTO.isEmpty()) {
            List<PlatformSettingsDTO> all = platformSettingsService.findAll();
            if (!all.isEmpty()) {
                return ResponseEntity.ok(all.get(0));
            }
            // Si aucun paramètre n'existe, créer et retourner des paramètres par défaut
            PlatformSettingsDTO defaultSettings = new PlatformSettingsDTO();
            defaultSettings.setAppName("Fotolou");
            defaultSettings.setContactEmail("support@fotolou.sn");
            defaultSettings.setContactPhone("+221 77 862 70 52");
            defaultSettings.setCommissionRate(10.0);
            defaultSettings.setOpeningTime("09:00");
            defaultSettings.setClosingTime("21:00");
            defaultSettings.setAllowRelativeBooking(true);
            defaultSettings.setMaintenanceMode(false);
            return ResponseEntity.ok(platformSettingsService.save(defaultSettings));
        }
        return ResponseUtil.wrapOrNotFound(platformSettingsDTO);
    }

    /**
     * {@code DELETE  /platform-settings/:id} : delete the "id" platformSettings.
     *
     * @param id the id of the platformSettingsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlatformSettings(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete PlatformSettings : {}", id);
        platformSettingsService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
