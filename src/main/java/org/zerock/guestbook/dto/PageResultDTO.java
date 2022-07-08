package org.zerock.guestbook.dto;


import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class PageResultDTO<DTO, EN> {
    private List<DTO> dtoList;

    private int totalPage;

    private int page;

    private int size;

    private int start, end;

    private  boolean prev, next;

    private List<Integer> pageList;
    public PageResultDTO(Page<EN> result, Function<EN, DTO> fn) {
        dtoList = result.stream().map(fn).collect(Collectors.toList());

        totalPage = result.getTotalPages();

        makePageList(result.getPageable());
    }
    private void makePageList(Pageable pageable) {
        this.page = pageable.getPageNumber() + 1;
        this.size = pageable.getPageSize();

        // 전페 페이지의 끝 번호를 구하는 방법으로 Math.ceil을 이용하면 소수점 올림으로 게산을 하기에
        // EX ) 1페이지의 경우는 Math.ceil( 1 / 10.0)) * 10 >> Math.ceil(0.1) * 10 = 10
        // 이런 식으로 올림으로 계산해서 10개의 페이지를 볼 수 있도록 설정
        int tempEnd = (int)(Math.ceil(page/10.0)) * 10;
        
        // 화면을 10개씩만 보여주기로 계산 했기 때문에 시작 위치는 마지막페이지에서 9를 뺀 값
        start = tempEnd - 9;
        // 시작 하는 페이지의 수가 1보다 크다면 이전 버튼을 표시하기 위해 bool 값으로 저장
        prev = start > 1;
        // 끝나는 페이지는 데이터가 15페이지까지 있다는 가정했을 경우 15까지만 표현을 해야하기 때문에 값을 비교해서
        // 마지막 페이지까지만 출력 되도록 설정
        end = totalPage > tempEnd ? tempEnd : totalPage;
        // 다음으로 이동하는 버튼의 경우 끝번호보다 큰 경우에만 출력 되어야 하기때문에 bool 값으로 저장
        next = totalPage > tempEnd;
        // 계산한 페이지 리스트를 stream으로 전달
        pageList = IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
    }
}
