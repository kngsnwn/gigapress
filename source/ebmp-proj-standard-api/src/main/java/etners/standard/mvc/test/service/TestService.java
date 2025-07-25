package etners.standard.mvc.test.service;


import etners.ebmp.lib.api.basemodel.ResultModel;
import etners.ebmp.lib.api.kendomodel.KendoPagingResultModel;
import etners.standard.mvc.test.domain.request.*;
import etners.standard.mvc.test.domain.response.*;

import java.util.List;

public interface TestService {

  KendoPagingResultModel<TestListResponse> getCodeList(
      TestListRequestParam testListRequestParam);

  ResultModel<TestDetailResponse> getCodeDetail(TestDetailRequestParam requestParam);

  ResultModel<TestInsertResponse> insertCodeGrup(TestInsertRequestParam testInsertRequestParam);

  ResultModel<TestUpdateResponse> updateCodeGrup(TestUpdateRequestParam requestParam);

  ResultModel<TestDeleteResponse> deleteCodeGrup(TestDeleteRequestParam requestParam);

  ResultModel<List<TestMenuListResponse>> getMenuList(TestMenuListRequestParam requestParam);

  ResultModel<TestFirstAccessInfoResponse> getFirstAccessInfo(
      TestFirstAccessInfoRequestParam requestParam);

  ResultModel<TestRsaResponse> getRsaTestDetail();

}
