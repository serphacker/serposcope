package com.serphacker.serposcope.querybuilder;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;

public class QGoogleRank
  extends RelationalPathBase<QGoogleRank>
{
  private static final long serialVersionUID = -721689647L;
  public static final String TABLE_NAME = "serposcope_google_rank";
  public static final QGoogleRank googleRank = new QGoogleRank(TABLE_NAME);
  public final NumberPath<Short> diff = createNumber("diff", Short.class);
  public final NumberPath<Integer> googleSearchId = createNumber("googleSearchId", Integer.class);
  public final NumberPath<Integer> googleTargetId = createNumber("googleTargetId", Integer.class);
  public final NumberPath<Integer> groupId = createNumber("groupId", Integer.class);
  public final NumberPath<Short> previousRank = createNumber("previousRank", Short.class);
  public final NumberPath<Short> hits = createNumber("hits", Short.class);
  public final NumberPath<Short> rank = createNumber("rank", Short.class);
  public final NumberPath<Integer> runId = createNumber("runId", Integer.class);
  public final StringPath url = createString("url");
  public final PrimaryKey<QGoogleRank> constraint6e2 = createPrimaryKey(new Path[] { this.googleSearchId, this.googleTargetId, this.groupId, this.runId });
  public final ForeignKey<QGroup> constraint6e222 = createForeignKey(this.groupId, "id");
  public final ForeignKey<QGoogleSearch> constraint6e22267 = createForeignKey(this.googleSearchId, "id");
  public final ForeignKey<QRun> constraint6e22 = createForeignKey(this.runId, "id");
  public final ForeignKey<QGoogleTarget> constraint6e2226 = createForeignKey(this.googleTargetId, "id");
  
  public QGoogleRank(String variable)
  {
    super(QGoogleRank.class, PathMetadataFactory.forVariable(variable), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGoogleRank(String variable, String schema, String table)
  {
    super(QGoogleRank.class, PathMetadataFactory.forVariable(variable), schema, table);
    addMetadata();
  }
  
  public QGoogleRank(Path<? extends QGoogleRank> path)
  {
    super(path.getType(), path.getMetadata(), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGoogleRank(PathMetadata metadata)
  {
    super(QGoogleRank.class, metadata, "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public void addMetadata()
  {
    addMetadata(this.diff, ColumnMetadata.named("diff").withIndex(7).ofType(5).withSize(5));
    addMetadata(this.googleSearchId, ColumnMetadata.named("google_search_id").withIndex(4).ofType(4).withSize(10).notNull());
    addMetadata(this.googleTargetId, ColumnMetadata.named("google_target_id").withIndex(3).ofType(4).withSize(10).notNull());
    addMetadata(this.groupId, ColumnMetadata.named("group_id").withIndex(2).ofType(4).withSize(10).notNull());
    addMetadata(this.previousRank, ColumnMetadata.named("previous_rank").withIndex(6).ofType(5).withSize(5));
    addMetadata(this.hits, ColumnMetadata.named("hits").withIndex(6).ofType(5).withSize(5));
    addMetadata(this.rank, ColumnMetadata.named("rank").withIndex(5).ofType(5).withSize(5));
    addMetadata(this.runId, ColumnMetadata.named("run_id").withIndex(1).ofType(4).withSize(10).notNull());
    addMetadata(this.url, ColumnMetadata.named("url").withIndex(8).ofType(12).withSize(256));
  }
}


/* Location:              D:\Download\serposcope-2.6.0.jar!\com\serphacker\serposcope\querybuilder\QGoogleRank.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */