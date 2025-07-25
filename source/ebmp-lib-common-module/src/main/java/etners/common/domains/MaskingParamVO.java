package etners.common.domains;

public interface MaskingParamVO {

  default boolean disableMasking() {
    return false;
  }
}
