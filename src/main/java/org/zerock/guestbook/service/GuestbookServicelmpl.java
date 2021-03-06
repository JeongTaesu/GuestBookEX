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
        // requestDTO ???????????? ?????? ????????? type??? ??????
        String type = requestDTO.getType();
        // ????????? ???????????? ?????? BooleanBuilder ?????? ??????
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        // qGuestbook?????? guestbook ????????? ??????
        QGuestbook qGuestbook = QGuestbook.guestbook;
        // ????????? ????????? keyword??? ??????
        String keyword = requestDTO.getKeyword();
        // ?????? ????????? ????????? gno > 0 ??? ????????? ????????? ??????
        BooleanExpression expression = qGuestbook.gno.gt(0L);
        booleanBuilder.and(expression);
        // type??? ??????????????? ????????? ?????? ?????? ???????????? 0??? ?????? booleanBuilder ??????
        if(type == null || type.trim().length() == 0) {
            return booleanBuilder;
        }

        // ?????? ????????? if????????? qGuestbook??? keyword??? ????????? ???????????? ????????? ?????? ??? ??????
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
        // ??????????????? ????????? ?????? ?????? ??? return
        booleanBuilder.and(builder);

        return booleanBuilder;
    }
}
