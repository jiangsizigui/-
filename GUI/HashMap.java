public class HashMap<K, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private Entry<K, V>[] table;
    private int size;

    @SuppressWarnings("unchecked")
    public HashMap() {
        table = new Entry[DEFAULT_CAPACITY];
        size = 0;
    }

    private static class Entry<K, V> {
        K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private int hash(K key) {
        return Math.abs(key.hashCode() % table.length);
    }

    public void put(K key, V value) {
        int index = hash(key);
        Entry<K, V> entry = table[index];

        while (entry != null) {
            if (entry.key.equals(key)) {
                entry.value = value;
                return;
            }
            entry = entry.next;
        }

        Entry<K, V> newEntry = new Entry<>(key, value);
        newEntry.next = table[index];
        table[index] = newEntry;
        size++;
    }

    public V get(K key) {
        int index = hash(key);
        Entry<K, V> entry = table[index];

        while (entry != null) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
            entry = entry.next;
        }
        return null;
    }

    public boolean remove(K key) {
        int index = hash(key);
        Entry<K, V> entry = table[index];
        Entry<K, V> prev = null;

        while (entry != null) {
            if (entry.key.equals(key)) {
                if (prev == null) {
                    table[index] = entry.next;
                } else {
                    prev.next = entry.next;
                }
                size--;
                return true;
            }
            prev = entry;
            entry = entry.next;
        }
        return false;
    }

    public int size() {
        return size;
    }
}