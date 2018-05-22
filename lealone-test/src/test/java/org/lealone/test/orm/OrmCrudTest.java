/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lealone.test.orm;

import java.util.List;

import org.lealone.test.SqlScript;
import org.lealone.test.UnitTestBase;
import org.lealone.test.generated.model.User;

import io.vertx.core.json.JsonObject;

public class OrmCrudTest extends UnitTestBase {

    public static void main(String[] args) {
        new OrmCrudTest().runTest();
    }

    @Override
    public void test() {
        SqlScript.createUserTable(this);
        crud();
        json();
    }

    void json() {
        User u = new User();
        u.name.set("Rob");

        JsonObject json = JsonObject.mapFrom(u);
        String str = json.encode();
        System.out.println("json: " + str);

        u = new JsonObject(str).mapTo(User.class);
        assertEquals("Rob", u.name.get());
    }

    void crud() {
        System.out.println("crud test");
        String url = getURL();
        System.out.println("jdbc url: " + url);

        long rowId1;
        long rowId2;

        User dao = User.dao;

        User u = User.create(url);

        // 增加两条记录
        rowId1 = u.id.set(1000).name.set("Rob1").notes.set("notes1").insert();

        assertTrue((rowId1 == 1) && (rowId1 == u._rowid_.get()));

        rowId2 = u.id.set(2000).name.set("Rob2").notes.set("notes2").insert();

        assertTrue((rowId2 == 2) && (rowId2 == u._rowid_.get()));

        // 以下出现的where()都不是必须的，加上之后更像SQL

        // 查找单条记录
        // select * from user where id = 1000;
        u = dao.where().id.eq(1000L).findOne();

        assertTrue((u.id.get() == 1000) && (1 == u._rowid_.get()));

        // 查找多条记录(取回所有字段)
        // select * from user where name like 'Rob%';
        List<User> customers = dao.where().name.like("Rob%").findList();

        assertEquals(2, customers.size());
        assertNotNull(customers.get(0).notes.get());

        // 查找多条记录(只取回name字段)
        // select name from user where name like 'Rob%';
        customers = dao.select(dao.name).where().name.like("Rob%").findList();

        assertEquals(2, customers.size());
        assertNull(customers.get(0).notes.get());

        // 统计行数
        // select count(*) from user where name like 'Rob%';
        int count = dao.where().name.like("Rob%").findCount();

        assertEquals(2, count);

        assertEquals(1, u._rowid_.get());

        // 更新单条记录
        // update user set notes = 'Doing an update' where _rowid_ = 1;
        u.notes.set("Doing an update");
        count = u.update();

        assertEquals(1, count);

        // 批量更新记录
        // update user set phone = 12345678, notes = 'Doing a batch update' where name like 'Rob%';
        count = dao.phone.set(12345678).notes.set("Doing a batch update").where().name.like("Rob%").update();

        assertEquals(2, count);

        // 删除单条记录
        // delete from user where _rowid_ = 1;
        count = u.delete();

        assertEquals(1, count);

        // 批量删除记录
        // delete from user where name like 'Rob%';
        count = dao.where().name.like("Rob%").delete();

        assertEquals(1, count);
    }
}
