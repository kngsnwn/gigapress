package etners.ebmp.lib.customdomain.code;

import lombok.Data;

@Data
public class CommonAllSolutionCodeParam {

  private String[] solCd;

  public CommonAllSolutionCodeParam() {
  }

  public CommonAllSolutionCodeParam(String... solCdArray) {
    this.solCd = solCdArray;
  }
}
