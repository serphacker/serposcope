package com.serphacker.serposcope.querybuilder;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;

import java.sql.Timestamp;

public class QGoogleRankBest
  extends RelationalPathBase<QGoogleRankBest>
{
  private static final long serialVersionUID = 1777540437L;
  public static final String TABLE_NAME = "serposcope_google_rank_best";
  public static final QGoogleRankBest googleRankBest = new QGoogleRankBest(TABLE_NAME);
  public final NumberPath<Integer> googleSearchId = createNumber("googleSearchId", Integer.class);
  public final NumberPath<Integer> googleTargetId = createNumber("googleTargetId", Integer.class);
  public final NumberPath<Integer> groupId = createNumber("groupId", Integer.class);
  public final NumberPath<Short> rank = createNumber("rank", Short.class);
  public final DateTimePath<Timestamp> runDay = createDateTime("runDay", Timestamp.class);
  public final StringPath url = createString("url");
  public final PrimaryKey<QGoogleRankBest> constraintB = createPrimaryKey(new Path[] { this.googleSearchId, this.googleTargetId, this.groupId });
  public final ForeignKey<QGoogleTarget> constraintB72 = createForeignKey(this.googleTargetId, "id");
  public final ForeignKey<QGoogleSearch> constraintB727 = createForeignKey(this.googleSearchId, "id");
  public final ForeignKey<QGroup> constraintB7 = createForeignKey(this.groupId, "id");
  
  public QGoogleRankBest(String variable)
  {
    super(QGoogleRankBest.class, PathMetadataFactory.forVariable(variable), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGoogleRankBest(String variable, String schema, String table)
  {
    super(QGoogleRankBest.class, PathMetadataFactory.forVariable(variable), schema, table);
    addMetadata();
  }
  
  public QGoogleRankBest(Path<? extends QGoogleRankBest> path)
  {
    super(path.getType(), path.getMetadata(), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGoogleRankBest(PathMetadata metadata)
  {
    super(QGoogleRankBest.class, metadata, "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public void addMetadata()
  {
    addMetadata(this.googleSearchId, ColumnMetadata.named("google_search_id").withIndex(3).ofType(4).withSize(10).notNull());
    addMetadata(this.googleTargetId, ColumnMetadata.named("google_target_id").withIndex(2).ofType(4).withSize(10).notNull());
    addMetadata(this.groupId, ColumnMetadata.named("group_id").withIndex(1).ofType(4).withSize(10).notNull());
    addMetadata(this.rank, ColumnMetadata.named("rank").withIndex(4).ofType(5).withSize(5));
    addMetadata(this.runDay, ColumnMetadata.named("run_day").withIndex(5).ofType(93).withSize(23).withDigits(10));
    addMetadata(this.url, ColumnMetadata.named("url").withIndex(6).ofType(12).withSize(256));
  }
}


/* Location:              D:\Download\serposcope-2.6.0.jar!\com\serphacker\serposcope\querybuilder\QGoogleRankBest.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */