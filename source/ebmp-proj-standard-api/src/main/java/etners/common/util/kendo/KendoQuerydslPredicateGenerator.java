package etners.common.util.kendo;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import common.util.convert.ObjectUtil;
import common.util.string.StringUtil;
import etners.ebmp.lib.api.kendomodel.KendoFilter;
import etners.ebmp.lib.api.kendomodel.KendoFilterModule;
import etners.ebmp.lib.api.kendomodel.KendoPagingParamVO;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class KendoQuerydslPredicateGenerator {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(KendoQuerydslPredicateGenerator.class);

  /**
   * <pre>
   * 메소드명	: generateWherePredicate
   * 작성자	: oxide
   * 작성일	: 2019. 3. 5.
   * 설명	:
   * </pre>
   *
   * @param kendoPagingParam
   * @return
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Predicate generateWherePredicate(KendoPagingParamVO kendoPagingParam,
                                          EntityPathBase entityPathBase) {
    HashMap<String, Field> classFieldMap = ObjectUtil.getClassFieldMap(entityPathBase.getClass());

    BooleanBuilder builder = new BooleanBuilder();

    KendoFilterModule[] filterModule = kendoPagingParam.getFilter();

    //조건 필터링이 존재하는 경우에만 필터링 적용.
    if (filterModule != null) {
      //and 또는 or를 가져온다.
      for (KendoFilterModule module : filterModule) {
        String logic = module.getLogic(); //and / or / single / between

        KendoFilter[] kendoFilters = module.getFilters();

        if ("between".equals(logic) && kendoFilters.length == 2) {

          KendoFilter filter0 = kendoFilters[0];
          KendoFilter filter1 = kendoFilters[1];

          String filter0Keyword = filter0.getValue();
          String filter1Keyword = filter1.getValue();

          String kendoField = filter0.getField();

          if (classFieldMap.containsKey(kendoField)) {
            /**
             * EntityDomain 빌드 -> QueryDSL 도메인에 정의된 TypePath를 Object 타입으로 가져온다.
             *
             * 각 TypePath는 DB 컬럼의 데이터 타입과 연관이 있다.
             *
             * 예를 들어 문자열인 경우는 StringPath
             * 숫자 계열인 경우는 NumberPath
             * 날짜/시간 계열인 경우는 DateTimePath 클래스가 리턴된다.
             */
            Object value = getJpaColumnTypePath(entityPathBase, classFieldMap, kendoField);

            StringPath currentStringPath = null;
            NumberPath currentNumberPath = null;
            DateTimePath currentDateTimePath = null;

            BooleanExpression booleanExpression = null;

            if (value instanceof StringPath) {
              currentStringPath = (StringPath) value;

              booleanExpression = currentStringPath.between(filter0Keyword, filter1Keyword);
            } else if (value instanceof DateTimePath) {
              currentDateTimePath = (DateTimePath) value;

              LocalDateTime filter0Date = convertJavaDateYYYYMMDD(ConvertDateMode.FROM,
                  filter0Keyword);
              LocalDateTime filter1Date = convertJavaDateYYYYMMDD(ConvertDateMode.TO,
                  filter1Keyword);

              booleanExpression = currentDateTimePath.between(filter0Date, filter1Date);
            } else if (value instanceof NumberPath) {
              currentNumberPath = (NumberPath) value;
              long filter0Number = Long.parseLong(filter0Keyword);
              long filter1Number = Long.parseLong(filter1Keyword);

              booleanExpression = currentNumberPath.between(filter0Number, filter1Number);
            } else {
              throw new IllegalArgumentException("정의되지 않은 타입입니다. 이윤성D에게 확인해주세요.");
            }

            /**
             * BooleanExpression이 제대로 생성되었다면 조건문을 추가한다.
             */
            if (booleanExpression != null) {
              boolean orExpression = false;

              //첫번째 순서의 filter는 무조건 and 연산이므로 false가 되어야함. -> 바뀌어야 할수도..
              //두번째 순서의 filter는 첫번째와 엮이기 때문에
              //logic 파라미터로 들어온 or 또는 and 값을 받아 그에 맞게 연산을 처리해줘야함.
              if ("or".equals(logic)
                //&& index != 0
              ) {
                orExpression = true;
              }

              if (orExpression) {
                builder.or(booleanExpression);
              } else {
                builder.and(booleanExpression);
              }
            }
          }


        } else {

          //kendo filter 데이터가 존재하면 필터를 순회하면서 각 필터에 맞는 조건절을 생성하도록 처리한다.
          for (int index = 0; index < kendoFilters.length; index++) {
            KendoFilter filter = kendoFilters[index];

            String kendoField = filter.getField();

            if (classFieldMap.containsKey(kendoField)) {
              /**
               * EntityDomain 빌드 -> QueryDSL 도메인에 정의된 TypePath를 Object 타입으로 가져온다.
               *
               * 각 TypePath는 DB 컬럼의 데이터 타입과 연관이 있다.
               *
               * 예를 들어 문자열인 경우는 StringPath
               * 숫자 계열인 경우는 NumberPath
               * 날짜/시간 계열인 경우는 DateTimePath 클래스가 리턴된다.
               */
              Object value = getJpaColumnTypePath(entityPathBase, classFieldMap, kendoField);

              /**
               * 해당 컬럼타입에 맞는 조건절을 생성하여 BooleanExpression 형태로 리턴한다.
               */
              BooleanExpression booleanExpression = makeBooleanExpressionForWhere(filter, value);

              /**
               * BooleanExpression이 제대로 생성되었다면 조건문을 추가한다.
               */
              if (booleanExpression != null) {
                boolean orExpression = false;

                //첫번째 순서의 filter는 무조건 and 연산이므로 false가 되어야함. -> 바뀌어야 할수도..
                //두번째 순서의 filter는 첫번째와 엮이기 때문에
                //logic 파라미터로 들어온 or 또는 and 값을 받아 그에 맞게 연산을 처리해줘야함.
                if ("or".equals(logic)
                  //&& index != 0
                ) {
                  orExpression = true;
                }

                if (orExpression) {
                  builder.or(booleanExpression);
                } else {
                  builder.and(booleanExpression);
                }
              }
            }

          }
        }


      }


    }

    return builder;
  }

  public enum ConvertDateMode {
    FROM, TO, TARGET
  }

  //
//    private Date convertJavaDateYYYYMMDD(ConvertDateMode mode, String yyyymmdd) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmm");
//
//        Date targetDate = null;
//        try {
//            switch(mode) {
//                case FROM:
//                case TARGET:
//                    targetDate = dateFormat.parse(yyyymmdd + "0000");
//                    break;
//                case TO:
//                    targetDate = dateFormat.parse(yyyymmdd + "2359");
//                    break;
//                default:
//                    throw new Exception("잘못된 타입입니다.");
//            }
//            //Date targetDate = dateFormat.parse(yyyymmdd + "2359");
//        } catch (ParseException e) {
//            LOGGER.error(StringUtil.extractStackTrace(e));
//        } catch (Exception e) {
//            LOGGER.error(StringUtil.extractStackTrace(e));
//        }
//
//        return targetDate;
//    }
//
  private LocalDateTime convertJavaDateYYYYMMDD(ConvertDateMode mode, String yyyymmdd) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    LocalDateTime targetDate = null;
    try {
      switch (mode) {
        case FROM:
        case TARGET:
          targetDate = LocalDateTime.parse(yyyymmdd + "0000", formatter);
          break;
        case TO:
          targetDate = LocalDateTime.parse(yyyymmdd + "2359", formatter);
          break;
        default:
          throw new Exception("잘못된 타입입니다.");
      }

    } catch (ParseException e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    } catch (Exception e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    }

    return targetDate;
  }

  private LocalDate convertJavaSimpleDateYYYYMMDD(ConvertDateMode mode, String yyyymmdd) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    LocalDate targetDate = null;
    try {
      switch (mode) {
        case FROM:
        case TARGET:
          targetDate = LocalDate.parse(yyyymmdd, formatter);
          break;
        case TO:
          targetDate = LocalDate.parse(yyyymmdd, formatter);
          break;
        default:
          throw new Exception("잘못된 타입입니다.");
      }

    } catch (ParseException e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    } catch (Exception e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    }

    return targetDate;
  }

  /**
   * <pre>
   * 메소드명	: getJpaColumnTypePath
   * 작성자	: oxide
   * 작성일	: 2019. 3. 5.
   * 설명	:
   * </pre>
   *
   * @param entityPathBase
   * @param classFieldMap
   * @param kendoField
   * @return
   */
  @SuppressWarnings("rawtypes")
  private Object getJpaColumnTypePath(EntityPathBase entityPathBase,
      HashMap<String, Field> classFieldMap, String kendoField) {
    Object value = null;

    try {
      Field field = classFieldMap.get(kendoField);
      field.setAccessible(true);

      value = field.get(entityPathBase);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    return value;
  }

  /**
   * <pre>
   * 메소드명 : makeBooleanExpressionForWhere
   * 작성자  : oxide
   * 작성일  : 2019. 3. 5.
   * 설명   :
   * </pre>
   *
   * @param filter
   * @param value
   * @return
   */
  @SuppressWarnings("rawtypes")
  private BooleanExpression makeBooleanExpressionForWhere(KendoFilter filter, Object value) {
    StringPath currentStringPath = null;
    NumberPath currentNumberPath = null;
    DatePath currentDatePath = null;
    DateTimePath currentDateTimePath = null;

    BooleanExpression booleanExpression = null;

    if (value instanceof StringPath) {
      currentStringPath = (StringPath) value;

      booleanExpression = whereString(currentStringPath, filter);
    } else if (value instanceof DatePath) {
      currentDatePath = (DatePath) value;

      booleanExpression = whereDate(currentDatePath, filter);

    } else if (value instanceof DateTimePath) {
      currentDateTimePath = (DateTimePath) value;

      booleanExpression = whereDateTime(currentDateTimePath, filter);
    } else if (value instanceof NumberPath) {
      currentNumberPath = (NumberPath) value;

      booleanExpression = whereNumber(currentNumberPath, filter);
    } else {
      throw new IllegalArgumentException("정의되지 않은 타입입니다. 이윤성D에게 확인해주세요.");
    }

    return booleanExpression;
  }

  /**
   * <pre>
   * 메소드명	: whereString
   * 작성자	: oxide
   * 작성일	: 2019. 3. 5.
   * 설명	:
   * </pre>
   *
   * @param currentStringPath
   * @param filter
   * @return
   */
  private BooleanExpression whereString(StringPath currentStringPath, KendoFilter filter) {
    String kendoValue = filter.getValue();
    String kendoOperator = filter.getOperator();

    String searchValueToLowerCase = kendoValue.toLowerCase();

    BooleanExpression expression = null;

    switch (kendoOperator) {
      case "contains": //contains
        //원래 contains는 정확히 일치해야만 하지만
        //현재는 kendoOperator 값이 contains 라면 대소문자 구분만 안하고 문자가 정확히 일치하는 것을 검색
        expression = currentStringPath.lower().eq(searchValueToLowerCase);
        break;
      case "doesNotContain": //does Not Contain
        expression = currentStringPath.lower().eq(searchValueToLowerCase).not();
        break;
      case "eq":
      case "isequalsto": //eq(equal), is Equals To
        //정확히 일치하는 것만 검색
        expression = currentStringPath.eq(kendoValue);
        break;
      case "ne":
      case "isnotequalsto": //ne(not equal), is Not Equals To
        //정확히 일치하지 않는 것만 검색
        expression = currentStringPath.eq(kendoValue).not();
        break;
      case "isnull": //is Null
        expression = currentStringPath.isNull();
        break;
      case "isnotnull": //is Not Null
        expression = currentStringPath.isNotNull();
        break;
      case "isempty": //is Empty1
        expression = currentStringPath.isEmpty();
        break;
      case "isnotempty": //is Not Empty
        expression = currentStringPath.isNotEmpty();
        break;
      case "loe": //loe, little or equal, <=, 작거나 같음.
      case "littleorequal":
        expression = currentStringPath.loe(kendoValue);
        break;
      case "lt": //lt, little, <, 작음.
      case "little":
        expression = currentStringPath.loe(kendoValue);
        break;
      case "goe": //goe, greater or equal, >=, 크거나 같음.
      case "greaterorequal":
        expression = currentStringPath.goe(kendoValue);
        break;
      case "gt": //gt, greater, >, 큼.
      case "greater":
        expression = currentStringPath.gt(kendoValue);
        break;
      case "notlike":
        expression = currentStringPath.lower().notLike("%" + searchValueToLowerCase + "%");
        break;
      case "upperstartwith":
        expression = currentStringPath.upper().like(kendoValue + "%");
        break;
      case "tellike":
        searchValueToLowerCase = searchValueToLowerCase.replaceAll("-", "");
        expression = Expressions
            .booleanTemplate("REPLACE({0}, '-', '') LIKE '%" + searchValueToLowerCase + "%'",
                currentStringPath);
        break;
      case "like":
      default:
        //지원되지 않는 kendoOperator 값이 들어온 경우 무조건 like문으로 간주.
        expression = currentStringPath.lower().like("%" + searchValueToLowerCase + "%");
        break;
    }

    return expression;


  }

  /**
   * <pre>
   * 메소드명	: whereNumber
   * 작성자	: oxide
   * 작성일	: 2019. 3. 5.
   * 설명	:
   * </pre>
   *
   * @param currentNumberPath
   * @param filter
   * @return
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private BooleanExpression whereNumber(NumberPath currentNumberPath, KendoFilter filter) {
    String kendoValue = filter.getValue(); //숫자라면 lowerCase가 의미 없음.
    String kendoOperator = filter.getOperator();

    long searchNumberValue = Long.parseLong(kendoValue);

    BooleanExpression expression = null;

    switch (kendoOperator) {
      case "eq":
      case "isequalsto": //is Equals To
        //정확히 일치하는 것만 검색
        expression = currentNumberPath.eq(kendoValue);
        break;
      case "ne":
      case "isnotequalsto": //is Not Equals To
        //정확히 일치하지 않는 것만 검색
        expression = currentNumberPath.eq(kendoValue).not();
        break;
      case "isnull": //is Null
        currentNumberPath.isNull();
        break;
      case "isnotnull": //is Not Null
        currentNumberPath.isNotNull();
        break;

      case "loe": //loe, little or equal, <=, 작거나 같음.
      case "littleorequal":
        expression = currentNumberPath.loe(searchNumberValue);
        break;
      case "lt": //lt, little, <, 작음.
      case "little":
        expression = currentNumberPath.loe(searchNumberValue);
        break;
      case "goe": //goe, greater or equal, >=, 크거나 같음.
      case "greaterorequal":
        expression = currentNumberPath.goe(searchNumberValue);
        break;
      case "gt": //gt, greater, >, 큼.
      case "greater":
        expression = currentNumberPath.gt(searchNumberValue);

      case "notlike":
        expression = currentNumberPath.like("%" + kendoValue + "%").not();
        break;
      case "like":
      default:
        //지원되지 않는 kendoOperator 값이 들어온 경우 무조건 like문으로 간주.
        expression = currentNumberPath.like("%" + kendoValue + "%");
        break;
    }

    return expression;
  }

  /**
   * <pre>
   * 메소드명	: whereDateTime
   * 작성자	: oxide
   * 작성일	: 2019. 3. 5.
   * 설명	:
   * </pre>
   *
   * @param currentDateTimePath
   * @param filter
   * @return
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private BooleanExpression whereDateTime(DateTimePath currentDateTimePath, KendoFilter filter) {
    String kendoValue = filter.getValue(); //숫자라면 lowerCase가 의미 없음.
    String kendoOperator = filter.getOperator();

    LocalDateTime searchNumberValue = convertJavaDateYYYYMMDD(ConvertDateMode.TARGET, kendoValue);

    BooleanExpression expression = null;

    switch (kendoOperator) {
      case "eq":
      case "isequalsto": //is Equals To
        //정확히 일치하는 것만 검색
        expression = currentDateTimePath.eq(kendoValue);
        break;
      case "ne":
      case "isnotequalsto": //is Not Equals To
        //정확히 일치하지 않는 것만 검색
        expression = currentDateTimePath.eq(kendoValue).not();
        break;
      case "isnull": //is Null
        currentDateTimePath.isNull();
        break;
      case "isnotnull": //is Not Null
        currentDateTimePath.isNotNull();
        break;

      case "loe": //loe, little or equal, <=, 작거나 같음.
      case "littleorequal":
        expression = currentDateTimePath.loe(searchNumberValue);
        break;
      case "lt": //lt, little, <, 작음.
      case "little":
        expression = currentDateTimePath.loe(searchNumberValue);
        break;
      case "goe": //goe, greater or equal, >=, 크거나 같음.
      case "greaterorequal":
        expression = currentDateTimePath.goe(searchNumberValue);
        break;
      case "gt": //gt, greater, >, 큼.
      case "greater":
        expression = currentDateTimePath.gt(searchNumberValue);
    }

    return expression;
  }

  /**
   * <pre>
   * 메소드명	: whereDate
   * 작성자	: hrkwon
   * 작성일	: 2020.06.26.
   * 설명	:
   * </pre>
   *
   * @param currentDatePath
   * @param filter
   * @return
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private BooleanExpression whereDate(DatePath currentDatePath, KendoFilter filter) {
    String kendoValue = filter.getValue(); //숫자라면 lowerCase가 의미 없음.
    String kendoOperator = filter.getOperator();

    LocalDate searchDateValue = convertJavaSimpleDateYYYYMMDD(ConvertDateMode.TARGET, kendoValue);

    BooleanExpression expression = null;

    switch (kendoOperator) {
      case "eq":
      case "isequalsto": //is Equals To
        //정확히 일치하는 것만 검색
        expression = currentDatePath.eq(kendoValue);
        break;
      case "ne":
      case "isnotequalsto": //is Not Equals To
        //정확히 일치하지 않는 것만 검색
        expression = currentDatePath.eq(kendoValue).not();
        break;
      case "isnull": //is Null
        currentDatePath.isNull();
        break;
      case "isnotnull": //is Not Null
        currentDatePath.isNotNull();
        break;

      case "loe": //loe, little or equal, <=, 작거나 같음.
      case "littleorequal":
        expression = currentDatePath.loe(searchDateValue);
        break;
      case "lt": //lt, little, <, 작음.
      case "little":
        expression = currentDatePath.loe(searchDateValue);
        break;
      case "goe": //goe, greater or equal, >=, 크거나 같음.
      case "greaterorequal":
        expression = currentDatePath.goe(searchDateValue);
        break;
      case "gt": //gt, greater, >, 큼.
      case "greater":
        expression = currentDatePath.gt(searchDateValue);
    }

    return expression;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public Predicate generateJoinWherePredicate(KendoPagingParamVO kendoPagingParam,
      EntityPathBase entityJoinMasterPathBase, EntityPathBase entityJoinLeftPathBase) {
    HashMap<String, Field> classFieldJoinMasterMap = ObjectUtil
        .getClassFieldMap(entityJoinMasterPathBase.getClass());
    HashMap<String, Field> classFieldJoinLeftMap = ObjectUtil
        .getClassFieldMap(entityJoinLeftPathBase.getClass());

    String joinMaster = entityJoinMasterPathBase.getClass().getSimpleName();

    BooleanBuilder builder = new BooleanBuilder();

    KendoFilterModule[] filterModule = kendoPagingParam.getFilter();

    //조건 필터링이 존재하는 경우에만 필터링 적용.
    if (filterModule != null) {
      //and 또는 or를 가져온다.
      for (KendoFilterModule module : filterModule) {
        String logic = module.getLogic(); //and / or / single / between

        KendoFilter[] kendoFilters = module.getFilters();

        if ("between".equals(logic) && kendoFilters.length == 2) {

          KendoFilter filter0 = kendoFilters[0];
          KendoFilter filter1 = kendoFilters[1];

          String filter0Keyword = filter0.getValue();
          String filter1Keyword = filter1.getValue();

          String[] kendoJoinField = kendoFilters[0].getField().split("[.]");

          HashMap<String, Field> classMap;
          EntityPathBase entityPathBase;
          String entityPathBaseName = "Q".concat(kendoJoinField[0]);
          String kendoField = kendoJoinField[1];
          if (entityPathBaseName.equals(joinMaster)) {
            classMap = classFieldJoinMasterMap;
            entityPathBase = entityJoinMasterPathBase;
          } else {
            classMap = classFieldJoinLeftMap;
            entityPathBase = entityJoinLeftPathBase;
          }

          if (classMap.containsKey(kendoField)) {
            /**
             * EntityDomain 빌드 -> QueryDSL 도메인에 정의된 TypePath를 Object 타입으로 가져온다.
             *
             * 각 TypePath는 DB 컬럼의 데이터 타입과 연관이 있다.
             *
             * 예를 들어 문자열인 경우는 StringPath
             * 숫자 계열인 경우는 NumberPath
             * 날짜/시간 계열인 경우는 DateTimePath 클래스가 리턴된다.
             */
            Object value = getJpaColumnTypePath(entityPathBase, classMap, kendoField);

            StringPath currentStringPath = null;
            NumberPath currentNumberPath = null;
            DateTimePath currentDateTimePath = null;

            BooleanExpression booleanExpression = null;

            if (value instanceof StringPath) {
              currentStringPath = (StringPath) value;

              booleanExpression = currentStringPath.between(filter0Keyword, filter1Keyword);
            } else if (value instanceof DateTimePath) {
              currentDateTimePath = (DateTimePath) value;

              LocalDateTime filter0Date = convertJavaDateYYYYMMDD(ConvertDateMode.FROM,
                  filter0Keyword);
              LocalDateTime filter1Date = convertJavaDateYYYYMMDD(ConvertDateMode.TO,
                  filter1Keyword);

              booleanExpression = currentDateTimePath.between(filter0Date, filter1Date);
            } else if (value instanceof NumberPath) {
              currentNumberPath = (NumberPath) value;
              long filter0Number = Long.parseLong(filter0Keyword);
              long filter1Number = Long.parseLong(filter1Keyword);

              booleanExpression = currentNumberPath.between(filter0Number, filter1Number);
            } else {
              throw new IllegalArgumentException("정의되지 않은 타입입니다. 이윤성D에게 확인해주세요.");
            }

            /**
             * BooleanExpression이 제대로 생성되었다면 조건문을 추가한다.
             */
            if (booleanExpression != null) {
              boolean orExpression = false;

              //첫번째 순서의 filter는 무조건 and 연산이므로 false가 되어야함. -> 바뀌어야 할수도..
              //두번째 순서의 filter는 첫번째와 엮이기 때문에
              //logic 파라미터로 들어온 or 또는 and 값을 받아 그에 맞게 연산을 처리해줘야함.
              if ("or".equals(logic)
                //&& index != 0
              ) {
                orExpression = true;
              }

              if (orExpression) {
                builder.or(booleanExpression);
              } else {
                builder.and(booleanExpression);
              }
            }
          }


        } else {

          //kendo filter 데이터가 존재하면 필터를 순회하면서 각 필터에 맞는 조건절을 생성하도록 처리한다.
          for (int index = 0; index < kendoFilters.length; index++) {
            KendoFilter filter = kendoFilters[index];
            String[] kendoJoinField = filter.getField().split("[.]");

            HashMap<String, Field> classMap = new HashMap<>();
            EntityPathBase entityPathBase = null;
            String entityPathBaseName = "Q".concat(kendoJoinField[0]);
            String kendoField = kendoJoinField[1];
            if (entityPathBaseName.equals(joinMaster)) {
              classMap = classFieldJoinMasterMap;
              entityPathBase = entityJoinMasterPathBase;
            } else {
              classMap = classFieldJoinLeftMap;
              entityPathBase = entityJoinLeftPathBase;
            }

            if (classMap.containsKey(kendoField)) {
              /**
               * EntityDomain 빌드 -> QueryDSL 도메인에 정의된 TypePath를 Object 타입으로 가져온다.
               *
               * 각 TypePath는 DB 컬럼의 데이터 타입과 연관이 있다.
               *
               * 예를 들어 문자열인 경우는 StringPath
               * 숫자 계열인 경우는 NumberPath
               * 날짜/시간 계열인 경우는 DateTimePath 클래스가 리턴된다.
               */
              Object value = getJpaColumnTypePath(entityPathBase, classMap, kendoField);

              /**
               * 해당 컬럼타입에 맞는 조건절을 생성하여 BooleanExpression 형태로 리턴한다.
               */
              BooleanExpression booleanExpression = makeBooleanExpressionForWhere(filter, value);

              /**
               * BooleanExpression이 제대로 생성되었다면 조건문을 추가한다.
               */
              if (booleanExpression != null) {
                boolean orExpression = false;

                //첫번째 순서의 filter는 무조건 and 연산이므로 false가 되어야함. -> 바뀌어야 할수도..
                //두번째 순서의 filter는 첫번째와 엮이기 때문에
                //logic 파라미터로 들어온 or 또는 and 값을 받아 그에 맞게 연산을 처리해줘야함.
                if ("or".equals(logic)
                  //&& index != 0
                ) {
                  orExpression = true;
                }

                if (orExpression) {
                  builder.or(booleanExpression);
                } else {
                  builder.and(booleanExpression);
                }
              }
            }

          }
        }


      }


    }

    return builder;
  }

  public Predicate generateThreeJoinWherePredicate(KendoPagingParamVO kendoPagingParam,
      EntityPathBase entityJoinMasterPathBase, EntityPathBase entityJoinLeftPathBase,
      EntityPathBase entityJoinThirdPathBase) {
    HashMap<String, Field> classFieldJoinMasterMap = ObjectUtil
        .getClassFieldMap(entityJoinMasterPathBase.getClass());
    HashMap<String, Field> classFieldJoinLeftMap = ObjectUtil
        .getClassFieldMap(entityJoinLeftPathBase.getClass());
    HashMap<String, Field> classFieldJoinThirdMap = ObjectUtil
        .getClassFieldMap(entityJoinThirdPathBase.getClass());

    String joinMaster = entityJoinMasterPathBase.getClass().getSimpleName();
    String secondEntity = entityJoinLeftPathBase.getClass().getSimpleName();
    String thirdEntity = entityJoinThirdPathBase.getClass().getSimpleName();

    BooleanBuilder builder = new BooleanBuilder();

    KendoFilterModule[] filterModule = kendoPagingParam.getFilter();

    //조건 필터링이 존재하는 경우에만 필터링 적용.
    if (filterModule != null) {
      //and 또는 or를 가져온다.
      for (KendoFilterModule module : filterModule) {
        String logic = module.getLogic(); //and / or / single / between

        KendoFilter[] kendoFilters = module.getFilters();

        if ("between".equals(logic) && kendoFilters.length == 2) {

          KendoFilter filter0 = kendoFilters[0];
          KendoFilter filter1 = kendoFilters[1];

          String filter0Keyword = filter0.getValue();
          String filter1Keyword = filter1.getValue();

          String[] kendoJoinField = kendoFilters[0].getField().split("[.]");

          HashMap<String, Field> classMap;
          EntityPathBase entityPathBase;
          String entityPathBaseName = "Q".concat(kendoJoinField[0]);
          String kendoField = kendoJoinField[1];
          if (entityPathBaseName.equals(joinMaster)) {
            classMap = classFieldJoinMasterMap;
            entityPathBase = entityJoinMasterPathBase;
          } else {
            classMap = classFieldJoinLeftMap;
            entityPathBase = entityJoinLeftPathBase;
          }

          if (classMap.containsKey(kendoField)) {
            /**
             * EntityDomain 빌드 -> QueryDSL 도메인에 정의된 TypePath를 Object 타입으로 가져온다.
             *
             * 각 TypePath는 DB 컬럼의 데이터 타입과 연관이 있다.
             *
             * 예를 들어 문자열인 경우는 StringPath
             * 숫자 계열인 경우는 NumberPath
             * 날짜/시간 계열인 경우는 DateTimePath 클래스가 리턴된다.
             */
            Object value = getJpaColumnTypePath(entityPathBase, classMap, kendoField);

            StringPath currentStringPath = null;
            NumberPath currentNumberPath = null;
            DateTimePath currentDateTimePath = null;

            BooleanExpression booleanExpression = null;

            if (value instanceof StringPath) {
              currentStringPath = (StringPath) value;

              booleanExpression = currentStringPath.between(filter0Keyword, filter1Keyword);
            } else if (value instanceof DateTimePath) {
              currentDateTimePath = (DateTimePath) value;

              LocalDateTime filter0Date = convertJavaDateYYYYMMDD(ConvertDateMode.FROM,
                  filter0Keyword);
              LocalDateTime filter1Date = convertJavaDateYYYYMMDD(ConvertDateMode.TO,
                  filter1Keyword);

              booleanExpression = currentDateTimePath.between(filter0Date, filter1Date);
            } else if (value instanceof NumberPath) {
              currentNumberPath = (NumberPath) value;
              long filter0Number = Long.parseLong(filter0Keyword);
              long filter1Number = Long.parseLong(filter1Keyword);

              booleanExpression = currentNumberPath.between(filter0Number, filter1Number);
            } else {
              throw new IllegalArgumentException("정의되지 않은 타입입니다. 이윤성D에게 확인해주세요.");
            }

            /**
             * BooleanExpression이 제대로 생성되었다면 조건문을 추가한다.
             */
            if (booleanExpression != null) {
              boolean orExpression = false;

              //첫번째 순서의 filter는 무조건 and 연산이므로 false가 되어야함. -> 바뀌어야 할수도..
              //두번째 순서의 filter는 첫번째와 엮이기 때문에
              //logic 파라미터로 들어온 or 또는 and 값을 받아 그에 맞게 연산을 처리해줘야함.
              if ("or".equals(logic)
                //&& index != 0
              ) {
                orExpression = true;
              }

              if (orExpression) {
                builder.or(booleanExpression);
              } else {
                builder.and(booleanExpression);
              }
            }
          }


        } else {

          //kendo filter 데이터가 존재하면 필터를 순회하면서 각 필터에 맞는 조건절을 생성하도록 처리한다.
          for (int index = 0; index < kendoFilters.length; index++) {
            KendoFilter filter = kendoFilters[index];
            String[] kendoJoinField = filter.getField().split("[.]");

            HashMap<String, Field> classMap = new HashMap<>();
            EntityPathBase entityPathBase = null;
            String entityPathBaseName = "Q".concat(kendoJoinField[0]);
            String kendoField = kendoJoinField[1];
            if (entityPathBaseName.equals(joinMaster)) {
              classMap = classFieldJoinMasterMap;
              entityPathBase = entityJoinMasterPathBase;
            } else if (entityPathBaseName.equals(secondEntity)) {
              classMap = classFieldJoinLeftMap;
              entityPathBase = entityJoinLeftPathBase;
            } else {
              classMap = classFieldJoinThirdMap;
              entityPathBase = entityJoinThirdPathBase;
            }

            if (classMap.containsKey(kendoField)) {
              /**
               * EntityDomain 빌드 -> QueryDSL 도메인에 정의된 TypePath를 Object 타입으로 가져온다.
               *
               * 각 TypePath는 DB 컬럼의 데이터 타입과 연관이 있다.
               *
               * 예를 들어 문자열인 경우는 StringPath
               * 숫자 계열인 경우는 NumberPath
               * 날짜/시간 계열인 경우는 DateTimePath 클래스가 리턴된다.
               */
              Object value = getJpaColumnTypePath(entityPathBase, classMap, kendoField);

              /**
               * 해당 컬럼타입에 맞는 조건절을 생성하여 BooleanExpression 형태로 리턴한다.
               */
              BooleanExpression booleanExpression = makeBooleanExpressionForWhere(filter, value);

              /**
               * BooleanExpression이 제대로 생성되었다면 조건문을 추가한다.
               */
              if (booleanExpression != null) {
                boolean orExpression = false;

                //첫번째 순서의 filter는 무조건 and 연산이므로 false가 되어야함. -> 바뀌어야 할수도..
                //두번째 순서의 filter는 첫번째와 엮이기 때문에
                //logic 파라미터로 들어온 or 또는 and 값을 받아 그에 맞게 연산을 처리해줘야함.
                if ("or".equals(logic)
                  //&& index != 0
                ) {
                  orExpression = true;
                }

                if (orExpression) {
                  builder.or(booleanExpression);
                } else {
                  builder.and(booleanExpression);
                }
              }
            }

          }
        }


      }


    }

    return builder;
  }

  public Predicate generateFourJoinWherePredicate(KendoPagingParamVO kendoPagingParam,
      EntityPathBase entityJoinMasterPathBase, EntityPathBase entityJoinLeftPathBase,
      EntityPathBase entityJoinThirdPathBase, EntityPathBase entityJoinFourPathBase) {
    HashMap<String, Field> classFieldJoinMasterMap = ObjectUtil
        .getClassFieldMap(entityJoinMasterPathBase.getClass());
    HashMap<String, Field> classFieldJoinLeftMap = ObjectUtil
        .getClassFieldMap(entityJoinLeftPathBase.getClass());
    HashMap<String, Field> classFieldJoinThirdMap = ObjectUtil
        .getClassFieldMap(entityJoinThirdPathBase.getClass());
    HashMap<String, Field> classFieldJoinFourMap = ObjectUtil
        .getClassFieldMap(entityJoinFourPathBase.getClass());

    String joinMaster = entityJoinMasterPathBase.getClass().getSimpleName();
    String secondEntity = entityJoinLeftPathBase.getClass().getSimpleName();
    String thirdEntity = entityJoinThirdPathBase.getClass().getSimpleName();
    String fourEntity = entityJoinFourPathBase.getClass().getSimpleName();

    BooleanBuilder builder = new BooleanBuilder();

    KendoFilterModule[] filterModule = kendoPagingParam.getFilter();

    //조건 필터링이 존재하는 경우에만 필터링 적용.
    if (filterModule != null) {
      //and 또는 or를 가져온다.
      for (KendoFilterModule module : filterModule) {
        String logic = module.getLogic(); //and / or / single / between

        KendoFilter[] kendoFilters = module.getFilters();

        if ("between".equals(logic) && kendoFilters.length == 2) {

          KendoFilter filter0 = kendoFilters[0];
          KendoFilter filter1 = kendoFilters[1];

          String filter0Keyword = filter0.getValue();
          String filter1Keyword = filter1.getValue();

          String[] kendoJoinField = kendoFilters[0].getField().split("[.]");

          HashMap<String, Field> classMap;
          EntityPathBase entityPathBase;
          String entityPathBaseName = "Q".concat(kendoJoinField[0]);
          String kendoField = kendoJoinField[1];
          if (entityPathBaseName.equals(joinMaster)) {
            classMap = classFieldJoinMasterMap;
            entityPathBase = entityJoinMasterPathBase;
          } else {
            classMap = classFieldJoinLeftMap;
            entityPathBase = entityJoinLeftPathBase;
          }

          if (classMap.containsKey(kendoField)) {
            /**
             * EntityDomain 빌드 -> QueryDSL 도메인에 정의된 TypePath를 Object 타입으로 가져온다.
             *
             * 각 TypePath는 DB 컬럼의 데이터 타입과 연관이 있다.
             *
             * 예를 들어 문자열인 경우는 StringPath
             * 숫자 계열인 경우는 NumberPath
             * 날짜/시간 계열인 경우는 DateTimePath 클래스가 리턴된다.
             */
            Object value = getJpaColumnTypePath(entityPathBase, classMap, kendoField);

            StringPath currentStringPath = null;
            NumberPath currentNumberPath = null;
            DateTimePath currentDateTimePath = null;

            BooleanExpression booleanExpression = null;

            if (value instanceof StringPath) {
              currentStringPath = (StringPath) value;

              booleanExpression = currentStringPath.between(filter0Keyword, filter1Keyword);
            } else if (value instanceof DateTimePath) {
              currentDateTimePath = (DateTimePath) value;

              LocalDateTime filter0Date = convertJavaDateYYYYMMDD(ConvertDateMode.FROM,
                  filter0Keyword);
              LocalDateTime filter1Date = convertJavaDateYYYYMMDD(ConvertDateMode.TO,
                  filter1Keyword);

              booleanExpression = currentDateTimePath.between(filter0Date, filter1Date);
            } else if (value instanceof NumberPath) {
              currentNumberPath = (NumberPath) value;
              long filter0Number = Long.parseLong(filter0Keyword);
              long filter1Number = Long.parseLong(filter1Keyword);

              booleanExpression = currentNumberPath.between(filter0Number, filter1Number);
            } else {
              throw new IllegalArgumentException("정의되지 않은 타입입니다. 이윤성D에게 확인해주세요.");
            }

            /**
             * BooleanExpression이 제대로 생성되었다면 조건문을 추가한다.
             */
            if (booleanExpression != null) {
              boolean orExpression = false;

              //첫번째 순서의 filter는 무조건 and 연산이므로 false가 되어야함. -> 바뀌어야 할수도..
              //두번째 순서의 filter는 첫번째와 엮이기 때문에
              //logic 파라미터로 들어온 or 또는 and 값을 받아 그에 맞게 연산을 처리해줘야함.
              if ("or".equals(logic)
                //&& index != 0
              ) {
                orExpression = true;
              }

              if (orExpression) {
                builder.or(booleanExpression);
              } else {
                builder.and(booleanExpression);
              }
            }
          }


        } else {

          //kendo filter 데이터가 존재하면 필터를 순회하면서 각 필터에 맞는 조건절을 생성하도록 처리한다.
          for (int index = 0; index < kendoFilters.length; index++) {
            KendoFilter filter = kendoFilters[index];
            String[] kendoJoinField = filter.getField().split("[.]");

            HashMap<String, Field> classMap = new HashMap<>();
            EntityPathBase entityPathBase = null;
            String entityPathBaseName = "Q".concat(kendoJoinField[0]);
            String kendoField = kendoJoinField[1];
            if (entityPathBaseName.equals(joinMaster)) {
              classMap = classFieldJoinMasterMap;
              entityPathBase = entityJoinMasterPathBase;
            } else if (entityPathBaseName.equals(secondEntity)) {
              classMap = classFieldJoinLeftMap;
              entityPathBase = entityJoinLeftPathBase;
            } else if (entityPathBaseName.equals(thirdEntity)) {
              classMap = classFieldJoinThirdMap;
              entityPathBase = entityJoinThirdPathBase;
            } else {
              classMap = classFieldJoinFourMap;
              entityPathBase = entityJoinFourPathBase;
            }

            if (classMap.containsKey(kendoField)) {
              /**
               * EntityDomain 빌드 -> QueryDSL 도메인에 정의된 TypePath를 Object 타입으로 가져온다.
               *
               * 각 TypePath는 DB 컬럼의 데이터 타입과 연관이 있다.
               *
               * 예를 들어 문자열인 경우는 StringPath
               * 숫자 계열인 경우는 NumberPath
               * 날짜/시간 계열인 경우는 DateTimePath 클래스가 리턴된다.
               */
              Object value = getJpaColumnTypePath(entityPathBase, classMap, kendoField);

              /**
               * 해당 컬럼타입에 맞는 조건절을 생성하여 BooleanExpression 형태로 리턴한다.
               */
              BooleanExpression booleanExpression = makeBooleanExpressionForWhere(filter, value);

              /**
               * BooleanExpression이 제대로 생성되었다면 조건문을 추가한다.
               */
              if (booleanExpression != null) {
                boolean orExpression = false;

                //첫번째 순서의 filter는 무조건 and 연산이므로 false가 되어야함. -> 바뀌어야 할수도..
                //두번째 순서의 filter는 첫번째와 엮이기 때문에
                //logic 파라미터로 들어온 or 또는 and 값을 받아 그에 맞게 연산을 처리해줘야함.
                if ("or".equals(logic)
                  //&& index != 0
                ) {
                  orExpression = true;
                }

                if (orExpression) {
                  builder.or(booleanExpression);
                } else {
                  builder.and(booleanExpression);
                }
              }
            }

          }
        }


      }


    }

    return builder;
  }

  /**
   * created 2019.11.19 hrkwon 조인은 하지 않지만 명확하게 엔티티를 지정해서 검색해야 할 경우 사용
   */
  public Predicate generateDefiniteWherePredicate(KendoPagingParamVO kendoPagingParam,
      EntityPathBase entityJoinMasterPathBase) {
    HashMap<String, Field> classFieldDefiniteMap = ObjectUtil
        .getClassFieldMap(entityJoinMasterPathBase.getClass());

    String definiteClassName = entityJoinMasterPathBase.getClass().getSimpleName();

    BooleanBuilder builder = new BooleanBuilder();

    KendoFilterModule[] filterModule = kendoPagingParam.getFilter();

    //조건 필터링이 존재하는 경우에만 필터링 적용.
    if (filterModule != null) {
      //and 또는 or를 가져온다.
      for (KendoFilterModule module : filterModule) {
        String logic = module.getLogic(); //and / or / single / between

        KendoFilter[] kendoFilters = module.getFilters();

        if ("between".equals(logic) && kendoFilters.length == 2) {

          KendoFilter filter0 = kendoFilters[0];
          KendoFilter filter1 = kendoFilters[1];

          String filter0Keyword = filter0.getValue();
          String filter1Keyword = filter1.getValue();

          String[] kendoJoinField = kendoFilters[0].getField().split("[.]");

          HashMap<String, Field> classMap = new HashMap<>();
          EntityPathBase entityPathBase = null;
          String entityPathBaseName = "Q".concat(kendoJoinField[0]);
          String kendoField = kendoJoinField[1];
          if (entityPathBaseName.equals(definiteClassName)) {
            classMap = classFieldDefiniteMap;
            entityPathBase = entityJoinMasterPathBase;
          }

          if (classMap.containsKey(kendoField)) {
            /**
             * EntityDomain 빌드 -> QueryDSL 도메인에 정의된 TypePath를 Object 타입으로 가져온다.
             *
             * 각 TypePath는 DB 컬럼의 데이터 타입과 연관이 있다.
             *
             * 예를 들어 문자열인 경우는 StringPath
             * 숫자 계열인 경우는 NumberPath
             * 날짜/시간 계열인 경우는 DateTimePath 클래스가 리턴된다.
             */
            Object value = getJpaColumnTypePath(entityPathBase, classMap, kendoField);

            StringPath currentStringPath = null;
            NumberPath currentNumberPath = null;
            DateTimePath currentDateTimePath = null;

            BooleanExpression booleanExpression = null;

            if (value instanceof StringPath) {
              currentStringPath = (StringPath) value;

              booleanExpression = currentStringPath.between(filter0Keyword, filter1Keyword);
            } else if (value instanceof DateTimePath) {
              currentDateTimePath = (DateTimePath) value;

              LocalDateTime filter0Date = convertJavaDateYYYYMMDD(ConvertDateMode.FROM,
                  filter0Keyword);
              LocalDateTime filter1Date = convertJavaDateYYYYMMDD(ConvertDateMode.TO,
                  filter1Keyword);

              booleanExpression = currentDateTimePath.between(filter0Date, filter1Date);
            } else if (value instanceof NumberPath) {
              currentNumberPath = (NumberPath) value;
              long filter0Number = Long.parseLong(filter0Keyword);
              long filter1Number = Long.parseLong(filter1Keyword);

              booleanExpression = currentNumberPath.between(filter0Number, filter1Number);
            } else {
              throw new IllegalArgumentException("정의되지 않은 타입입니다.");
            }

            /**
             * BooleanExpression이 제대로 생성되었다면 조건문을 추가한다.
             */
            if (booleanExpression != null) {
              boolean orExpression = false;

              //첫번째 순서의 filter는 무조건 and 연산이므로 false가 되어야함. -> 바뀌어야 할수도..
              //두번째 순서의 filter는 첫번째와 엮이기 때문에
              //logic 파라미터로 들어온 or 또는 and 값을 받아 그에 맞게 연산을 처리해줘야함.
              if ("or".equals(logic)
                //&& index != 0
              ) {
                orExpression = true;
              }

              if (orExpression) {
                builder.or(booleanExpression);
              } else {
                builder.and(booleanExpression);
              }
            }
          }


        } else {

          //kendo filter 데이터가 존재하면 필터를 순회하면서 각 필터에 맞는 조건절을 생성하도록 처리한다.
          for (int index = 0; index < kendoFilters.length; index++) {
            KendoFilter filter = kendoFilters[index];
            String[] kendoJoinField = filter.getField().split("[.]");

            HashMap<String, Field> classMap = new HashMap<>();
            EntityPathBase entityPathBase = null;
            String entityPathBaseName = "Q".concat(kendoJoinField[0]);
            String kendoField = kendoJoinField[1];

            if (entityPathBaseName.equals(definiteClassName)) {
              classMap = classFieldDefiniteMap;
              entityPathBase = entityJoinMasterPathBase;
            }

            if (classMap.containsKey(kendoField)) {
              /**
               * EntityDomain 빌드 -> QueryDSL 도메인에 정의된 TypePath를 Object 타입으로 가져온다.
               *
               * 각 TypePath는 DB 컬럼의 데이터 타입과 연관이 있다.
               *
               * 예를 들어 문자열인 경우는 StringPath
               * 숫자 계열인 경우는 NumberPath
               * 날짜/시간 계열인 경우는 DateTimePath 클래스가 리턴된다.
               */
              Object value = getJpaColumnTypePath(entityPathBase, classMap, kendoField);

              /**
               * 해당 컬럼타입에 맞는 조건절을 생성하여 BooleanExpression 형태로 리턴한다.
               */
              BooleanExpression booleanExpression = makeBooleanExpressionForWhere(filter, value);

              /**
               * BooleanExpression이 제대로 생성되었다면 조건문을 추가한다.
               */
              if (booleanExpression != null) {
                boolean orExpression = false;

                //첫번째 순서의 filter는 무조건 and 연산이므로 false가 되어야함. -> 바뀌어야 할수도..
                //두번째 순서의 filter는 첫번째와 엮이기 때문에
                //logic 파라미터로 들어온 or 또는 and 값을 받아 그에 맞게 연산을 처리해줘야함.
                if ("or".equals(logic)
                  //&& index != 0
                ) {
                  orExpression = true;
                }

                if (orExpression) {
                  builder.or(booleanExpression);
                } else {
                  builder.and(booleanExpression);
                }
              }
            }

          }
        }


      }


    }

    return builder;
  }


  /**
   * created 2020.08.18 hrkwon 여러개 조인된 데이터를 검색하기 위한 동적메서드
   */
  public <Q extends EntityPathBase> Predicate dynamicGenerateWherePredicate(
      KendoPagingParamVO kendoPagingParam, Q... qClassArray) {
    BooleanBuilder finalBoolean = new BooleanBuilder();

    for (Q qClass : qClassArray) {
      HashMap<String, Field> classFieldDefiniteMap = ObjectUtil.getClassFieldMap(qClass.getClass());
      EntityPathBase entityJoinMasterPathBase = qClass;
      String definiteClassName = entityJoinMasterPathBase.getClass().getSimpleName();

      BooleanBuilder builder = new BooleanBuilder();

      KendoFilterModule[] filterModule = kendoPagingParam.getFilter();

      //조건 필터링이 존재하는 경우에만 필터링 적용.
      if (filterModule != null) {
        //and 또는 or를 가져온다.
        for (KendoFilterModule module : filterModule) {
          String logic = module.getLogic(); //and / or / single / between

          KendoFilter[] kendoFilters = module.getFilters();

          if ("between".equals(logic) && kendoFilters.length == 2) {

            KendoFilter filter0 = kendoFilters[0];
            KendoFilter filter1 = kendoFilters[1];

            String filter0Keyword = filter0.getValue();
            String filter1Keyword = filter1.getValue();

            String[] kendoJoinField = kendoFilters[0].getField().split("[.]");

            HashMap<String, Field> classMap = new HashMap<>();
            EntityPathBase entityPathBase = null;
            String entityPathBaseName = "Q".concat(kendoJoinField[0]);
            String kendoField = kendoJoinField[1];
            if (entityPathBaseName.equals(definiteClassName)) {
              classMap = classFieldDefiniteMap;
              entityPathBase = entityJoinMasterPathBase;
            }

            if (classMap.containsKey(kendoField)) {
              /**
               * EntityDomain 빌드 -> QueryDSL 도메인에 정의된 TypePath를 Object 타입으로 가져온다.
               *
               * 각 TypePath는 DB 컬럼의 데이터 타입과 연관이 있다.
               *
               * 예를 들어 문자열인 경우는 StringPath
               * 숫자 계열인 경우는 NumberPath
               * 날짜/시간 계열인 경우는 DateTimePath 클래스가 리턴된다.
               */
              Object value = getJpaColumnTypePath(entityPathBase, classMap, kendoField);

              StringPath currentStringPath = null;
              NumberPath currentNumberPath = null;
              DateTimePath currentDateTimePath = null;

              BooleanExpression booleanExpression = null;

              if (value instanceof StringPath) {
                currentStringPath = (StringPath) value;

                booleanExpression = currentStringPath.between(filter0Keyword, filter1Keyword);
              } else if (value instanceof DateTimePath) {
                currentDateTimePath = (DateTimePath) value;

                LocalDateTime filter0Date = convertJavaDateYYYYMMDD(ConvertDateMode.FROM,
                    filter0Keyword);
                LocalDateTime filter1Date = convertJavaDateYYYYMMDD(ConvertDateMode.TO,
                    filter1Keyword);

                booleanExpression = currentDateTimePath.between(filter0Date, filter1Date);
              } else if (value instanceof NumberPath) {
                currentNumberPath = (NumberPath) value;
                long filter0Number = Long.parseLong(filter0Keyword);
                long filter1Number = Long.parseLong(filter1Keyword);

                booleanExpression = currentNumberPath.between(filter0Number, filter1Number);
              } else {
                throw new IllegalArgumentException("정의되지 않은 타입입니다.");
              }

              /**
               * BooleanExpression이 제대로 생성되었다면 조건문을 추가한다.
               */
              if (booleanExpression != null) {
                boolean orExpression = false;

                //첫번째 순서의 filter는 무조건 and 연산이므로 false가 되어야함. -> 바뀌어야 할수도..
                //두번째 순서의 filter는 첫번째와 엮이기 때문에
                //logic 파라미터로 들어온 or 또는 and 값을 받아 그에 맞게 연산을 처리해줘야함.
                if ("or".equals(logic)
                  //&& index != 0
                ) {
                  orExpression = true;
                }

                if (orExpression) {
                  builder.or(booleanExpression);
                } else {
                  builder.and(booleanExpression);
                }
              }
            }


          } else {

            //kendo filter 데이터가 존재하면 필터를 순회하면서 각 필터에 맞는 조건절을 생성하도록 처리한다.
            for (int index = 0; index < kendoFilters.length; index++) {
              KendoFilter filter = kendoFilters[index];
              String[] kendoJoinField = filter.getField().split("[.]");

              HashMap<String, Field> classMap = new HashMap<>();
              EntityPathBase entityPathBase = null;
              String entityPathBaseName = "Q".concat(kendoJoinField[0]);
              String kendoField = kendoJoinField[1];

              if (entityPathBaseName.equals(definiteClassName)) {
                classMap = classFieldDefiniteMap;
                entityPathBase = entityJoinMasterPathBase;
              }

              if (classMap.containsKey(kendoField)) {
                /**
                 * EntityDomain 빌드 -> QueryDSL 도메인에 정의된 TypePath를 Object 타입으로 가져온다.
                 *
                 * 각 TypePath는 DB 컬럼의 데이터 타입과 연관이 있다.
                 *
                 * 예를 들어 문자열인 경우는 StringPath
                 * 숫자 계열인 경우는 NumberPath
                 * 날짜/시간 계열인 경우는 DateTimePath 클래스가 리턴된다.
                 */
                Object value = getJpaColumnTypePath(entityPathBase, classMap, kendoField);

                /**
                 * 해당 컬럼타입에 맞는 조건절을 생성하여 BooleanExpression 형태로 리턴한다.
                 */
                BooleanExpression booleanExpression = makeBooleanExpressionForWhere(filter, value);

                /**
                 * BooleanExpression이 제대로 생성되었다면 조건문을 추가한다.
                 */
                if (booleanExpression != null) {
                  boolean orExpression = false;

                  //첫번째 순서의 filter는 무조건 and 연산이므로 false가 되어야함. -> 바뀌어야 할수도..
                  //두번째 순서의 filter는 첫번째와 엮이기 때문에
                  //logic 파라미터로 들어온 or 또는 and 값을 받아 그에 맞게 연산을 처리해줘야함.
                  if ("or".equals(logic)
                    //&& index != 0
                  ) {
                    orExpression = true;
                  }

                  if (orExpression) {
                    builder.or(booleanExpression);
                  } else {
                    builder.and(booleanExpression);
                  }
                }
              }

            }
          }


        }


      }
      finalBoolean.and(builder);
    }
    return finalBoolean;
  }


  public <Q extends EntityPathBase> Predicate generateMultipleJoinWherePredicate(
      KendoPagingParamVO kendoPagingParam, Q... qClassArray) {
    BooleanBuilder builder = new BooleanBuilder();
    EntityPathBase[] var3 = qClassArray;

    int var4 = qClassArray.length;

    for (int var5 = 0; var5 < var4; ++var5) {
      Q qClass = (Q) var3[var5];
      //   String joinMaster = qClass.getClass().getSimpleName();
      builder.and(generateDefiniteWherePredicate(kendoPagingParam, qClass));

    }

    return builder;
  }


}
