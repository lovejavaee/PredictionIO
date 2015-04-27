/** Copyright 2015 TappingStone, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package io.prediction.data.storage.jdbc

import grizzled.slf4j.Logging
import io.prediction.data.storage.App
import io.prediction.data.storage.Apps
import scalikejdbc._

/** JDBC implementation of Apps. */
class JDBCApps(client: String, database: String) extends Apps with Logging {
  DB autoCommit { implicit session =>
    try {
      sql"""
      create table apps (
        id serial not null primary key,
        name text not null,
        description text)""".execute.apply()
    } catch {
      case e: Exception => debug(e.getMessage, e)
    }
  }

  def insert(app: App): Option[Int] = DB localTx { implicit session =>
    try {
      val q = if (app.id == 0)
        sql"""
        insert into apps (name, description) values(${app.name}, ${app.description})
        """
      else
        sql"""
        insert into apps values(${app.id}, ${app.name}, $app{description}())
        """
      Some(q.updateAndReturnGeneratedKey().apply().toInt)
    } catch {
      case e: Exception =>
        error(e.getMessage, e)
        None
    }
  }

  def get(id: Int): Option[App] = DB readOnly { implicit session =>
    try {
      sql"SELECT id, name, description FROM apps WHERE id = ${id}".map(rs =>
        App(
          id = rs.int("id"),
          name = rs.string("name"),
          description = rs.stringOpt("description"))
      ).single().apply()
    } catch {
      case e: Exception =>
        error(e.getMessage, e)
        None
    }
  }

  def getByName(name: String): Option[App] = DB readOnly { implicit session =>
    try {
      sql"SELECT id, name, description FROM apps WHERE name = ${name}".map(rs =>
        App(
          id = rs.int("id"),
          name = rs.string("name"),
          description = rs.stringOpt("description"))
      ).single().apply()
    } catch {
      case e: Exception =>
        error(e.getMessage, e)
        None
    }
  }

  def getAll(): Seq[App] = DB readOnly { implicit session =>
    try {
      sql"SELECT id, name, description FROM apps".map(rs =>
        App(
          id = rs.int("id"),
          name = rs.string("name"),
          description = rs.stringOpt("description"))
      ).list().apply()
    } catch {
      case e: Exception =>
        error(e.getMessage, e)
        Seq()
    }
  }

  def update(app: App): Boolean = DB localTx { implicit session =>
    try {
      sql"""
      update apps set name = ${app.name}, description = ${app.description}
      where id = ${app.id}""".update().apply()
      true
    } catch {
      case e: Exception =>
        error(e.getMessage, e)
        false
    }
  }

  def delete(id: Int): Boolean = DB localTx { implicit session =>
    try {
      sql"DELETE FROM apps WHERE id = $id".update().apply()
      true
    } catch {
      case e: Exception =>
        error(e.getMessage, e)
        false
    }
  }
}
