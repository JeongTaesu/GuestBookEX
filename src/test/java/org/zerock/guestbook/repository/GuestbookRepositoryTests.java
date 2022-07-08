package org.zerock.guestbook.repository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.zerock.guestbook.entity.Guestbook;
import org.zerock.guestbook.entity.QGuestbook;

import java.util.Optional;
import java.util.stream.IntStream;

@SpringBootTest
public class GuestbookRepositoryTests {

    @Autowired
    private GuestbookRepository guestbookRepository;

    @Test
    public void insertDummies() {
        IntStream.rangeClosed(1,300).forEach(i -> {
            Guestbook guestbook = Guestbook.builder()
                    .title("Title... " + i)
                    .content("Content... " + i)
                    .writer("user" + (i % 10))
                    .build();
            System.out.println(guestbookRepository.save(guestbook));
        });
    }

    @Test
    public void updateTest() {
        Optional<Guestbook> result = guestbookRepository.findById(300L);

        if(result.isPresent()) {
            Guestbook guestbook = result.get();

            guestbook.changeTitle("Changed Title...");
            guestbook.changeContent("Changed Content...");

            guestbookRepository.save(guestbook);
        }
    }

    @Test
    public void testQuery1() {
        // 테스트를 진행할 데이터 범위 지정
        Pageable pageable = PageRequest.of(0, 10, Sort.by("gno").descending());
        // QueryDSL로 생성된 Q도메인 객체를 변수로 저장
        QGuestbook qGuestbook = QGuestbook.guestbook;
        // 검색을 진행할 조건 정의
        String keyword = "1";
        BooleanBuilder builder = new BooleanBuilder();
        // Q도메인이 가지고 있는 title 항목에 contains 메서드를 활용하여 keyword를 포함하고 있는지 Bool값으로 저장
        BooleanExpression expression = qGuestbook.title.contains(keyword);
        // 단일 항목 검사이기에 And를 활용하여 BooleanBuilder에 검색 조건을 저장
        builder.and(expression);
        // DB에서 검사할 조건과 데이터 범위를 매개변수로 전달하여 검색을 진행
        Page<Guestbook> result = guestbookRepository.findAll(builder, pageable);
        // 검색 조건에 충족하는 객체를 반환하여 출력
        result.stream().forEach(guestbook -> {
            System.out.println(guestbook);
        });
    }

    @Test
    public void testQuery2() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("gno").descending());

        QGuestbook qGuestbook = QGuestbook.guestbook;
        String keyword = "1";
        BooleanBuilder builder = new BooleanBuilder();
        // 단일 항목과 동일하게 검색 조건을 정의 하지만 다중 조건이기에 BooleanExpression 객체를 2개 활용하여
        // 2가지 조건을 BooleanBuilder에 저장
        BooleanExpression exTitle = qGuestbook.title.contains(keyword);
        BooleanExpression exContent = qGuestbook.content.contains(keyword);
        BooleanExpression exAll = exTitle.or(exContent);
        builder.and(exAll);

        builder.and(qGuestbook.gno.gt(0L));

        Page<Guestbook> result = guestbookRepository.findAll(builder, pageable);

        result.stream().forEach(guestbook -> {
            System.out.println(guestbook);
        });
    }
}
