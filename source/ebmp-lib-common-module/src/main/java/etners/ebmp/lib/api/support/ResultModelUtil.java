package etners.ebmp.lib.api.support;

import etners.ebmp.lib.api.basemodel.ResultModel;
import etners.ebmp.lib.api.kendomodel.KendoPagingResultModel;

public class ResultModelUtil {

  /**
   * 설명	: 메시지 코드값이 2000번으로 들어왔을 때만 성공으로 판단함.
   */
  public static <T> boolean isSuccess(ResultModel<T> resultModel) {
    return "2000".equals(resultModel.getResultStatus().getMessageCode());
  }

  /**
   * 설명	: 메시지 코드값이 2000번으로 들어왔을 때만 성공으로 판단함.
   */
  public static <T> boolean isSuccess(KendoPagingResultModel<T> resultModel) {
    return "2000".equals(resultModel.getResultStatus().getMessageCode());
  }

  /**
   * 설명	: 메시지 코드값이 2000번이 아니면 실패로 판단함.
   */
  public static <T> boolean isFail(ResultModel<T> resultModel) {
    return !isSuccess(resultModel);
  }

  /**
   * 설명   : 메시지 코드값이 2000번이 아니면 실패로 판단함.
   */
  public static <T> boolean isFail(KendoPagingResultModel<T> resultModel) {
    return !isSuccess(resultModel);
  }
}
