/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.http;


public class ByteCharSequence implements CharSequence {

    private final byte[] data;
    private final int length;
    private final int offset;

    public ByteCharSequence(byte[] data) {
        this(data, 0, data.length);
    }

    public ByteCharSequence(byte[] data, int offset, int length) {
        this.data = data;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public int length() {
        return this.length;
    }

    @Override
    public char charAt(int index) {
        return (char) (data[offset + index] & 0xff);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new ByteCharSequence(data, offset + start, end - start);
    }

    @Override
    public String toString() {
        return new String(data, offset, length);
    }
    
}
