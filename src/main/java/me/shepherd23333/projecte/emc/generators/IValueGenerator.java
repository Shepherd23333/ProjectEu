package me.shepherd23333.projecte.emc.generators;

import java.util.Map;

/**
 * Defines something that can simply yield a mapping of values.
 *
 * @param <T> The key type
 * @param <V> The value type
 */
public interface IValueGenerator<T, V extends Comparable<V>> {
    Map<T, V> generateValues();
}
