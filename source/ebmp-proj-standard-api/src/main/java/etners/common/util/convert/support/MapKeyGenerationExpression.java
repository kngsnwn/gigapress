package etners.common.util.convert.support;

public interface MapKeyGenerationExpression<K, V> {

  K generateKey(V valueObject);

}
