package com.fotolou.app.web.rest;

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

    private final SalonQueryService salonQueryService;

    private final com.fotolou.app.service.custom.salon.SalonCustomService salonCustomService;

    private final com.fotolou.app.service.custom.realtime.RealtimeEventService realtimeEventService;
    private final com.fotolou.app.repository.UserRepository userRepository;
    private final com.fotolou.app.repository.AuthorityRepository authorityRepository;
    private final com.fotolou.app.repository.CoiffeurProfileRepository coiffeurProfileRepository;
    private final com.fotolou.app.repository.SalonRepository salonRepository;
    private final com.fotolou.app.repository.TicketRepository ticketRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.fotolou.app.service.mapper.SalonMapper salonMapper;

    public SalonResource(
        SalonService salonService,
        SalonQueryService salonQueryService,
        com.fotolou.app.service.custom.salon.SalonCustomService salonCustomService,
        com.fotolou.app.service.custom.realtime.RealtimeEventService realtimeEventService,
        com.fotolou.app.repository.UserRepository userRepository,
        com.fotolou.app.repository.AuthorityRepository authorityRepository,
        com.fotolou.app.repository.CoiffeurProfileRepository coiffeurProfileRepository,
        com.fotolou.app.repository.SalonRepository salonRepository,
        com.fotolou.app.repository.TicketRepository ticketRepository,
        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
        com.fotolou.app.service.mapper.SalonMapper salonMapper
    ) {
        this.salonService = salonService;
        this.salonQueryService = salonQueryService;
        this.salonCustomService = salonCustomService;
        this.realtimeEventService = realtimeEventService;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.coiffeurProfileRepository = coiffeurProfileRepository;
        this.salonRepository = salonRepository;
        this.ticketRepository = ticketRepository;
        this.passwordEncoder = passwordEncoder;
        this.salonMapper = salonMapper;
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

        String normalizedPhone = normalizePhoneForAccount(salonDTO.getPhone());
        if (!normalizedPhone.isBlank()) {
            if (phoneAlreadyUsed(normalizedPhone)) {
                throw new BadRequestAlertException(
                    "Ce numéro de téléphone est déjà utilisé par un autre utilisateur.",
                    ENTITY_NAME,
                    "phonealreadyused"
                );
            }
            salonDTO.setPhone(normalizedPhone);
        }

        final String ownerDisplayName = normalizeOwnerName(salonDTO.getOwnerName(), salonDTO.getCoiffeurName());
        if (!ownerDisplayName.isBlank()) {
            salonDTO.setOwnerName(ownerDisplayName);
            salonDTO.setCoiffeurName(ownerDisplayName);
        }

        salonDTO = salonService.save(salonDTO);

        // Auto-provision ou association du compte Coiffeur Propriétaire avec ROLE_COIFFEUR
        if (salonDTO.getPhone() != null && !salonDTO.getPhone().isBlank()) {
            final String cleanPhone = normalizePhoneForAccount(salonDTO.getPhone());
            final String salonName = salonDTO.getName();
            com.fotolou.app.domain.Salon salonEntity = salonRepository.findById(salonDTO.getId()).orElse(null);

            if (salonEntity != null) {
                com.fotolou.app.domain.User ownerUser = userRepository.findOneWithAuthoritiesByLogin(cleanPhone).orElseGet(() -> {
                    com.fotolou.app.domain.User u = new com.fotolou.app.domain.User();
                    u.setLogin(cleanPhone);
                    u.setPassword(passwordEncoder.encode(cleanPhone + "_fotolou_secret_key"));
                    applyUserDisplayName(u, ownerDisplayName, "Coiffeur Proprietaire");
                    u.setEmail(cleanPhone.replace("+", "") + "@fotolou.sn");
                    u.setActivated(true);
                    u.setLangKey("fr");
                    return u;
                });

                java.util.Set<com.fotolou.app.domain.Authority> auths = new java.util.HashSet<>(ownerUser.getAuthorities());
                authorityRepository.findById(com.fotolou.app.security.AuthoritiesConstants.COIFFEUR).ifPresent(auths::add);
                authorityRepository.findById(com.fotolou.app.security.AuthoritiesConstants.USER).ifPresent(auths::add);
                auths.removeIf(a -> com.fotolou.app.security.AuthoritiesConstants.CLIENT.equals(a.getName()));
                ownerUser.setAuthorities(auths);
                if (!ownerDisplayName.isBlank()) {
                    applyUserDisplayName(ownerUser, ownerDisplayName, userDisplayName(ownerUser));
                }
                ownerUser = userRepository.save(ownerUser);

                final com.fotolou.app.domain.User finalOwner = ownerUser;
                com.fotolou.app.domain.CoiffeurProfile profile = coiffeurProfileRepository.findByPhone(cleanPhone).orElseGet(() -> {
                    com.fotolou.app.domain.CoiffeurProfile cp = new com.fotolou.app.domain.CoiffeurProfile();
                    cp.setPhone(cleanPhone);
                    cp.setCreatedDate(java.time.Instant.now());
                    return cp;
                });
                profile.setName(!ownerDisplayName.isBlank() ? ownerDisplayName : fallbackOwnerName(finalOwner, salonName));
                profile.setUser(finalOwner);
                profile.setSalon(salonEntity);
                profile.setActive(true);
                coiffeurProfileRepository.save(profile);
            }
        }

        salonDTO = enrichOwnerInfo(salonDTO);
        realtimeEventService.broadcast("SALON_CREATED", salonDTO);
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

        if (!salonService.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        salonDTO = salonService.update(salonDTO);
        syncOwnerNameFromDto(salonDTO);
        salonDTO = enrichOwnerInfo(salonDTO);
        realtimeEventService.broadcast("SALON_UPDATED", salonDTO);
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
            salonDTO.setId(id);
        }
        if (!Objects.equals(id, salonDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!salonService.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SalonDTO> result = salonService.partialUpdate(salonDTO).map(dto -> {
            syncOwnerNameFromDto(salonDTO);
            return enrichOwnerInfo(dto);
        });
        result.ifPresent(dto -> realtimeEventService.broadcast("SALON_UPDATED", dto));

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, salonDTO.getId().toString())
        );
    }

    /**
     * {@code PUT /salons/:id/toggle-status} : Bascule l'état d'ouverture/fermeture du salon.
     */
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<SalonDTO> toggleSalonStatus(@PathVariable("id") String idOrSlug) {
        LOG.debug("REST request to toggle Salon status : {}", idOrSlug);

        Optional<com.fotolou.app.domain.Salon> salonOpt = Optional.empty();
        try {
            Long id = Long.parseLong(idOrSlug);
            salonOpt = salonRepository.findById(id);
        } catch (NumberFormatException ignored) {
            // Non-numeric value, fallback to slug lookup.
        }

        if (salonOpt.isEmpty()) {
            salonOpt = salonRepository.findOneBySlug(idOrSlug);
        }

        com.fotolou.app.domain.Salon salon = salonOpt.orElseThrow(() ->
            new BadRequestAlertException("Salon not found", ENTITY_NAME, "idnotfound")
        );

        com.fotolou.app.domain.enumeration.SalonStatus nextStatus =
            salon.getStatus() == com.fotolou.app.domain.enumeration.SalonStatus.OPEN
                ? com.fotolou.app.domain.enumeration.SalonStatus.CLOSED
                : com.fotolou.app.domain.enumeration.SalonStatus.OPEN;

        salon.setStatus(nextStatus);
        com.fotolou.app.domain.Salon saved = salonRepository.save(salon);
        SalonDTO dto = enrichOwnerInfo(salonMapper.toDto(saved));
        realtimeEventService.broadcast("SALON_UPDATED", dto);
        return ResponseEntity.ok(dto);
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
        page.getContent().forEach(dto -> {
            long liveWaiting = ticketRepository.countBySalonIdAndStatusIn(
                dto.getId(),
                List.of(com.fotolou.app.domain.enumeration.TicketStatus.WAITING, com.fotolou.app.domain.enumeration.TicketStatus.YOUR_TURN)
            );
            dto.setPeopleWaiting((int) liveWaiting);
            dto.setEstimatedWaitMinutes((int) liveWaiting * 20);
            enrichOwnerInfo(dto);
        });
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
     * {@code GET  /salons/:idOrSlug} : get the "id" or "slug" salon.
     *
     * @param idOrSlug the id or slug of the salonDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the salonDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{idOrSlug}")
    public ResponseEntity<SalonDTO> getSalon(@PathVariable("idOrSlug") String idOrSlug) {
        LOG.debug("REST request to get Salon by id or slug : {}", idOrSlug);
        Optional<SalonDTO> result = Optional.empty();
        try {
            Long id = Long.parseLong(idOrSlug);
            result = salonService.findOne(id);
        } catch (NumberFormatException ignored) {
            // Non-numeric ID, lookup by slug
        }
        if (result.isEmpty()) {
            result = salonCustomService.findBySlug(idOrSlug);
        }

        result.ifPresent(dto -> {
            long liveWaiting = ticketRepository.countBySalonIdAndStatusIn(
                dto.getId(),
                List.of(com.fotolou.app.domain.enumeration.TicketStatus.WAITING, com.fotolou.app.domain.enumeration.TicketStatus.YOUR_TURN)
            );
            dto.setPeopleWaiting((int) liveWaiting);
            dto.setEstimatedWaitMinutes((int) liveWaiting * 20);
            enrichOwnerInfo(dto);
        });

        return ResponseUtil.wrapOrNotFound(result);
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
        realtimeEventService.broadcast("SALON_DELETED", java.util.Map.of("id", id));
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    private SalonDTO enrichOwnerInfo(SalonDTO salonDTO) {
        if (salonDTO == null || salonDTO.getId() == null) {
            return salonDTO;
        }

        coiffeurProfileRepository
            .findBySalonId(salonDTO.getId())
            .stream()
            .findFirst()
            .ifPresent(profile -> {
                String ownerName = resolveOwnerDisplayName(profile, salonDTO.getName());
                if (!ownerName.isBlank()) {
                    salonDTO.setOwnerName(ownerName);
                    salonDTO.setCoiffeurName(ownerName);
                }
            });

        return salonDTO;
    }

    private void syncOwnerNameFromDto(SalonDTO salonDTO) {
        if (salonDTO == null || salonDTO.getId() == null) {
            return;
        }

        String ownerName = normalizeOwnerName(salonDTO.getOwnerName(), salonDTO.getCoiffeurName());
        if (ownerName.isBlank()) {
            return;
        }

        coiffeurProfileRepository.findBySalonId(salonDTO.getId()).forEach(profile -> {
            profile.setName(ownerName);
            if (profile.getUser() != null) {
                applyUserDisplayName(profile.getUser(), ownerName, userDisplayName(profile.getUser()));
                userRepository.save(profile.getUser());
            }
            coiffeurProfileRepository.save(profile);
        });
    }

    private String resolveOwnerDisplayName(com.fotolou.app.domain.CoiffeurProfile profile, String salonName) {
        if (profile == null) {
            return "";
        }

        String userName = normalizeOwnerName(userDisplayName(profile.getUser()));
        if (isRealOwnerName(userName, salonName)) {
            return userName;
        }

        String profileName = normalizeOwnerName(cleanGeneratedOwnerName(profile.getName(), salonName));
        if (isRealOwnerName(profileName, salonName)) {
            return profileName;
        }

        return "";
    }

    private String fallbackOwnerName(com.fotolou.app.domain.User user, String salonName) {
        String userName = userDisplayName(user);
        if (isRealOwnerName(userName, salonName)) {
            return userName;
        }
        return "Coiffeur Proprietaire";
    }

    private String userDisplayName(com.fotolou.app.domain.User user) {
        if (user == null) {
            return "";
        }

        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        return (firstName + (lastName.isBlank() ? "" : " " + lastName)).trim();
    }

    private void applyUserDisplayName(com.fotolou.app.domain.User user, String displayName, String fallbackName) {
        if (user == null) {
            return;
        }

        String cleanName = normalizeOwnerName(displayName);
        if (cleanName.isBlank()) {
            cleanName = normalizeOwnerName(fallbackName);
        }
        if (cleanName.isBlank()) {
            cleanName = "Coiffeur Proprietaire";
        }

        String[] parts = cleanName.split("\\s+", 2);
        user.setFirstName(parts[0]);
        user.setLastName(parts.length > 1 ? parts[1] : "");
    }

    private String cleanGeneratedOwnerName(String ownerName, String salonName) {
        String cleanName = ownerName != null ? ownerName.trim() : "";
        if (cleanName.isBlank()) {
            return "";
        }

        cleanName = cleanName.replace("(Propriétaire)", "").replace("(Proprietaire)", "").trim();
        if (salonName != null && !salonName.isBlank() && cleanName.equalsIgnoreCase(salonName.trim())) {
            return "";
        }
        return cleanName;
    }

    private boolean isRealOwnerName(String ownerName, String salonName) {
        String cleanName = normalizeOwnerName(ownerName);
        if (cleanName.isBlank()) {
            return false;
        }

        String normalized = cleanName.toLowerCase();
        String normalizedSalon = salonName != null ? salonName.trim().toLowerCase() : "";
        return (
            !normalized.equals("coiffeur proprietaire") &&
            !normalized.equals("barbier fotolou") &&
            (normalizedSalon.isBlank() ||
                (!normalized.equals(normalizedSalon) &&
                    !normalized.equals(normalizedSalon + " propriétaire") &&
                    !normalized.equals(normalizedSalon + " proprietaire")))
        );
    }

    private String normalizeOwnerName(String... names) {
        if (names == null) {
            return "";
        }

        for (String name : names) {
            if (name != null && !name.trim().isBlank()) {
                return name.trim().replaceAll("\\s+", " ");
            }
        }
        return "";
    }

    private boolean phoneAlreadyUsed(String normalizedPhone) {
        return (
            userRepository.findOneByLogin(normalizedPhone).isPresent() ||
            userRepository
                .findAll()
                .stream()
                .anyMatch(user -> samePhone(normalizedPhone, user.getLogin())) ||
            coiffeurProfileRepository.findByPhone(normalizedPhone).isPresent() ||
            coiffeurProfileRepository
                .findAll()
                .stream()
                .anyMatch(profile -> samePhone(normalizedPhone, profile.getPhone())) ||
            salonRepository
                .findAll()
                .stream()
                .anyMatch(salon -> samePhone(normalizedPhone, salon.getPhone()))
        );
    }

    private boolean samePhone(String normalizedPhone, String existingPhone) {
        return normalizedPhone.equals(normalizePhoneForAccount(existingPhone));
    }

    private String normalizePhoneForAccount(String rawPhone) {
        if (rawPhone == null) {
            return "";
        }

        String trimmed = rawPhone.trim();
        if (trimmed.isBlank()) {
            return "";
        }

        String compact = trimmed.replaceAll("[^0-9+]", "");
        String digits = compact.replaceAll("[^0-9]", "");
        if (digits.length() < 9) {
            return trimmed.replaceAll("\\s+", "");
        }

        if (digits.length() == 10 && digits.startsWith("0")) {
            digits = digits.substring(1);
        }

        if (compact.startsWith("00")) {
            return "+" + compact.substring(2);
        }
        if (compact.startsWith("+")) {
            return "+" + digits;
        }
        if (digits.startsWith("221")) {
            return "+" + digits;
        }
        if (digits.length() == 9) {
            return "+221" + digits;
        }
        return "+" + digits;
    }
}
