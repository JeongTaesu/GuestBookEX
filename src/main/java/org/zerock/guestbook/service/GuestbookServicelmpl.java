package org.zerock.guestbook.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.zerock.guestbook.dto.GuestbookDTO;
import org.zerock.guestbook.dto.PageRequestDTO;
import org.zerock.guestbook.dto.PageResultDTO;
import org.zerock.guestbook.entity.Guestbook;
import org.zerock.guestbook.entity.QGuestbook;
import org.zerock.guestbook.repository.GuestbookRepository;

import java.util.Optional;
import java.util.function.Function;

@Service
@Log4j2
@RequiredArgsConstructor
public class GuestbookServicelmpl  implements GuestbookService{
    private  final GuestbookRepository guestbookRepository;
    @Override
    public Long register(GuestbookDTO dto) {

        log.info("DTO__________________________");
        log.info(dto);

        Guestbook entity = dtoToEntity(dto);
        log.info(entity);
        guestbookRepository.save(entity);
        return  null;
    }

    @Override
    public void remove(Long gno) {
        guestbookRepository.deleteById(gno);
    }

    @Override
    public void modify(GuestbookDTO dto) {
        Optional<Guestbook> result = guestbookRepository.findById(dto.getGno());

        if(result.isPresent()) {
            Guestbook entity = result.get();

            entity.changeTitle(dto.getTitle());
            entity.changeContent(dto.getContent());

            guestbookRepository.save(entity);
        }
    }

    @Override
    public GuestbookDTO read(Long gno) {
        Optional<Guestbook> result = guestbookRepository.findById(gno);
        return result.isPresent() ? entityToDto(result.get()) : null;
    }

    @Override
    public PageResultDTO<GuestbookDTO, Guestbook> getList(PageRequestDTO requestDTO) {
        Pageable pageable = requestDTO.getPageable(Sort.by("gno").descending());
        BooleanBuilder booleanBuilder = getSearch(requestDTO);


        Page<Guestbook> result = guestbookRepository.findAll(booleanBuilder, pageable);
        Function<Guestbook, GuestbookDTO> fn = (entity -> entityToDto(entity));
        return new PageResultDTO<>(result, fn);
    }

    private BooleanBuilder getSearch(PageRequestDTO requestDTO) {
        // requestDTO 객체에서 검색 조건을 type에 저장
        String type = requestDTO.getType();
        // 조건을 추가하기 위해 BooleanBuilder 객체 생성
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        // qGuestbook에서 guestbook 데이터 저장
        QGuestbook qGuestbook = QGuestbook.guestbook;
        // 검색한 단어를 keyword에 저장
        String keyword = requestDTO.getKeyword();
        // 검색 조건이 없다면 gno > 0 인 경우로 조건을 설정
        BooleanExpression expression = qGuestbook.gno.gt(0L);
        booleanBuilder.and(expression);
        // type이 비어있거나 공백을 제거 했을 경우에도 0인 경우 booleanBuilder 반환
        if(type == null || type.trim().length() == 0) {
            return booleanBuilder;
        }

        // 검색 조건을 if문으로 qGuestbook에 keyword를 포함한 데이터가 있는지 판별 후 저장
        BooleanBuilder builder = new BooleanBuilder();
        if(type.contains("t")) {
            builder.or(qGuestbook.title.contains(keyword));
        }
        if(type.contains("c")) {
            builder.or(qGuestbook.content.contains(keyword));
        }
        if(type.contains("w")) {
            builder.or(qGuestbook.writer.contains(keyword));
        }
        // 최종적으로 조건을 모두 저장 후 return
        booleanBuilder.and(builder);

        return booleanBuilder;
    }
}
