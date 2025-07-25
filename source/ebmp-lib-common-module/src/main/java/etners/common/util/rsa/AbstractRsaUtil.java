package etners.common.util.rsa;


import static etners.common.util.object.ObjectCommonUtil.getAllFields;
import static etners.common.util.object.ObjectCommonUtil.getFieldValue;
import static etners.common.util.object.ObjectCommonUtil.getFieldsWithAnnotation;
import static etners.common.util.object.ObjectCommonUtil.hasAnnotationInFields;
import static etners.common.util.object.ObjectCommonUtil.isCollectionType;
import static etners.common.util.object.ObjectCommonUtil.isCustomObject;
import static etners.common.util.object.ObjectCommonUtil.isJavaBasicType;

import common.util.cipher.RSA;
import common.util.string.StringUtil;
import etners.common.domains.rsa.RsaCryptoVO;
import etners.common.util.annotation.rsa.RsaCrypto;
import etners.common.util.annotation.rsa.RsaKey;
import etners.common.util.enumType.CryptoMode;
import etners.common.util.enumType.KeyType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

@Slf4j
public abstract class AbstractRsaUtil {

  // RSA 전용 필드 캐시
  private static final Map<String, List<Field>> RSA_CRYPTO_FIELDS_CACHE = new ConcurrentHashMap<>();
  // 모든 필드 탐색용 캐시 (새로 추가)
  private static final Map<Class<?>, List<Field>> ALL_PROCESSABLE_FIELDS_CACHE = new ConcurrentHashMap<>();


  protected abstract PrivateKey fetchPrivateKey(String publicKey);

  /**
   * 객체를 재귀적으로 암호화 (새로운 통합 메서드)
   */
  public void encryptObjectRecursively(Object object) {
    try {
      String publicKey = extractPublicKey(object);
      if (StringUtil.isEmpty(publicKey)) {
        log.debug("Public key를 찾을 수 없어 암호화를 건너뜁니다.");
        return;

      }
      encryptObjectWithPublicKey(object, publicKey);
    } catch (Exception e) {
      log.warn("암호화 처리 중 오류가 발생하여 암호화를 건너뜁니다.");
      log.error(StringUtil.extractStackTrace(e));
    }
  }


  /**
   * 객체를 재귀적으로 복호화 (새로운 통합 메서드)
   */
  public void decryptObjectRecursively(Object object) {
    try {
      if (ObjectUtils.isNotEmpty(object)) {
        if (object instanceof RsaCryptoVO || hasAnnotationInFields(object.getClass(), RsaKey.class)) {
          PrivateKey privateKey = extractPrivateKey(object);
          if (ObjectUtils.isEmpty(privateKey)) {
            log.warn("PrivateKey를 찾을 수 없습니다. 복호화를 건너뜁니다.");
            return;
          }

          decryptObjectWithPrivateKey(object, privateKey);
        }
      }
    } catch (Exception e) {
      log.warn("복호화 처리 중 오류가 발생하여 복호화를 건너뜁니다.");
      log.error(StringUtil.extractStackTrace(e));
    }
  }

  private void encryptObjectWithPublicKey(Object object, String publicKey) throws IllegalAccessException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
    if (ObjectUtils.isEmpty(object) || isJavaBasicType(object.getClass())) {
      return;
    }
    for (Field field : getAllProcessableFields(object.getClass())) {
      field.setAccessible(true);
      Object originalValue = field.get(object);
      if (ObjectUtils.isEmpty(originalValue)) {
        continue;
      }
      processFieldForEncryption(object, field, originalValue, publicKey);
    }
  }

  private void decryptObjectWithPrivateKey(Object object, PrivateKey privateKey) throws IllegalAccessException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    if (ObjectUtils.isEmpty(object) || isJavaBasicType(object.getClass())) {
      return;
    }
    for (Field field : getAllProcessableFields(object.getClass())) {
      field.setAccessible(true);
      Object originalValue = field.get(object);

      if (ObjectUtils.isEmpty(originalValue)) {
        continue;
      }
      processFieldForDecryption(object, field, originalValue, privateKey);
    }
  }

  /**
   * 암호화 필드 처리 (개선됨)
   */
  private void processFieldForEncryption(Object object, Field field, Object originalValue, String publicKey)
    throws IllegalAccessException, NoSuchPaddingException, IllegalBlockSizeException,
    NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {

    // 1. @RsaCrypto 어노테이션이 있는 String 필드 직접 처리
    if (field.isAnnotationPresent(RsaCrypto.class)) {
      RsaCrypto annotation = field.getAnnotation(RsaCrypto.class);
      CryptoMode mode = annotation.mode();

      // 암호화 모드가 아니면 스킵
      if (mode != CryptoMode.ENCRYPT && mode != CryptoMode.ALL) {
        return;
      }

      if (originalValue instanceof String value) {
        if (StringUtil.isNotEmpty(value) && shouldProcessField(object, field, true)) {
          String encryptedValue = RSA.encryptRsa(publicKey, value);
          field.set(object, encryptedValue);
        }
        return; // String 필드 처리 완료
      }
    }

    // 2. 하위 객체나 컬렉션 재귀 처리
    if (originalValue instanceof Map<?, ?> mapValue) {
      encryptMapWithPublicKey(mapValue, publicKey);
    } else if (originalValue instanceof List<?> listValue) {
      encryptListWithPublicKey(listValue, publicKey);
    } else if (originalValue instanceof Set<?> setValue) {
      encryptSetWithPublicKey(setValue, publicKey);
    } else if (originalValue.getClass().isArray()) {
      encryptArrayWithPublicKey(originalValue, publicKey);
    } else if (!isJavaBasicType(originalValue.getClass())) {
      // 커스텀 객체인 경우 재귀적으로 처리 (내부에 @RsaCrypto가 있을 수 있음)
      encryptObjectWithPublicKey(originalValue, publicKey);
    }
  }

  /**
   * 복호화 필드 처리 (개선됨)
   */
  private void processFieldForDecryption(Object object, Field field, Object originalValue, PrivateKey privateKey)
    throws IllegalAccessException, NoSuchPaddingException, IllegalBlockSizeException,
    NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

    // 1. @RsaCrypto 어노테이션이 있는 String 필드 직접 처리
    if (field.isAnnotationPresent(RsaCrypto.class)) {
      RsaCrypto annotation = field.getAnnotation(RsaCrypto.class);
      CryptoMode mode = annotation.mode();

      // 복호화 모드가 아니면 스킵
      if (mode != CryptoMode.DECRYPT && mode != CryptoMode.ALL) {
        return;
      }

      if (originalValue instanceof String value) {
        if (StringUtil.isNotEmpty(value) && shouldProcessField(object, field, false)) {
          String decryptedValue = RSA.decryptRsa(privateKey, value);
          field.set(object, decryptedValue);
        }
        return; // String 필드 처리 완료
      }
    }

    // 2. 하위 객체나 컬렉션 재귀 처리
    if (originalValue instanceof Map<?, ?> mapValue) {
      decryptMapWithPrivateKey(mapValue, privateKey);
    } else if (originalValue instanceof List<?> listValue) {
      decryptListWithPrivateKey(listValue, privateKey);
    } else if (originalValue instanceof Set<?> setValue) {
      decryptSetWithPrivateKey(setValue, privateKey);
    } else if (originalValue.getClass().isArray()) {
      decryptArrayWithPrivateKey(originalValue, privateKey);
    } else if (!isJavaBasicType(originalValue.getClass())) {
      // 커스텀 객체인 경우 재귀적으로 처리 (내부에 @RsaCrypto가 있을 수 있음)
      decryptObjectWithPrivateKey(originalValue, privateKey);
    }
  }


  /**
   * Set 내부 객체들을 암호화
   */
  private void encryptSetWithPublicKey(Set<?> setObject, String publicKey)
    throws IllegalAccessException, NoSuchPaddingException, IllegalBlockSizeException,
    NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {

    if (ObjectUtils.isEmpty(setObject)) {
      return;
    }

    for (Object item : setObject) {
      if (!isJavaBasicType(item.getClass())) {
        encryptObjectWithPublicKey(item, publicKey);
      }
    }
  }

  /**
   * Set 내부 객체들을 복호화
   */
  private void decryptSetWithPrivateKey(Set<?> setObject, PrivateKey privateKey)
    throws IllegalAccessException, NoSuchPaddingException, IllegalBlockSizeException,
    NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

    if (ObjectUtils.isEmpty(setObject)) {
      return;
    }

    for (Object item : setObject) {
      if (!isJavaBasicType(item.getClass())) {
        decryptObjectWithPrivateKey(item, privateKey);
      }
    }
  }

  /**
   * 처리 가능한 모든 필드 가져오기 (캐시 적용) - @RsaCrypto가 있는 필드 - 하위 객체 필드 (내부에 @RsaCrypto가 있을 가능성) - 컬렉션 필드 (내부 요소에 @RsaCrypto가 있을 가능성)
   */
  private List<Field> getAllProcessableFields(Class<?> clazz) {
    return ALL_PROCESSABLE_FIELDS_CACHE.computeIfAbsent(clazz, c ->
      getAllFields(c).stream()
        .filter(this::isProcessableField)
        .toList()
    );
  }

  /**
   * 필드가 처리 가능한지 판단 1. @RsaCrypto 어노테이션이 있는 필드 2. 커스텀 객체 필드 (내부에 @RsaCrypto가 있을 수 있음) 3. 컬렉션 필드 (내부 요소에 @RsaCrypto가 있을 수 있음)
   */
  private boolean isProcessableField(Field field) {
    // 1. @RsaCrypto 어노테이션이 있는 필드는 항상 처리
    if (field.isAnnotationPresent(RsaCrypto.class)) {
      return true;
    }

    // 2. 컬렉션 타입은 포함 (내부 요소 처리를 위해)
    if (isCollectionType(field.getType())) {
      return true;
    }

    // 3. Java 기본 타입은 제외
    if (isJavaBasicType(field.getType())) {
      return false;
    }

    // 4. 커스텀 객체는 포함 (내부에 @RsaCrypto가 있을 수 있음)
    return !isCustomObject(field.getType());
  }


  /**
   * RSA 암호화 대상 필드들 가져오기 (캐시 적용)
   */
  private List<Field> getRsaCryptoFields(Class<?> clazz, CryptoMode targetMode) {
    String cacheKey = clazz.getName() + "_" + targetMode.name();

    return RSA_CRYPTO_FIELDS_CACHE.computeIfAbsent(cacheKey, k ->
      getFieldsWithAnnotation(clazz, RsaCrypto.class, annotation -> {
        CryptoMode mode = annotation.mode();
        return mode == targetMode || mode == CryptoMode.ALL;
      })
    );
  }

  /**
   * 배열 내부 객체들을 암호화
   */
  private void encryptArrayWithPublicKey(Object arrayObject, String publicKey)
    throws IllegalAccessException, NoSuchPaddingException, IllegalBlockSizeException,
    NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {

    if (arrayObject == null) {
      return;
    }

    int length = Array.getLength(arrayObject);
    for (int i = 0; i < length; i++) {
      Object item = Array.get(arrayObject, i);
      if (item != null && !isJavaBasicType(item.getClass())) {
        encryptObjectWithPublicKey(item, publicKey);
      }
    }
  }

  /**
   * 배열 내부 객체들을 복호화
   */
  private void decryptArrayWithPrivateKey(Object arrayObject, PrivateKey privateKey)
    throws IllegalAccessException, NoSuchPaddingException, IllegalBlockSizeException,
    NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

    if (arrayObject == null) {
      return;
    }

    int length = Array.getLength(arrayObject);
    for (int i = 0; i < length; i++) {
      Object item = Array.get(arrayObject, i);
      if (item != null && !isJavaBasicType(item.getClass())) {
        decryptObjectWithPrivateKey(item, privateKey);
      }
    }
  }

  /**
   * Map 내부 값들을 암호화 (개선됨)
   */
  private void encryptMapWithPublicKey(Map<?, ?> mapObject, String publicKey)
    throws IllegalAccessException, NoSuchPaddingException, IllegalBlockSizeException,
    NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {

    if (ObjectUtils.isEmpty(mapObject)) {
      return;
    }

    for (Entry<?, ?> entry : mapObject.entrySet()) {
      Object key = entry.getKey();
      Object value = entry.getValue();

      // Map의 key도 처리 (커스텀 객체인 경우)
      if (key != null && !isJavaBasicType(key.getClass())) {
        encryptObjectWithPublicKey(key, publicKey);
      }

      // Map의 value 처리
      if (value != null && !isJavaBasicType(value.getClass())) {
        encryptObjectWithPublicKey(value, publicKey);
      }
    }
  }

  /**
   * Map 내부 값들을 복호화 (개선됨)
   */
  private void decryptMapWithPrivateKey(Map<?, ?> mapObject, PrivateKey privateKey)
    throws IllegalAccessException, NoSuchPaddingException, IllegalBlockSizeException,
    NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

    if (ObjectUtils.isEmpty(mapObject)) {
      return;
    }

    for (Entry<?, ?> entry : mapObject.entrySet()) {
      Object key = entry.getKey();
      Object value = entry.getValue();

      // Map의 key도 처리 (커스텀 객체인 경우)
      if (key != null && !isJavaBasicType(key.getClass())) {
        decryptObjectWithPrivateKey(key, privateKey);
      }

      // Map의 value 처리
      if (value != null && !isJavaBasicType(value.getClass())) {
        decryptObjectWithPrivateKey(value, privateKey);
      }
    }
  }

  /**
   * List 내부 객체들을 암호화 (개선됨)
   */
  private void encryptListWithPublicKey(List<?> listObject, String publicKey)
    throws IllegalAccessException, NoSuchPaddingException, IllegalBlockSizeException,
    NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {

    if (ObjectUtils.isEmpty(listObject)) {
      return;
    }

    for (Object item : listObject) {
      if (item != null && !isJavaBasicType(item.getClass())) {
        encryptObjectWithPublicKey(item, publicKey);
      }
    }
  }

  /**
   * List 내부 객체들을 복호화 (개선됨)
   */
  private void decryptListWithPrivateKey(List<?> listObject, PrivateKey privateKey)
    throws IllegalAccessException, NoSuchPaddingException, IllegalBlockSizeException,
    NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

    if (ObjectUtils.isEmpty(listObject)) {
      return;
    }

    for (Object item : listObject) {
      if (item != null && !isJavaBasicType(item.getClass())) {
        decryptObjectWithPrivateKey(item, privateKey);
      }
    }
  }

  /**
   * 필드 처리 여부 결정 (Dirty 체크 포함)
   */
  private boolean shouldProcessField(Object object, Field field, boolean isEncryption) {
    try {
      String dirtyFieldName = field.getName() + "Dirty";
      Optional<Object> dirtyValue = getFieldValue(object, dirtyFieldName);

      if (dirtyValue.isPresent() && dirtyValue.get() instanceof Boolean isDirty) {

        if (isEncryption) {
          // 암호화: dirty가 true인 경우에만 처리
          return Boolean.TRUE.equals(isDirty);
        } else {
          // 복호화: dirty가 false이거나 null인 경우에만 처리
          return !Boolean.TRUE.equals(isDirty);
        }
      }

      // dirty 필드가 없거나 Boolean이 아니면 항상 처리
      return true;

    } catch (Exception e) {
      log.debug("Dirty 필드 확인 실패, 기본 처리 진행: {}", e.getMessage());
      return true;
    }
  }

  public String extractPublicKey(Object object) {
    if (ObjectUtils.isEmpty(object)) {
      return null;
    }
    List<Field> keyFields = getFieldsWithAnnotation(object.getClass(), RsaKey.class,
      rsaKey -> KeyType.PUBLIC.equals(rsaKey.type()));

    for (Field field : keyFields) {
      try {
        field.setAccessible(true);
        return (String) field.get(object);
      } catch (IllegalAccessException e) {
        log.warn("PublicKey 필드 접근 실패: {}", field.getName());
      }
    }

    return null;
  }

  public PrivateKey extractPrivateKey(Object object) {
    if (ObjectUtils.isEmpty(object)) {
      return null;
    }
    List<Field> keyFields = getFieldsWithAnnotation(object.getClass(), RsaKey.class,
      rsaKey -> KeyType.PRIVATE.equals(rsaKey.type()));

    for (Field field : keyFields) {
      try {
        field.setAccessible(true);
        Object value = field.get(object);

        if (value != null) {
          return convertToPrivateKey(value, field);
        } else {
          // PrivateKey가 null인 경우 PublicKey로부터 가져오기
          PrivateKey privateKey = fetchPrivateKeyFromPublicKey(object);
          field.set(object, privateKey);
          return privateKey;
        }
      } catch (IllegalAccessException e) {
        log.warn("PrivateKey 필드 접근 실패: {}", field.getName());
      }
    }

    throw new IllegalArgumentException("PRIVATE 키가 설정되지 않았습니다.");
  }

  /**
   * 값을 PrivateKey로 변환
   */
  private PrivateKey convertToPrivateKey(Object value, Field field) {
    if (PrivateKey.class.isAssignableFrom(field.getType())) {
      return (PrivateKey) value;
    } else if (String.class.isAssignableFrom(field.getType())) {
      return RSA.String2PrivateKey((String) value);
    } else {
      throw new IllegalArgumentException(
        String.format("PRIVATE 키 필드는 PrivateKey 혹은 String 타입이어야 합니다. 현재 타입: %s",
          field.getType().getName()));
    }
  }

  private PrivateKey fetchPrivateKeyFromPublicKey(Object object) {
    try {
      String publicKey = extractPublicKey(object);
      if (StringUtil.isNotEmpty(publicKey)) {
        return fetchPrivateKey(publicKey);
      }
    } catch (Exception e) {
      log.error("PublicKey로부터 PrivateKey 조회 실패: {}", e.getMessage());
    }
    return null;
  }

  public void encryptFields(Object object) {
    encryptObjectRecursively(object);
  }

  public void decryptFields(Object object) {
    decryptObjectRecursively(object);
  }

  public String decryptField(Object object) {
    try {
      PrivateKey privateKey = extractPrivateKey(object);
      if (privateKey == null) {
        return null;
      }

      List<Field> cryptoFields = getRsaCryptoFields(object.getClass(), CryptoMode.DECRYPT);

      for (Field field : cryptoFields) {
        if (shouldProcessField(object, field, false)) {
          field.setAccessible(true);
          String originalValue = (String) field.get(object);

          if (StringUtil.isNotEmpty(originalValue)) {
            return RSA.decryptRsa(privateKey, originalValue);
          }
        }
      }
    } catch (Exception e) {
      log.error("필드 복호화 중 오류 발생: {}", e.getMessage());
    }
    return null;
  }

}
