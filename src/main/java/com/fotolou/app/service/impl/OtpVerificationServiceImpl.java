package com.fotolou.app.service.impl;

import com.fotolou.app.domain.OtpVerification;
import com.fotolou.app.repository.OtpVerificationRepository;
import com.fotolou.app.service.OtpVerificationService;
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
public class OtpVerificationServiceImpl implements OtpVerificationService {

    private static final Logger LOG = LoggerFactory.getLogger(OtpVerificationServiceImpl.class);

    private final OtpVerificationRepository otpVerificationRepository;
    private final OtpVerificationMapper otpVerificationMapper;

    public OtpVerificationServiceImpl(OtpVerificationRepository otpVerificationRepository, OtpVerificationMapper otpVerificationMapper) {
        this.otpVerificationRepository = otpVerificationRepository;
        this.otpVerificationMapper = otpVerificationMapper;
    }

    @Override
    public OtpVerificationDTO save(OtpVerificationDTO otpVerificationDTO) {
        LOG.debug("Request to save OtpVerification : {}", otpVerificationDTO);
        OtpVerification otpVerification = otpVerificationMapper.toEntity(otpVerificationDTO);
        otpVerification = otpVerificationRepository.save(otpVerification);
        return otpVerificationMapper.toDto(otpVerification);
    }

    @Override
    public OtpVerificationDTO update(OtpVerificationDTO otpVerificationDTO) {
        LOG.debug("Request to update OtpVerification : {}", otpVerificationDTO);
        OtpVerification otpVerification = otpVerificationMapper.toEntity(otpVerificationDTO);
        otpVerification = otpVerificationRepository.save(otpVerification);
        return otpVerificationMapper.toDto(otpVerification);
    }

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public List<OtpVerificationDTO> findAll() {
        LOG.debug("Request to get all OtpVerifications");
        return otpVerificationRepository
            .findAll()
            .stream()
            .map(otpVerificationMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OtpVerificationDTO> findOne(Long id) {
        LOG.debug("Request to get OtpVerification : {}", id);
        return otpVerificationRepository.findById(id).map(otpVerificationMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete OtpVerification : {}", id);
        otpVerificationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return otpVerificationRepository.existsById(id);
    }
}
