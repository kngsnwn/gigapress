package etners.common.feignClient.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RefundRequest  {

  @Schema(
    example = "ko",
    allowableValues = "ko/en/zh/vn/jp",
    description = "언어 로케일 설정. ko(한국어) / en(영어) / zh(중국어) / vn(베트남어) / jp(일본어) 중 하나의 값이 요청 파라미터로 넘어오면 해당 언어에 대한 메시지 데이터를 응답시 제공한다."
  )
  private String locale = "ko";


  @Schema(description = "솔루션 코드")
  private String solCd;

  @Schema(description = "웹/모바일 구분")
  private String wmGbn;

  @Schema(description = "요청 솔루션에서 사용하는 주문번호")
  private String solOid;

  @Schema(description = "요청 솔루션에서 사용하는 품목번호")
  private String solItemId;

  @Schema(description = "환불진행 상태 공통코드(SCODE: 1020, DTL: 015) 추후 공통으로 변경")
  private String refundStatus;

  @Schema(description = "취소요청 사유")
  private String msg;
  @Schema(description = "취소요청 사유")
  private String msgNm;

  @Schema(description = "환불 계좌 예금주명")
  private String refundAcctNum;

  @Schema(description = "환불 계좌코드 공통코드(SCODE: 0000, DTL: 227)")
  private String refundBankCode;

  @Schema(description = "환불 계좌 예금주명")
  private String refundAcctName;

  @Schema(description = "취소요청 금액")
  private long price;

  @Builder
  public RefundRequest(String locale, String solCd, String wmGbn, String solOid, String solItemId, String refundStatus, String msg, String msgNm, String refundAcctNum, String refundBankCode, String refundAcctName, long price) {
    this.locale = locale;
    this.solCd = solCd;
    this.wmGbn = wmGbn;
    this.solOid = solOid;
    this.solItemId = solItemId;
    this.refundStatus = refundStatus;
    this.msg = msg;
    this.msgNm = msgNm;
    this.refundAcctNum = refundAcctNum;
    this.refundBankCode = refundBankCode;
    this.refundAcctName = refundAcctName;
    this.price = price;
  }

  public static RefundRequest create(String locale, String solCd, String wmGbn, String solOid, String solItemId, String refundStatus, String msg, String msgNm, String refundAcctNum, String refundBankCode, String refundAcctName, long price) {
    return RefundRequest.builder()
      .locale(locale)
      .solCd(solCd)
      .wmGbn(wmGbn)
      .solOid(solOid)
      .solItemId(solItemId)
      .refundStatus(refundStatus)
      .msg(msg)
      .msgNm(msgNm)
      .refundAcctNum(refundAcctNum)
      .refundBankCode(refundBankCode)
      .refundAcctName(refundAcctName)
      .price(price)
      .build();
  }


}
