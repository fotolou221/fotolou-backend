package com.fotolou.app.service.impl;

import com.fotolou.app.domain.Relative;
import com.fotolou.app.repository.RelativeRepository;
import com.fotolou.app.service.RelativeService;
import com.fotolou.app.service.UserService;
import com.fotolou.app.service.dto.RelativeDTO;
import com.fotolou.app.service.mapper.RelativeMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fotolou.app.domain.Relative}.
 */
@Service
@Transactional
public class RelativeServiceImpl implements RelativeService {

    private static final Logger LOG = LoggerFactory.getLogger(RelativeServiceImpl.class);

    private final RelativeRepository relativeRepository;
    private final RelativeMapper relativeMapper;
    private final UserService userService;

    public RelativeServiceImpl(RelativeRepository relativeRepository, RelativeMapper relativeMapper, UserService userService) {
        this.relativeRepository = relativeRepository;
        this.relativeMapper = relativeMapper;
        this.userService = userService;
    }

    @Override
    public RelativeDTO save(RelativeDTO relativeDTO) {
        LOG.debug("Request to save Relative : {}", relativeDTO);
        if (relativeDTO.getUser() == null) {
            com.fotolou.app.security.SecurityUtils.getCurrentUserLogin()
                .flatMap(userService::findOneByLogin)
                .ifPresent(u -> {
                    com.fotolou.app.service.dto.UserDTO userDTO = new com.fotolou.app.service.dto.UserDTO();
                    userDTO.setId(u.getId());
                    userDTO.setLogin(u.getLogin());
                    relativeDTO.setUser(userDTO);
                });
        }
        Relative relative = relativeMapper.toEntity(relativeDTO);
        relative = relativeRepository.save(relative);
        return relativeMapper.toDto(relative);
    }

    @Override
    public RelativeDTO update(RelativeDTO relativeDTO) {
        LOG.debug("Request to update Relative : {}", relativeDTO);
        Relative relative = relativeMapper.toEntity(relativeDTO);
        relative = relativeRepository.save(relative);
        return relativeMapper.toDto(relative);
    }

    @Override
    public Optional<RelativeDTO> partialUpdate(RelativeDTO relativeDTO) {
        LOG.debug("Request to partially update Relative : {}", relativeDTO);

        return relativeRepository
            .findById(relativeDTO.getId())
            .map(existingRelative -> {
                relativeMapper.partialUpdate(existingRelative, relativeDTO);
                return existingRelative;
            })
            .map(relativeRepository::save)
            .map(relativeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RelativeDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Relatives");
        return relativeRepository.findAll(pageable).map(relativeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RelativeDTO> findOne(Long id) {
        LOG.debug("Request to get Relative : {}", id);
        return relativeRepository.findById(id).map(relativeMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Relative : {}", id);
        relativeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return relativeRepository.existsById(id);
    }
}
