package etners.common.basetype;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class CommonMessage {

  private Map<String, String> code = new HashMap<>();
  private Map<String, String> message = new HashMap<>();
  private Map<String, String> menu = new HashMap<>();

  public void pushCode(Map<String, String> codeMaster) {
    this.code.putAll(codeMaster);
  }

  public void pushMenu(Map<String, String> menuMaster) {
    this.menu.putAll(menuMaster);
  }

  public void pushMessage(Map<String, String> messageMaster) {
    this.message.putAll(messageMaster);
  }
}
