package etners.standard.mvc.jpa.epcDeptMst.entity;

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
@IdClass(EpcDeptMstPK.class)
@Table(name = "EPC_DEPT_MST")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class EpcDeptMst {

    @Id
    @Schema(description = "회사코드")
    @Column(name = "CMPY_CD")
    private String cmpyCd;

    @Id
    @Schema(description = "부서코드")
    @Column(name = "DEPT_CD")
    private String deptCd;

    @Schema(description = "부서명")
    @Column(name = "DEPT_NM")
    private String deptNm;

    @Schema(description = "상위 부서 코드(start with ~~ connect by)")
    @Column(name = "SUPER_DEPT_CD")
    private String superDeptCd;

    @Schema(description = "부서가 위치하는 단계 표시. 기본값은 0.")
    @Column(name = "DEPT_LEVEL")
    private Long deptLevel;

    @Schema(description = "최상위 루트인 경우만 Y로 체크. 기본값 N.")
    @Column(name = "ROOT_YN")
    private String rootYn;

    @Schema(description = "모바일/웹 화면 표시 여부")
    @Column(name = "DISPLAY_YN")
    private String displayYn;

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

  public EpcDeptMst(String cmpyCd, String deptCd, String deptNm, String superDeptCd, Long deptLevel, String rootYn, String displayYn, Long seqNo, String useYn, String frstRgstId, LocalDateTime frstRgstDt, String lastMdfyId, LocalDateTime lastMdfyDt) {
    this.cmpyCd = cmpyCd;
    this.deptCd = deptCd;
    this.deptNm = deptNm;
    this.superDeptCd = superDeptCd;
    this.deptLevel = deptLevel;
    this.rootYn = rootYn;
    this.displayYn = displayYn;
    this.seqNo = seqNo;
    this.useYn = useYn;
    this.frstRgstId = frstRgstId;
    this.frstRgstDt = frstRgstDt;
    this.lastMdfyId = lastMdfyId;
    this.lastMdfyDt = lastMdfyDt;
  }
}
