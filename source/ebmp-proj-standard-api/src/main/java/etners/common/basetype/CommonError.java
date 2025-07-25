package etners.common.basetype;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CommonError {

  private Integer result;

  private String message;

  private String className;

  private String redirectUrl;

}
