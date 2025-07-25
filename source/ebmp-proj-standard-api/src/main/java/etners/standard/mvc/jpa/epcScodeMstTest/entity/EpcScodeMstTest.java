package etners.standard.mvc.jpa.epcScodeMstTest.entity;

import etners.common.base.BaseEntity;
import etners.common.util.enumType.SolutionType;
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

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@IdClass(EpcScodeMstTestPK.class)
@Table(name = "EPC_SCODE_MST_TEST")
public class EpcScodeMstTest extends BaseEntity {

  @Id
  @Column(name = "SOL_CD")
  @Schema(description = "솔루션코드", nullable = false)
  private SolutionType solCd;
  @Id
  @Column(name = "CD_GRUP")
  @Schema(description = "그룹코드", nullable = false)
  private String cdGrup;
  @Column(name = "CD_GRUP_AS")
  @Schema(description = "그룹코드알리아스", nullable = true)
  private String cdGrupAs;
  @Column(name = "CD_GRUP_NM")
  @Schema(description = "그룹코드명", nullable = false)
  private String cdGrupNm;
  @Column(name = "CD_DESC")
  @Schema(description = "코드설명", nullable = false)
  private String cdDesc;
  @Column(name = "CD_DESC1")
  @Schema(description = "", nullable = true)
  private String cdDesc1;
  @Column(name = "CD_DESC2")
  @Schema(description = "코드설명2", nullable = true)
  private String cdDesc2;
  @Column(name = "CD_DESC3")
  @Schema(description = "코드설명3", nullable = true)
  private String cdDesc3;
  @Column(name = "CD_DESC4")
  @Schema(description = "코드설명4", nullable = true)
  private String cdDesc4;
  @Column(name = "CD_DESC5")
  @Schema(description = "코드설명5", nullable = true)
  private String cdDesc5;
  @Column(name = "CMPY_CD")
  @Schema(description = "회사코드", nullable = true)
  private String cmpyCd;

  @Builder
  public EpcScodeMstTest(SolutionType solCd, String cdGrup, String cdGrupAs, String cdGrupNm,
      String cdDesc, String cdDesc1, String cdDesc2, String cdDesc3, String cdDesc4,
      String cdDesc5, String cmpyCd) {
    this.solCd = solCd;
    this.cdGrup = cdGrup;
    this.cdGrupAs = cdGrupAs;
    this.cdGrupNm = cdGrupNm;
    this.cdDesc = cdDesc;
    this.cdDesc1 = cdDesc1;
    this.cdDesc2 = cdDesc2;
    this.cdDesc3 = cdDesc3;
    this.cdDesc4 = cdDesc4;
    this.cdDesc5 = cdDesc5;
    this.cmpyCd = cmpyCd;
  }

  public static EpcScodeMstTest create(SolutionType solCd, String cdGrup, String cdGrupAs,
      String cdGrupNm,
      String cdDesc, String cdDesc1, String cdDesc2, String cdDesc3, String cdDesc4,
      String cdDesc5, String cmpyCd) {
    return EpcScodeMstTest.builder()
        .solCd(solCd)
        .cdGrup(cdGrup)
        .cdGrupAs(cdGrupAs)
        .cdGrupNm(cdGrupNm)
        .cdDesc(cdDesc)
        .cdDesc1(cdDesc1)
        .cdDesc2(cdDesc2)
        .cdDesc3(cdDesc3)
        .cdDesc4(cdDesc4)
        .cdDesc5(cdDesc5)
        .cmpyCd(cmpyCd)
        .build();
  }

  public void update(String cdGrupAs, String cdGrupNm,
      String cdDesc, String cdDesc1, String cdDesc2, String cdDesc3, String cdDesc4,
      String cdDesc5, String cmpyCd, boolean useYn) {

    this.cdGrupAs = cdGrupAs;
    this.cdGrupNm = cdGrupNm;
    this.cdDesc = cdDesc;
    this.cdDesc1 = cdDesc1;
    this.cdDesc2 = cdDesc2;
    this.cdDesc3 = cdDesc3;
    this.cdDesc4 = cdDesc4;
    this.cdDesc5 = cdDesc5;
    this.cmpyCd = cmpyCd;
    super.updateUseYn(useYn);
  }

  @Override
  public void updateUseYn(boolean useYn) {
    super.updateUseYn(useYn);
  }


}
