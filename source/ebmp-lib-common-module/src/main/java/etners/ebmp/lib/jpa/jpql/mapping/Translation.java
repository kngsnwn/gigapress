package etners.ebmp.lib.jpa.jpql.mapping;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class Translation {

  private Map<String, String> codeMaster;

  public Translation(String cmpyCd, String cdNm) {

    Map<String, String> map = new HashMap<>();
    map.put(cmpyCd, cdNm);
    this.codeMaster = map;
  }


}
