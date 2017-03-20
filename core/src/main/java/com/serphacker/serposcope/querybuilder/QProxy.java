package com.serphacker.serposcope.querybuilder;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;

import java.sql.Timestamp;

public class QProxy
  extends RelationalPathBase<QProxy>
{
  private static final long serialVersionUID = -2094116574L;
  public static final String TABLE_NAME = "serposcope_proxy";
  public static final QProxy proxy = new QProxy(TABLE_NAME);
  public final NumberPath<Integer> id = createNumber("id", Integer.class);
  public final StringPath ip = createString("ip");
  public final DateTimePath<Timestamp> lastCheck = createDateTime("lastCheck", Timestamp.class);
  public final StringPath password = createString("password");
  public final NumberPath<Integer> port = createNumber("port", Integer.class);
  public final StringPath remoteIp = createString("remoteIp");
  public final NumberPath<Byte> status = createNumber("status", Byte.class);
  public final NumberPath<Integer> type = createNumber("type", Integer.class);
  public final StringPath user = createString("user");
  public final PrimaryKey<QProxy> constraint48 = createPrimaryKey(new Path[] { this.id });
  
  public QProxy(String variable)
  {
    super(QProxy.class, PathMetadataFactory.forVariable(variable), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QProxy(String variable, String schema, String table)
  {
    super(QProxy.class, PathMetadataFactory.forVariable(variable), schema, table);
    addMetadata();
  }
  
  public QProxy(Path<? extends QProxy> path)
  {
    super(path.getType(), path.getMetadata(), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QProxy(PathMetadata metadata)
  {
    super(QProxy.class, metadata, "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public void addMetadata()
  {
    addMetadata(this.id, ColumnMetadata.named("id").withIndex(1).ofType(4).withSize(10).notNull());
    addMetadata(this.ip, ColumnMetadata.named("ip").withIndex(3).ofType(2005).withSize(Integer.MAX_VALUE));
    addMetadata(this.lastCheck, ColumnMetadata.named("last_check").withIndex(7).ofType(93).withSize(23).withDigits(10));
    addMetadata(this.password, ColumnMetadata.named("password").withIndex(6).ofType(2005).withSize(Integer.MAX_VALUE));
    addMetadata(this.port, ColumnMetadata.named("port").withIndex(4).ofType(4).withSize(10));
    addMetadata(this.remoteIp, ColumnMetadata.named("remote_ip").withIndex(9).ofType(12).withSize(256));
    addMetadata(this.status, ColumnMetadata.named("status").withIndex(8).ofType(-6).withSize(3));
    addMetadata(this.type, ColumnMetadata.named("type").withIndex(2).ofType(4).withSize(10));
    addMetadata(this.user, ColumnMetadata.named("user").withIndex(5).ofType(2005).withSize(Integer.MAX_VALUE));
  }
}


/* Location:              D:\Download\serposcope-2.6.0.jar!\com\serphacker\serposcope\querybuilder\QProxy.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */