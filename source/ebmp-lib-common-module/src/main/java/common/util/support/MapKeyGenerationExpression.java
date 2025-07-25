package common.util.support;

public interface MapKeyGenerationExpression<K, V> {

  K generateKey(V valueObject);

}
