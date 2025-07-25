package etners.ebmp.lib.jpa.entity.epc.epcScodeDtl;

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
@Table(name = "EPC_SCODE_DTL")
@IdClass(EpcScodeDtlPK.class)
public class EpcScodeDtl extends BaseEntity implements Serializable {

  private static final long serialVersionUID = -2637855117208243767L;

  @Id
  @Column(name = "SOL_CD")
  private String solCd;

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

  @Column(name = "CD_DESC1")
  private String cdDesc1;

  @Column(name = "ICON_FILE_ID")
  private String iconFileId;

  @Builder
  public EpcScodeDtl(String solCd, String cdGrup, String cdId, String cdNm, String cdValue, String cdDesc, String cdDesc1, String iconFileId) {
    this.solCd = solCd;
    this.cdGrup = cdGrup;
    this.cdId = cdId;
    this.cdNm = cdNm;
    this.cdValue = cdValue;
    this.cdDesc = cdDesc;
    this.cdDesc1 = cdDesc1;
    this.iconFileId = iconFileId;
  }
}
