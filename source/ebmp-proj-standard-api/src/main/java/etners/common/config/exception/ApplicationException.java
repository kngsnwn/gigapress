package etners.common.config.exception;

import etners.common.util.enumType.ErrorCode;

public class ApplicationException extends RuntimeException {
  private final ErrorCode errorCode;

  public ApplicationException() {
    this(ErrorCode.DEFAULT);
  }

  public ApplicationException(ErrorCode errorCode) {
    super(errorCode.getDesc());
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode.getCode();
  }

  public String getErrorDescription() {
    return errorCode.getDesc();
  }
}
