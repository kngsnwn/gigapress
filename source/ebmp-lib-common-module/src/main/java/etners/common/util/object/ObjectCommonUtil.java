package etners.common.util.object;


import common.util.string.StringUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectCommonUtil {

  // 필드 캐시 - 리플렉션 성능 최적화
  private static final Map<Class<?>, List<Field>> ALL_FIELDS_CACHE = new ConcurrentHashMap<>();
  private static final Map<String, List<Field>> ANNOTATION_FIELDS_CACHE = new ConcurrentHashMap<>();
  private static final Map<Class<?>, Boolean> CUSTOM_OBJECT_CACHE = new ConcurrentHashMap<>();

  // Java 기본 타입 캐시
  private static final Set<Class<?>> JAVA_BASIC_TYPES = Set.of(
    String.class, Boolean.class, Byte.class, Character.class, Short.class,
    Integer.class, Long.class, Float.class, Double.class, BigDecimal.class,
    BigInteger.class, Date.class, LocalDate.class, LocalDateTime.class,
    LocalTime.class, UUID.class
  );

  /**
   * 클래스의 모든 필드 가져오기 (상속 관계 포함, 캐시 적용)
   */
  public static List<Field> getAllFields(Class<?> clazz) {
    if (clazz == null) {
      return Collections.emptyList();
    }

    return ALL_FIELDS_CACHE.computeIfAbsent(clazz, c -> {
      List<Field> accessibleFields = new ArrayList<>();
      Class<?> currentClass = c;

      while (currentClass != null && currentClass != Object.class) {
        Field[] declaredFields = currentClass.getDeclaredFields();

        for (Field field : declaredFields) {
          if (isFieldAccessible(field)) {
            accessibleFields.add(field);
          }
        }

        currentClass = currentClass.getSuperclass();
      }

      log.debug("Cached {} accessible fields for class: {}",
        accessibleFields.size(), clazz.getSimpleName());
      return Collections.unmodifiableList(accessibleFields);
    });
  }

  /**
   * 필드가 접근 가능한지 확인
   */
  private static boolean isFieldAccessible(Field field) {
    // 이미 public이면 setAccessible 호출 없이도 접근 가능
    if (Modifier.isPublic(field.getModifiers()) &&
      Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
      return true;
    }

    try {
      field.setAccessible(true);
      return true;
    } catch (SecurityException | InaccessibleObjectException e) {
      return false;
    }
  }

  /**
   * 특정 어노테이션이 있는 필드들 가져오기 (캐시 적용)
   */
  public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
    if (clazz == null || annotationClass == null) {
      return Collections.emptyList();
    }

    String cacheKey = clazz.getName() + "_" + annotationClass.getSimpleName();

    return ANNOTATION_FIELDS_CACHE.computeIfAbsent(cacheKey, k ->
      getAllFields(clazz).stream()
        .filter(field -> field.isAnnotationPresent(annotationClass))
        .toList()
    );
  }

  /**
   * 조건부 필터링된 어노테이션 필드들 가져오기
   */
  public static <T extends Annotation> List<Field> getFieldsWithAnnotation(
    Class<?> clazz,
    Class<T> annotationClass,
    Predicate<T> annotationFilter) {

    if (clazz == null || annotationClass == null || annotationFilter == null) {
      return Collections.emptyList();
    }

    return getAllFields(clazz).stream()
      .filter(field -> field.isAnnotationPresent(annotationClass))
      .filter(field -> annotationFilter.test(field.getAnnotation(annotationClass)))
      .collect(Collectors.toList());
  }

  /**
   * 클래스에 특정 어노테이션이 있는 필드가 존재하는지 확인
   */
  public static boolean hasAnnotationInFields(Class<?> clazz, Class<? extends Annotation> annotationClass) {
    if (clazz == null || annotationClass == null) {
      return false;
    }

    return !getFieldsWithAnnotation(clazz, annotationClass).isEmpty();
  }

  /**
   * Java 기본 타입 여부 확인 (성능 최적화된 버전)
   */
  public static boolean isJavaBasicType(Class<?> clazz) {
    if (clazz == null) {
      return true;
    }

    // 원시 타입 확인
    if (clazz.isPrimitive()) {
      return true;
    }

    // 기본 래퍼 클래스 확인
    if (JAVA_BASIC_TYPES.contains(clazz)) {
      return true;
    }

    // 배열 타입 확인
    if (clazz.isArray()) {
      return isJavaBasicType(clazz.getComponentType());
    }

    // Enum 확인
    if (clazz.isEnum()) {
      return true;
    }

    // Java 패키지 확인
    Package pkg = clazz.getPackage();
    if (pkg == null) {
      return true; // 패키지가 없는 경우 (예: 원시 타입)
    }

    String packageName = pkg.getName();
    return packageName.startsWith("java.") ||
      packageName.startsWith("javax.") ||
      packageName.startsWith("com.sun.") ||
      packageName.startsWith("sun.");
  }

  /**
   * 커스텀 객체 여부 확인 (암복호화 대상 가능성)
   */
  public static boolean isCustomObject(Object object) {
    if (object == null) {
      return false;
    }

    Class<?> clazz = object.getClass();

    // 캐시에서 먼저 확인
    return CUSTOM_OBJECT_CACHE.computeIfAbsent(clazz, c ->
      !isJavaBasicType(c) && !isCollectionType(c)
    );
  }

  /**
   * 특정 어노테이션을 가진 커스텀 객체인지 확인
   */
  public static boolean isCustomObjectWithAnnotation(Object object, Class<? extends Annotation> annotationClass) {
    if (!isCustomObject(object)) {
      return false;
    }

    return hasAnnotationInFields(object.getClass(), annotationClass);
  }

  /**
   * 컬렉션 타입 여부 확인
   */
  public static boolean isCollectionType(Class<?> clazz) {
    return Collection.class.isAssignableFrom(clazz) ||
      Map.class.isAssignableFrom(clazz) ||
      clazz.isArray();
  }

  /**
   * 특정 필드 찾기 (상속 관계 포함)
   */
  public static Optional<Field> findField(Class<?> clazz, String fieldName) {
    if (clazz == null || StringUtil.isEmpty(fieldName)) {
      return Optional.empty();
    }

    return getAllFields(clazz).stream()
      .filter(field -> fieldName.equals(field.getName()))
      .findFirst();
  }

  /**
   * 특정 타입의 필드들 찾기
   */
  public static List<Field> getFieldsByType(Class<?> clazz, Class<?> fieldType) {
    if (clazz == null || fieldType == null) {
      return Collections.emptyList();
    }

    return getAllFields(clazz).stream()
      .filter(field -> fieldType.isAssignableFrom(field.getType()))
      .collect(Collectors.toList());
  }

  /**
   * 필드값 안전하게 가져오기
   */
  public static Optional<Object> getFieldValue(Object object, String fieldName) {
    if (object == null || StringUtil.isEmpty(fieldName)) {
      return Optional.empty();
    }

    try {
      Optional<Field> fieldOpt = findField(object.getClass(), fieldName);
      if (fieldOpt.isPresent()) {
        Field field = fieldOpt.get();
        field.setAccessible(true);
        return Optional.ofNullable(field.get(object));
      }
    } catch (IllegalAccessException e) {
      log.warn("Failed to get field value: {} from {}", fieldName, object.getClass().getSimpleName());
    }

    return Optional.empty();
  }

  /**
   * 필드값 안전하게 설정하기
   */
  public static boolean setFieldValue(Object object, String fieldName, Object value) {
    if (object == null || StringUtil.isEmpty(fieldName)) {
      return false;
    }

    try {
      Optional<Field> fieldOpt = findField(object.getClass(), fieldName);
      if (fieldOpt.isPresent()) {
        Field field = fieldOpt.get();
        field.setAccessible(true);
        field.set(object, value);
        return true;
      }
    } catch (IllegalAccessException e) {
      log.warn("Failed to set field value: {} in {}", fieldName, object.getClass().getSimpleName());
    }

    return false;
  }

  /**
   * 어노테이션이 있는 필드의 값들 가져오기
   */
  public static <T extends Annotation> Map<Field, Object> getAnnotatedFieldValues(
    Object object,
    Class<T> annotationClass) {

    if (object == null || annotationClass == null) {
      return Collections.emptyMap();
    }

    Map<Field, Object> result = new HashMap<>();

    getFieldsWithAnnotation(object.getClass(), annotationClass)
      .forEach(field -> {
        try {
          field.setAccessible(true);
          Object value = field.get(object);
          result.put(field, value);
        } catch (IllegalAccessException e) {
          log.warn("Failed to get annotated field value: {} from {}",
            field.getName(), object.getClass().getSimpleName());
        }
      });

    return result;
  }

  /**
   * 두 객체의 필드 값 비교
   */
  public static boolean compareFieldValues(Object obj1, Object obj2, String fieldName) {
    if (obj1 == null || obj2 == null) {
      return obj1 == obj2;
    }

    Optional<Object> value1 = getFieldValue(obj1, fieldName);
    Optional<Object> value2 = getFieldValue(obj2, fieldName);

    return Objects.equals(value1.orElse(null), value2.orElse(null));
  }

  /**
   * 객체의 특정 어노테이션 필드들이 모두 비어있는지 확인
   */
  public static boolean areAnnotatedFieldsEmpty(Object object, Class<? extends Annotation> annotationClass) {
    if (object == null || annotationClass == null) {
      return true;
    }

    return getAnnotatedFieldValues(object, annotationClass).values().stream()
      .allMatch(value -> value == null ||
        (value instanceof String && StringUtil.isEmpty((String) value)) ||
        (value instanceof Collection && ((Collection<?>) value).isEmpty()) ||
        (value instanceof Map && ((Map<?, ?>) value).isEmpty()));
  }

  /**
   * 캐시 정리 (메모리 관리용)
   */
  public static void clearCaches() {
    ALL_FIELDS_CACHE.clear();
    ANNOTATION_FIELDS_CACHE.clear();
    CUSTOM_OBJECT_CACHE.clear();
    log.info("ObjectCommonUtil caches cleared");
  }

  /**
   * 캐시 통계 정보
   */
  public static Map<String, Integer> getCacheStats() {
    Map<String, Integer> stats = new HashMap<>();
    stats.put("allFieldsCache", ALL_FIELDS_CACHE.size());
    stats.put("annotationFieldsCache", ANNOTATION_FIELDS_CACHE.size());
    stats.put("customObjectCache", CUSTOM_OBJECT_CACHE.size());
    return stats;
  }

  /**
   * 여러 어노테이션 중 하나라도 있는 필드가 존재하는지 확인
   */
  @SafeVarargs
  public final boolean hasAnyAnnotationInFields(Class<?> clazz, Class<? extends Annotation>... annotationClasses) {
    if (clazz == null || annotationClasses == null || annotationClasses.length == 0) {
      return false;
    }

    return Arrays.stream(annotationClasses)
      .anyMatch(annotationClass -> hasAnnotationInFields(clazz, annotationClass));
  }
}
