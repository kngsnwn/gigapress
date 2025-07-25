package etners.standard.mvc.jpa.epcUserMst.entity.epcUserMst;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Builder
@Entity
@IdClass(EpcUserMstPK.class)
@Table(name = "EPC_USER_MST")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class EpcUserMst {

    @Id
    @Schema(description = "고유 사용자 아이디(아이디가 변경 가능하도록 Key 조합함)")
    @Column(name = "UNQ_USER_ID")
    private String unqUserId;

    @Schema(description = "회사코드")
    @Column(name = "CMPY_CD")
    private String cmpyCd;

    @Schema(description = "사번")
    @Column(name = "SABUN")
    private String sabun;

    @Schema(description = "사용자구분, 회사코드와 사번으로 PK 구성되지 않을시 사용. Default: A")
    @Column(name = "USER_GBN")
    private String userGbn;

    @Schema(description = "회사명")
    @Column(name = "CMPY_NM")
    private String cmpyNm;

    @Schema(description = "성명")
    @Column(name = "EMP_NM")
    private String empNm;

    @Schema(description = "아이디")
    @Column(name = "USER_ID")
    private String userId;

    @Schema(description = "패스워드(SHA-256 암호화 적용)")
    @Column(name = "USER_PWD")
    private String userPwd;

    @Schema(description = "이메일")
    @Column(name = "EMAIL")
    private String email;

    @Schema(description = "사용자타입(CD:103)")
    @Column(name = "USER_BIZ_TYPE")
    private String userBizType;

    @Schema(description = "부서코드")
    @Column(name = "DEPT_CD")
    private String deptCd;

    @Schema(description = "사업장코드")
    @Column(name = "BP_CD")
    private String bpCd;

    @Schema(description = "직위(CD:202)")
    @Column(name = "PSN_SCN")
    private String psnScn;

    @Schema(description = "직책(CD:203)")
    @Column(name = "DUTY_SCN")
    private String dutyScn;

    @Schema(description = "직무(CD:204)")
    @Column(name = "JOB_SCN")
    private String jobScn;

    @Schema(description = "세부직무")
    @Column(name = "JOB_DTL_SCN")
    private String jobDtlScn;

    @Schema(description = "전화번호(회사)")
    @Column(name = "WORK_TEL_NO")
    private String workTelNo;

    @Schema(description = "전화번호(스마트폰)")
    @Column(name = "SP_TEL_NO")
    private String spTelNo;

    @Schema(description = "전화번호(집)")
    @Column(name = "HOME_TEL_NO")
    private String homeTelNo;

    @Schema(description = "성별(M/F/MF)")
    @Column(name = "GEN_SCN")
    private String genScn;

    @Schema(description = "입사일자(YYYYMMDD)")
    @Column(name = "JOIN_DATE")
    private String joinDate;

    @Schema(description = "수습해지일자(YYYYMMDD)")
    @Column(name = "PBT_DATE")
    private String pbtDate;

    @Schema(description = "퇴사여부")
    @Column(name = "RST_YN")
    private String rstYn;

    @Schema(description = "퇴사일자(YYYYMMDD)")
    @Column(name = "RST_DATE")
    private String rstDate;

    @Schema(description = "재직구분(CD:206)")
    @Column(name = "WORK_SCN")
    private String workScn;

    @Schema(description = "사용자 로그인카운트(누적)")
    @Column(name = "LOGIN_CNT")
    private Long loginCnt;

    @Schema(description = "생년월일(YYYYMMDD)")
    @Column(name = "BIRTH_DATE")
    private String birthDate;

    @Schema(description = "자동 로그인 여부(Y/N)")
    @Column(name = "AUTO_LOGIN")
    private String autoLogin;

    @Schema(description = "근무장소")
    @Column(name = "WORK_SPOT")
    private String workSpot;

    @Schema(description = "근무부서")
    @Column(name = "WORK_DEPT")
    private String workDept;

    @Schema(description = "기준근무시간 From(시/분)")
    @Column(name = "STD_TIME_FROM")
    private String stdTimeFrom;

    @Schema(description = "기준근무시간 To(시/분)")
    @Column(name = "STD_TIME_TO")
    private String stdTimeTo;

    @Schema(description = "우편번호(회사)")
    @Column(name = "CMPY_POST_NO")
    private String cmpyPostNo;

    @Schema(description = "주소(회사)")
    @Column(name = "CMPY_ADDR")
    private String cmpyAddr;

    @Schema(description = "상세주소(회사)")
    @Column(name = "CMPY_ADDR_DTL")
    private String cmpyAddrDtl;

    @Schema(description = "우편번호(집)")
    @Column(name = "HOME_POST_NO")
    private String homePostNo;

    @Schema(description = "주소(집)")
    @Column(name = "HOME_ADDR")
    private String homeAddr;

    @Schema(description = "상세주소(집)")
    @Column(name = "HOME_ADDR_DTL")
    private String homeAddrDtl;

    @Schema(description = "사진 FILE ID")
    @Column(name = "PIC_FILE_ID")
    private String picFileId;

    @Schema(description = "인증번호")
    @Column(name = "AUTH_NO")
    private String authNo;

    @Schema(description = "인증일시")
    @Column(name = "AUTH_DATE")
    private LocalDateTime authDate;

    @Schema(description = "마지막로그인일시")
    @Column(name = "LAST_LOGIN_DT")
    private LocalDateTime lastLoginDt;

    @Schema(description = "웹에서 로그인 성공 후 최초로 들어가게 될 화면의 솔루션 코드값. 기본값은 0000(플랫폼 메인)")
    @Column(name = "DEFAULT_REDIRECT_SOL_CD")
    private String defaultRedirectSolCd;

    @Schema(description = "휴직 시작일자(YYYYMMDD)")
    @Column(name = "LOA_START_DATE")
    private String loaStartDate;

    @Schema(description = "휴직 종료일자(YYYYMMDD)")
    @Column(name = "LOA_END_DATE")
    private String loaEndDate;

    @Schema(description = "소속회사코드(EBMP원청/하청 관계파악을 위함)")
    @Column(name = "AFF_CMPY_CD")
    private String affCmpyCd;

    @Schema(description = "출입ID")
    @Column(name = "ENTER_ID")
    private String enterId;

    @Schema(description = "비고")
    @Column(name = "CONTENTS")
    private String contents;

    @Schema(description = "최근 비밀번호 변경 일시")
    @Column(name = "PWD_CHG_DT")
    private LocalDateTime pwdChgDt;

    @Schema(description = "인증 성공 일시")
    @Column(name = "SUCCESS_AUTH_DATE")
    private LocalDateTime successAuthDate;

    @Schema(description = "인증 요청 횟수")
    @Column(name = "SEND_AUTH_CNT")
    private Long sendAuthCnt;

    @Schema(description = "DS SHARED OFFICE 회원가입 연동시 사업장명 등록")
    @Column(name = "WORKSPACE")
    private String workspace;

    @Schema(description = "조회순서")
    @Column(name = "SEQ_NO")
    private Long seqNo;

    @Schema(description = "사용여부")
    @Column(name = "USE_YN")
    private String useYn;

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

  public EpcUserMst(String unqUserId, String cmpyCd, String sabun, String userGbn, String cmpyNm, String empNm, String userId, String userPwd, String email, String userBizType, String deptCd, String bpCd, String psnScn, String dutyScn, String jobScn, String jobDtlScn, String workTelNo, String spTelNo, String homeTelNo, String genScn, String joinDate, String pbtDate, String rstYn, String rstDate, String workScn, Long loginCnt, String birthDate, String autoLogin, String workSpot, String workDept, String stdTimeFrom, String stdTimeTo, String cmpyPostNo, String cmpyAddr, String cmpyAddrDtl, String homePostNo, String homeAddr, String homeAddrDtl, String picFileId, String authNo, LocalDateTime authDate, LocalDateTime lastLoginDt, String defaultRedirectSolCd, String loaStartDate, String loaEndDate, String affCmpyCd, String enterId, String contents, LocalDateTime pwdChgDt, LocalDateTime successAuthDate, Long sendAuthCnt, String workspace, Long seqNo, String useYn, String frstRgstId, LocalDateTime frstRgstDt, String lastMdfyId, LocalDateTime lastMdfyDt) {
    this.unqUserId = unqUserId;
    this.cmpyCd = cmpyCd;
    this.sabun = sabun;
    this.userGbn = userGbn;
    this.cmpyNm = cmpyNm;
    this.empNm = empNm;
    this.userId = userId;
    this.userPwd = userPwd;
    this.email = email;
    this.userBizType = userBizType;
    this.deptCd = deptCd;
    this.bpCd = bpCd;
    this.psnScn = psnScn;
    this.dutyScn = dutyScn;
    this.jobScn = jobScn;
    this.jobDtlScn = jobDtlScn;
    this.workTelNo = workTelNo;
    this.spTelNo = spTelNo;
    this.homeTelNo = homeTelNo;
    this.genScn = genScn;
    this.joinDate = joinDate;
    this.pbtDate = pbtDate;
    this.rstYn = rstYn;
    this.rstDate = rstDate;
    this.workScn = workScn;
    this.loginCnt = loginCnt;
    this.birthDate = birthDate;
    this.autoLogin = autoLogin;
    this.workSpot = workSpot;
    this.workDept = workDept;
    this.stdTimeFrom = stdTimeFrom;
    this.stdTimeTo = stdTimeTo;
    this.cmpyPostNo = cmpyPostNo;
    this.cmpyAddr = cmpyAddr;
    this.cmpyAddrDtl = cmpyAddrDtl;
    this.homePostNo = homePostNo;
    this.homeAddr = homeAddr;
    this.homeAddrDtl = homeAddrDtl;
    this.picFileId = picFileId;
    this.authNo = authNo;
    this.authDate = authDate;
    this.lastLoginDt = lastLoginDt;
    this.defaultRedirectSolCd = defaultRedirectSolCd;
    this.loaStartDate = loaStartDate;
    this.loaEndDate = loaEndDate;
    this.affCmpyCd = affCmpyCd;
    this.enterId = enterId;
    this.contents = contents;
    this.pwdChgDt = pwdChgDt;
    this.successAuthDate = successAuthDate;
    this.sendAuthCnt = sendAuthCnt;
    this.workspace = workspace;
    this.seqNo = seqNo;
    this.useYn = useYn;
    this.frstRgstId = frstRgstId;
    this.frstRgstDt = frstRgstDt;
    this.lastMdfyId = lastMdfyId;
    this.lastMdfyDt = lastMdfyDt;
  }
}
