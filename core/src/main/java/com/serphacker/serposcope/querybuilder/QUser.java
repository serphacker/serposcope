package com.serphacker.serposcope.querybuilder;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.ForeignKey;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;

import java.sql.Blob;
import java.sql.Timestamp;

public class QUser
  extends RelationalPathBase<QUser>
{
  private static final long serialVersionUID = -1729970537L;
  public static final String TABLE_NAME = "serposcope_user";
  public static final QUser user = new QUser(TABLE_NAME);
  public final BooleanPath admin = createBoolean("admin");
  public final StringPath email = createString("email");
  public final NumberPath<Integer> id = createNumber("id", Integer.class);
  public final DateTimePath<Timestamp> logout = createDateTime("logout", Timestamp.class);
  public final SimplePath<Blob> passwordHash = createSimple("passwordHash", Blob.class);
  public final SimplePath<Blob> passwordSalt = createSimple("passwordSalt", Blob.class);
  public final PrimaryKey<QUser> constraint2 = createPrimaryKey(new Path[] { this.id });
  public final ForeignKey<QUserGroup> _constraintC6 = createInvForeignKey(this.id, "user_id");
  
  public QUser(String variable)
  {
    super(QUser.class, PathMetadataFactory.forVariable(variable), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QUser(String variable, String schema, String table)
  {
    super(QUser.class, PathMetadataFactory.forVariable(variable), schema, table);
    addMetadata();
  }
  
  public QUser(Path<? extends QUser> path)
  {
    super(path.getType(), path.getMetadata(), "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public QUser(PathMetadata metadata)
  {
    super(QUser.class, metadata, "PUBLIC", TABLE_NAME);
    addMetadata();
  }
  
  public void addMetadata()
  {
    addMetadata(this.admin, ColumnMetadata.named("admin").withIndex(5).ofType(16).withSize(1));
    addMetadata(this.email, ColumnMetadata.named("email").withIndex(2).ofType(12).withSize(255));
    addMetadata(this.id, ColumnMetadata.named("id").withIndex(1).ofType(4).withSize(10).notNull());
    addMetadata(this.logout, ColumnMetadata.named("logout").withIndex(6).ofType(93).withSize(23).withDigits(10));
    addMetadata(this.passwordHash, ColumnMetadata.named("password_hash").withIndex(3).ofType(2004).withSize(Integer.MAX_VALUE));
    addMetadata(this.passwordSalt, ColumnMetadata.named("password_salt").withIndex(4).ofType(2004).withSize(Integer.MAX_VALUE));
  }
}


/* Location:              D:\Download\serposcope-2.6.0.jar!\com\serphacker\serposcope\querybuilder\QUser.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */