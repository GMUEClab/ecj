package ec.eda.dovs;

import java.util.*;
import java.util.Map.Entry;
import ec.*;

/**
 * CornerMap can help us to quickly identify the possible individuals that is
 * able to form a hyperbox around best individual. It has multiple key-value
 * pairs. Each key can have multiple values. The elements in CornerMap is sorted
 * based on the key of the elements. If two elements have the same key value,
 * it's order is determined by their insertion time.
 * 
 * <p>
 * It stores the map between one of the coordinate of the individual to the
 * individual. For example, we have a individual "ind" with 5 dimension (12, 3,
 * 4, 2, 8), we should create a array "corners" with 5 CornerMap. For each of
 * the CornerMap, we should insert the coordinate of the individual as key, and
 * the individual itself as the value, like (12, ind), (3, ind) .... into their
 * corresponding CornerMap.
 *
 * <p>
 * CornerMap is essentially a mimic of multimap in C++ where keys are in sorted,
 * but in the ArrayList for each key, the order is determined by their insertion
 * order. Here we simplify it with only useful function such as lowerBound and
 * upperBound.
 * 
 * @author Ermo Wei and David Freelan
 */

public class CornerMap
    {

    /**
     * Simple structure store the key and value from this CornerMap. This is
     * userd for retrieving data from CornerMap
     * 
     * @author Ermo Wei
     *
     */
    public class Pair
        {
        public Integer key;
        public Individual value;

        public int getKey()
            {
            return key;
            }

        public Individual getValue()
            {
            return value;
            }
        }

    /** major data structure used for this CornerMap, it is order by key */
    TreeMap<Integer, ArrayList<Individual>> map = new TreeMap<Integer, ArrayList<Individual>>();

    /** Insert a key and value pair into CornerMap */
    public void insert(int coordindate, Individual ind)
        {
        if (!map.containsKey(coordindate))
            map.put(coordindate, new ArrayList<Individual>());
        map.get(coordindate).add(ind);
        }

    /**
     * This returns the smallest element whose key is equal to or bigger than
     * the argument "key".
     */
    public Pair lowerBound(int key)
        {
        Pair entry = new Pair();
        if (map.get(key).size() == 0)
            return null;

        entry.key = key;
        entry.value = map.get(key).get(0);
        return entry;
        }

    /**
     * This method returns the smallest element whose key is bigger than
     * (excluding equal to) "key",
     */
    public Pair upperBound(int key)
        {
        Entry<Integer, ArrayList<Individual>> entry = map.higherEntry(key);
        if (entry != null)
            {
            if (entry.getValue().size() == 0)
                return null;
            Pair pair = new Pair();
            pair.key = entry.getKey();
            pair.value = entry.getValue().get(0);
            return pair;
            }
        else
            return null;
        }

    /** Test if we have another key value pair before parameter pair */
    public boolean hasSmaller(Pair pair)
        {
        // First search this individual in the list
        ArrayList<Individual> currentList = map.get(pair.key);
        for (int i = currentList.size() - 1; i >= 0; i--)
            {
            // We want to compare EXACT SAME OBJECT
            if (currentList.get(i) == pair.value)
                {
                // find, can we just return true?
                if (i == 0)
                    {
                    // if this is already the first element in current list,
                    // find previous list
                    Entry<Integer, ArrayList<Individual>> entry = map.lowerEntry(pair.key);
                    if (entry != null)
                        {
                        if (entry.getValue().size() == 0)
                            return false;
                        else
                            return true;
                        }
                    else
                        return false;
                    }
                else
                    return true;
                }
            }
        // we didn't find it in the list, which should not happen
        return false;
        }

    /** Test if we have another key value pair after parameter pair */
    public boolean hasLarger(Pair pair)
        {
        // First search this individual in the list
        ArrayList<Individual> currentList = map.get(pair.key);
        for (int i = 0; i < currentList.size(); ++i)
            {
            // We want to compare EXACT SAME OBJECT
            if (currentList.get(i) == pair.value)
                {
                // find, can we just return true?
                if (i == currentList.size() - 1)
                    {
                    // if this is already the last element in current list,
                    // find next list
                    Entry<Integer, ArrayList<Individual>> entry = map.higherEntry(pair.key);
                    if (entry != null)
                        {
                        if (entry.getValue().size() == 0)
                            return false;
                        else
                            return true;
                        }
                    else
                        return false;
                    }
                else
                    return true;
                }
            }
        // we didn't find it in the list, which should not happen
        return false;
        }

    /**
     * Get a greatest key value pair from this CornerMap who is the immediate
     * previous element of pair
     */
    public Pair smaller(Pair pair)
        {
        Pair newPair = new Pair();
        // First search this individual in the list
        ArrayList<Individual> currentList = map.get(pair.key);
        for (int i = currentList.size() - 1; i >= 0; i--)
            {
            // We want to compare EXACT SAME OBJECT
            if (currentList.get(i) == pair.value)
                {
                // find, can we just return true?
                if (i == 0)
                    {
                    // if this is already the first element in current list,
                    // find previous list
                    Entry<Integer, ArrayList<Individual>> entry = map.lowerEntry(pair.key);
                    if (entry != null)
                        {
                        if (entry.getValue().size() == 0)
                            return null;
                        else
                            {
                            newPair.key = entry.getKey();
                            newPair.value = entry.getValue().get(entry.getValue().size() - 1);
                            return newPair;
                            }
                        }
                    else
                        return null;
                    }
                else
                    {
                    newPair.key = pair.key;
                    newPair.value = currentList.get(i - 1);
                    return newPair;
                    }
                }
            }
        // we didn't find it in the list, which should not happen
        return null;
        }
    }
