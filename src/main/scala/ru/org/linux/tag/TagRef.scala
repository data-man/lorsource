/*
 * Copyright 1998-2024 Linux.org.ru
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ru.org.linux.tag

import scala.beans.BeanProperty
import scala.jdk.CollectionConverters._
import scala.collection.Seq

case class TagRef(@BeanProperty name: String, @BeanProperty url: Option[String]) extends Ordered[TagRef] {
  def compare(that: TagRef): Int = name.compareTo(that.name)
}

object TagRef {
  def names(list: Seq[TagRef]): String = list.map(_.name).mkString(",")

  def names(list: java.util.List[TagRef]): String = names(list.asScala)
}
