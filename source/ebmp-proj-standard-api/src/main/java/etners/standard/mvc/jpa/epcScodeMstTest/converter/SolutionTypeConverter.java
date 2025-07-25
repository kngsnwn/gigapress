package etners.standard.mvc.jpa.epcScodeMstTest.converter;

import etners.common.util.converter.AbstractEnumAttributeConverter;
import etners.common.util.enumType.SolutionType;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SolutionTypeConverter extends
    AbstractEnumAttributeConverter<SolutionType> {

  public static final String ENUM_NAME = "솔루션 코드";

  public SolutionTypeConverter() {
    super(false, ENUM_NAME);
  }
}
