package etners.ebmp.lib.jpa.entity.epc.epcCodeLan;

import common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "EPC_CODE_LAN")
@IdClass(EpcCodeLanPK.class)
public class EpcCodeLan extends BaseEntity {

  @Id
  @Column(name = "CMPY_CD", nullable = false, insertable = false, updatable = false)
  private String cmpyCd;
  @Id
  @Column(name = "CD_GRUP", nullable = false)
  private String cdGrup;

  @Column(name = "CD_GRUP_EN", nullable = false, length = 1000)
  private String cdGrupEn;

  @Column(name = "CD_GRUP_ZH", nullable = false, length = 1000)
  private String cdGrupZh;

  @Column(name = "CD_GRUP_VN", nullable = false, length = 1000)
  private String cdGrupVn;

  @Column(name = "CD_GRUP_JP", nullable = false, length = 1000)
  private String cdGrupJp;

  @Builder
  public EpcCodeLan(String cmpyCd, String cdGrup, String cdGrupEn, String cdGrupZh, String cdGrupVn, String cdGrupJp) {
    this.cmpyCd = cmpyCd;
    this.cdGrup = cdGrup;
    this.cdGrupEn = cdGrupEn;
    this.cdGrupZh = cdGrupZh;
    this.cdGrupVn = cdGrupVn;
    this.cdGrupJp = cdGrupJp;
  }
}
