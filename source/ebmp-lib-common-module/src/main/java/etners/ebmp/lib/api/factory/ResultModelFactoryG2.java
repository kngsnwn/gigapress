package etners.ebmp.lib.api.factory;

import etners.ebmp.lib.api.basemodel.ResultModel;
import etners.ebmp.lib.api.basemodel.ResultStatusG2;
import etners.ebmp.lib.api.kendomodel.KendoPagingExtendResultModel;
import etners.ebmp.lib.api.kendomodel.KendoPagingResultModel;
import etners.ebmp.lib.enums.lang.EbmpLang;
import java.util.List;

public class ResultModelFactoryG2 {

  private static ResultModelFactoryG2 instance;

  public static ResultModelFactoryG2 getInstance() {
    if (instance == null) {
      instance = new ResultModelFactoryG2();
    }

    return instance;
  }


  public <RD> ResultModel<RD> makeResultModel(EbmpLang ebmpLang, ResultStatusG2 resultStatus, RD resultData) {
    return new ResultModel<>(resultStatus.changeLang(ebmpLang), resultData);
  }


  public <RD> ResultModel<RD> makeResultModel(EbmpLang ebmpLang, ResultStatusG2 resultStatus) {
    return makeResultModel(ebmpLang, resultStatus.changeLang(ebmpLang), null);
  }


  public <RD> ResultModel<RD> makeResultModel(boolean successFl, EbmpLang ebmpLang, RD resultData) {
    ResultStatusG2 resultStatus = successFl ? ResultModel.RESULT_OK_G2 : ResultModel.RESULT_FAIL_G2;

    return makeResultModel(ebmpLang, resultStatus, resultData);
  }


  public <RD> ResultModel<RD> makeResultModel(boolean successFl, EbmpLang ebmpLang) {
    return makeResultModel(successFl, ebmpLang, null);
  }


  public <RD> ResultModel<RD> successResultModel(EbmpLang ebmpLang) {
    return makeResultModel(true, ebmpLang, null);
  }


  public <RD> ResultModel<RD> successResultModel(EbmpLang ebmpLang, RD resultData) {
    return makeResultModel(true, ebmpLang, resultData);
  }


  public <RD> ResultModel<RD> failResultModel(EbmpLang ebmpLang) {
    return makeResultModel(false, ebmpLang, null);
  }


  public <RD> ResultModel<RD> failResultModel(EbmpLang ebmpLang, RD resultData) {
    return makeResultModel(false, ebmpLang, resultData);
  }

  public <T, D> KendoPagingExtendResultModel<T, D> makeKendoPagingExtendResultModel(EbmpLang ebmpLang, ResultStatusG2 resultStatus, List<T> resultData, long count, D otherData) {
    KendoPagingResultModel<T> resultModel = new KendoPagingResultModel<>(resultStatus.changeLang(ebmpLang), resultData, count);

    return new KendoPagingExtendResultModel<T, D>(resultModel, otherData);
  }

  public <T, D> KendoPagingExtendResultModel<T, D> makeKendoPagingExtendResultModel(boolean successFl, EbmpLang ebmpLang, List<T> resultData, long count, D otherData) {
    ResultStatusG2 resultStatus = successFl ? ResultModel.RESULT_OK_G2 : ResultModel.RESULT_FAIL_G2;

    KendoPagingResultModel<T> resultModel = makeKendoPagingResultModel(ebmpLang, resultStatus, resultData, count);

    return new KendoPagingExtendResultModel<T, D>(resultModel, otherData);
  }

  public <T> KendoPagingResultModel<T> makeKendoPagingResultModel(EbmpLang ebmpLang, ResultStatusG2 resultStatus, List<T> resultData, long count) {
    return new KendoPagingResultModel<>(resultStatus.changeLang(ebmpLang), resultData, count);
  }

  public <T> KendoPagingResultModel<T> makeKendoPagingResultModel(boolean successFl, EbmpLang ebmpLang, List<T> resultData, long count) {
    ResultStatusG2 resultStatus = successFl ? ResultModel.RESULT_OK_G2 : ResultModel.RESULT_FAIL_G2;

    return makeKendoPagingResultModel(ebmpLang, resultStatus, resultData, count);
  }


  public <T> KendoPagingResultModel<T> successKendoPagingResultModel(EbmpLang ebmpLang, List<T> resultData, long count) {
    return makeKendoPagingResultModel(true, ebmpLang, resultData, count);
  }

  /**
   * <pre>
   * 메소드명	: successKendoPagingExtendResultModel
   * 작성자	: oxide
   * 작성일	: 2019. 7. 30.
   * 설명	:
   * </pre>
   *
   * @param ebmpLang
   * @param resultData
   * @param count
   * @param otherData
   * @return
   */
  public <T, D> KendoPagingExtendResultModel<T, D> successKendoPagingExtendResultModel(EbmpLang ebmpLang, List<T> resultData, long count, D otherData) {
    KendoPagingResultModel<T> resultModel = makeKendoPagingResultModel(true, ebmpLang, resultData, count);

    return new KendoPagingExtendResultModel<T, D>(resultModel, otherData);
  }

  public <T, D> KendoPagingExtendResultModel<T, D> successKendoPagingExtendResultModel(EbmpLang ebmpLang, List<T> resultData, long count) {
    KendoPagingResultModel<T> resultModel = makeKendoPagingResultModel(true, ebmpLang, resultData, count);

    return new KendoPagingExtendResultModel<T, D>(resultModel);
  }

  @Deprecated
  public <RD> ResultModel<RD> makeTestResultModel(String messageCode, String messageText, RD resultData) {
    ResultStatusG2 resultStatus = new ResultStatusG2(messageCode, messageText);

    return new ResultModel<>(resultStatus, resultData);
  }

  @Deprecated
  public <RD> ResultModel<RD> makeTestResultModel(String messageCode, String messageText) {
    return makeTestResultModel(messageCode, messageText, null);
  }


  public <T> KendoPagingResultModel<T> failKendoPagingResultModel(EbmpLang ebmpLang) {
    return makeKendoPagingResultModel(false, ebmpLang, null, 0);
  }


  public <T> KendoPagingResultModel<T> failKendoPagingResultModel(EbmpLang ebmpLang, List<T> resultData, long count) {
    return makeKendoPagingResultModel(false, ebmpLang, resultData, count);
  }

  public <T, D> KendoPagingExtendResultModel<T, D> failKendoPagingExtendResultModel(EbmpLang ebmpLang) {
    KendoPagingResultModel<T> resultModel = makeKendoPagingResultModel(false, ebmpLang, null, 0);

    return new KendoPagingExtendResultModel<T, D>(resultModel);
  }

  public <T, D> KendoPagingExtendResultModel<T, D> failKendoPagingExtendResultModel(EbmpLang ebmpLang, List<T> resultData, long count) {
    KendoPagingResultModel<T> resultModel = makeKendoPagingResultModel(false, ebmpLang, resultData, count);

    return new KendoPagingExtendResultModel<T, D>(resultModel);
  }

  public <T, D> KendoPagingExtendResultModel<T, D> failKendoPagingExtendResultModel(EbmpLang ebmpLang, List<T> resultData, long count, D otherData) {
    KendoPagingResultModel<T> resultModel = makeKendoPagingResultModel(false, ebmpLang, resultData, count);

    return new KendoPagingExtendResultModel<T, D>(resultModel, otherData);
  }


  @Deprecated
  public <T> KendoPagingResultModel<T> makeTestKendoPagingResultModel(EbmpLang ebmpLang, String messageCode, String messageText, List<T> resultData, long count) {
    ResultStatusG2 resultStatus = new ResultStatusG2(messageCode, messageText);

    return makeKendoPagingResultModel(ebmpLang, resultStatus, resultData, count);
  }

  @Deprecated
  public <T> KendoPagingResultModel<T> makeTestKendoPagingResultModel(EbmpLang ebmpLang, ResultStatusG2 resultStatus, List<T> resultData, long count) {
    return makeKendoPagingResultModel(ebmpLang, resultStatus, resultData, count);
  }

  @Deprecated
  public <T> KendoPagingResultModel<T> makeTestKendoPagingResultModel(EbmpLang ebmpLang, ResultStatusG2 resultStatus) {
    return makeTestKendoPagingResultModel(ebmpLang, resultStatus, null, 0);
  }

  @Deprecated
  public <T> KendoPagingResultModel<T> makeTestKendoPagingResultModel(EbmpLang ebmpLang, String messageCode, String messageText) {
    return makeTestKendoPagingResultModel(ebmpLang, messageCode, messageText, null, 0);
  }

}
