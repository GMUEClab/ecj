/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.util;
import java.lang.reflect.*;
import javax.swing.tree.*;
import javax.swing.event.*;

public class ReflectedObject implements TreeModel
    {
    Class type;     // the class or TYPE of the object
    Object obj;     // the object.  Primitive objects are cast into their wrapper types
    String name;    // the name of the instance variable holding the object.  Top-level objects have a name of "->"
    Object uniq;    // a unique identifier for this particular instance variable (used to make getIndexOfChild() possible, ick)
    static ReflectedObject unknown = new ReflectedObject(null)
        {
        public String toString() { return "<unknown>"; }
        };
    
    public ReflectedObject(Object o)
        {
        this(o,(o==null ? Object.class : o.getClass()),null,null);
        }
        
    protected ReflectedObject(Object o, Class t, String n, Object u)
        {
        obj = o;
        type = t;
        name = n;
        uniq = u;
        }
    
    public String toString()
        {
        String field = (name == null ? "" : name + ": ");
        try {
            if (obj == null) return field + "null";
            else if (type.isArray()) return field + type.getName() + ", length=" + Array.getLength(obj);
            return field+type.getName()+" "+obj.toString();
            } catch (Exception e) {
            e.printStackTrace();
            return field + type.getName() + " <error>";
            }
        }
        
    public int getNumChildren()
        {
        return getNumFields() + getNumProperties();
        }
    
    public ReflectedObject getChild(int index)
        {
        int f = getNumFields();
        if (index < f) return getField(index);
        else return getProperty(index - f);
        }
    
    public ReflectedObject[] getChildren()
        {
        ReflectedObject[] fields = getFields();
        ReflectedObject[] props = getProperties();
        ReflectedObject o[] = new ReflectedObject[fields.length + props.length];
        System.arraycopy(fields,0,o,0,fields.length);
        System.arraycopy(props,0,o,fields.length,props.length);
        return o;
        }

    public int getNumFields()
        {
        try
            {
            if (obj == null || type.isPrimitive()) return 0;
            else if (type.isArray())
                {
                return Array.getLength(obj);
                }
            else
                {
                return type.getFields().length;
                }
            }
        catch (IllegalArgumentException e) { e.printStackTrace(); throw new RuntimeException("Unexpected Exception: " + e); }
        }
    
    public ReflectedObject getField(int index)
        {
        try
            {
            if (obj == null || type.isPrimitive() || index < 0) return null;
            else if (type.isArray())
                {
                int len = Array.getLength(obj);
                if (index > len) return null;
                return new ReflectedObject(Array.get(obj,index), type.getComponentType(),""+index, ""+index);
                }
            else
                {
                Field[] f = type.getFields();
                int len = f.length;
                if (index > len) return null;
                return new ReflectedObject(f[index].get(obj), 
                        (f[index].get(obj) == null || f[index].getType().isPrimitive() ? 
                        f[index].getType() : f[index].get(obj).getClass()), 
                    f[index].getName(), f[index]);
                }
            }
        catch (IllegalArgumentException e) { e.printStackTrace(); throw new RuntimeException("Unexpected Exception: " + e); }
        catch (IllegalAccessException e) { e.printStackTrace(); throw new RuntimeException("Unexpected Exception: " + e); }
        }
    
    public ReflectedObject[] getFields()
        {
        try
            {
            if (obj == null || type.isPrimitive()) return new ReflectedObject[0];
            else if (type.isArray())
                {
                int len = Array.getLength(obj);
                ReflectedObject[] ref = new ReflectedObject[len];
                for(int x = 0; x < len; x++)    
                    ref[x] = new ReflectedObject(Array.get(obj,x), type.getComponentType(),""+x,""+x);
                return ref;
                }
            else
                {
                Field[] f = type.getFields();
                int len = f.length;
                ReflectedObject[] ref = new ReflectedObject[len];
                for(int x=0;x<len; x++)
                    ref[x] = new ReflectedObject(f[x].get(obj), 
                        (f[x].get(obj) == null || f[x].getType().isPrimitive() ? f[x].getType() : f[x].get(obj).getClass()), 
                        f[x].getName(), f[x]);
                return ref;
                }
            }
        catch (IllegalArgumentException e) { e.printStackTrace(); throw new RuntimeException("Unexpected Exception: " + e); }
        catch (IllegalAccessException e) { e.printStackTrace(); throw new RuntimeException("Unexpected Exception: " + e); }
        }
    
    public boolean equals(Object obj)
        {
        if (obj == null) return false;
        if (!(obj instanceof ReflectedObject)) return false;
        if (uniq == null && ((ReflectedObject)obj).uniq == null) return true;
        if (uniq == null || ((ReflectedObject)obj).uniq == null) return false;
        return ((ReflectedObject)obj).uniq.equals(uniq);
        }
        
    // tree model stuff
    public Object getRoot() { return this; }
    public Object getChild(Object parent, int index)
        {
        return ((ReflectedObject)parent).getChild(index);
        }
    public int getChildCount(Object parent)
        {
        return ((ReflectedObject)parent).getNumChildren();
        }
    // This could get grotesquely expensive!
    public int getIndexOfChild(Object parent, Object child)
        {
        ReflectedObject[] children = ((ReflectedObject)parent).getChildren();
        for(int x=0;x<children.length;x++)
            if (children[x].equals(child)) return x;
        throw new IndexOutOfBoundsException("No such child " + child + " in parent " + ((ReflectedObject)parent).toString());
        }
    public boolean isLeaf(Object parent)
        {
        return getChildCount(parent) == 0;
        }
    public void valueForPathChanged(TreePath path, Object newValue)
        {
        // do nothing
        }
    public void addTreeModelListener(TreeModelListener l)
        {
        // do nothing
        }
    public void removeTreeModelListener(TreeModelListener l)
        {
        // do nothing
        }
        
    // Java Bean Properties
    int getNumProperties()
        {
        if (obj==null) return 0;
        int count = 0;
        
        // generate the properties
        try
            {
            Class c = obj.getClass();
            Method[] m = (c.getMethods());
            for(int x = 0 ; x < m.length; x++)
                {
                if (m[x].getName().startsWith("get") || m[x].getName().startsWith("is")) // corrrect syntax?
                    {
                    int modifier = m[x].getModifiers();
                    if (m[x].getParameterTypes().length == 0 &&
                        Modifier.isPublic(modifier) &&
                        m[x].getReturnType() != Void.TYPE) // no arguments, and public, non-void, non-abstract?
                        {
                        count++;
                        }
                    }
                }
            }
        catch (Exception e)
            {
            count = 0;
            e.printStackTrace();
            }
        return count;
        }

    ReflectedObject getProperty(int index)
        {
        if (obj==null) return null;
        int count = 0;
        
        // generate the properties
        try
            {
            Class c = obj.getClass();
            Method[] m = (c.getMethods());
            for(int x = 0 ; x < m.length; x++)
                {
                if (m[x].getName().startsWith("get") || m[x].getName().startsWith("is")) // corrrect syntax?
                    {
                    int modifier = m[x].getModifiers();
                    if (m[x].getParameterTypes().length == 0 &&
                        Modifier.isPublic(modifier) &&
                        m[x].getReturnType() != Void.TYPE) // no arguments, and public, non-void, non-abstract?
                        {
                        if (count==index)
                            {
                            Object o = null;
                            try
                                {
                                o = m[x].invoke(obj, new Object[0]);
                                }
                            catch (InvocationTargetException e)
                                {
                                return unknown;
                                }
                            return new ReflectedObject(o, o == null || m[x].getReturnType().isPrimitive() ? 
                                m[x].getReturnType() : o.getClass(),
                                "Property " + m[x].getName(), "Property " + m[x].getName());
                            }
                        count++;
                        }
                    }
                }
            }
        catch (Exception e)
            {
            e.printStackTrace();
            }
        return unknown;
        }
 
    
    ReflectedObject[] getProperties()
        {
        if (obj==null) return new ReflectedObject[0];
        int len = getNumProperties();
        int count = 0;
        
        ReflectedObject[] refs = new ReflectedObject[len];
        
        // generate the properties
        try
            {
            Class c = obj.getClass();
            Method[] m = (c.getMethods());
            for(int x = 0 ; x < m.length; x++)
                {
                if (m[x].getName().startsWith("get") || m[x].getName().startsWith("is")) // corrrect syntax?
                    {
                    int modifier = m[x].getModifiers();
                    if (m[x].getParameterTypes().length == 0 &&
                        Modifier.isPublic(modifier) &&
                        m[x].getReturnType() != Void.TYPE) // no arguments, and public, non-void, non-abstract?
                        {
                        Object o = null;
                        try
                            {
                            o = m[x].invoke(obj, new Object[0]);
                            refs[count] = new ReflectedObject(o, o == null || m[x].getReturnType().isPrimitive() ? 
                                m[x].getReturnType() : o.getClass(),
                                "Property " + m[x].getName(), "Property " + m[x].getName());
                            }
                        catch (InvocationTargetException e)
                            {
                            refs[count] = unknown;
                            }
                        count++;
                        }
                    }
                }
            }
        catch (Exception e)
            {
            e.printStackTrace();
            }
        return refs;
        }
          
    }
