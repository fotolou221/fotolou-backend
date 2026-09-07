package com.fotolou.app.web.rest;

import com.fotolou.app.security.AuthoritiesConstants;
import com.fotolou.app.security.SecurityUtils;
import com.fotolou.app.service.RelativeService;
import com.fotolou.app.service.custom.sms.SmsService;
import com.fotolou.app.service.dto.RelativeDTO;
import com.fotolou.app.service.dto.UserDTO;
import com.fotolou.app.web.rest.errors.BadRequestAlertException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
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
 * REST controller for managing {@link com.fotolou.app.domain.Relative}.
 */
@Tag(name = "4. Proches & Famille", description = "Gestion des proches pour réservation multi-tickets")
@RestController
@RequestMapping("/api/relatives")
public class RelativeResource {

    private static final Logger LOG = LoggerFactory.getLogger(RelativeResource.class);

    private static final String ENTITY_NAME = "relative";

    @Value("${jhipster.clientApp.name:fotolouBackend}")
    private String applicationName;

    private final RelativeService relativeService;
    private final SmsService smsService;

    public RelativeResource(RelativeService relativeService, SmsService smsService) {
        this.relativeService = relativeService;
        this.smsService = smsService;
    }

    /**
     * {@code POST  /relatives} : Create a new relative for the authenticated user.
     *
     * @param relativeDTO the relativeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new relativeDTO, or with status {@code 400 (Bad Request)} if the relative has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<RelativeDTO> createRelative(@Valid @RequestBody RelativeDTO relativeDTO) throws URISyntaxException {
        LOG.debug("REST request to save Relative : {}", relativeDTO);
        if (relativeDTO.getId() != null) {
            throw new BadRequestAlertException("A new relative cannot already have an ID", ENTITY_NAME, "idexists");
        }

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null) {
            throw new BadRequestAlertException("Vous devez être connecté pour ajouter un proche", ENTITY_NAME, "unauthorized");
        }

        RelativeDTO result = relativeService.saveForUser(relativeDTO, currentLogin);

        // Si un numéro est renseigné, envoyer une notification SMS au proche
        if (result.getPhone() != null && !result.getPhone().isBlank()) {
            try {
                String sender = currentLogin;
                String name = result.getName() != null && !result.getName().isBlank() ? result.getName() : "bonjour";
                String msg = String.format(
                    "Fotolou : Bonjour %s, vous avez ete ajoute(e) comme proche sur Fotolou par %s. Vos alertes de tickets vous seront transmises sur ce numero.",
                    name,
                    sender
                );
                smsService.sendSms(result.getPhone().trim(), msg);
            } catch (Exception e) {
                LOG.warn("Impossible d'envoyer le SMS au proche {}: {}", result.getPhone(), e.getMessage());
            }
        }

        return ResponseEntity.created(new URI("/api/relatives/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /relatives/:id} : Updates an existing relative.
     *
     * @param id the id of the relativeDTO to save.
     * @param relativeDTO the relativeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated relativeDTO,
     * or with status {@code 400 (Bad Request)} if the relativeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the relativeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RelativeDTO> updateRelative(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody RelativeDTO relativeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Relative : {}, {}", id, relativeDTO);
        if (relativeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, relativeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            if (!relativeService.existsById(id)) {
                throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
            }
            relativeDTO = relativeService.update(relativeDTO);
            return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, relativeDTO.getId().toString()))
                .body(relativeDTO);
        }

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null) {
            throw new BadRequestAlertException("Non autorisé", ENTITY_NAME, "unauthorized");
        }

        if (!relativeService.existsByIdAndUser(id, currentLogin)) {
            throw new BadRequestAlertException("Ce proche est introuvable ou ne vous appartient pas", ENTITY_NAME, "notowned");
        }

        relativeDTO = relativeService.updateForUser(relativeDTO, currentLogin);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, relativeDTO.getId().toString()))
            .body(relativeDTO);
    }

    /**
     * {@code PATCH  /relatives/:id} : Partial updates given fields of an existing relative, field will ignore if it is null
     *
     * @param id the id of the relativeDTO to save.
     * @param relativeDTO the relativeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated relativeDTO,
     * or with status {@code 400 (Bad Request)} if the relativeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the relativeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the relativeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<RelativeDTO> partialUpdateRelative(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody RelativeDTO relativeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Relative partially : {}, {}", id, relativeDTO);
        if (relativeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, relativeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            if (!relativeService.existsById(id)) {
                throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
            }
            Optional<RelativeDTO> result = relativeService.partialUpdate(relativeDTO);
            return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, relativeDTO.getId().toString())
            );
        }

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null) {
            throw new BadRequestAlertException("Non autorisé", ENTITY_NAME, "unauthorized");
        }

        if (!relativeService.existsByIdAndUser(id, currentLogin)) {
            throw new BadRequestAlertException("Ce proche est introuvable ou ne vous appartient pas", ENTITY_NAME, "notowned");
        }

        Optional<RelativeDTO> result = relativeService.partialUpdateForUser(relativeDTO, currentLogin);
        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, relativeDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /relatives} : get all the Relatives for the current user (or all if admin).
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Relatives in body.
     */
    @GetMapping("")
    public ResponseEntity<List<RelativeDTO>> getAllRelatives(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Relatives");

        // Admins can see all relatives
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            Page<RelativeDTO> page = relativeService.findAll(pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        }

        // Regular users (clients) strictly see ONLY their own relatives
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null) {
            return ResponseEntity.ok().body(Collections.emptyList());
        }

        Page<RelativeDTO> page = relativeService.findAllForUser(currentLogin, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /relatives/:id} : get the "id" relative for the current user (or if admin).
     *
     * @param id the id of the relativeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the relativeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RelativeDTO> getRelative(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Relative : {}", id);

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            Optional<RelativeDTO> relativeDTO = relativeService.findOne(id);
            return ResponseUtil.wrapOrNotFound(relativeDTO);
        }

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null) {
            return ResponseEntity.notFound().build();
        }

        Optional<RelativeDTO> relativeDTO = relativeService.findOneForUser(id, currentLogin);
        return ResponseUtil.wrapOrNotFound(relativeDTO);
    }

    /**
     * {@code DELETE  /relatives/:id} : delete the "id" relative for current user.
     *
     * @param id the id of the relativeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRelative(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Relative : {}", id);

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            relativeService.delete(id);
            return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                .build();
        }

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null) {
            throw new BadRequestAlertException("Non autorisé", ENTITY_NAME, "unauthorized");
        }

        if (!relativeService.existsByIdAndUser(id, currentLogin)) {
            throw new BadRequestAlertException("Ce proche est introuvable ou ne vous appartient pas", ENTITY_NAME, "notowned");
        }

        relativeService.deleteForUser(id, currentLogin);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
