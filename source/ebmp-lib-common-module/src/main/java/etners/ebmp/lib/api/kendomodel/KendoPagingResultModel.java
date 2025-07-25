package etners.ebmp.lib.api.kendomodel;

import etners.ebmp.lib.api.basemodel.ResultModel;
import etners.ebmp.lib.api.basemodel.ResultStatusG2;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@SuppressWarnings("rawtypes")
public class KendoPagingResultModel<T> extends ResultModel<List> {

  private static final long serialVersionUID = -7986459155221150316L;

  @Schema(
    example = "null",
    required = true,
    description = "필수값. 페이징 요청에 대한 결과로 요청값에 해당하는 페이지의 게시물 데이터를 리스트 형태로 가지는 필드입니다.",
    hidden = false
  )
  protected List<T> resultData;

  @Schema(
    example = "reqNo",
    required = true,
    description = "필수값. 페이징 요청시 현재 페이지의 데이터만이 아니라 해당 페이징 요청(검색옵션 포함)에 맞는 전체 게시물 갯수를 나타냅니다. 일반적으로 프론트에서 다음 세가지 요소를 알고 있으면 페이지 넘버링 및 페이징 표시를 할 수 있습니다: 전체 게시물 갯수, 페이지당 게시물 갯수, 현재 요청한 페이지 번호.",
    hidden = false
  )
  private final long total;


  public KendoPagingResultModel(ResultStatusG2 resultStatus, List<T> resultData, long total) {
    super(resultStatus, resultData);

    this.total = total;
  }

  public long getTotal() {
    return total;
  }

  @Override
  public String toString() {
    return "KendoPagingResultModel [" + (resultData != null ? "resultData=" + resultData + ", " : "") + "total=" + total + "]";
  }

}
