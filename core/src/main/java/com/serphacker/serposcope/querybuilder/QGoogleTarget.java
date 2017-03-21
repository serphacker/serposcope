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

public class QGoogleTarget
  extends RelationalPathBase<QGoogleTarget>
{
  private static final long serialVersionUID = -1996639242L;
  public static final String TABLE_NAME = "serposcope_google_target";
  public static final QGoogleTarget googleTarget = new QGoogleTarget(TABLE_NAME);
  public final NumberPath<Integer> groupId = createNumber("groupId", Integer.class);
  public final NumberPath<Integer> id = createNumber("id", Integer.class);
  public final StringPath name = createString("name");
  public final StringPath pattern = createString("pattern");
  public final NumberPath<Byte> patternType = createNumber("patternType", Byte.class);
  public final PrimaryKey<QGoogleTarget> constraint71 = createPrimaryKey(new Path[] { this.id });
  public final ForeignKey<QGroup> constraint719 = createForeignKey(this.groupId, "id");
  public final ForeignKey<QGoogleRankBest> _constraintB72 = createInvForeignKey(this.id, "google_target_id");
  public final ForeignKey<QGoogleTargetSummary> _constraint41d3 = createInvForeignKey(this.id, "google_target_id");
  public final ForeignKey<QGoogleRank> _constraint6e2226 = createInvForeignKey(this.id, "google_target_id");
  
  public QGoogleTarget(String variable)
  {
    super(QGoogleTarget.class, PathMetadataFactory.forVariable(variable), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGoogleTarget(String variable, String schema, String table)
  {
    super(QGoogleTarget.class, PathMetadataFactory.forVariable(variable), schema, table);
    addMetadata();
  }
  
  public QGoogleTarget(Path<? extends QGoogleTarget> path)
  {
    super(path.getType(), path.getMetadata(), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGoogleTarget(PathMetadata metadata)
  {
    super(QGoogleTarget.class, metadata, "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public void addMetadata()
  {
    addMetadata(this.groupId, ColumnMetadata.named("group_id").withIndex(2).ofType(4).withSize(10));
    addMetadata(this.id, ColumnMetadata.named("id").withIndex(1).ofType(4).withSize(10).notNull());
    addMetadata(this.name, ColumnMetadata.named("name").withIndex(3).ofType(12).withSize(255));
    addMetadata(this.pattern, ColumnMetadata.named("pattern").withIndex(5).ofType(12).withSize(255));
    addMetadata(this.patternType, ColumnMetadata.named("pattern_type").withIndex(4).ofType(-6).withSize(3));
  }
}


/* Location:              D:\Download\serposcope-2.6.0.jar!\com\serphacker\serposcope\querybuilder\QGoogleTarget.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */