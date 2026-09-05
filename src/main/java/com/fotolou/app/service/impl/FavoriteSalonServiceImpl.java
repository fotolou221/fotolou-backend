package com.fotolou.app.service.impl;

import com.fotolou.app.domain.FavoriteSalon;
import com.fotolou.app.repository.FavoriteSalonRepository;
import com.fotolou.app.service.FavoriteSalonService;
import com.fotolou.app.service.dto.FavoriteSalonDTO;
import com.fotolou.app.service.mapper.FavoriteSalonMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.fotolou.app.domain.FavoriteSalon}.
 */
@Service
@Transactional
public class FavoriteSalonServiceImpl implements FavoriteSalonService {

    private static final Logger LOG = LoggerFactory.getLogger(FavoriteSalonServiceImpl.class);

    private final FavoriteSalonRepository favoriteSalonRepository;
    private final FavoriteSalonMapper favoriteSalonMapper;

    public FavoriteSalonServiceImpl(FavoriteSalonRepository favoriteSalonRepository, FavoriteSalonMapper favoriteSalonMapper) {
        this.favoriteSalonRepository = favoriteSalonRepository;
        this.favoriteSalonMapper = favoriteSalonMapper;
    }

    @Override
    public FavoriteSalonDTO save(FavoriteSalonDTO favoriteSalonDTO) {
        LOG.debug("Request to save FavoriteSalon : {}", favoriteSalonDTO);
        FavoriteSalon favoriteSalon = favoriteSalonMapper.toEntity(favoriteSalonDTO);
        favoriteSalon = favoriteSalonRepository.save(favoriteSalon);
        return favoriteSalonMapper.toDto(favoriteSalon);
    }

    @Override
    public FavoriteSalonDTO update(FavoriteSalonDTO favoriteSalonDTO) {
        LOG.debug("Request to update FavoriteSalon : {}", favoriteSalonDTO);
        FavoriteSalon favoriteSalon = favoriteSalonMapper.toEntity(favoriteSalonDTO);
        favoriteSalon = favoriteSalonRepository.save(favoriteSalon);
        return favoriteSalonMapper.toDto(favoriteSalon);
    }

    @Override
    public Optional<FavoriteSalonDTO> partialUpdate(FavoriteSalonDTO favoriteSalonDTO) {
        LOG.debug("Request to partially update FavoriteSalon : {}", favoriteSalonDTO);

        return favoriteSalonRepository
            .findById(favoriteSalonDTO.getId())
            .map(existingFavoriteSalon -> {
                favoriteSalonMapper.partialUpdate(existingFavoriteSalon, favoriteSalonDTO);
                return existingFavoriteSalon;
            })
            .map(favoriteSalonRepository::save)
            .map(favoriteSalonMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteSalonDTO> findAll() {
        LOG.debug("Request to get all FavoriteSalons");
        return favoriteSalonRepository.findAll().stream().map(favoriteSalonMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FavoriteSalonDTO> findOne(Long id) {
        LOG.debug("Request to get FavoriteSalon : {}", id);
        return favoriteSalonRepository.findById(id).map(favoriteSalonMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete FavoriteSalon : {}", id);
        favoriteSalonRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return favoriteSalonRepository.existsById(id);
    }
}
