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

public class QGoogleTargetSummary
  extends RelationalPathBase<QGoogleTargetSummary>
{
  private static final long serialVersionUID = 161696464L;
  public static final String TABLE_NAME = "serposcope_google_target_summary";
  public static final QGoogleTargetSummary googleTargetSummary = new QGoogleTargetSummary(TABLE_NAME);
  public final NumberPath<Integer> googleTargetId = createNumber("googleTargetId", Integer.class);
  public final NumberPath<Integer> groupId = createNumber("groupId", Integer.class);
  public final NumberPath<Integer> previousScoreBasisPoint = createNumber("previousScoreBasisPoint", Integer.class);
  public final NumberPath<Integer> runId = createNumber("runId", Integer.class);
  public final NumberPath<Integer> scoreBasisPoint = createNumber("scoreBasisPoint", Integer.class);
  public final NumberPath<Integer> scoreRaw = createNumber("scoreRaw", Integer.class);
  public final StringPath topImprovements = createString("topImprovements");
  public final StringPath topLosts = createString("topLosts");
  public final StringPath topRanks = createString("topRanks");
  public final NumberPath<Integer> totalOut = createNumber("totalOut", Integer.class);
  public final NumberPath<Integer> totalTop10 = createNumber("totalTop10", Integer.class);
  public final NumberPath<Integer> totalTop100 = createNumber("totalTop100", Integer.class);
  public final NumberPath<Integer> totalTop3 = createNumber("totalTop3", Integer.class);
  public final PrimaryKey<QGoogleTargetSummary> constraint41 = createPrimaryKey(new Path[] { this.googleTargetId, this.groupId, this.runId });
  public final ForeignKey<QRun> constraint41d36 = createForeignKey(this.runId, "id");
  public final ForeignKey<QGoogleTarget> constraint41d3 = createForeignKey(this.googleTargetId, "id");
  public final ForeignKey<QGroup> constraint41d = createForeignKey(this.groupId, "id");
  
  public QGoogleTargetSummary(String variable)
  {
    super(QGoogleTargetSummary.class, PathMetadataFactory.forVariable(variable), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGoogleTargetSummary(String variable, String schema, String table)
  {
    super(QGoogleTargetSummary.class, PathMetadataFactory.forVariable(variable), schema, table);
    addMetadata();
  }
  
  public QGoogleTargetSummary(Path<? extends QGoogleTargetSummary> path)
  {
    super(path.getType(), path.getMetadata(), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGoogleTargetSummary(PathMetadata metadata)
  {
    super(QGoogleTargetSummary.class, metadata, "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public void addMetadata()
  {
    addMetadata(this.googleTargetId, ColumnMetadata.named("google_target_id").withIndex(2).ofType(4).withSize(10).notNull());
    addMetadata(this.groupId, ColumnMetadata.named("group_id").withIndex(1).ofType(4).withSize(10).notNull());
    addMetadata(this.previousScoreBasisPoint, ColumnMetadata.named("previous_score_basis_point").withIndex(13).ofType(4).withSize(10));
    addMetadata(this.runId, ColumnMetadata.named("run_id").withIndex(3).ofType(4).withSize(10).notNull());
    addMetadata(this.scoreBasisPoint, ColumnMetadata.named("score_basis_point").withIndex(12).ofType(4).withSize(10));
    addMetadata(this.scoreRaw, ColumnMetadata.named("score_raw").withIndex(11).ofType(4).withSize(10));
    addMetadata(this.topImprovements, ColumnMetadata.named("top_improvements").withIndex(9).ofType(12).withSize(128));
    addMetadata(this.topLosts, ColumnMetadata.named("top_losts").withIndex(10).ofType(12).withSize(128));
    addMetadata(this.topRanks, ColumnMetadata.named("top_ranks").withIndex(8).ofType(12).withSize(128));
    addMetadata(this.totalOut, ColumnMetadata.named("total_out").withIndex(7).ofType(4).withSize(10));
    addMetadata(this.totalTop10, ColumnMetadata.named("total_top_10").withIndex(5).ofType(4).withSize(10));
    addMetadata(this.totalTop100, ColumnMetadata.named("total_top_100").withIndex(6).ofType(4).withSize(10));
    addMetadata(this.totalTop3, ColumnMetadata.named("total_top_3").withIndex(4).ofType(4).withSize(10));
  }
}


/* Location:              D:\Download\serposcope-2.6.0.jar!\com\serphacker\serposcope\querybuilder\QGoogleTargetSummary.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */