package etners.ebmp.lib.customdomain.code;

import lombok.Data;

@Data
public class CommonAllCodeParam {

  private String[] cmpyCd;

  public CommonAllCodeParam() {
  }

  public CommonAllCodeParam(String... cmpyCdArray) {
    this.cmpyCd = cmpyCdArray;
  }

}
