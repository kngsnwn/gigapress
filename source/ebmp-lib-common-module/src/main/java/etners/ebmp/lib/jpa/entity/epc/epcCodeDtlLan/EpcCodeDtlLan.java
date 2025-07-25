package etners.ebmp.lib.jpa.entity.epc.epcCodeDtlLan;

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
@IdClass(EpcCodeDtlLanPK.class)
@Table(name = "EPC_CODE_DTL_LAN")
public class EpcCodeDtlLan extends BaseEntity {

  @Id
  @Column(name = "CMPY_CD", nullable = false, insertable = false, updatable = false)
  private String cmpyCd;

  @Id
  @Column(name = "CD_GRUP", nullable = false)
  private String cdGrup;

  @Id
  @Column(name = "CD_ID", nullable = false, length = 20)
  private String cdId;

  @Column(name = "CD_EN", nullable = false, length = 1000)
  private String cdEn;

  @Column(name = "CD_ZH", nullable = false, length = 1000)
  private String cdZh;

  @Column(name = "CD_VN", nullable = false, length = 1000)
  private String cdVn;

  @Column(name = "CD_JP", nullable = false, length = 1000)
  private String cdJp;

  @Builder
  public EpcCodeDtlLan(String cmpyCd, String cdGrup, String cdId, String cdEn, String cdZh, String cdVn, String cdJp) {
    this.cmpyCd = cmpyCd;
    this.cdGrup = cdGrup;
    this.cdId = cdId;
    this.cdEn = cdEn;
    this.cdZh = cdZh;
    this.cdVn = cdVn;
    this.cdJp = cdJp;
  }
}
