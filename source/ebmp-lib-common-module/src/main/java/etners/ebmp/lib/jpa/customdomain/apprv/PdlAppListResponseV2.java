package etners.ebmp.lib.jpa.customdomain.apprv;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PdlAppListResponseV2 implements Serializable {

  private static final long serialVersionUID = 4527552197161346986L;

  /**
   * 결재ID 시퀀스(YYYYMMDD + 0001)
   */
  @Schema(
    example = "20190516000006",
    required = true,
    description = "결재ID. 형식은 다음과 같다. YYYYMMDD + 000001, 000002 ... 009998, 009999.",
    hidden = false
  )
  private String approvalId;

  /**
   * 결재양식 유형
   */
  @Schema(
    example = "SEND",
    required = true,
    description = "결재 수신 유형. SEND(보낸결재), APPROVAL(받은결재), REFERENCE(공유결재)",
    hidden = false
  )
  private String apprvReceiveType;

  /**
   * 결재양식 유형
   */
  @Schema(
    example = "01",
    required = true,
    description = "결재 유형. COMMUTE(근태 수정요청), VACATION(휴가 신청), OVERTIME(연장근무 신청), ETC(예외근무 신청)",
    hidden = false
  )
  private String apprvTmplt;

  /**
   * 결재유형명
   */
  @Schema(
    example = "근태수정요청",
    required = true,
    description = "결재유형명. 근태 수정요청(COMMUTE), 휴가 신청(VACATION), 연장근무 신청(OVERTIME), 예외근무 신청(ETC)",
    hidden = false
  )
  private String apprvTmpltNm;

  /**
   * 결재 타이틀
   */
  @Schema(
    example = "휴가 신청",
    required = true,
    description = "휴가 신청, 근태수정요청, 연장근무 신청, 예외근무 신청 등등..",
    hidden = false
  )
  private String apprvTitle;

  /**
   * 결재 사유
   */
  @Schema(
    example = "개인사유",
    required = true,
    description = "결재 사용 관련 사유. 근태수정요청, 휴가신청, 잔업/특근, 예외근무",
    hidden = false
  )
  private String apprvReason;

  /**
   * 시작일
   */
  @Schema(
    example = "20190527",
    required = true,
    description = "결재 사용 시작일(YYYYMMDD).",
    hidden = false
  )
  private String startDt;

  /**
   * 종료일
   */
  @Schema(
    example = "20190527",
    required = true,
    description = "결재 사용 종료일(YYYYMMDD).",
    hidden = false
  )
  private String endDt;

  /**
   * 파일 ID(년월일시분초1/1000초)
   */
  @Schema(
    example = "201905211617475",
    required = true,
    description = "파일 ID(년월일시분초1/1000초)",
    hidden = false
  )
  private String sqFileNo;

  /**
   * 요청자 성명(결재 기록 보관용)
   */
  @Schema(
    example = "홍길동",
    required = true,
    description = "결재 요청자 성명(결재 기록 보관용).",
    hidden = false
  )
  private String empNm;

  /**
   * 부서명(결재 기록 보관용)
   */
  @Schema(
    example = "개발팀",
    required = true,
    description = "결재 부서명(결재 기록 보관용)",
    hidden = false
  )
  private String deptNm;

  /**
   * 직급명(결재 기록 보관용)
   */
  @Schema(
    example = "과장",
    required = true,
    description = "결재 직급명(결재 기록 보관용).",
    hidden = false
  )
  private String rankNm;

  /**
   * 직책명(결재 기록 보관용)
   */
  @Schema(
    example = "팀장",
    required = true,
    description = "결재 직책명.",
    hidden = false
  )
  private String positionNm;

  /**
   * 결재 상태코드
   */
  @Schema(
    example = "APPROVAL_WAIT",
    required = true,
    description = "결재 상태 : 승인대기(APPROVAL_WAIT), 승인완료(APPROVAL_COMPLETE), 삭제대기(DELETE_WAIT), 삭제완료(DELETE_COMPLETE), 반려(REJECT)",
    hidden = false
  )
  private String apprvProcessStatus;

  /**
   * 결재 상태명
   */
  @Schema(
    example = "승인대기",
    required = true,
    description = "결재 상태 : 승인대기(APPROVAL_WAIT), 승인완료(APPROVAL_COMPLETE), 삭제대기(DELETE_WAIT), 삭제완료(DELETE_COMPLETE), 반려(REJECT)",
    hidden = false
  )
  private String apprvProcessStatusNm;

  /**
   * 결재 상태 진행여부
   */
  @Schema(
    example = "Y(진행중),N(대기중, 승인완료)",
    required = true,
    description = "결재 상태 진행여부",
    hidden = false
  )
  private String progressYn;

  /**
   * 신청일(yyyy-mm-dd HH:mm:ss)
   */
  @Schema(
    example = "2019-05-23 15:58:00",
    required = true,
    description = "신청일(yyyy-MM-dd HH:mm:ss)",
    hidden = false
  )
  private String dtRegApprv;

  /**
   * 결재 읽은날짜(yyyy-mm-dd HH:mm:ss)
   */
  @Schema(
    example = "2019-10-10 15:58:00",
    required = true,
    description = "결재 읽은시간(yyyy-MM-dd HH:mm:ss)",
    hidden = false
  )
  private String tmReadDt;

  /**
   * 최초등록일시
   */
  @Schema(
    hidden = true
  )
  private LocalDateTime frstRgstDt;

}
