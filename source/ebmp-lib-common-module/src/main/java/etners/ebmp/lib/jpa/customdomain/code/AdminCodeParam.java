package etners.ebmp.lib.jpa.customdomain.code;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminCodeParam {

  private String cmpyCd;
  private String solCd;
  private String cdGrup;
  private String cdGrupAs;
  private CommonCodeParamOption options;
  private List<String> exceptCommonCodes;


  public AdminCodeParam(String cmpyCd, String cdGrup, CommonCodeParamOption options) {
    this.cmpyCd = cmpyCd;
    this.cdGrup = cdGrup;
    this.options = options;
  }

  public AdminCodeParam(CommonCodeSelectOneParam commonCodeSelectOneParam) {
    this.cmpyCd = commonCodeSelectOneParam.getCmpyCd();
    this.cdGrup = commonCodeSelectOneParam.getCdGrup();
    this.options = new CommonCodeParamOption(commonCodeSelectOneParam.getCdId());
  }

  public AdminCodeParam(String cmpyCd, String cdGrup) {
    this(cmpyCd, cdGrup, null);
  }


}
