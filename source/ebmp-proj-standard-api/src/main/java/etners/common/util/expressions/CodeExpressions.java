package etners.common.util.expressions;

import static etners.ebmp.lib.jpa.entity.epc.epcCodeDtl.QEpcCodeDtl.epcCodeDtl;
import static etners.ebmp.lib.jpa.entity.epc.epcCodeDtlLan.QEpcCodeDtlLan.epcCodeDtlLan;
import static etners.ebmp.lib.jpa.entity.epc.epcScodeDtl.QEpcScodeDtl.epcScodeDtl;
import static etners.ebmp.lib.jpa.entity.epc.epcScodeDtlLan.QEpcScodeDtlLan.epcScodeDtlLan;
import static etners.standard.mvc.jpa.epcCmpyMst.entity.QEpcCmpyMst.epcCmpyMst;
import static etners.standard.mvc.jpa.epcDeptMst.entity.QEpcDeptMst.epcDeptMst;
import static etners.standard.mvc.jpa.epcUserMst.entity.epcUserMst.QEpcUserMst.epcUserMst;
import static java.util.Objects.isNull;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import common.util.string.StringUtil;
import etners.ebmp.lib.enums.lang.EbmpLang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeExpressions {

  private static final Logger LOGGER = LoggerFactory.getLogger(CodeExpressions.class);

  public static Expression<?> solutionCodeDetailExpression(String solCd, String cdGrup,
    StringExpression cdId, EbmpLang ebmpLang, String alias) {
    Expression<?> solutionCdNm = ExpressionUtils.as(Expressions.asString(""), alias); //빈값이 기본값
    if (StringUtil.isEmpty(cdGrup)) {
      return solutionCdNm;
    }
    try {
      if (StringUtil.isEmpty(solCd)) {
        solCd = "1019";
      }

      if (isNull(cdId)) {
        throw new IllegalArgumentException("잘못된 값입니다.");
      }

      if (StringUtil.isEmpty(alias)) {
        alias = alias.concat("Nm"); //ex) reqScn + Nm
      }

      StringPath codeDetailCdNm = null;

      switch (ebmpLang) {
        case EN:
          codeDetailCdNm = epcScodeDtlLan.cdEn;
          break;
        case JP:
          codeDetailCdNm = epcScodeDtlLan.cdJp;
          break;
        case VN:
          codeDetailCdNm = epcScodeDtlLan.cdVn;
          break;
        case ZH:
          codeDetailCdNm = epcScodeDtlLan.cdZh;
          break;
        default:
          codeDetailCdNm = epcScodeDtl.cdNm;
      }

      solutionCdNm = ExpressionUtils.as(
        JPAExpressions.select(codeDetailCdNm)
          .from(epcScodeDtl)
          .leftJoin(epcScodeDtlLan)
          .on(epcScodeDtl.solCd.eq(epcScodeDtlLan.solCd)
            .and(epcScodeDtl.cdGrup.eq(epcScodeDtlLan.cdGrup)
              .and(epcScodeDtl.cdId.eq(epcScodeDtlLan.cdId))))
          .where(
            epcScodeDtl.solCd.eq(solCd)
              .and(epcScodeDtl.cdId.eq(cdId)
                .and(epcScodeDtl.cdGrup.eq(cdGrup))))
        , alias);
    } catch (IllegalArgumentException e) {
      LOGGER.debug(StringUtil.extractStackTrace(e));
    } catch (Exception e) {
      LOGGER.debug(StringUtil.extractStackTrace(e));
    }
    return solutionCdNm;
  }

  public static Expression<?> companyMasterExpression(StringExpression cmpyCd, String alias) {
    Expression<?> companyCdNm = ExpressionUtils.as(Expressions.asString(""), alias); //빈값이 기본값
    try {
      if (isNull(cmpyCd)) {
        throw new IllegalArgumentException("잘못된 값입니다.");
      }

      if (StringUtil.isEmpty(alias)) {
        alias = alias.concat("Nm"); //ex) reqScn + Nm
      }
      StringPath companyName = epcCmpyMst.cmpyNm;

      companyCdNm = ExpressionUtils.as(
        JPAExpressions.select(companyName)
          .from(epcCmpyMst)
          .where(epcCmpyMst.cmpyCd.eq(cmpyCd))
        , alias);
    } catch (IllegalArgumentException e) {
      LOGGER.debug(StringUtil.extractStackTrace(e));
    } catch (Exception e) {
      LOGGER.debug(StringUtil.extractStackTrace(e));
    }
    return companyCdNm;
  }

  public static Expression<?> userMasterExpression(StringExpression unqUserId, String alias) {
    Expression<?> userNm = ExpressionUtils.as(Expressions.asString(""), alias); //빈값이 기본값
    try {
      if (isNull(unqUserId)) {
        throw new IllegalArgumentException("잘못된 값입니다.");
      }

      if (StringUtil.isEmpty(alias)) {
        alias = alias.concat("Nm"); //ex) reqScn + Nm
      }

      StringPath empNm = epcUserMst.empNm;

      userNm = ExpressionUtils.as(
        JPAExpressions.select(empNm)
          .from(epcUserMst)
          .where(epcUserMst.unqUserId.eq(unqUserId))
        , alias);
    } catch (IllegalArgumentException e) {
      LOGGER.debug(StringUtil.extractStackTrace(e));
    } catch (Exception e) {
      LOGGER.debug(StringUtil.extractStackTrace(e));
    }
    return userNm;
  }

  public static Expression<?> codeDetailExpression(String cmpyCd, String cdGrup,
    StringExpression cdId, EbmpLang ebmpLang, String alias) {
    Expression<?> cdNm = ExpressionUtils.as(Expressions.asString(""), alias); //빈값이 기본값
    if (StringUtil.isEmpty(cdGrup)) {
      return cdNm;
    }
    try {
      if (StringUtil.isEmpty(cmpyCd)) {
        cmpyCd = "00000";
      }

      if (isNull(cdId)) {
        throw new IllegalArgumentException("잘못된 값입니다.");
      }

      if (StringUtil.isEmpty(alias)) {
        alias = alias.concat("Nm"); //ex) reqScn + Nm
      }

      StringPath codeDetailCdNm = null;

      switch (ebmpLang) {
        case EN:
          codeDetailCdNm = epcCodeDtlLan.cdEn;
          break;
        case JP:
          codeDetailCdNm = epcCodeDtlLan.cdJp;
          break;
        case VN:
          codeDetailCdNm = epcCodeDtlLan.cdVn;
          break;
        case ZH:
          codeDetailCdNm = epcCodeDtlLan.cdZh;
          break;
        default:
          codeDetailCdNm = epcCodeDtl.cdNm;
      }

      cdNm = ExpressionUtils.as(
        JPAExpressions.select(codeDetailCdNm)
          .from(epcCodeDtl)
          .leftJoin(epcCodeDtlLan)
          .on(epcCodeDtl.cmpyCd.eq(epcCodeDtlLan.cmpyCd)
            .and(epcCodeDtl.cdGrup.eq(epcCodeDtlLan.cdGrup)
              .and(epcCodeDtl.cdId.eq(epcCodeDtlLan.cdId))))
          .where(
            epcCodeDtl.cmpyCd.eq(cmpyCd)
              .and(epcCodeDtl.cdId.eq(cdId)
                .and(epcCodeDtl.cdGrup.eq(cdGrup))))
        , alias);
    } catch (IllegalArgumentException e) {
      LOGGER.debug(StringUtil.extractStackTrace(e));
    } catch (Exception e) {
      LOGGER.debug(StringUtil.extractStackTrace(e));
    }
    return cdNm;
  }

  public static Expression<?> departmentNameExpression(StringExpression cmpyCd, StringExpression deptCd, String alias) {
    Expression<?> deptNm = ExpressionUtils.as(Expressions.asString(""), alias); //빈값이 기본값
    if (StringUtil.isEmpty(String.valueOf(cmpyCd)) || StringUtil.isEmpty(String.valueOf(deptCd))) {
      return deptNm;
    }
    try {
      deptNm = ExpressionUtils.as(
        JPAExpressions.select(epcDeptMst.deptNm)
          .from(epcDeptMst)
          .where(epcDeptMst.cmpyCd.eq(cmpyCd)
            .and(epcDeptMst.deptCd.eq(deptCd))
            .and(epcDeptMst.useYn.eq("Y"))), alias);
    } catch (Exception e) {
      LOGGER.debug(StringUtil.extractStackTrace(e));
    }
    return deptNm;
  }

  public static Expression<?> superDepartmentNameExpression(StringExpression cmpyCd, StringExpression deptCd, String alias) {
    Expression<?> deptNm = ExpressionUtils.as(Expressions.asString(""), alias); //빈값이 기본값
    if (StringUtil.isEmpty(String.valueOf(cmpyCd)) || StringUtil.isEmpty(String.valueOf(deptCd))) {
      return deptNm;
    }
    try {
      deptNm = ExpressionUtils.as(
        JPAExpressions.select(epcDeptMst.deptNm)
          .from(epcDeptMst)
          .where(epcDeptMst.cmpyCd.eq(cmpyCd)
            .and(epcDeptMst.deptCd.eq(JPAExpressions.select(epcDeptMst.superDeptCd)
              .from(epcDeptMst)
              .where(epcDeptMst.cmpyCd.eq(cmpyCd)
                .and(epcDeptMst.deptCd.eq(deptCd))
                .and(epcDeptMst.useYn.eq("Y")))))
            .and(epcDeptMst.useYn.eq("Y"))), alias);
    } catch (Exception e) {
      LOGGER.debug(StringUtil.extractStackTrace(e));
    }
    return deptNm;
  }

  public static Expression<?> departmentNameExpressionByUnqUserId(StringExpression cmpyCd, StringExpression unqUserId, String alias) {
    Expression<?> deptNm = ExpressionUtils.as(Expressions.asString(""), alias); //빈값이 기본값
    if (StringUtil.isEmpty(String.valueOf(cmpyCd)) || StringUtil.isEmpty(String.valueOf(unqUserId))) {
      return deptNm;
    }
    try {
      deptNm = ExpressionUtils.as(
        JPAExpressions.select(epcDeptMst.deptNm)
          .from(epcDeptMst)
          .where(epcDeptMst.cmpyCd.eq(cmpyCd)
            .and(epcDeptMst.deptCd.eq(
              JPAExpressions.select(epcUserMst.deptCd)
                .from(epcUserMst)
                .where(epcUserMst.unqUserId.eq(unqUserId))))
            .and(epcDeptMst.useYn.eq("Y"))), alias);
    } catch (Exception e) {
      LOGGER.debug(StringUtil.extractStackTrace(e));
    }
    return deptNm;
  }

  public static Expression<?> superDepartmentNameExpressionByUnqUserId(StringExpression cmpyCd, StringExpression unqUserId, String alias) {
    Expression<?> deptNm = ExpressionUtils.as(Expressions.asString(""), alias); //빈값이 기본값
    if (StringUtil.isEmpty(String.valueOf(cmpyCd)) || StringUtil.isEmpty(String.valueOf(unqUserId))) {
      return deptNm;
    }
    try {
      deptNm = ExpressionUtils.as(
        JPAExpressions.select(epcDeptMst.deptNm)
          .from(epcDeptMst)
          .where(epcDeptMst.cmpyCd.eq(cmpyCd)
            .and(epcDeptMst.deptCd.eq(JPAExpressions.select(epcDeptMst.superDeptCd)
              .from(epcDeptMst)
              .where(epcDeptMst.cmpyCd.eq(cmpyCd)
                .and(epcDeptMst.deptCd.eq(
                  JPAExpressions.select(epcUserMst.deptCd)
                    .from(epcUserMst)
                    .where(epcUserMst.unqUserId.eq(unqUserId))))
                .and(epcDeptMst.useYn.eq("Y")))))
            .and(epcDeptMst.useYn.eq("Y"))), alias);
    } catch (Exception e) {
      LOGGER.debug(StringUtil.extractStackTrace(e));
    }
    return deptNm;
  }
}
