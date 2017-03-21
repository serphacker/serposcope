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

public class QGroup
  extends RelationalPathBase<QGroup>
{
  private static final long serialVersionUID = -2102428365L;
  public static final String TABLE_NAME = "serposcope_group";
  public static final QGroup group = new QGroup(TABLE_NAME);
  public final NumberPath<Integer> id = createNumber("id", Integer.class);
  public final NumberPath<Integer> moduleId = createNumber("moduleId", Integer.class);
  public final StringPath name = createString("name");
  public final PrimaryKey<QGroup> constraint4 = createPrimaryKey(new Path[] { this.id });
  public final ForeignKey<QGoogleRank> _constraint6e222 = createInvForeignKey(this.id, "group_id");
  public final ForeignKey<QGoogleTarget> _constraint719 = createInvForeignKey(this.id, "group_id");
  public final ForeignKey<QUserGroup> _constraintC62 = createInvForeignKey(this.id, "group_id");
  public final ForeignKey<QGoogleTargetSummary> _constraint41d = createInvForeignKey(this.id, "group_id");
  public final ForeignKey<QEvent> _constraint3f = createInvForeignKey(this.id, "group_id");
  public final ForeignKey<QGoogleRankBest> _constraintB7 = createInvForeignKey(this.id, "group_id");
  public final ForeignKey<QGoogleSearchGroup> _constraint135 = createInvForeignKey(this.id, "group_id");
  
  public QGroup(String variable)
  {
    super(QGroup.class, PathMetadataFactory.forVariable(variable), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGroup(String variable, String schema, String table)
  {
    super(QGroup.class, PathMetadataFactory.forVariable(variable), schema, table);
    addMetadata();
  }
  
  public QGroup(Path<? extends QGroup> path)
  {
    super(path.getType(), path.getMetadata(), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGroup(PathMetadata metadata)
  {
    super(QGroup.class, metadata, "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public void addMetadata()
  {
    addMetadata(this.id, ColumnMetadata.named("id").withIndex(1).ofType(4).withSize(10).notNull());
    addMetadata(this.moduleId, ColumnMetadata.named("module_id").withIndex(2).ofType(4).withSize(10));
    addMetadata(this.name, ColumnMetadata.named("name").withIndex(3).ofType(12).withSize(255));
  }
}


/* Location:              D:\Download\serposcope-2.6.0.jar!\com\serphacker\serposcope\querybuilder\QGroup.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */