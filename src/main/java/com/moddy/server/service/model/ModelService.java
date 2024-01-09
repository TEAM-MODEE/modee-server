package com.moddy.server.service.model;


import com.moddy.server.common.exception.enums.ErrorCode;
import com.moddy.server.common.exception.model.NotFoundException;
import com.moddy.server.controller.model.dto.response.*;
import com.moddy.server.domain.day_off.DayOfWeek;
import com.moddy.server.domain.day_off.DayOff;
import com.moddy.server.domain.day_off.repository.DayOffJpaRespository;
import com.moddy.server.domain.designer.Designer;
import com.moddy.server.domain.designer.repository.DesignerJpaRepository;
import com.moddy.server.domain.hair_model_application.HairModelApplication;
import com.moddy.server.domain.hair_model_application.repository.HairModelApplicationJpaRepository;
import com.moddy.server.domain.hair_service_offer.HairServiceOffer;
import com.moddy.server.domain.hair_service_offer.repository.HairServiceOfferJpaRepository;
import com.moddy.server.domain.model.ModelApplyStatus;
import com.moddy.server.domain.model.repository.ModelJpaRepository;
import com.moddy.server.domain.prefer_hair_style.HairStyle;
import com.moddy.server.domain.prefer_hair_style.PreferHairStyle;
import com.moddy.server.domain.prefer_hair_style.repository.PreferHairStyleJpaRepository;
import com.moddy.server.domain.prefer_offer_condition.OfferCondition;
import com.moddy.server.domain.prefer_offer_condition.PreferOfferCondition;
import com.moddy.server.domain.prefer_offer_condition.repository.PreferOfferConditionJpaRepository;
import com.moddy.server.domain.user.User;
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
public class ModelService {

    private final ModelJpaRepository modelJpaRepository;
    private final DesignerJpaRepository designerJpaRepository;
    private final HairModelApplicationJpaRepository hairModelApplicationJpaRepository;
    private final HairServiceOfferJpaRepository hairServiceOfferJpaRepository;
    private final PreferOfferConditionJpaRepository preferOfferConditionJpaRepository;
    private final DayOffJpaRespository dayOffJpaRespository;
    private final PreferHairStyleJpaRepository preferHairStyleJpaRepository;

    private Page<HairServiceOffer> findOffers(Long userId, int page, int size){
        PageRequest pageRequest = PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC,"id"));
        Page<HairServiceOffer> offerPage = hairServiceOfferJpaRepository.findByUserId(userId, pageRequest);

        return offerPage;
    }

    private ModelApplyStatus calModelStatus(boolean apply, boolean offer){

        if(!apply && !offer) return ModelApplyStatus.NOTHING;
        else if (apply && !offer) return ModelApplyStatus.APPLY;
        else if (apply && offer) return ModelApplyStatus.APPLY_AND_OFFER;
        else throw new NotFoundException(ErrorCode.NOT_FOUND_MODEL_STATUS);

    }

    public ModelMainResponse getModelMainInfo(Long userId, int page, int size){

        User user = modelJpaRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_MODEL_INFO));

        Page<HairServiceOffer> offerPage = findOffers(userId, page, size);

        boolean applyStatus = hairModelApplicationJpaRepository.existsByUserId(userId);
        boolean offerStatus = hairServiceOfferJpaRepository.existsByUserId(userId);
        ModelApplyStatus modelApplyStatus = calModelStatus(applyStatus, offerStatus);

        if(modelApplyStatus != ModelApplyStatus.APPLY_AND_OFFER){
            return new ModelMainResponse(
                    page,
                    size,
                    modelApplyStatus,
                    user.getName(),
                    new ArrayList<>()
            );
        }

        List<OfferResponse> offerResponseList = offerPage.stream().map(offer -> {
            Designer designer = offer.getDesigner();
            List<PreferOfferCondition> preferOfferCondition = preferOfferConditionJpaRepository.findTop2ByHairServiceOfferId(offer.getId());
            List<OfferCondition> offerConditionTop2List = preferOfferCondition.stream().map(PreferOfferCondition::getOfferCondition).collect(Collectors.toList());

            OfferResponse offerResponse = new OfferResponse(
                    offer.getId(),
                    designer.getProfileImgUrl(),
                    designer.getName(),
                    designer.getHairShop().getName(),
                    offerConditionTop2List,
                    offer.getIsClicked()
            );
            return offerResponse;
        }).collect(Collectors.toList());

        return new ModelMainResponse(
                page,
                size,
                modelApplyStatus,
                user.getName(),
                offerResponseList
        );
    }

    public DetailOfferResponse getModelDetailOfferInfo(Long userId, Long offerId){

        Designer designer = designerJpaRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.DESIGNER_NOT_FOUND_EXCEPTION));
        List<DayOff> dayOffList = dayOffJpaRespository.findByUserId(userId);

        List<String> dayOfWeekList = dayOffList.stream().map(dayOff -> {
            return dayOff.getDayOfWeek().getValue();
        }).collect(Collectors.toList());

        DesignerInfoResponse designerInfoResponseList = new DesignerInfoResponse(
                designer.getProfileImgUrl(),
                designer.getHairShop().getName(),
                designer.getName(),
                designer.getPortfolio().getInstagramUrl(),
                designer.getPortfolio().getNaverPlaceUrl(),
                designer.getIntroduction(),
                designer.getGender(),
                dayOfWeekList,
                designer.getHairShop().getAddress(),
                designer.getHairShop().getDetailAddress()
        );


        HairServiceOffer hairServiceOffer = hairServiceOfferJpaRepository.findById(offerId).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_OFFER_EXCEPTION));
        HairModelApplication hairModelApplication = hairModelApplicationJpaRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_APPLICATION_EXCEPTION));
        List<PreferHairStyle> preferHairStyles = preferHairStyleJpaRepository.findAllById(hairServiceOffer.getId());
        List<HairStyle> hairStyleList = preferHairStyles.stream().map(hairStyle -> {
            return hairStyle.getHairStyle();
        }).collect(Collectors.toList());


        List<Boolean> preferOfferConditionList = hairStyleList.stream().map(hairStyle ->{
            if(HairStyle.values().equals(hairStyle)) return true;
            else return false;
        }).collect(Collectors.toList());


        StyleDetailResponse styleDetailResponse = new StyleDetailResponse(
                hairServiceOffer.getIsModelAgree(),
                preferHairStyles,
                hairServiceOffer.getOfferDetail(),
                hairModelApplication.getHairDetail(),
                preferOfferConditionList
        );


        return new DetailOfferResponse(designerInfoResponseList, styleDetailResponse);
    }

}
