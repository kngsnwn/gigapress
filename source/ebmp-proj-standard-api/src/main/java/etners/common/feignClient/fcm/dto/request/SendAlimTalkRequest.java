package etners.common.feignClient.fcm.dto.request;

import lombok.Data;

@Data
public class SendAlimTalkRequest {

  private String solCd;

  private String cmpyCd;

  /**
   * 보내려는 메시지 타입(ATA/SMS/LMS)
   * ATA : 알림톡
   * SMS : 단문 문자
   * LMS : 장문 문자
   */
  private String messageType = "ATA";

  private String wmGbn = "02";

  private String toNumber;

  private String tmpltSn;

  private String tmpltId;

  private String[] messageParams;

  private String fromNumber;

  private boolean sendOnlyAlimTalk;

  private String reservationSendYYYYMMDDHHMI;

  private String alimTalkEventNm = "";

  private String alimTalkEventCode = "";

  private boolean devSendMessageFl = false;
}
