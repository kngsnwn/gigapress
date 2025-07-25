package etners.common.domains.publisher;

import java.util.Map;
import kotlinx.serialization.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Serializable
public class MessageDto {

  private String message;

  private String sender;

  private Map<String, String> data;

  @Builder
  public MessageDto(Map<String, String> data, String message, String sender) {
    this.data = data;
    this.message = message;
    this.sender = sender;
  }
}
