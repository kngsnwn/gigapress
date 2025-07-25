package etners.common.util.converter;

import java.lang.reflect.ParameterizedType;
import jakarta.persistence.AttributeConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

@Getter
public class AbstractEnumAttributeConverter<E extends Enum<E> & CommonType> implements
    AttributeConverter<E, String> {

  private Class<E> targetEnumClass;

  private boolean nullable;

  private String enumName;

  private Class<E> detectAttributeType() {
    ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
    return (Class<E>) type.getActualTypeArguments()[0];
  }

  public AbstractEnumAttributeConverter(boolean nullable, String enumName) {
    this.targetEnumClass = detectAttributeType();
    this.nullable = nullable;
    this.enumName = enumName;
  }

  /**
   * Converts the value stored in the entity attribute into the data representation to be stored in
   * the database.
   *
   * @param attribute the entity attribute value to be converted
   * @return the converted data to be stored in the database column
   */
  @Override
  public String convertToDatabaseColumn(E attribute) {
    if (!nullable && attribute == null) {
      throw new IllegalArgumentException(String.format("%s는 NULL로 저장할 수 없습니다.", enumName));
    }
    return EnumValueConvertUtils.toCode(attribute);
  }

  /**
   * Converts the data stored in the database column into the value to be stored in the entity
   * attribute. Note that it is the responsibility of the converter writer to specify the correct
   * <code>dbData</code> type for the corresponding column for use by the JDBC driver: i.e.,
   * persistence providers are not expected to do such type conversion.
   *
   * @param dbData the data from the database column to be converted
   * @return the converted value to be stored in the entity attribute
   */
  @Override
  public E convertToEntityAttribute(String dbData) {
    if (!nullable && ObjectUtils.isEmpty(dbData)) {
      throw new IllegalArgumentException(
          String.format("%s는 DB에 NULL 혹은 빈값(%s) 저장되어 있습니다.", enumName, dbData));
    }
    return EnumValueConvertUtils.ofCode(targetEnumClass, dbData);
  }
}
