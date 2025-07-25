package etners.ebmp.lib.api.basemodel;

import etners.ebmp.lib.enums.lang.EbmpLang;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;

/**
 * <pre>
 *   결과를 전달할 때 별도의 데이터가 존재할 경우 resultData에 바인딩하여 전달한다.
 *    특별히 사용하지 않는 경우, new 키워드나 리턴값 선언시 Object로 정의한다.
 * </pre>
 *
 * @param <RD>
 */
@Getter
@Schema(description = "요청된 API에 대해 응답을 돌려줄 때 그 응답에서 필요로 하는 데이터를 래핑(Wrapping)하여 전달하는 동시에 처리 결과의 상태값(ResultStatus)를 가지는 도메인 클래스입니다. API 호출에 대한 응답은 이 클래스로 래핑되어 전달되는 것을 권장합니다.")
public class ResultModel<RD> implements Serializable {

  @Serial
  private static final long serialVersionUID = 954212328871000209L;

  public static final ResultStatusG2 RESULT_OK_G2;
  public static final ResultStatusG2 RESULT_FAIL_G2;

  static {
    /**
     * <pre>
     *   성공값 다국어 세팅
     * </pre>
     *
     */
    HashMap<EbmpLang, String> resultOkI18NHashMap = new HashMap<>();

    resultOkI18NHashMap.put(EbmpLang.KO, "요청이 정상적으로 처리되었습니다.");
    resultOkI18NHashMap.put(EbmpLang.EN, "The request was successfully processed.");
    resultOkI18NHashMap.put(EbmpLang.JP, "要求は正常に処理されました。");
    resultOkI18NHashMap.put(EbmpLang.VN, "Yêu cầu đã được xử lý thành công.");
    resultOkI18NHashMap.put(EbmpLang.ZH, "请求已成功处理。");

    RESULT_OK_G2 = new ResultStatusG2("2000", resultOkI18NHashMap, EbmpLang.KO);

    /**
     * <pre>
     *    실패값 다국어 세팅
     * </pre>
     *
     */
    HashMap<EbmpLang, String> resultFailI18NHashMap = new HashMap<>();

    resultFailI18NHashMap.put(EbmpLang.KO, "요청을 처리하는데 실패하였습니다.");
    resultFailI18NHashMap.put(EbmpLang.EN, "Failed to process request.");
    resultFailI18NHashMap.put(EbmpLang.JP, "要求を処理できませんでした。");
    resultFailI18NHashMap.put(EbmpLang.VN, "Không thể xử lý yêu cầu.");
    resultFailI18NHashMap.put(EbmpLang.ZH, "无法处理请求。");

    RESULT_FAIL_G2 = new ResultStatusG2("4000", resultFailI18NHashMap, EbmpLang.KO);
  }


  @Setter
  @Schema(
    requiredMode = RequiredMode.NOT_REQUIRED,
    description = "선택옵션값. API 요청에 대한 결과로 Front에 어떤 값을 돌려줘야할 때 resultData 부분에 바인딩하여 리턴합니다. API 요청이 단순히 백엔드 처리만을 필요로 한다면 처리 결과의 상태값(ResultStatus)만 전달합니다."
  )
  protected RD resultData;

  @Schema(
    requiredMode = RequiredMode.REQUIRED
  )
  protected ResultStatusG2 resultStatus;


  public ResultModel(ResultStatusG2 resultStatus, RD resultData) {
    if (resultStatus == null) {
      throw new IllegalArgumentException("모든 ResultModel은 초기화할 때 반드시 ResultStatus를 가져야 합니다.");
    }

    this.resultStatus = resultStatus;
    this.resultData = resultData;
  }

  public ResultModel(ResultStatusG2 resultStatus) {
    this(resultStatus, null);
  }

  @Override
  public String toString() {
    return "ResultModel [resultData=" + resultData + ", resultStatus=" + resultStatus + "]";
  }

}
