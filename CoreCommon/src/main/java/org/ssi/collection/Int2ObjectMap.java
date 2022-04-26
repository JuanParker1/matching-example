package org.ssi.collection;

import static org.ssi.util.BitUtil.findNextPositivePowerOfTwo;
import static org.ssi.util.CollectionUtil.validateLoadFactor;

import java.util.Arrays;

import org.ssi.util.Hashing;

public class Int2ObjectMap<V> {
	
	private static final int MIN_CAPACITY = 32;
	private final float loadFactor;
	private int resizeThreshold;
	private int size;
	
	
	private int[] keys;
	private Object[] values;
	
	public Int2ObjectMap()
    {
        this(MIN_CAPACITY, Hashing.DEFAULT_LOAD_FACTOR);
    }
	
	public Int2ObjectMap(final int initialCapacit) {
		this(initialCapacit, Hashing.DEFAULT_LOAD_FACTOR);
	}
	
	public Int2ObjectMap(
	        final int initialCapacity,
	        final float loadFactor) {
        validateLoadFactor(loadFactor);

        this.loadFactor = loadFactor;

        final int capacity = findNextPositivePowerOfTwo(Math.max(MIN_CAPACITY, initialCapacity));
        resizeThreshold = (int)(capacity * loadFactor);

        keys = new int[capacity];
        values = new Object[capacity];
    }
	
	public float loadFactor() {
        return loadFactor;
    }
	
	public int capacity() {
        return values.length;
    }
	
	public int size() {
		return size;
	}
	
	public int resizeThreshold() {
        return resizeThreshold;
    }
	
	public boolean isEmpty() {
        return 0 == size;
    }
	
	 public boolean containsKey(final int key) {
        final int mask = values.length - 1;
        int index = Hashing.hash(key, mask);

        boolean found = false;
        while (null != values[index]) {
            if (key == keys[index]) {
                found = true;
                break;
            }

            index = ++index & mask;
        }

        return found;
	}
	
	@SuppressWarnings("unchecked")
	public V get(final int key) {
		final int mask = values.length - 1;
        int index = Hashing.hash(key, mask);

        Object value;
        while (null != (value = values[index])) {
            if (key == keys[index]) {
                break;
            }

            index = ++index & mask;
        }
        
        return (V)value;
    }
	
	@SuppressWarnings("unchecked")
	public V put(final int key, final V value) {
		V oldValue = null;
        final int mask = values.length - 1;
        int index = Hashing.hash(key, mask);

        while (null != values[index]) {
            if (key == keys[index]) {
                oldValue = (V)values[index];
                break;
            }

            index = ++index & mask;
        }

        if (null == oldValue)
        {
            ++size;
            keys[index] = key;
        }

        values[index] = value;

        if (size > resizeThreshold)
        {
            increaseCapacity();
        }
        
        return oldValue;
	}
	
	private void increaseCapacity()
    {
        final int newCapacity = values.length << 1;
        if (newCapacity < 0) {
            throw new IllegalStateException("Max capacity reached at size = " + size);
        }

        rehash(newCapacity);
    }
	
	private void rehash(final int newCapacity) {
        final int mask = newCapacity - 1;
        resizeThreshold = (int)(newCapacity * loadFactor);

        final int[] tempKeys = new int[newCapacity];
        final Object[] tempValues = new Object[newCapacity];

        for (int i = 0, size = values.length; i < size; i++) {
            final Object value = values[i];
            if (null != value) {
                final int key = keys[i];
                int index = Hashing.hash(key, mask);
                
                while (null != tempValues[index]) {
                    index = ++index & mask;
                }

                tempKeys[index] = key;
                tempValues[index] = value;
            }
        }

        keys = tempKeys;
        values = tempValues;
    }
	
	@SuppressWarnings("unchecked")
	public V remove(final int key) {
        final int mask = values.length - 1;
        int index = Hashing.hash(key, mask);

        Object value;
        while (null != (value = values[index])) {
        	
            if (key == keys[index]) {
                values[index] = null;
                --size;

                compactChain(index);
                break;
            }

            index = ++index & mask;
        }

        return (V)value;
    }
	
	private void compactChain(int deleteIndex)
    {
        final int mask = values.length - 1;
        int index = deleteIndex;
        while (true)
        {
            index = ++index & mask;
            if (null == values[index])
            {
                break;
            }

            final int hash = Hashing.hash(keys[index], mask);

            if ((index < hash && (hash <= deleteIndex || deleteIndex <= index)) ||
                (hash <= deleteIndex && deleteIndex <= index))
            {
                keys[deleteIndex] = keys[index];
                values[deleteIndex] = values[index];

                values[index] = null;
                deleteIndex = index;
            }
        }
    }
	
	public boolean equals(Object o)
    {
		if(!(o instanceof Int2ObjectMap<?>)) {
			return false;
		}
		
		Int2ObjectMap<V> map2 = (Int2ObjectMap<V>)o;
        if (this == map2)
        {
            return true;
        }

        if (size != map2.size())
        {
            return false;
        }

        for (int i = 0, length = values.length; i < length; i++)
        {
            final Object thisValue = values[i];
            if (null != thisValue)
            {
                final Object thatValue = map2.get(keys[i]);
                if (thatValue == null || !thisValue.equals(thatValue))
                {
                    return false;
                }
            }
        }

        return true;
    }
	
	public void clear()
    {
        if (size > 0)
        {
            Arrays.fill(values, null);
            size = 0;
        }
    }

	public int[] keys() {
		return keys;
	}
	
	public Object[] values() {
		return values;
	}
}
