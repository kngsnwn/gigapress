package etners.ebmp.lib.jpa.entity.epc.epcUserSol;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.YesNoConverter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Builder
@Entity
@IdClass(EpcUserSolCommonPK.class)
@Table(name = "EPC_USER_SOL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class EpcUserSolCommon {

  @Id
  @Schema(description = "고유 사용자 아이디(아이디가 변경 가능하도록 Key 조합함)")
  @Column(name = "UNQ_USER_ID")
  private String unqUserId;

  @Id
  @Schema(description = "권한구분코드")
  @Column(name = "AUTH_CD")
  private String authCd;

  @Id
  @Schema(description = "솔루션 코드(EPC_SOL_MST, SOL_CD 와 연결)")
  @Column(name = "SOL_CD")
  private String solCd;

  @Schema(description = "아이디")
  @Column(name = "USER_ID")
  private String userId;

  @Schema(description = "솔루션 URL")
  @Column(name = "SOL_URL")
  private String solUrl;

  @Schema(description = "솔루션 ID")
  @Column(name = "SOL_ID")
  private String solId;

  @Schema(description = "솔루션 파라미터")
  @Column(name = "SOL_PRM")
  private String solPrm;

  @Schema(description = "SMS 수신여부")
  @Column(name = "SMS_RECV_YN")
  private String smsRecvYn;

  @Schema(description = "EMAIL 수신여부")
  @Column(name = "EMAIL_RECV_YN")
  private String emailRecvYn;

  @Schema(description = "Push Alarm 수신여부")
  @Column(name = "MBL_ALARM_RECV_YN")
  private String mblAlarmRecvYn;

  @Schema(description = "모바일 FCM KEY")
  @Column(name = "MBL_FCM_KEY")
  private String mblFcmKey;

  @Schema(description = "모바일 OS TYPE")
  @Column(name = "MBL_OS_TYPE")
  private String mblOsType;

  @Schema(description = "모바일 OS VERSION")
  @Column(name = "MBL_OS_VERSION")
  private String mblOsVersion;

  @Schema(description = "모바일 정보 기타 필요한 내용 JSON 형식으로 추가")
  @Column(name = "MBL_INFO")
  private String mblInfo;

  @Schema(description = "단말고유번호정보")
  @Column(name = "MBL_UID")
  private String mblUid;

  @Schema(description = "비콘체크여부")
  @Column(name = "BCN_CK_YN")
  private String bcnCkYn;

  @Id
  @Schema(description = "WEB/MOBILE 구분(CD:102)")
  @Column(name = "WM_GBN")
  private String wmGbn;

  @Schema(description = "모바일 FCM KEY")
  @Column(name = "MBL_FMC_KEY")
  private String mblFmcKey;

  @Schema(description = "모바일 기기 고유번호")
  @Column(name = "MBL_DEV")
  private String mblDev;

  @Schema(description = "조회순서")
  @Column(name = "SEQ_NO")
  private Long seqNo;

  @Schema(description = "사용여부")
  @Column(name = "USE_YN")
  @Convert(converter = YesNoConverter.class)
  private boolean useYn;

  @CreatedBy
  @Schema(description = "최초등록자")
  @Column(name = "FRST_RGST_ID")
  private String frstRgstId;

  @CreationTimestamp
  @Schema(description = "최초등록일시")
  @Column(name = "FRST_RGST_DT")
  private LocalDateTime frstRgstDt;

  @LastModifiedBy
  @Schema(description = "최종수정자")
  @Column(name = "LAST_MDFY_ID")
  private String lastMdfyId;

  @UpdateTimestamp
  @Schema(description = "최종수정일시")
  @Column(name = "LAST_MDFY_DT")
  private LocalDateTime lastMdfyDt;

  public EpcUserSolCommon(String unqUserId, String authCd, String solCd, String userId, String solUrl, String solId, String solPrm, String smsRecvYn, String emailRecvYn, String mblAlarmRecvYn, String mblFcmKey, String mblOsType, String mblOsVersion, String mblInfo, String mblUid, String bcnCkYn, String wmGbn, String mblFmcKey, String mblDev, Long seqNo, boolean useYn, String frstRgstId,
    LocalDateTime frstRgstDt, String lastMdfyId, LocalDateTime lastMdfyDt) {
    this.unqUserId = unqUserId;
    this.authCd = authCd;
    this.solCd = solCd;
    this.userId = userId;
    this.solUrl = solUrl;
    this.solId = solId;
    this.solPrm = solPrm;
    this.smsRecvYn = smsRecvYn;
    this.emailRecvYn = emailRecvYn;
    this.mblAlarmRecvYn = mblAlarmRecvYn;
    this.mblFcmKey = mblFcmKey;
    this.mblOsType = mblOsType;
    this.mblOsVersion = mblOsVersion;
    this.mblInfo = mblInfo;
    this.mblUid = mblUid;
    this.bcnCkYn = bcnCkYn;
    this.wmGbn = wmGbn;
    this.mblFmcKey = mblFmcKey;
    this.mblDev = mblDev;
    this.seqNo = seqNo;
    this.useYn = useYn;
    this.frstRgstId = frstRgstId;
    this.frstRgstDt = frstRgstDt;
    this.lastMdfyId = lastMdfyId;
    this.lastMdfyDt = lastMdfyDt;
  }
}
