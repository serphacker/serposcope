package com.serphacker.serposcope.querybuilder;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;

public class QGoogleSearchGroup
  extends RelationalPathBase<QGoogleSearchGroup>
{
  private static final long serialVersionUID = 77356658L;
  public static final String TABLE_NAME = "serposcope_google_search_group";
  public static final QGoogleSearchGroup googleSearchGroup = new QGoogleSearchGroup(TABLE_NAME);
  public final NumberPath<Integer> googleSearchId = createNumber("googleSearchId", Integer.class);
  public final NumberPath<Integer> groupId = createNumber("groupId", Integer.class);
  public final PrimaryKey<QGoogleSearchGroup> constraint13 = createPrimaryKey(new Path[] { this.googleSearchId, this.groupId });
  public final ForeignKey<QGoogleSearch> constraint1359 = createForeignKey(this.googleSearchId, "id");
  public final ForeignKey<QGroup> constraint135 = createForeignKey(this.groupId, "id");
  
  public QGoogleSearchGroup(String variable)
  {
    super(QGoogleSearchGroup.class, PathMetadataFactory.forVariable(variable), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGoogleSearchGroup(String variable, String schema, String table)
  {
    super(QGoogleSearchGroup.class, PathMetadataFactory.forVariable(variable), schema, table);
    addMetadata();
  }
  
  public QGoogleSearchGroup(Path<? extends QGoogleSearchGroup> path)
  {
    super(path.getType(), path.getMetadata(), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGoogleSearchGroup(PathMetadata metadata)
  {
    super(QGoogleSearchGroup.class, metadata, "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public void addMetadata()
  {
    addMetadata(this.googleSearchId, ColumnMetadata.named("google_search_id").withIndex(1).ofType(4).withSize(10).notNull());
    addMetadata(this.groupId, ColumnMetadata.named("group_id").withIndex(2).ofType(4).withSize(10).notNull());
  }
}


/* Location:              D:\Download\serposcope-2.6.0.jar!\com\serphacker\serposcope\querybuilder\QGoogleSearchGroup.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */