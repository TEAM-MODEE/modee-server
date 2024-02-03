package com.moddy.server.service.offer;

import com.moddy.server.common.exception.enums.ErrorCode;
import com.moddy.server.common.exception.model.NotFoundException;
import com.moddy.server.controller.model.dto.DesignerInfoOpenChatDto;
import com.moddy.server.controller.model.dto.response.DesignerInfoOpenChatResponse;
import com.moddy.server.controller.model.dto.response.OfferResponse;
import com.moddy.server.controller.model.dto.response.OpenChatResponse;
import com.moddy.server.controller.offer.dto.response.ModelMainOfferResponse;
import com.moddy.server.domain.designer.Designer;
import com.moddy.server.domain.hair_service_offer.HairServiceOffer;
import com.moddy.server.domain.hair_service_offer.repository.HairServiceOfferJpaRepository;
import com.moddy.server.domain.model.ModelApplyStatus;
import com.moddy.server.domain.prefer_offer_condition.PreferOfferCondition;
import com.moddy.server.domain.prefer_offer_condition.repository.PreferOfferConditionJpaRepository;
import com.moddy.server.service.application.HairModelApplicationRetrieveService;
import com.moddy.server.service.designer.DesignerRetrieveService;
import com.moddy.server.service.model.ModelRetrieveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HairServiceOfferRetrieveService {

    private final HairServiceOfferJpaRepository hairServiceOfferJpaRepository;
    private final PreferOfferConditionJpaRepository preferOfferConditionJpaRepository;
    private final DesignerRetrieveService designerRetrieveService;
    private final HairModelApplicationRetrieveService hairModelApplicationRetrieveService;
    private final ModelRetrieveService modelRetrieveService;

    public OpenChatResponse getOpenChatInfo(final Long userId, final Long offerId) {
        HairServiceOffer hairServiceOffer = hairServiceOfferJpaRepository.findById(offerId).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUNT_OFFER_EXCEPTION));

        Long designerId = hairServiceOffer.getDesigner().getId();
        Long applicationId = hairServiceOffer.getHairModelApplication().getId();

        DesignerInfoOpenChatDto openChatDto = designerRetrieveService.getDesignerOpenDetail(designerId);

        DesignerInfoOpenChatResponse response = new DesignerInfoOpenChatResponse(openChatDto.imgUrl(), openChatDto.shopName(), openChatDto.name(), openChatDto.introduction());

        OpenChatResponse openChatResponse = new OpenChatResponse(hairModelApplicationRetrieveService.getApplicationCaptureUrl(applicationId), openChatDto.kakaoUrl(), response);

        return openChatResponse;
    }

    public ModelMainOfferResponse getModelMainOfferInfo(final Long modelId, final int page, final int size) {
        String modelName = modelRetrieveService.getModelName(modelId);

        Page<HairServiceOffer> offerPage = findOffersByPaging(modelId, page, size);
        long totalElements = offerPage.getTotalElements();

        ModelApplyStatus modelApplyStatus = calModelApplyAndOfferStatus(modelId);
        if (modelApplyStatus != ModelApplyStatus.APPLY_AND_OFFER) {
            return new ModelMainOfferResponse(page, size, totalElements, modelApplyStatus, modelName, new ArrayList<>());
        }
        return new ModelMainOfferResponse(page, size, totalElements, modelApplyStatus, modelName, getModelMainOfferList(offerPage));
    }

    private ModelApplyStatus calModelApplyAndOfferStatus(final Long modelId) {
        boolean applyStatus = hairModelApplicationRetrieveService.fetchModelApplyStatus(modelId);
        boolean offerStatus = hairServiceOfferJpaRepository.existsByModelId(modelId);

        if (!applyStatus && !offerStatus) return ModelApplyStatus.NOTHING;
        else if (applyStatus && !offerStatus) return ModelApplyStatus.APPLY;
        else if (applyStatus && offerStatus) return ModelApplyStatus.APPLY_AND_OFFER;
        else throw new NotFoundException(ErrorCode.NOT_FOUND_MODEL_STATUS);
    }

    private List<OfferResponse> getModelMainOfferList(final Page<HairServiceOffer> offerPage) {
        List<OfferResponse> offerResponseList = offerPage.stream().map(offer -> {
            Designer designer = offer.getDesigner();
            List<PreferOfferCondition> preferOfferCondition = preferOfferConditionJpaRepository.findTop2ByHairServiceOfferId(offer.getId());
            List<String> offerConditionTop2List = preferOfferCondition.stream().map(offerCondition -> {
                return offerCondition.getOfferCondition().getValue();
            }).collect(Collectors.toList());

            OfferResponse offerResponse = new OfferResponse(offer.getId(), designer.getProfileImgUrl(), designer.getName(), designer.getHairShop().getName(), offerConditionTop2List, offer.getIsClicked());
            return offerResponse;
        }).collect(Collectors.toList());

        return offerResponseList;
    }

    private Page<HairServiceOffer> findOffersByPaging(final Long modelId, final int page, final int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<HairServiceOffer> offerPage = hairServiceOfferJpaRepository.findByModelId(modelId, pageRequest);

        return offerPage;
    }
}
