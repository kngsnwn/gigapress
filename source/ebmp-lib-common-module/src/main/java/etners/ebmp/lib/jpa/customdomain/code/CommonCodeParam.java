package etners.ebmp.lib.jpa.customdomain.code;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommonCodeParam {

  private String cmpyCd;
  private String solCd;
  private String cdGrup;
  private String cdGrupAs;
  private CommonCodeParamOption options;

  public CommonCodeParam(String cmpyCd, String cdGrup, CommonCodeParamOption options) {
    this.cmpyCd = cmpyCd;
    this.cdGrup = cdGrup;
    this.options = options;
  }

  public CommonCodeParam(CommonCodeSelectOneParam commonCodeSelectOneParam) {
    this.cmpyCd = commonCodeSelectOneParam.getCmpyCd();
    this.cdGrup = commonCodeSelectOneParam.getCdGrup();
    this.options = new CommonCodeParamOption(commonCodeSelectOneParam.getCdId());
  }

  public CommonCodeParam(String cmpyCd, String cdGrup) {
    this(cmpyCd, cdGrup, null);
  }
}
