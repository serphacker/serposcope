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

public class QGoogleSearch
  extends RelationalPathBase<QGoogleSearch>
{
  private static final long serialVersionUID = -2022070259L;
  public static final String TABLE_NAME = "serposcope_google_search";
  public static final QGoogleSearch googleSearch = new QGoogleSearch(TABLE_NAME);
  public final StringPath customParameters = createString("customParameters");
  public final StringPath datacenter = createString("datacenter");
  public final NumberPath<Byte> device = createNumber("device", Byte.class);
  public final NumberPath<Integer> id = createNumber("id", Integer.class);
  public final StringPath keyword = createString("keyword");
  public final StringPath local = createString("local");
  public final StringPath tld = createString("tld");
  public final PrimaryKey<QGoogleSearch> constraint70 = createPrimaryKey(new Path[] { this.id });
  public final ForeignKey<QGoogleSearchGroup> _constraint1359 = createInvForeignKey(this.id, "google_search_id");
  public final ForeignKey<QGoogleRank> _constraint6e22267 = createInvForeignKey(this.id, "google_search_id");
  public final ForeignKey<QGoogleSerp> _constraint6e = createInvForeignKey(this.id, "google_search_id");
  public final ForeignKey<QGoogleRankBest> _constraintB727 = createInvForeignKey(this.id, "google_search_id");
  
  public QGoogleSearch(String variable)
  {
    super(QGoogleSearch.class, PathMetadataFactory.forVariable(variable), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGoogleSearch(String variable, String schema, String table)
  {
    super(QGoogleSearch.class, PathMetadataFactory.forVariable(variable), schema, table);
    addMetadata();
  }
  
  public QGoogleSearch(Path<? extends QGoogleSearch> path)
  {
    super(path.getType(), path.getMetadata(), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QGoogleSearch(PathMetadata metadata)
  {
    super(QGoogleSearch.class, metadata, "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public void addMetadata()
  {
    addMetadata(this.customParameters, ColumnMetadata.named("custom_parameters").withIndex(7).ofType(12).withSize(255));
    addMetadata(this.datacenter, ColumnMetadata.named("datacenter").withIndex(4).ofType(12).withSize(64));
    addMetadata(this.device, ColumnMetadata.named("device").withIndex(5).ofType(-6).withSize(3));
    addMetadata(this.id, ColumnMetadata.named("id").withIndex(1).ofType(4).withSize(10).notNull());
    addMetadata(this.keyword, ColumnMetadata.named("keyword").withIndex(2).ofType(12).withSize(255).notNull());
    addMetadata(this.local, ColumnMetadata.named("local").withIndex(6).ofType(12).withSize(64));
    addMetadata(this.tld, ColumnMetadata.named("tld").withIndex(3).ofType(12).withSize(16));
  }
}


/* Location:              D:\Download\serposcope-2.6.0.jar!\com\serphacker\serposcope\querybuilder\QGoogleSearch.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */