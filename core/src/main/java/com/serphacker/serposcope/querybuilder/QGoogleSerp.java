package com.serphacker.serposcope.querybuilder;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;

import java.sql.Blob;
import java.sql.Timestamp;

public class QGoogleSerp
  extends RelationalPathBase<QGoogleSerp>
{
  private static final long serialVersionUID = -721655883L;
  public static final String TABLE_NAME = "serposcope_google_serp";
  public static final QGoogleSerp googleSerp = new QGoogleSerp(TABLE_NAME);
  public final NumberPath<Integer> googleSearchId = createNumber("googleSearchId", Integer.class);
  public final DateTimePath<Timestamp> runDay = createDateTime("runDay", Timestamp.class);
  public final NumberPath<Integer> runId = createNumber("runId", Integer.class);
  public final SimplePath<Blob> serp = createSimple("serp", Blob.class);
  public final PrimaryKey<QGoogleSerp> constraint6 = createPrimaryKey(new Path[] { this.googleSearchId, this.runId });
  public final ForeignKey<QGoogleSearch> constraint6e = createForeignKey(this.googleSearchId, "id");
  
  public QGoogleSerp(String variable)
  {
    super(QGoogleSerp.class, PathMetadataFactory.forVariable(variable), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGoogleSerp(String variable, String schema, String table)
  {
    super(QGoogleSerp.class, PathMetadataFactory.forVariable(variable), schema, table);
    addMetadata();
  }
  
  public QGoogleSerp(Path<? extends QGoogleSerp> path)
  {
    super(path.getType(), path.getMetadata(), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGoogleSerp(PathMetadata metadata)
  {
    super(QGoogleSerp.class, metadata, "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public void addMetadata()
  {
    addMetadata(this.googleSearchId, ColumnMetadata.named("google_search_id").withIndex(2).ofType(4).withSize(10).notNull());
    addMetadata(this.runDay, ColumnMetadata.named("run_day").withIndex(3).ofType(93).withSize(23).withDigits(10));
    addMetadata(this.runId, ColumnMetadata.named("run_id").withIndex(1).ofType(4).withSize(10).notNull());
    addMetadata(this.serp, ColumnMetadata.named("serp").withIndex(4).ofType(2004).withSize(Integer.MAX_VALUE));
  }
}


/* Location:              D:\Download\serposcope-2.6.0.jar!\com\serphacker\serposcope\querybuilder\QGoogleSerp.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */