package bot.utils;

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
