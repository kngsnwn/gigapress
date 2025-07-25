package etners.standard.mvc.test.service;


import static etners.standard.mvc.jpa.epcMenuMst.entity.QEpcMenuMst.epcMenuMst;
import static etners.standard.mvc.jpa.epcScodeMstTest.entity.QEpcScodeMstTest.epcScodeMstTest;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import common.util.string.StringUtil;
import etners.common.config.exception.ApplicationException;
import etners.common.domains.CommonRequestParam;
import etners.common.feignClient.PayClient;
import etners.common.feignClient.request.RefundRequest;
import etners.common.util.annotation.masking.ApplyMasking;
import etners.common.util.enumType.ErrorCode;
import etners.common.util.enumType.SolutionType;
import etners.common.util.enumType.WebAccessType;
import etners.common.util.kendo.KendoUtils;
import etners.common.util.scope.CurrentUserData;
import etners.ebmp.lib.api.basemodel.ResultModel;
import etners.ebmp.lib.api.kendomodel.KendoPagingResultModel;
import etners.ebmp.lib.api.resultstatus.manager.ResultStatusManager;
import etners.ebmp.lib.enums.lang.EbmpLang;
import etners.standard.mvc.jpa.epcMenuMst.entity.EpcMenuMst;
import etners.standard.mvc.jpa.epcMenuMst.repo.EpcMenuMstRepository;
import etners.standard.mvc.jpa.epcScodeMstTest.entity.EpcScodeMstTest;
import etners.standard.mvc.jpa.epcScodeMstTest.mapper.EpcScodeMstTestMapper;
import etners.standard.mvc.jpa.epcScodeMstTest.repo.EpcScodeMstRepository;
import etners.standard.mvc.jpa.epcScodeMstTest.repo.EpcScodeMstTestRepositoryForQueryDsl;
import etners.standard.mvc.jpa.epcUserMst.entity.epcUserMst.EpcUserMst;
import etners.standard.mvc.jpa.epcUserMst.repo.EpcUserMstRepository;
import etners.standard.mvc.jpa.epcUserMst.repo.EpcUserMstRepositoryForQueryDsl;
import etners.standard.mvc.test.domain.request.TestDeleteRequestParam;
import etners.standard.mvc.test.domain.request.TestDetailRequestParam;
import etners.standard.mvc.test.domain.request.TestFirstAccessInfoRequestParam;
import etners.standard.mvc.test.domain.request.TestInsertRequestParam;
import etners.standard.mvc.test.domain.request.TestListRequestParam;
import etners.standard.mvc.test.domain.request.TestMenuListRequestParam;
import etners.standard.mvc.test.domain.request.TestUpdateRequestParam;
import etners.standard.mvc.test.domain.response.TestDeleteResponse;
import etners.standard.mvc.test.domain.response.TestDetailResponse;
import etners.standard.mvc.test.domain.response.TestFirstAccessInfoResponse;
import etners.standard.mvc.test.domain.response.TestInsertResponse;
import etners.standard.mvc.test.domain.response.TestListResponse;
import etners.standard.mvc.test.domain.response.TestMenuListResponse;
import etners.standard.mvc.test.domain.response.TestRsaResponse;
import etners.standard.mvc.test.domain.response.TestUpdateResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@RequiredArgsConstructor
@Service
public class TestServiceImpl implements TestService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestServiceImpl.class);

  private final EpcScodeMstRepository epcScodeMstTestRepository;
  private final EpcScodeMstTestRepositoryForQueryDsl epcScodeMstTestRepositoryForQueryDsl;
  private final EpcMenuMstRepository epcMenuMstRepository;
  private final EpcUserMstRepositoryForQueryDsl epcUserMstRepositoryForQueryDsl;
  private final CurrentUserData currentUserData;
  private final PayClient payClient;
  private final EpcUserMstRepository epcUserMstRepository;

  @Override
  public KendoPagingResultModel<TestListResponse> getCodeList(TestListRequestParam requestParam) {

    EbmpLang ebmpLang = currentUserData.getEbmpLang();

    String searchSolCd = requestParam.getSearchSolCd();
    String searchCompoundText = requestParam.getSearchCompoundText();

    List<String> defaultSortColumnName = new ArrayList<>();

    OrderSpecifier[] orderArray = KendoUtils
      .getQuerydslMultiOrderSpecifierList(requestParam, epcScodeMstTest);

    Pageable pageable = KendoUtils.toSpringPageable(requestParam, defaultSortColumnName);

    BooleanBuilder builder = new BooleanBuilder();

    //검색
    if (ObjectUtils.isNotEmpty(searchSolCd)) {
      SolutionType solutionType = SolutionType.get(searchSolCd);
      builder.and(epcScodeMstTest.solCd.eq(solutionType));
    }

    //복합 검색
    if (ObjectUtils.isNotEmpty(searchCompoundText)) {
      builder.and(epcScodeMstTest.cdGrupNm.like("%" + searchCompoundText + "%")
        .or(epcScodeMstTest.cdGrupAs.like("%" + searchCompoundText + "%")));
    }

    Page<TestListResponse> pageContainer = epcScodeMstTestRepositoryForQueryDsl
      .findByQuerydslForAllCode(builder, pageable, orderArray, requestParam.getRequestType());

    return ResultStatusManager
      .successKendoPagingResultModel(ebmpLang, pageContainer.getContent(),
        pageContainer.getTotalElements());
  }

  @Override
  public ResultModel<TestDetailResponse> getCodeDetail(TestDetailRequestParam requestParam) {
    EbmpLang ebmpLang = currentUserData.getEbmpLang();

    String solCd = requestParam.getSolCd();
    String cdGrup = requestParam.getCdGrup();

    EpcScodeMstTest epcScodeMstTest = findEpcScodeMstTest(solCd, cdGrup, true);

    if (ObjectUtils.isEmpty(epcScodeMstTest)) {
      //요청한 데이터가 없거나 조회할 수 있는 권한이 없습니다.
      return ResultStatusManager.failResultModel(ebmpLang, 4253);
    }

    TestDetailResponse testDetailResponse = EpcScodeMstTestMapper.INSTANCE.toDto(epcScodeMstTest);

    return ResultStatusManager.successResultModel(ebmpLang, testDetailResponse);
  }

  @Override
  @Transactional
  public ResultModel<TestInsertResponse> insertCodeGrup(TestInsertRequestParam requestParam) {

    EbmpLang ebmpLang = currentUserData.getEbmpLang();

    String solCd = requestParam.getSolCd();
    String cdGrup = requestParam.getCdGrup();
    String cdGrupNm = requestParam.getCdGrupNm();
    String cdDesc = requestParam.getCdDesc();

    EpcScodeMstTest epcScodeMstTest = EpcScodeMstTest
      .create(SolutionType.get(solCd), cdGrup, "", cdGrupNm, cdDesc, "", "", "", "", "", "");

    epcScodeMstTestRepository.save(epcScodeMstTest);

    TestInsertResponse testInsertResponse = new TestInsertResponse();
    testInsertResponse.setSolCd(solCd);
    testInsertResponse.setCdGrup(cdGrup);

    return ResultStatusManager.successResultModel(ebmpLang, testInsertResponse);
  }

  public EpcScodeMstTest findEpcScodeMstTest(String solCd, String cdGrup) {
    return epcScodeMstTestRepository.findBySolCdAndCdGrup(SolutionType.get(solCd), cdGrup)
      .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_AUTH));
  }

  public EpcScodeMstTest findEpcScodeMstTest(String solCd, String cdGrup, boolean useYn) {
    return epcScodeMstTestRepository.findBySolCdAndCdGrupAndUseYn(SolutionType.get(solCd), cdGrup, useYn)
      .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND_AUTH));
  }

  @Override
  @Transactional
  public ResultModel<TestUpdateResponse> updateCodeGrup(TestUpdateRequestParam requestParam) {
    EbmpLang ebmpLang = currentUserData.getEbmpLang();

    String solCd = requestParam.getSolCd();
    String cdGrup = requestParam.getCdGrup();
    String useYn = requestParam.getUseYn();

    try {
      EpcScodeMstTest epcScodeMstTest = findEpcScodeMstTest(solCd, cdGrup);
      epcScodeMstTest.updateUseYn("Y".equals(useYn));
      epcScodeMstTestRepository.save(epcScodeMstTest);

      TestUpdateResponse testUpdateResponse = TestUpdateResponse.builder()
        .solCd(solCd)
        .cdGrup(cdGrup)
        .build();

      return ResultStatusManager.successResultModel(ebmpLang, testUpdateResponse);
    } catch (Exception e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      e.printStackTrace();
      return ResultStatusManager.failResultModel(ebmpLang, 4000);
    }
  }

  @Override
  @Transactional
  public ResultModel<TestDeleteResponse> deleteCodeGrup(TestDeleteRequestParam requestParam) {
    EbmpLang ebmpLang = currentUserData.getEbmpLang();

    String solCd = requestParam.getSolCd();
    String cdGrup = requestParam.getCdGrup();

    try {
      EpcScodeMstTest epcScodeMstTest = findEpcScodeMstTest(solCd, cdGrup);
      epcScodeMstTestRepository.delete(epcScodeMstTest);

      TestDeleteResponse testDeleteResponse = TestDeleteResponse.builder()
        .solCd(solCd)
        .cdGrup(cdGrup)
        .build();

      return ResultStatusManager.successResultModel(ebmpLang, testDeleteResponse);
    } catch (Exception e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      e.printStackTrace();
      return ResultStatusManager.failResultModel(ebmpLang, 4000);
    }
  }

  @Override
  public ResultModel<List<TestMenuListResponse>> getMenuList(
    TestMenuListRequestParam requestParam) {

    EbmpLang ebmpLang = currentUserData.getEbmpLang();
    WebAccessType webAccessType = currentUserData.getWebAccessType();
    String solCd = requestParam.getSolCd();

    BooleanBuilder builder = new BooleanBuilder();
    builder.and(epcMenuMst.solCd.eq(solCd));
    builder.and(epcMenuMst.wmGbn.eq(webAccessType.getCode()));
    builder.and(epcMenuMst.useYn.eq("Y"));

    List<EpcMenuMst> epcMenuMsts = (List<EpcMenuMst>) epcMenuMstRepository.findAll(builder);
    List<TestMenuListResponse> testMenuListResponses = new ArrayList<>();
    for (EpcMenuMst epcMenuMst : epcMenuMsts) {
      String menuId = epcMenuMst.getMenuId();
      String menuNm = epcMenuMst.getMenuNm();
      String superMenuId = epcMenuMst.getSuperMenuId();
      String urlLink = epcMenuMst.getUrlLink();

      TestMenuListResponse testMenuListResponse = new TestMenuListResponse();
      testMenuListResponse.setMenuId(menuId);
      testMenuListResponse.setMenuNm(menuNm);
      testMenuListResponse.setSuperMenuId(superMenuId);
      testMenuListResponse.setUrlLink(urlLink);
      testMenuListResponses.add(testMenuListResponse);
    }

    testMenuListResponses = testMenuListResponses.stream()
      .sorted(Comparator.comparing(TestMenuListResponse::getMenuId)).collect(
        Collectors.toList());

    return ResultStatusManager.successResultModel(ebmpLang, testMenuListResponses);
  }

  @Override
  public ResultModel<TestFirstAccessInfoResponse> getFirstAccessInfo(
    TestFirstAccessInfoRequestParam requestParam) {

    EbmpLang ebmpLang = currentUserData.getEbmpLang();
    String cmpyCd = currentUserData.getCmpyCd();
    String unqUserId = currentUserData.getUnqUserId();

    TestFirstAccessInfoResponse testFirstAccessInfoResponse = epcUserMstRepositoryForQueryDsl
      .findByQuerydslForFirstAccessInfo(cmpyCd, unqUserId);

    return ResultStatusManager.successResultModel(ebmpLang, testFirstAccessInfoResponse);
  }

  @Override
  @ApplyMasking
  public ResultModel<TestRsaResponse> getRsaTestDetail() {
    EpcUserMst epcUserMst = epcUserMstRepository.findByUnqUserId(currentUserData.getUnqUserId());
    TestRsaResponse response = TestRsaResponse.builder()
      .spTelNo(epcUserMst.getSpTelNo())
      .email(epcUserMst.getEmail())
      .userId(epcUserMst.getUserId())
      .empNm(epcUserMst.getEmpNm())
      .build();
    return ResultStatusManager.successResultModel(currentUserData.getEbmpLang(), response);
  }

  public ResultModel refundRequest(CommonRequestParam requestParam) {

    EbmpLang ebmpLang = currentUserData.getEbmpLang();
    String unqUserId = currentUserData.getUnqUserId();
    String accessToken = "accessToken";
    String sapmpleString = "";
    boolean isFeignClientSuccess = false;

    RefundRequest refundRequest = RefundRequest.builder()
      .locale(ebmpLang.getLocale())
      .solCd(sapmpleString)
      .wmGbn(sapmpleString)
      .solOid(sapmpleString)
      .solItemId(sapmpleString)
      .refundStatus(sapmpleString)
      .msg(sapmpleString)
      .refundAcctNum(sapmpleString)
      .refundBankCode(sapmpleString)
      .refundAcctName(sapmpleString)
      .price(0L)
      .build();

    String authorization = "Bearer ";
    authorization += accessToken;

    JSONObject jsonResultRefund = payClient.refundRequest(authorization, refundRequest);
    LinkedHashMap<String, String> refundResultStatus = (LinkedHashMap<String, String>) jsonResultRefund.get("resultStatus");
    if (!"2000".equals(refundResultStatus.get("messageCode"))) {
      String dynamicMessage = "payClient refund 실패 ::: " + refundResultStatus.get("messageText");
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return ResultStatusManager.failResultModelWithDynamicMessage(ebmpLang, refundResultStatus.get("messageCode"), dynamicMessage);
    } else {
      isFeignClientSuccess = true;
    }

    return ResultStatusManager.successResultModel(ebmpLang, 2000);
  }

}
