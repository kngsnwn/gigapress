package etners.ebmp.lib.api.resultstatus.manager;

import common.util.string.StringUtil;
import etners.ebmp.lib.api.basemodel.ResultModel;
import etners.ebmp.lib.api.basemodel.ResultStatusG2;
import etners.ebmp.lib.api.factory.ResultModelFactoryG2;
import etners.ebmp.lib.api.kendomodel.KendoPagingExtendResultModel;
import etners.ebmp.lib.api.kendomodel.KendoPagingResultModel;
import etners.ebmp.lib.api.resultstatus.manager.support.EtnersHttpConnector;
import etners.ebmp.lib.api.resultstatus.service.ResultStatusService;
import etners.ebmp.lib.enums.lang.EbmpLang;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResultStatusManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResultStatusManager.class);

  private static ResultStatusService resultStatusService;

  @Autowired
  private ResultStatusService resultStatusServiceDependancy;

  private static HashMap<String, ResultStatusG2> resultStatusMap = new HashMap<>();

  private static EtnersHttpConnector etnersHttpConnector;

  private final String targetDomain = "http://dev-ebmp-single.etnersplatform.com/";

  @PostConstruct
  public void init() {
    LOGGER.info("ResultStatusManager @PostConstruct Initialize.");

    LOGGER.info("this.resultStatusServiceDependancy : " + this.resultStatusServiceDependancy);
    LOGGER.info("resultStatusService : " + resultStatusService);

    resultStatusService = this.resultStatusServiceDependancy;
  }

  public ResultStatusManager() {
    LOGGER.info("ResultStatusManager !!");
  }


  public static ResultStatusG2 getResultStatus(EbmpLang ebmpLang, int messageCode) {
    return getResultStatus(ebmpLang, messageCode + "");
  }

  @SuppressWarnings("deprecation")
  public static ResultStatusG2 getResultStatus(EbmpLang ebmpLang, String messageCode) {
    if (!StringUtil.isNumberFormat(messageCode)) {
      throw new IllegalArgumentException("메시지코드 값은 현재 숫자만 허용됩니다.");
    }

    if (resultStatusMap == null) {
      resultStatusMap = new HashMap<>();
    }

    if (!resultStatusMap.containsKey(messageCode)) {

      try {
        ResultStatusG2 resultStatus = null;

        LOGGER.info("resultStatusService : " + resultStatusService);

        if (resultStatusService != null) {
          resultStatus = resultStatusService.getResultStatusByMessageCode(messageCode);
        } else {
          /**
           * resultStatusService를 가져오지 못한 경우에는 HTTP 통신을 통해서 메시지 코드를 가져오도록 한다.
           *
           * 2019-08-02 oxide.
           */
          if (etnersHttpConnector == null) {
            etnersHttpConnector = new EtnersHttpConnector();
          }

          resultStatus = etnersHttpConnector.getResultStatus(ebmpLang, messageCode);
        }

        if (resultStatus != null) {
          resultStatusMap.put(messageCode, resultStatus);
        }
      } catch (Exception e) {
        LOGGER.error(StringUtil.extractStackTrace(e));

        System.err.println("messageCode : " + messageCode + "\n");
        System.err.println(StringUtil.extractStackTrace(e));
        return new ResultStatusG2("NOT_DEFINED", "[messageCode : " + messageCode + "]은 정의되지 않은 메시지 코드입니다. DB에 " + messageCode + "에 해당하는 메시지 코드 row를 정의 후 사용해주세요. 11111");
      }
    }

    //한번 더 체크.
    if (resultStatusMap.containsKey(messageCode)) {
      return resultStatusMap.get(messageCode);
    } else {
      return new ResultStatusG2("NOT_DEFINED", "[messageCode : " + messageCode + "]은 정의되지 않은 메시지 코드입니다. DB에 " + messageCode + "에 해당하는 메시지 코드 row를 정의 후 사용해주세요. 2222");
    }

  }

  public static ResultStatusG2 getResultStatusByKeyNamespace(String keyNamespace) {
    ResultStatusG2 resultStatus = resultStatusService.getResultStatusByKeyNamespace(keyNamespace);

    if (resultStatus != null) {
      resultStatusMap.put(resultStatus.getMessageCode(), resultStatus);
    }

    return resultStatus;
  }

  public static <RD> ResultModel<RD> successResultModel(EbmpLang ebmpLang) {
    return ResultModelFactoryG2.getInstance().successResultModel(ebmpLang);
  }

  public static <RD> ResultModel<RD> successResultModel(EbmpLang ebmpLang, RD resultModel) {
    return ResultModelFactoryG2.getInstance().successResultModel(ebmpLang, resultModel);
  }

  public static <RD> ResultModel<RD> successResultModelWithCustomMessage(EbmpLang ebmpLang, String customMessage, RD resultModel) {

    HashMap<EbmpLang, String> messageTextI18N = new HashMap<>();

    for (EbmpLang lang : EbmpLang.values()) {
      messageTextI18N.put(lang, customMessage);
    }

    ResultStatusG2 customSuccessResultStatus = new ResultStatusG2("2000", messageTextI18N, ebmpLang);

    return new ResultModel<RD>(customSuccessResultStatus, resultModel);
  }

  public static <RD> ResultModel<RD> successResultModelWithCustomMessage(EbmpLang ebmpLang, String customMessage) {
    return successResultModelWithCustomMessage(ebmpLang, customMessage, null);
  }

  public static <RD> ResultModel<RD> failResultModel(EbmpLang ebmpLang, String messageCode, RD resultModel) {
    if (!StringUtil.isNumberFormat(messageCode)) {
      throw new IllegalArgumentException("메시지코드 값은 현재 숫자만 허용됩니다.");
    }

    ResultStatusG2 resultStatus = getResultStatus(ebmpLang, messageCode);

    return ResultModelFactoryG2.getInstance().makeResultModel(ebmpLang, resultStatus, resultModel);
  }

  public static <RD> ResultModel<RD> failResultModel(EbmpLang ebmpLang, int messageCode, RD resultModel) {
    return failResultModel(ebmpLang, messageCode + "", resultModel);
  }

  public static <RD extends Object> ResultModel<RD> failResultModel(EbmpLang ebmpLang, String messageCode) {
    if (!StringUtil.isNumberFormat(messageCode)) {
      throw new IllegalArgumentException("메시지코드 값은 현재 숫자만 허용됩니다.");
    }

    return failResultModel(ebmpLang, messageCode, null);
  }

  public static <RD extends Object> ResultModel<RD> failResultModel(EbmpLang ebmpLang, int messageCode) {
    return failResultModel(ebmpLang, messageCode + "");
  }

  public static <RD> ResultModel<RD> failResultModel(EbmpLang ebmpLang, ResultStatusG2 resultStatus, RD resultModel) {
    return ResultModelFactoryG2.getInstance().makeResultModel(ebmpLang, resultStatus, resultModel);
  }

  public static <RD> ResultModel<RD> failResultModel(EbmpLang ebmpLang, ResultStatusG2 resultStatus) {
    return ResultModelFactoryG2.getInstance().makeResultModel(ebmpLang, resultStatus);
  }

  public static <RD> ResultModel<RD> failResultModelWithCustomMessage(EbmpLang ebmpLang, String customMessage, RD resultModel) {

    HashMap<EbmpLang, String> messageTextI18N = new HashMap<>();

    for (EbmpLang lang : EbmpLang.values()) {
      messageTextI18N.put(lang, customMessage);
    }

    ResultStatusG2 customFailResultStatus = new ResultStatusG2("4000", messageTextI18N, ebmpLang);

    return new ResultModel<RD>(customFailResultStatus, resultModel);
  }

  public static <RD> ResultModel<RD> failResultModelWithDynamicMessage(EbmpLang ebmpLang, String messageCode, RD resultModel, String... dynamicMessages) {
    if (!StringUtil.isNumberFormat(messageCode)) {
      throw new IllegalArgumentException("메시지코드 값은 현재 숫자만 허용됩니다.");
    }

    ResultStatusG2 resultStatus = getResultStatus(ebmpLang, messageCode);

    resultStatus.setDynamicMessageDataList(dynamicMessages);

    return ResultModelFactoryG2.getInstance().makeResultModel(ebmpLang, resultStatus, resultModel);
  }

  public static <RD> ResultModel<RD> failResultModelWithDynamicMessage(EbmpLang ebmpLang, int messageCode, RD resultModel, String... dynamicMessages) {
    return failResultModelWithDynamicMessage(ebmpLang, messageCode + "", null, dynamicMessages);
  }

  public static <RD> ResultModel<RD> failResultModelWithDynamicMessage(EbmpLang ebmpLang, String messageCode, String... dynamicMessages) {
    if (!StringUtil.isNumberFormat(messageCode)) {
      throw new IllegalArgumentException("메시지코드 값은 현재 숫자만 허용됩니다.");
    }

    return failResultModelWithDynamicMessage(ebmpLang, messageCode, null, dynamicMessages);
  }

  public static <RD> ResultModel<RD> failResultModelWithDynamicMessage(EbmpLang ebmpLang, int messageCode, String... dynamicMessages) {
    return failResultModelWithDynamicMessage(ebmpLang, messageCode + "", dynamicMessages);
  }

  public static <T> KendoPagingResultModel<T> successKendoPagingResultModel(EbmpLang ebmpLang, List<T> resultData, long totalCount) {
    return ResultModelFactoryG2.getInstance().successKendoPagingResultModel(ebmpLang, resultData, totalCount);
  }

  public static <T, D> KendoPagingExtendResultModel<T, D> successKendoPagingExtendResultModel(EbmpLang ebmpLang, List<T> resultData, long totalCount, D otherData) {
    return ResultModelFactoryG2.getInstance().successKendoPagingExtendResultModel(ebmpLang, resultData, totalCount, otherData);
  }

  public static <T, D> KendoPagingExtendResultModel<T, D> successKendoPagingExtendResultModel(EbmpLang ebmpLang, List<T> resultData, long totalCount) {
    return ResultModelFactoryG2.getInstance().successKendoPagingExtendResultModel(ebmpLang, resultData, totalCount);
  }

  public static <T> KendoPagingResultModel<T> failKendoPagingResultModel(EbmpLang ebmpLang, String messageCode, List<T> resultData, long totalCount) {
    if (!StringUtil.isNumberFormat(messageCode)) {
      throw new IllegalArgumentException("메시지코드 값은 현재 숫자만 허용됩니다.");
    }

    ResultStatusG2 resultStatus = getResultStatus(ebmpLang, messageCode);

    return ResultModelFactoryG2.getInstance().makeKendoPagingResultModel(ebmpLang, resultStatus, resultData, totalCount);
  }

  public static <T> KendoPagingResultModel<T> failKendoPagingResultModel(EbmpLang ebmpLang, int messageCode, List<T> resultData, long totalCount) {
    return failKendoPagingResultModel(ebmpLang, messageCode + "", resultData, totalCount);
  }

  public static <T extends Object> KendoPagingResultModel<T> failKendoPagingResultModel(EbmpLang ebmpLang, String messageCode) {
    if (!StringUtil.isNumberFormat(messageCode)) {
      throw new IllegalArgumentException("메시지코드 값은 현재 숫자만 허용됩니다.");
    }

    return failKendoPagingResultModel(ebmpLang, messageCode, null, 0);
  }

  public static <T extends Object> KendoPagingResultModel<T> failKendoPagingResultModel(EbmpLang ebmpLang, int messageCode) {
    return failKendoPagingResultModel(ebmpLang, messageCode + "", null, 0);
  }

  public static <T> KendoPagingResultModel<T> failKendoPagingResultModel(EbmpLang currentEbmpLang, ResultStatusG2 resultStatus, List<T> resultData, long totalCount) {
    return ResultModelFactoryG2.getInstance().makeKendoPagingResultModel(currentEbmpLang, resultStatus, resultData, totalCount);
  }

  public static <T extends Object> KendoPagingResultModel<T> failKendoPagingResultModel(EbmpLang currentEbmpLang, ResultStatusG2 resultStatus) {
    return ResultModelFactoryG2.getInstance().makeKendoPagingResultModel(currentEbmpLang, resultStatus, null, 0);
  }


  public static <T, D> KendoPagingExtendResultModel<T, D> failKendoPagingExtendResultModel(EbmpLang ebmpLang, String messageCode, List<T> resultData, long totalCount, D otherData) {
    if (!StringUtil.isNumberFormat(messageCode)) {
      throw new IllegalArgumentException("메시지코드 값은 현재 숫자만 허용됩니다.");
    }

    ResultStatusG2 resultStatus = getResultStatus(ebmpLang, messageCode);

    return ResultModelFactoryG2.getInstance().makeKendoPagingExtendResultModel(ebmpLang, resultStatus, resultData, totalCount, otherData);
  }

  public static <T, D> KendoPagingExtendResultModel<T, D> failKendoPagingExtendResultModel(EbmpLang ebmpLang, int messageCode, List<T> resultData, long totalCount, D otherData) {
    return failKendoPagingExtendResultModel(ebmpLang, messageCode + "", resultData, totalCount, otherData);
  }

  public static <T extends Object, D extends Object> KendoPagingExtendResultModel<T, D> failKendoPagingExtendResultModel(EbmpLang ebmpLang, String messageCode, D otherData) {
    if (!StringUtil.isNumberFormat(messageCode)) {
      throw new IllegalArgumentException("메시지코드 값은 현재 숫자만 허용됩니다.");
    }

    return failKendoPagingExtendResultModel(ebmpLang, messageCode, null, 0, otherData);
  }

  public static <T extends Object, D extends Object> KendoPagingExtendResultModel<T, D> failKendoPagingExtendResultModel(EbmpLang ebmpLang, int messageCode, D otherData) {
    return failKendoPagingExtendResultModel(ebmpLang, messageCode + "", null, 0, otherData);
  }

  public static <T, D> KendoPagingExtendResultModel<T, D> failKendoPagingExtendResultModel(EbmpLang currentEbmpLang, ResultStatusG2 resultStatus, List<T> resultData, long totalCount, D otherData) {
    return ResultModelFactoryG2.getInstance().makeKendoPagingExtendResultModel(currentEbmpLang, resultStatus, resultData, totalCount, otherData);
  }

  public static <T extends Object, D extends Object> KendoPagingExtendResultModel<T, D> failKendoPagingExtendResultModel(EbmpLang currentEbmpLang, ResultStatusG2 resultStatus, D otherData) {
    return ResultModelFactoryG2.getInstance().makeKendoPagingExtendResultModel(currentEbmpLang, resultStatus, null, 0, otherData);
  }

  public static <T extends Object, D extends Object> KendoPagingExtendResultModel<T, D> failKendoPagingExtendResultModel(EbmpLang currentEbmpLang, ResultStatusG2 resultStatus) {
    return ResultModelFactoryG2.getInstance().makeKendoPagingExtendResultModel(currentEbmpLang, resultStatus, null, 0, null);
  }

}
