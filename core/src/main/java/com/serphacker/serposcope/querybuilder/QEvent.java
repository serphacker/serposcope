package com.serphacker.serposcope.querybuilder;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;

import java.sql.Date;

public class QEvent
  extends RelationalPathBase<QEvent>
{
  private static final long serialVersionUID = -2104166066L;
  public static final String TABLE_NAME = "serposcope_event";
  public static final QEvent event = new QEvent(TABLE_NAME);
  public final DatePath<Date> day = createDate("day", Date.class);
  public final StringPath description = createString("description");
  public final NumberPath<Integer> groupId = createNumber("groupId", Integer.class);
  public final StringPath title = createString("title");
  public final PrimaryKey<QEvent> constraint3 = createPrimaryKey(new Path[] { this.day, this.groupId });
  public final ForeignKey<QGroup> constraint3f = createForeignKey(this.groupId, "id");
  
  public QEvent(String variable)
  {
    super(QEvent.class, PathMetadataFactory.forVariable(variable), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QEvent(String variable, String schema, String table)
  {
    super(QEvent.class, PathMetadataFactory.forVariable(variable), schema, table);
    addMetadata();
  }
  
  public QEvent(Path<? extends QEvent> path)
  {
    super(path.getType(), path.getMetadata(), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QEvent(PathMetadata metadata)
  {
    super(QEvent.class, metadata, "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public void addMetadata()
  {
    addMetadata(this.day, ColumnMetadata.named("day").withIndex(2).ofType(91).withSize(8).notNull());
    addMetadata(this.description, ColumnMetadata.named("description").withIndex(4).ofType(2005).withSize(Integer.MAX_VALUE));
    addMetadata(this.groupId, ColumnMetadata.named("group_id").withIndex(1).ofType(4).withSize(10).notNull());
    addMetadata(this.title, ColumnMetadata.named("title").withIndex(3).ofType(12).withSize(255));
  }
}


/* Location:              D:\Download\serposcope-2.6.0.jar!\com\serphacker\serposcope\querybuilder\QEvent.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */