package etners.ebmp.lib.jpa.entity.epc.epcScodeDtlLan;

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
@Table(name = "EPC_SCODE_DTL_LAN")
@IdClass(EpcScodeDtlLanPK.class)
public class EpcScodeDtlLan extends BaseEntity implements Serializable {

  private static final long serialVersionUID = 4401953495530103874L;
  @Id
  @Column(name = "SOL_CD")
  private String solCd;

  @Id
  @Column(name = "CD_GRUP")
  private String cdGrup;

  @Id
  @Column(name = "CD_ID")
  private String cdId;


  @Column(name = "CD_EN", nullable = false, length = 1000)
  private String cdEn;


  @Column(name = "CD_ZH", nullable = true, length = 1000)
  private String cdZh;


  @Column(name = "CD_VN", nullable = true, length = 1000)
  private String cdVn;


  @Column(name = "CD_JP", nullable = true, length = 1000)
  private String cdJp;


  @Builder
  public EpcScodeDtlLan(String solCd, String cdGrup, String cdId, String cdEn, String cdZh, String cdVn, String cdJp) {
    this.solCd = solCd;
    this.cdGrup = cdGrup;
    this.cdId = cdId;
    this.cdEn = cdEn;
    this.cdZh = cdZh;
    this.cdVn = cdVn;
    this.cdJp = cdJp;
  }
}
