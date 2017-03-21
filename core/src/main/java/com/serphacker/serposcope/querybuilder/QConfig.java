package com.serphacker.serposcope.querybuilder;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;

public class QConfig
  extends RelationalPathBase<QConfig>
{
  private static final long serialVersionUID = -868101362L;
  public static final String TABLE_NAME = "serposcope_config";
  public static final QConfig config = new QConfig(TABLE_NAME);
  public final StringPath name = createString("name");
  public final StringPath value = createString("value");
  public final PrimaryKey<QConfig> constraint7 = createPrimaryKey(new Path[] { this.name });
  
  public QConfig(String variable)
  {
    super(QConfig.class, PathMetadataFactory.forVariable(variable), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QConfig(String variable, String schema, String table)
  {
    super(QConfig.class, PathMetadataFactory.forVariable(variable), schema, table);
    addMetadata();
  }
  
  public QConfig(Path<? extends QConfig> path)
  {
    super(path.getType(), path.getMetadata(), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QConfig(PathMetadata metadata)
  {
    super(QConfig.class, metadata, "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public void addMetadata()
  {
    addMetadata(this.name, ColumnMetadata.named("name").withIndex(1).ofType(12).withSize(255).notNull());
    addMetadata(this.value, ColumnMetadata.named("value").withIndex(2).ofType(2005).withSize(Integer.MAX_VALUE));
  }
}


/* Location:              D:\Download\serposcope-2.6.0.jar!\com\serphacker\serposcope\querybuilder\QConfig.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */