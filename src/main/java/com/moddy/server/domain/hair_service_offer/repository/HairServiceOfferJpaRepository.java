package com.moddy.server.domain.hair_service_offer.repository;

import com.moddy.server.domain.hair_service_offer.HairServiceOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HairServiceOfferJpaRepository extends JpaRepository<HairServiceOffer, Long> {
    Page<HairServiceOffer> findByModelId(Long modelId, Pageable pageable);

    Optional<HairServiceOffer> findByHairModelApplicationIdAndModelId(Long applicationId, Long modelId);

    Boolean existsByModelId(Long modelId);

    List<HairServiceOffer> findAllByModelId(Long modelId);

    List<HairServiceOffer> findAllByDesignerId(Long userId);
}
