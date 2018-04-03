package bot.utils;

import bot.tasks.RandomVkItem;

/**
 * Util class for returning two values from method.
 * @param <K> key type
 * @param <V> value type
 * @see RandomVkItem#randomMeme()
 */
public class Pair<K,V> {
    private K key;
    private V value;

    public Pair(K key,V value){
        this.key=key;
        this.value=value;
    }

    public V getValue() {
        return value;
    }

    public K getKey() {
        return key;
    }
}
