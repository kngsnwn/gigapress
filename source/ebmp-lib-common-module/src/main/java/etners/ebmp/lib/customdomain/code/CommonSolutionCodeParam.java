package etners.ebmp.lib.customdomain.code;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommonSolutionCodeParam {

  private String solCd;
  private String cdGrup;
  private String cdGrupAs;
  private CommonCodeParamOption options;

  public CommonSolutionCodeParam(String solCd, String cdGrup, CommonCodeParamOption options) {
    this.solCd = solCd;
    this.cdGrup = cdGrup;
    this.options = options;
  }

  public CommonSolutionCodeParam(String solCd, String cdGrup) {
    this(solCd, cdGrup, null);
  }
}
