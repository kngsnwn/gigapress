package etners.standard.mvc.jpa.epcScodeMstTest.repo;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import etners.standard.mvc.test.domain.response.TestListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import static etners.standard.mvc.jpa.epcScodeMstTest.entity.QEpcScodeMstTest.epcScodeMstTest;

import java.util.List;


@RequiredArgsConstructor
@Repository
public class EpcScodeMstTestRepositoryForQueryDsl {

  private final JPAQueryFactory queryFactory;

  public Page<TestListResponse> findByQuerydslForAllCode(BooleanBuilder builder, Pageable pageable,
                                                         OrderSpecifier[] orderArray, String requestType) {


    JPAQuery<TestListResponse> resultQuery = queryFactory.select(
        Projections.fields(
            TestListResponse.class,
            epcScodeMstTest.solCd.stringValue().as("solCd"),
            epcScodeMstTest.cdGrup,
            epcScodeMstTest.cdGrupAs,
            epcScodeMstTest.cdGrupNm,
            epcScodeMstTest.cdDesc,
            epcScodeMstTest.cdDesc1,
            epcScodeMstTest.cdDesc2,
            epcScodeMstTest.cdDesc3,
            epcScodeMstTest.cdDesc4,
            epcScodeMstTest.cdDesc5,
            epcScodeMstTest.cmpyCd
        )
    ).from(epcScodeMstTest)
        .where(builder)
        .orderBy(orderArray);

    if ("PAGING".equals(requestType)) {
      resultQuery.limit(pageable.getPageSize()).offset(pageable.getOffset());
    }

    List<TestListResponse> list = resultQuery.fetch();

    JPAQuery<Long> countQuery = queryFactory.select(
        Wildcard.count
    )
        .from(epcScodeMstTest)
        .where(builder);

    return PageableExecutionUtils.getPage(list, pageable, countQuery::fetchOne);
  }
}
