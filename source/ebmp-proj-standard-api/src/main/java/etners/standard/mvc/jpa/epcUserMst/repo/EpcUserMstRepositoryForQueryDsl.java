package etners.standard.mvc.jpa.epcUserMst.repo;


import static etners.standard.mvc.jpa.epcCmpyMst.entity.QEpcCmpyMst.epcCmpyMst;
import static etners.standard.mvc.jpa.epcUserMst.entity.epcUserMst.QEpcUserMst.epcUserMst;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import etners.common.util.expressions.CodeExpressions;
import etners.common.util.scope.CurrentUserData;
import etners.standard.mvc.test.domain.response.TestFirstAccessInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class EpcUserMstRepositoryForQueryDsl {

  private final JPAQueryFactory queryFactory;

  private final CurrentUserData currentUserData;

  public TestFirstAccessInfoResponse findByQuerydslForFirstAccessInfo(String cmpyCd,
    String unqUserId) {

    TestFirstAccessInfoResponse result = queryFactory.select(
        Projections.fields(
          TestFirstAccessInfoResponse.class,
          epcUserMst.cmpyCd,
          epcCmpyMst.cmpyNm,
          epcUserMst.empNm,
          epcUserMst.userId,
          epcUserMst.unqUserId,
          epcUserMst.deptCd,
          epcUserMst.spTelNo,
          epcUserMst.sabun,
          epcUserMst.email,
          epcUserMst.psnScn,
          CodeExpressions.solutionCodeDetailExpression("0000", "202", epcUserMst.psnScn,
            currentUserData.getEbmpLang(), "psnScnNm")
        )
      )
      .from(epcUserMst)
      .join(epcCmpyMst)
      .on(epcUserMst.cmpyCd.eq(epcCmpyMst.cmpyCd))
      .on(epcCmpyMst.useYn.eq("Y"))
      .where(epcUserMst.unqUserId.eq(unqUserId)
        .and(epcUserMst.cmpyCd.eq(cmpyCd))
        .and(epcUserMst.useYn.eq("Y")))
      .fetchOne();

    return result;
  }
}
