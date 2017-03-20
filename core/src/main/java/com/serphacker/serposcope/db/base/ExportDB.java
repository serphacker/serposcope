/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.base;

import com.google.common.io.ByteStreams;
import com.serphacker.serposcope.db.AbstractDB;
import static com.serphacker.serposcope.db.base.MigrationDB.TABLES;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.zip.GZIPOutputStream;
import javax.inject.Inject;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class ExportDB extends AbstractDB {
    
    @Inject
    MigrationDB migrationDB;
    
    public final static int DEFAULT_MAX_ALLOWED_PACKET = 4194304/2;
//    public final static int DEFAULT_MAX_ALLOWED_PACKET = 4194304;

    public boolean export(String path) throws Exception {
        OutputStream os = new FileOutputStream(path, false);
        if(path.endsWith(".gz")){
            os = new GZIPOutputStream(os);
        }
        try (
            OutputStreamWriter osw = new OutputStreamWriter(os, Charset.forName("UTF-8"));
            PrintWriter writer = new PrintWriter(osw);
        ){
            return export(writer);
        }
    }
    
    public boolean importStream(BufferedReader reader) throws SQLException, IOException, Exception{
        String line = null;
        try (Connection con = ds.getConnection()) {
            while((line=reader.readLine()) != null){
                try(Statement stmt = con.createStatement()){
                    if(line.isEmpty() || line.startsWith("--") || line.startsWith("#")){
                        continue;
                    }
                    stmt.executeUpdate(line);
                }
            }
        }
        migrationDB.migrateIfNeeded();
        return true;
    }

    // default max_allowed_packet = 4194304
    public boolean export(Writer writer) throws IOException {
        for (String resource : MigrationDB.DB_SCHEMA_FILES) {
            String sql = new String(ByteStreams.toByteArray(MigrationDB.class.getResourceAsStream(resource)));
            sql = sql.replaceAll("--.*\n", "\n");
            sql = sql.replaceAll("\\s+", " ");
            sql = sql.replaceAll(";\\s*", ";\n");
            writer.append(sql);
            writer.append("\n");
        }
        
        writer.append("\nSET FOREIGN_KEY_CHECKS=0;\n");
        try (Connection con = ds.getConnection()) {
            for (String table : TABLES) {
                writer.flush();
                try (Statement stmt = con.createStatement()) {
                    LOG.info("exporting table {}", table);
                    long _start = System.currentTimeMillis();
                    
                    stmt.setQueryTimeout(3600*24);
                    ResultSet rs = stmt.executeQuery("SELECT * FROM `" + table + "`");
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columns = metaData.getColumnCount();
                    
                    String insertStatement = "INSERT INTO `" + table + "` VALUES ";
                    
                    StringBuilder stmtBuilder = new StringBuilder(insertStatement);
                    while (rs.next()) {
                        
                        StringBuilder entryBuilder= new StringBuilder("(");
                        for (int colIndex = 1; colIndex <= columns; colIndex++) {
                            Object object = rs.getObject(colIndex);
                            String colName = metaData.getColumnName(colIndex);
                            String colClassName = metaData.getColumnClassName(colIndex);
                            String escaped = escape(object, colClassName, colName);
                            entryBuilder.append(escaped);
                            if (colIndex < columns) {
                                entryBuilder.append(',');
                            }
                        }
                        entryBuilder.append("),");
                        
                        if(
                            stmtBuilder.length() != insertStatement.length() && 
                            stmtBuilder.length() + entryBuilder.length() > DEFAULT_MAX_ALLOWED_PACKET
                        ){
                            stmtBuilder.setCharAt(stmtBuilder.length()-1, ';');
                            writer.append(stmtBuilder).append('\n');
                            stmtBuilder = new StringBuilder(insertStatement);
                        }
                        
                        stmtBuilder.append(entryBuilder);
                    }
                    
                    if(stmtBuilder.length() != insertStatement.length()){
                        stmtBuilder.setCharAt(stmtBuilder.length()-1, ';');
                        writer.append(stmtBuilder).append('\n');
                    }
                    
                    LOG.info("exported table {} in {}", table, DurationFormatUtils.formatDurationHMS(System.currentTimeMillis()-_start));
                }
            }
            writer.append("SET FOREIGN_KEY_CHECKS=1;\n");
        } catch (Exception ex) {
            LOG.error("SQL error", ex);
            return false;
        }

        return true;
    }

    protected String escape(Object colVal, String className, String colName) throws Exception {
        if (colVal == null) {
            return "NULL";
        }
        switch (className) {
            case "java.lang.String":
                return "'" + escapeString((String)colVal) + "'";
            case "java.sql.Clob":
                return "'" + clobToStringEscaped((Clob) colVal) + "'";
                
            case "java.sql.Blob":
                return blobToString((Blob) colVal);
            case "[B":
                return blobToString((byte[]) colVal);

            case "java.lang.Boolean":
                return (Boolean) colVal ? "1" : "0";
                
            case "java.lang.Integer":
            case "java.lang.Short":
            case "java.lang.Long":
            case "java.lang.Byte":
                return colVal.toString();
                
            case "java.sql.Date":
            case "java.sql.Timestamp":
                return "'" + colVal.toString() + "'";
                
            default:
                throw new UnsupportedOperationException("escaping not implemented for class " + className + " of column " + colName);
        }
    }
    
    protected String escapeString(String str){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            sbEscape(builder, str.charAt(i));
        }
        return builder.toString();
    }

    protected String clobToStringEscaped(java.sql.Clob data) throws Exception {
        final StringBuilder builder = new StringBuilder();

        try (
            final Reader reader = data.getCharacterStream();
            final BufferedReader br = new BufferedReader(reader);
        ){
            int b;
            while (-1 != (b = br.read())) {
                sbEscape(builder, (char)b);
            }
        }
        return builder.toString();
    }
    
    protected StringBuilder sbEscape(StringBuilder builder, char c){
        switch(c){
            case '\'':
                builder.append('\'');
                builder.append(c);
                break;
            case '\0':
            case '\r':
            case '\n':
            case '\t':
            case '\b':
                builder.append(' ');
                break;
            default:
                builder.append(c);
        }
        return builder;
    }

    protected String blobToString(Blob data) throws Exception {
        final StringBuilder sb = new StringBuilder("X'");

        int b;
        try (InputStream br = data.getBinaryStream()) {
            while (-1 != (b = br.read())) {
                if (b < 0x10) {
                    sb.append('0');
                }
                sb.append(Integer.toString(b, 16));
            }
            sb.append("'");
        }

        return sb.toString();
    }

    protected String blobToString(byte[] blob) {
        final StringBuilder sb = new StringBuilder("X'");

        for (int i = 0; i < blob.length; i++) {
            int b = (blob[i] & 0xFF);
            if (b < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toString(b, 16));
        }
        sb.append("'");

        return sb.toString();
    }

}
