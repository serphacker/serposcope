/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.base;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.querydsl.core.Tuple;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLMergeClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.serphacker.serposcope.db.AbstractDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.User;
import com.serphacker.serposcope.querybuilder.QGroup;
import com.serphacker.serposcope.querybuilder.QUser;
import com.serphacker.serposcope.querybuilder.QUserGroup;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.sql.rowset.serial.SerialBlob;

@Singleton
public class UserDB extends AbstractDB {

    @Inject
    GroupDB groupDB;

    QUser t_user = QUser.user;
    QGroup t_group = QGroup.group;
    QUserGroup t_user_group = QUserGroup.userGroup;

    public int insert(User user) {

        int id = -1;
        try (Connection con = ds.getConnection()) {

            id = new SQLInsertClause(con, dbTplConf, t_user)
                .set(t_user.email, user.getEmail())
                .set(t_user.passwordHash, user.getPasswordHash() == null ? null : new SerialBlob(user.getPasswordHash()))
                .set(t_user.passwordSalt, user.getPasswordSalt() == null ? null : new SerialBlob(user.getPasswordSalt()))
                .set(t_user.admin, user.isAdmin())
                .set(t_user.logout, user.getLogout() == null ? null : Timestamp.valueOf(user.getLogout()))
                .executeWithKey(t_user.id)
                ;
              user.setId(id);

        } catch (Exception ex) {
            LOG.error("SQL ex", ex);
        }
        return id;

    }

    public boolean update(User user) {

        boolean updated = false;
        try (Connection con = ds.getConnection()) {

            updated = new SQLUpdateClause(con, dbTplConf, t_user)
                .set(t_user.email, user.getEmail())
                .set(t_user.passwordHash, user.getPasswordHash() == null ? null : new SerialBlob(user.getPasswordHash()))
                .set(t_user.passwordSalt, user.getPasswordSalt() == null ? null : new SerialBlob(user.getPasswordSalt()))
                .set(t_user.admin, user.isAdmin())
                .set(t_user.logout, user.getLogout() == null ? null : Timestamp.valueOf(user.getLogout()))
                .where(t_user.id.eq(user.getId()))
                .execute() == 1;

        } catch (Exception ex) {
            LOG.error("SQL ex", ex);
        }
        return updated;

    }

    public boolean delete(int id) {

        boolean deleted = false;
        try (Connection con = ds.getConnection()) {

            deleted = new SQLDeleteClause(con, dbTplConf, t_user)
                .where(t_user.id.eq(id))
                .execute() == 1;

        } catch (Exception ex) {
            LOG.error("SQL ex", ex);
        }
        return deleted;

    }

    public List<User> list() {
        List<User> users = new ArrayList<>();

        try (Connection con = ds.getConnection()) {

            List<Tuple> userTuples = new SQLQuery<Void>(con, dbTplConf)
                .select(t_user.all())
                .from(t_user)
                .fetch();

            for (Tuple userTuple : userTuples) {

                User user = fromTuple(userTuple);
                addUserGroup(con, user);
                users.add(user);
            }

        } catch (Exception ex) {
            LOG.error("SQL ex", ex);
        }
        return users;
    }

    public User findById(int id) {
        return find(null, id);
    }

    public User findByEmail(String email) {
        return find(email, null);
    }

    protected User find(String email, Integer id) {

        User user = null;

        if (email == null && id == null) {
            return null;
        }

        try (Connection con = ds.getConnection()) {

            SQLQuery<Tuple> query = new SQLQuery<Void>(con, dbTplConf)
                .select(t_user.all())
                .from(t_user);

            if (email != null) {
                query.where(t_user.email.eq(email));
            }

            if (id != null) {
                query.where(t_user.id.eq(id));
            }

            Tuple tuple = query.fetchOne();

            user = fromTuple(tuple);
            addUserGroup(con, user);

        } catch (Exception ex) {
            LOG.error("SQL ex", ex);
        }

        return user;
    }

    protected void addUserGroup(Connection con, User user) {
        if(user == null){
            return;
        }
        
        List<Tuple> groupTuples = new SQLQuery<Void>(con, dbTplConf)
            .select(t_group.all())
            .from(t_group)
            .join(t_user_group)
            .on(t_group.id.eq(t_user_group.groupId))
            .where(t_user_group.userId.eq(user.getId()))
            .fetch();

        for (Tuple groupTuple : groupTuples) {
            user.addGroup(groupDB.fromTuple(groupTuple));
        }
    }

    public boolean hasAdmin() {
        boolean hasAdmin = false;
        try (Connection con = ds.getConnection()) {

            hasAdmin = new SQLQuery<Void>(con, dbTplConf)
                .select(t_user.all())
                .from(t_user)
                .where(t_user.admin.isTrue())
                .fetchFirst() != null;

        } catch (Exception ex) {
            LOG.error("SQL ex", ex);
        }

        return hasAdmin;
    }

    public boolean addPerm(User user, Group group) {
        boolean updated = false;
        try (Connection con = ds.getConnection()) {
            updated = new SQLMergeClause(con, dbTplConf, t_user_group)
                .set(t_user_group.userId, user.getId())
                .set(t_user_group.groupId, group.getId())
                .execute() == 1;
        } catch (Exception ex) {
            LOG.error("SQL ex", ex);
        }
        return updated;
    }

    public boolean delPerm(User user, Group group) {
        boolean updated = false;
        try (Connection con = ds.getConnection()) {
            updated = new SQLDeleteClause(con, dbTplConf, t_user_group)
                .where(t_user_group.userId.eq(user.getId()))
                .where(t_user_group.groupId.eq(group.getId()))
                .execute() == 1;
        } catch (Exception ex) {
            LOG.error("SQL ex", ex);
        }
        return updated;
    }
    
    public boolean delPerm(Group group) {
        boolean updated = false;
        try (Connection con = ds.getConnection()) {
            updated = new SQLDeleteClause(con, dbTplConf, t_user_group)
                .where(t_user_group.groupId.eq(group.getId()))
                .execute() == 1;
        } catch (Exception ex) {
            LOG.error("SQL ex", ex);
        }
        return updated;
    }
    
    public boolean delPerm(User user) {
        boolean updated = false;
        try (Connection con = ds.getConnection()) {
            updated = new SQLDeleteClause(con, dbTplConf, t_user_group)
                .where(t_user_group.userId.eq(user.getId()))
                .execute() == 1;
        } catch (Exception ex) {
            LOG.error("SQL ex", ex);
        }
        return updated;
    }    
    
    public boolean hasPerm(User user, Group group) {
        boolean updated = false;
        try (Connection con = ds.getConnection()) {
            updated = new SQLQuery<Void>(con, dbTplConf)
                .select(t_user_group.all())
                .from(t_user_group)
                .where(t_user_group.userId.eq(user.getId()))
                .where(t_user_group.groupId.eq(group.getId()))
                .fetchOne()!= null;
        } catch (Exception ex) {
            LOG.error("SQL ex", ex);
        }
        return updated;
    }    

    protected User fromTuple(Tuple tuple) throws SQLException {

        if (tuple == null) {
            return null;
        }
        
        User user = new User();
        user.setId(tuple.get(t_user.id));
        user.setEmail(tuple.get(t_user.email));
        Blob passwordHash = tuple.get(t_user.passwordHash);
        if(passwordHash != null){
            user.setPasswordHash(passwordHash.getBytes(1,(int)passwordHash.length()));
        }
        Blob passwordSalt = tuple.get(t_user.passwordSalt);
        if(passwordSalt != null){
            user.setPasswordSalt(passwordSalt.getBytes(1,(int)passwordSalt.length()));
        }        
        user.setAdmin(tuple.get(t_user.admin));
        user.setLogout(tuple.get(t_user.logout) == null ? null : tuple.get(t_user.logout).toLocalDateTime());

        return user;
    }

}
