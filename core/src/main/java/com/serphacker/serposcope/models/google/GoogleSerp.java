/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.google;

import it.unimi.dsi.fastutil.shorts.Short2ShortArrayMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class GoogleSerp {
    
    private final static byte SERIAL_VERSION = 0;
    
    int runId;
    int googleSearchId;
    LocalDateTime runDay;
    List<GoogleSerpEntry> entries = new ArrayList<>();

    public GoogleSerp(int runId, int googleSearchId, LocalDateTime runDay) {
        this.runId = runId;
        this.googleSearchId = googleSearchId;
        this.runDay = runDay;
    }
    
    public void addEntry(GoogleSerpEntry entry){
        entries.add(entry);
    }

    public int getRunId() {
        return runId;
    }
    public int getGoogleSearchId() {
        return googleSearchId;
    }
    
    public List<GoogleSerpEntry> getEntries() {
        return entries;
    }

    public LocalDateTime getRunDay() {
        return runDay;
    }

    public void setRunDay(LocalDateTime runDay) {
        this.runDay = runDay;
    }
    
    public void setSerializedEntries(byte[] data) throws IOException{
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        
        byte version = dis.readByte();
        if(version != SERIAL_VERSION){
            throw new UnsupportedOperationException("unsupported serialized version");
        }

        int entrySize = dis.readShort();
        entries = new ArrayList<>(entrySize);
        
        for (int i = 0; i < entrySize; i++) {
            GoogleSerpEntry entry = new GoogleSerpEntry(dis.readUTF());
            byte mapSize = dis.readByte();
            for (int j = 0; j < mapSize; j++) {
                short key = dis.readShort();
                short value = dis.readShort();
                entry.map.put(key, value);
            }
            entries.add(entry);
        }
    }
    
    public byte[] getSerializedEntries() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeByte(SERIAL_VERSION);
        dos.writeShort(entries.size());
        for (GoogleSerpEntry entry : entries) {
            dos.writeUTF(entry.url);
            dos.writeByte(entry.map.size());
            for (Map.Entry<Short, Short> mapEntry : entry.map.entrySet()) {
                dos.writeShort(mapEntry.getKey());
                dos.writeShort(mapEntry.getValue());
            }
        }
        
        baos.close();
        return baos.toByteArray();
    }
    
    
    

}
