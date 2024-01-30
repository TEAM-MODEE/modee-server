package com.moddy.server.service.designer;

import com.moddy.server.common.exception.enums.ErrorCode;
import com.moddy.server.common.exception.model.NotFoundException;
import com.moddy.server.controller.model.dto.response.DesignerInfoOpenChatResponse;
import com.moddy.server.controller.model.dto.DesignerInfoOpenChatDto;
import com.moddy.server.domain.designer.Designer;
import com.moddy.server.domain.designer.repository.DesignerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesignerRetrieveService {

    private final DesignerJpaRepository designerJpaRepository;
    public DesignerInfoOpenChatDto getDesignerOpenDetail(final Long userId){
        Designer designer = designerJpaRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCode.DESIGNER_NOT_FOUND_EXCEPTION));
        return new DesignerInfoOpenChatDto(designer.getKakaoOpenChatUrl(),designer.getProfileImgUrl(), designer.getHairShop().getName(), designer.getName(), designer.getIntroduction());
    }

}