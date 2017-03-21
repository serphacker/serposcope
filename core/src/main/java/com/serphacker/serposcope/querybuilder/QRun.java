package com.serphacker.serposcope.querybuilder;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;

import java.sql.Date;
import java.sql.Timestamp;

public class QRun
  extends RelationalPathBase<QRun>
{
  private static final long serialVersionUID = -194355649L;
  public static final String TABLE_NAME = "serposcope_run";
  public static final QRun run = new QRun(TABLE_NAME);
  public final NumberPath<Integer> captchas = createNumber("captchas", Integer.class);
  public final DatePath<Date> day = createDate("day", Date.class);
  public final NumberPath<Integer> errors = createNumber("errors", Integer.class);
  public final DateTimePath<Timestamp> finished = createDateTime("finished", Timestamp.class);
  public final NumberPath<Integer> id = createNumber("id", Integer.class);
  public final NumberPath<Integer> mode = createNumber("mode", Integer.class);
  public final NumberPath<Integer> moduleId = createNumber("moduleId", Integer.class);
  public final NumberPath<Integer> progress = createNumber("progress", Integer.class);
  public final DateTimePath<Timestamp> started = createDateTime("started", Timestamp.class);
  public final NumberPath<Integer> status = createNumber("status", Integer.class);
  public final PrimaryKey<QRun> constraint1 = createPrimaryKey(new Path[] { this.id });
  public final ForeignKey<QGoogleTargetSummary> _constraint41d36 = createInvForeignKey(this.id, "run_id");
  public final ForeignKey<QGoogleRank> _constraint6e22 = createInvForeignKey(this.id, "run_id");
  
  public QRun(String variable)
  {
    super(QRun.class, PathMetadataFactory.forVariable(variable), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QRun(String variable, String schema, String table)
  {
    super(QRun.class, PathMetadataFactory.forVariable(variable), schema, table);
    addMetadata();
  }
  
  public QRun(Path<? extends QRun> path)
  {
    super(path.getType(), path.getMetadata(), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QRun(PathMetadata metadata)
  {
    super(QRun.class, metadata, "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public void addMetadata()
  {
    addMetadata(this.captchas, ColumnMetadata.named("captchas").withIndex(7).ofType(4).withSize(10));
    addMetadata(this.day, ColumnMetadata.named("day").withIndex(3).ofType(91).withSize(8));
    addMetadata(this.errors, ColumnMetadata.named("errors").withIndex(8).ofType(4).withSize(10));
    addMetadata(this.finished, ColumnMetadata.named("finished").withIndex(5).ofType(93).withSize(23).withDigits(10));
    addMetadata(this.id, ColumnMetadata.named("id").withIndex(1).ofType(4).withSize(10).notNull());
    addMetadata(this.mode, ColumnMetadata.named("mode").withIndex(10).ofType(4).withSize(10));
    addMetadata(this.moduleId, ColumnMetadata.named("module_id").withIndex(2).ofType(4).withSize(10));
    addMetadata(this.progress, ColumnMetadata.named("progress").withIndex(6).ofType(4).withSize(10));
    addMetadata(this.started, ColumnMetadata.named("started").withIndex(4).ofType(93).withSize(23).withDigits(10));
    addMetadata(this.status, ColumnMetadata.named("status").withIndex(9).ofType(4).withSize(10));
  }
}


/* Location:              D:\Download\serposcope-2.6.0.jar!\com\serphacker\serposcope\querybuilder\QRun.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */