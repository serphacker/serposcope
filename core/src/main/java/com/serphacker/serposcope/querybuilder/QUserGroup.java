package com.serphacker.serposcope.querybuilder;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;

public class QUserGroup
  extends RelationalPathBase<QUserGroup>
{
  private static final long serialVersionUID = -487731672L;
  public static final String TABLE_NAME = "serposcope_user_group";
  public static final QUserGroup userGroup = new QUserGroup(TABLE_NAME);
  public final NumberPath<Integer> groupId = createNumber("groupId", Integer.class);
  public final NumberPath<Integer> userId = createNumber("userId", Integer.class);
  public final PrimaryKey<QUserGroup> constraintC = createPrimaryKey(new Path[] { this.groupId, this.userId });
  public final ForeignKey<QGroup> constraintC62 = createForeignKey(this.groupId, "id");
  public final ForeignKey<QUser> constraintC6 = createForeignKey(this.userId, "id");
  
  public QUserGroup(String variable)
  {
    super(QUserGroup.class, PathMetadataFactory.forVariable(variable), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QUserGroup(String variable, String schema, String table)
  {
    super(QUserGroup.class, PathMetadataFactory.forVariable(variable), schema, table);
    addMetadata();
  }
  
  public QUserGroup(Path<? extends QUserGroup> path)
  {
    super(path.getType(), path.getMetadata(), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QUserGroup(PathMetadata metadata)
  {
    super(QUserGroup.class, metadata, "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public void addMetadata()
  {
    addMetadata(this.groupId, ColumnMetadata.named("group_id").withIndex(2).ofType(4).withSize(10).notNull());
    addMetadata(this.userId, ColumnMetadata.named("user_id").withIndex(1).ofType(4).withSize(10).notNull());
  }
}


/* Location:              D:\Download\serposcope-2.6.0.jar!\com\serphacker\serposcope\querybuilder\QUserGroup.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */