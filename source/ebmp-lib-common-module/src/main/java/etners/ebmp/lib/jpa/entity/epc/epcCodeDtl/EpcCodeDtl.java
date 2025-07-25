package etners.ebmp.lib.jpa.entity.epc.epcCodeDtl;

import common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "EPC_CODE_DTL")
@IdClass(EpcCodeDtlPK.class)
public class EpcCodeDtl extends BaseEntity implements Serializable {

  private static final long serialVersionUID = -6408506050350432271L;

  @Id
  @Column(name = "CMPY_CD")
  private String cmpyCd;

  @Id
  @Column(name = "CD_GRUP")
  private String cdGrup;

  @Id
  @Column(name = "CD_ID")
  private String cdId;

  @Column(name = "CD_NM")
  private String cdNm;

  @Column(name = "CD_VALUE")
  private String cdValue;

  @Column(name = "CD_DESC")
  private String cdDesc;

  @Column(name = "ICON_FILE_ID")
  private String iconFileId;
  @Column(name = "CD_DESC1", nullable = false, length = 2000)
  private String cdDesc1;

  @Builder
  public EpcCodeDtl(String cmpyCd, String cdGrup, String cdId, String cdNm, String cdValue, String cdDesc, String iconFileId, String cdDesc1) {
    this.cmpyCd = cmpyCd;
    this.cdGrup = cdGrup;
    this.cdId = cdId;
    this.cdNm = cdNm;
    this.cdValue = cdValue;
    this.cdDesc = cdDesc;
    this.iconFileId = iconFileId;
    this.cdDesc1 = cdDesc1;
  }
}
