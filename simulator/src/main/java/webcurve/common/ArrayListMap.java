package webcurve.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ArrayListMap<K, T> 
{
	Map<K, ArrayList<T>> map = Collections.synchronizedMap(new HashMap<K, ArrayList<T>>());
	
	public boolean add(K key, T type)
	{
		ArrayList<T> list = map.get(key);
		if ( null == list)
		{
			list = new ArrayList<T>();
			map.put(key, list);
		}
		else if (list.contains(type))
				return false;
	
		list.add(type);
		return true;
		
	}

	public boolean remove(K key, T type)
	{
		ArrayList<T> list = map.get(key);
		if ( null == list)
			return false;
		
		return list.remove(type);
	}
	
	public ArrayList<T> remove(K key)
	{
		return map.remove(key);
	}
	
	public ArrayList<T> getAll(K key)
	{
		return map.get(key);
	}
}
