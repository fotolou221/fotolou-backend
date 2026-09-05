package com.fotolou.app.service;

import com.fotolou.app.service.dto.OtpVerificationDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.fotolou.app.domain.OtpVerification}.
 */
public interface OtpVerificationService {
    /**
     * Save a otpVerification.
     *
     * @param otpVerificationDTO the entity to save.
     * @return the persisted entity.
     */
    OtpVerificationDTO save(OtpVerificationDTO otpVerificationDTO);

    /**
     * Update a otpVerification.
     *
     * @param otpVerificationDTO the entity to save.
     * @return the persisted entity.
     */
    OtpVerificationDTO update(OtpVerificationDTO otpVerificationDTO);

    /**
     * Partially update a otpVerification.
     *
     * @param otpVerificationDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<OtpVerificationDTO> partialUpdate(OtpVerificationDTO otpVerificationDTO);

    /**
     * Get all the otpVerifications.
     *
     * @return the list of entities.
     */
    List<OtpVerificationDTO> findAll();

    /**
     * Get one otpVerification by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<OtpVerificationDTO> findOne(Long id);

    /**
     * Delete the otpVerification by id.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Check if an otpVerification exists by id.
     *
     * @param id the id of the entity.
     * @return true if exists.
     */
    boolean existsById(Long id);
}
