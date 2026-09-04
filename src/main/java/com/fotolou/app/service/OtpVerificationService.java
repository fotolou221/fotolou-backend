package com.fotolou.app.service;

import com.fotolou.app.domain.OtpVerification;
import com.fotolou.app.repository.OtpVerificationRepository;
import com.fotolou.app.service.dto.OtpVerificationDTO;
import com.fotolou.app.service.mapper.OtpVerificationMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fotolou.app.domain.OtpVerification}.
 */
@Service
@Transactional
public class OtpVerificationService {

    private static final Logger LOG = LoggerFactory.getLogger(OtpVerificationService.class);

    private final OtpVerificationRepository otpVerificationRepository;

    private final OtpVerificationMapper otpVerificationMapper;

    public OtpVerificationService(OtpVerificationRepository otpVerificationRepository, OtpVerificationMapper otpVerificationMapper) {
        this.otpVerificationRepository = otpVerificationRepository;
        this.otpVerificationMapper = otpVerificationMapper;
    }

    /**
     * Save a otpVerification.
     *
     * @param otpVerificationDTO the entity to save.
     * @return the persisted entity.
     */
    public OtpVerificationDTO save(OtpVerificationDTO otpVerificationDTO) {
        LOG.debug("Request to save OtpVerification : {}", otpVerificationDTO);
        OtpVerification otpVerification = otpVerificationMapper.toEntity(otpVerificationDTO);
        otpVerification = otpVerificationRepository.save(otpVerification);
        return otpVerificationMapper.toDto(otpVerification);
    }

    /**
     * Update a otpVerification.
     *
     * @param otpVerificationDTO the entity to save.
     * @return the persisted entity.
     */
    public OtpVerificationDTO update(OtpVerificationDTO otpVerificationDTO) {
        LOG.debug("Request to update OtpVerification : {}", otpVerificationDTO);
        OtpVerification otpVerification = otpVerificationMapper.toEntity(otpVerificationDTO);
        otpVerification = otpVerificationRepository.save(otpVerification);
        return otpVerificationMapper.toDto(otpVerification);
    }

    /**
     * Partially update a otpVerification.
     *
     * @param otpVerificationDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<OtpVerificationDTO> partialUpdate(OtpVerificationDTO otpVerificationDTO) {
        LOG.debug("Request to partially update OtpVerification : {}", otpVerificationDTO);

        return otpVerificationRepository
            .findById(otpVerificationDTO.getId())
            .map(existingOtpVerification -> {
                otpVerificationMapper.partialUpdate(existingOtpVerification, otpVerificationDTO);

                return existingOtpVerification;
            })
            .map(otpVerificationRepository::save)
            .map(otpVerificationMapper::toDto);
    }

    /**
     * Get all the otpVerifications.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<OtpVerificationDTO> findAll() {
        LOG.debug("Request to get all OtpVerifications");
        return otpVerificationRepository
            .findAll()
            .stream()
            .map(otpVerificationMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one otpVerification by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<OtpVerificationDTO> findOne(Long id) {
        LOG.debug("Request to get OtpVerification : {}", id);
        return otpVerificationRepository.findById(id).map(otpVerificationMapper::toDto);
    }

    /**
     * Delete the otpVerification by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete OtpVerification : {}", id);
        otpVerificationRepository.deleteById(id);
    }
}
