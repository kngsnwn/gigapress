package etners.standard.mvc.jpa.epcMenuMst.entity;

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
@IdClass(EpcMenuMstPK.class)
@Table(name = "EPC_MENU_MST")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class EpcMenuMst {

    @Id
    @Schema(description = "솔루션코드")
    @Column(name = "SOL_CD")
    private String solCd;

    @Id
    @Schema(description = "WEB/MOBILE 구분(CD:102), 단 이솝메인(solCd : 0002)은 메뉴 구성을 위해 11, 21을 별도로 추가하여 사용하고 있다.")
    @Column(name = "WM_GBN")
    private String wmGbn;

    @Id
    @Schema(description = "메뉴 ID")
    @Column(name = "MENU_ID")
    private String menuId;

    @Schema(description = "메뉴명(게시판)")
    @Column(name = "MENU_NM")
    private String menuNm;

    @Schema(description = "상위 메뉴 ID(start with ~~ connect by)")
    @Column(name = "SUPER_MENU_ID")
    private String superMenuId;

    @Schema(description = "URL_LINK")
    @Column(name = "URL_LINK")
    private String urlLink;

    @Schema(description = "메뉴계층")
    @Column(name = "MENU_DEPTH")
    private String menuDepth;

    @Schema(description = "메뉴 아이콘 파일 ID")
    @Column(name = "MENU_ICON_FILE_ID")
    private String menuIconFileId;

    @Schema(description = "비활성화 유저 소개 페이지 링크")
    @Column(name = "INFO_PAGE_URL_LINK")
    private String infoPageUrlLink;

    @Schema(description = "메뉴 설명(이솝메인, 플랫폼 메인 등에서 활용)")
    @Column(name = "MENU_DESCRIPTION")
    private String menuDescription;

    @Schema(description = "이 값이 Y인 경우 로그인한 사용자가 이트너스(cmpyCd : 00001)소속일 때 activateMenuYn 값과 상관없이 메뉴가 반드시 활성화된다.")
    @Column(name = "ETNERS_FLAG_YN")
    private String etnersFlagYn;

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

    @Schema(description = "A솔루션 메뉴에서 링크타고 B솔루션으로 넘어가는 경우 B솔루션의 솔루션 코드를 넣는 컬럼. 예를 들어 ESOP 메인처럼 다른 솔루션으로 넘어가는 경우, EPC_USER_SOL 정보와 대조하여 이 사용자에게 메뉴를 표시해도 되는지 확인하는 용도로 사용한다. 필수값 아님.")
    @Column(name = "TARGET_SOL_CD")
    private String targetSolCd;

    @Schema(description = "이솝 메인에서 이 메뉴 아이콘을 활성화할지 판단하기 위한 용도로 추가되는 컬럼. 기존에는 EPC_USER_SOLUTION의 유무로만 체크해서 비즈니스 로직에서 실시간으로 판단하여 이 값을 제공했으나 별도의 플래그 처리가 필요하다는 것을 확인하게 되어 추가함. N이면 소개 페이지 안내, F면 '서비스 준비 중입니다.'라는 안내 메시지를 표시. Y인 경우 URL_LINK 값으로 페이지 이동을 제공해야 한다.")
    @Column(name = "ACTIVATE_MENU_YN")
    private String activateMenuYn;

  public EpcMenuMst(String solCd, String wmGbn, String menuId, String menuNm, String superMenuId, String urlLink, String menuDepth, String menuIconFileId, String infoPageUrlLink, String menuDescription, String etnersFlagYn, Long seqNo, String useYn, String frstRgstId, LocalDateTime frstRgstDt, String lastMdfyId, LocalDateTime lastMdfyDt, String targetSolCd, String activateMenuYn) {
    this.solCd = solCd;
    this.wmGbn = wmGbn;
    this.menuId = menuId;
    this.menuNm = menuNm;
    this.superMenuId = superMenuId;
    this.urlLink = urlLink;
    this.menuDepth = menuDepth;
    this.menuIconFileId = menuIconFileId;
    this.infoPageUrlLink = infoPageUrlLink;
    this.menuDescription = menuDescription;
    this.etnersFlagYn = etnersFlagYn;
    this.seqNo = seqNo;
    this.useYn = useYn;
    this.frstRgstId = frstRgstId;
    this.frstRgstDt = frstRgstDt;
    this.lastMdfyId = lastMdfyId;
    this.lastMdfyDt = lastMdfyDt;
    this.targetSolCd = targetSolCd;
    this.activateMenuYn = activateMenuYn;
  }
}
