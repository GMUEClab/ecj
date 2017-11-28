/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.test;

import ec.util.MersenneTwisterFast;

/**
 * A stub version of ECJ's PRNG that allows us to replace the output of
 * its pseudo-random functions with constant, injected values.
 * 
 * @author Eric O. Scott
 */
public class TestPRNG extends MersenneTwisterFast
{
    Option<Integer> fixedInt = Option.NONE;
    Option<Short> fixedShort = Option.NONE;
    Option<Character> fixedChar = Option.NONE;
    Option<Boolean> fixedBool = Option.NONE;
    Option<Byte> fixedByte = Option.NONE;
    Option<byte[]> fixedBytes = Option.NONE;
    Option<Long> fixedLong = Option.NONE;
    Option<Double> fixedDouble = Option.NONE;
    Option<Double> fixedGaussian = Option.NONE;
    Option<Float> fixedFloat = Option.NONE;
    
    public void setFixedInt(final int i)
    {
        fixedInt = new Option<Integer>(i);
    }
    
    public void setFixedShort(final short i)
    {
        fixedShort = new Option<Short>(i);
    }
    
    public void setFixedChar(final char c)
    {
        fixedChar = new Option<Character>(c);
    }
    
    public void setFixedBool(final boolean b)
    {
        fixedBool = new Option<Boolean>(b);
    }
    
    public void setFixedByte(final byte b)
    {
        fixedByte = new Option<Byte>(b);
    }
    
    public void setFixedBytes(final byte[] b)
    {
        fixedBytes = new Option<byte[]>(b);
    }
    
    public void setFixedLong(final long l)
    {
        fixedLong = new Option<Long>(l);
    }
    
    public void setFixedDouble(final double d)
    {
        fixedDouble = new Option<Double>(d);
    }
    
    public void setFixedGaussian(final double d)
    {
        fixedGaussian = new Option<Double>(d);
    }
    
    public void setFixedFloat(final float f)
    {
        fixedFloat = new Option<Float>(f);
    }
    
    @Override
    public int nextInt()
    {
        if (!fixedInt.isDefined())
            return super.nextInt();
        return fixedInt.get();
    }

    @Override
    public short nextShort()
    {
        if (!fixedShort.isDefined())
            return super.nextShort();
        return fixedShort.get();
    }


    @Override
    public char nextChar()
    {
        if (!fixedChar.isDefined())
            return super.nextChar();
        return fixedChar.get();
    }

    @Override
    public boolean nextBoolean()
    {
        if (!fixedBool.isDefined())
            return super.nextBoolean();
        return fixedBool.get();
    }

    @Override
    public byte nextByte()
    {
        if (!fixedByte.isDefined())
            return super.nextByte();
        return fixedByte.get();
    }


    @Override
    public void nextBytes(byte[] bytes)
    {
        if (!fixedBytes.isDefined())
            super.nextBytes(bytes);
        assert(bytes != null);
        assert(bytes.length == fixedBytes.get().length);
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = fixedBytes.get()[i];
    }

    @Override
    public long nextLong()
    {
        if (!fixedLong.isDefined())
            return super.nextLong();
        return fixedLong.get();
    }

    @Override
    public long nextLong(long n)
    {
        if (!fixedLong.isDefined())
            return super.nextLong(n);
        final long result = fixedLong.get();
        assert(result < n);
        assert(result >= 0);
        return result;
    }

    @Override
    public double nextDouble()
    {
        if (!fixedDouble.isDefined())
            return super.nextDouble();
        final double result = fixedDouble.get();
        assert(result >= 0.0);
        assert(result < 1.0);
        return result;
    }

    @Override
    public double nextDouble(boolean includeZero, boolean includeOne)
    {
        if (!fixedDouble.isDefined())
            return super.nextDouble(includeZero, includeOne);
        final double result = fixedDouble.get();
        assert(result >= 0.0);
        assert(includeZero || result > 0.0);
        assert(result <= 1.0);
        assert(includeOne || result < 1.0);
        return result;
    }

    @Override
    public double nextGaussian()
    {
        if (!fixedGaussian.isDefined())
            return super.nextGaussian();
        final double result = fixedGaussian.get();
        return result;
    }
    
    public float nextFloat()
    {
        if (!fixedFloat.isDefined())
            return super.nextFloat();
        final float result = fixedFloat.get();
        assert(result >= 0.0);
        assert(result < 1.0);
        return result;
    }
    
    public float nextFloat(boolean includeZero, boolean includeOne)
    {
        if (!fixedFloat.isDefined())
            return super.nextFloat();
        final float result = fixedFloat.get();
        assert(result >= 0.0);
        assert(includeZero || result > 0.0);
        assert(result <= 1.0);
        assert(includeOne || result < 1.0);
        return result;
    }

    @Override
    public int nextInt(int n)
    {
        if (!fixedInt.isDefined())
            return super.nextInt(n);
        final int result = fixedInt.get();
        assert(result >= 0);
        assert(result < n);
        return result;
    }
}
