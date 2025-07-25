package etners.standard.mvc.jpa.epcCmpyMst.entity;

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
@IdClass(EpcCmpyMstPK.class)
@Table(name = "EPC_CMPY_MST")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class EpcCmpyMst {

    @Id
    @Schema(description = "업체코드")
    @Column(name = "CMPY_CD")
    private String cmpyCd;

    @Schema(description = "업체구분(CD:104)")
    @Column(name = "CMPY_GBN")
    private String cmpyGbn;

    @Schema(description = "업체명")
    @Column(name = "CMPY_NM")
    private String cmpyNm;

    @Schema(description = "업체업종(CD:105)")
    @Column(name = "BIZ_TYPE")
    private String bizType;

    @Schema(description = "업체 CI 이미지명")
    @Column(name = "CI_IMG")
    private String ciImg;

    @Schema(description = "업체 CI 이미지 경로")
    @Column(name = "CI_IMG_PATH")
    private String ciImgPath;

    @Schema(description = "업체 CI이미지파일ID")
    @Column(name = "CI_FILE_ID")
    private String ciFileId;

    @Schema(description = "업체 인장 이미지명")
    @Column(name = "STAMP_IMG")
    private String stampImg;

    @Schema(description = "업체 인장 이미지 경로")
    @Column(name = "STAMP_IMG_PATH")
    private String stampImgPath;

    @Schema(description = "직인이미지파일ID")
    @Column(name = "STAMP_FILE_ID")
    private String stampFileId;

    @Schema(description = "사업자등록번호")
    @Column(name = "CORPORATE_REG_NO")
    private String corporateRegNo;

    @Schema(description = "대표자명")
    @Column(name = "CEO_NM")
    private String ceoNm;

    @Schema(description = "대표전화번호")
    @Column(name = "CMPY_MAIN_TEL_NO")
    private String cmpyMainTelNo;

    @Schema(description = "업체우편번호")
    @Column(name = "ZIP_NO")
    private String zipNo;

    @Schema(description = "업체주소")
    @Column(name = "ADDR")
    private String addr;

    @Schema(description = "대표메일")
    @Column(name = "CMPY_MAIN_MAIL")
    private String cmpyMainMail;

    @Schema(description = "사업자등록증파일ID")
    @Column(name = "CORPORATE_FILE_ID")
    private String corporateFileId;

    @Schema(description = "회계담당자명")
    @Column(name = "ACCOUNT_MGR_NM")
    private String accountMgrNm;

    @Schema(description = "회계담당자전화번호")
    @Column(name = "ACCOUNT_MGR_TEL_NO")
    private String accountMgrTelNo;

    @Schema(description = "회계담당자이메일")
    @Column(name = "ACCOUNT_MGR_EMAIL")
    private String accountMgrEmail;

    @Schema(description = "계좌은행코드")
    @Column(name = "ACCOUNT_BANK_CD")
    private String accountBankCd;

    @Schema(description = "계좌번호(암호화)")
    @Column(name = "ACCOUNT_NO")
    private String accountNo;

    @Schema(description = "통장사본파일ID")
    @Column(name = "BANK_FILE_ID")
    private String bankFileId;

    @Schema(description = "관리자명")
    @Column(name = "MANAGER_NM")
    private String managerNm;

    @Schema(description = "관리자 Email")
    @Column(name = "MANAGER_EMAIL")
    private String managerEmail;

    @Schema(description = "관리자전화번호")
    @Column(name = "MANAGER_TEL_NO")
    private String managerTelNo;

    @Schema(description = "사번 사용여부. N인 회사는 임의의 사번을 생성하여 적용.")
    @Column(name = "SABUN_USE_YN")
    private String sabunUseYn;

    @Schema(description = "상세주소")
    @Column(name = "ADDR_DTL")
    private String addrDtl;

    @Schema(description = "고유 회사 공개키")
    @Column(name = "KEY_PUBLIC")
    private String keyPublic;

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

  public EpcCmpyMst(String cmpyCd, String cmpyGbn, String cmpyNm, String bizType, String ciImg, String ciImgPath, String ciFileId, String stampImg, String stampImgPath, String stampFileId, String corporateRegNo, String ceoNm, String cmpyMainTelNo, String zipNo, String addr, String cmpyMainMail, String corporateFileId, String accountMgrNm, String accountMgrTelNo, String accountMgrEmail, String accountBankCd, String accountNo, String bankFileId, String managerNm, String managerEmail, String managerTelNo, String sabunUseYn, String addrDtl, String keyPublic, Long seqNo, String useYn, String frstRgstId, LocalDateTime frstRgstDt, String lastMdfyId, LocalDateTime lastMdfyDt) {
    this.cmpyCd = cmpyCd;
    this.cmpyGbn = cmpyGbn;
    this.cmpyNm = cmpyNm;
    this.bizType = bizType;
    this.ciImg = ciImg;
    this.ciImgPath = ciImgPath;
    this.ciFileId = ciFileId;
    this.stampImg = stampImg;
    this.stampImgPath = stampImgPath;
    this.stampFileId = stampFileId;
    this.corporateRegNo = corporateRegNo;
    this.ceoNm = ceoNm;
    this.cmpyMainTelNo = cmpyMainTelNo;
    this.zipNo = zipNo;
    this.addr = addr;
    this.cmpyMainMail = cmpyMainMail;
    this.corporateFileId = corporateFileId;
    this.accountMgrNm = accountMgrNm;
    this.accountMgrTelNo = accountMgrTelNo;
    this.accountMgrEmail = accountMgrEmail;
    this.accountBankCd = accountBankCd;
    this.accountNo = accountNo;
    this.bankFileId = bankFileId;
    this.managerNm = managerNm;
    this.managerEmail = managerEmail;
    this.managerTelNo = managerTelNo;
    this.sabunUseYn = sabunUseYn;
    this.addrDtl = addrDtl;
    this.keyPublic = keyPublic;
    this.seqNo = seqNo;
    this.useYn = useYn;
    this.frstRgstId = frstRgstId;
    this.frstRgstDt = frstRgstDt;
    this.lastMdfyId = lastMdfyId;
    this.lastMdfyDt = lastMdfyDt;
  }
}
