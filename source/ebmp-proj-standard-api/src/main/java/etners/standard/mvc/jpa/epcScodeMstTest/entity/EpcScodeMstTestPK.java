package etners.standard.mvc.jpa.epcScodeMstTest.entity;

import etners.common.util.enumType.SolutionType;
import java.io.Serializable;

import etners.standard.mvc.jpa.epcScodeMstTest.converter.SolutionTypeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpcScodeMstTestPK implements Serializable {

  private static final long serialVersionUID = 1L;

  //@Id 와 @Convert는 같이 사용할 수 없음.
  @Convert(converter = SolutionTypeConverter.class)
  @Column(name = "SOL_CD")
  private SolutionType solCd;

  @Column(name = "CD_GRUP")
  private String cdGrup;

  @Builder
  public EpcScodeMstTestPK(SolutionType solCd, String cdGrup) {
    this.solCd = solCd;
    this.cdGrup = cdGrup;
  }
}
