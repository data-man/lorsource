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
package ru.org.linux.section

import org.springframework.stereotype.Service

@Service
class SectionService(sectionDao: SectionDao) {
  val sections: Seq[Section] = sectionDao.getAllSections
  val nameToSection: Map[String, Section] = sections.map(section => section.getUrlName -> section).toMap
  val idToSection: Map[Int, Section] = sections.map(section => section.getId -> section).toMap

  val fuzzyNameToSection: Map[String, Section] = nameToSection ++ idToSection.map { case (k, v) => k.toString -> v }

  /**
   * Получить идентификатор секции по url-имени.
   *
   * @param name название секции
   * @return объект секции
   * @throws SectionNotFoundException если секция не найдена
   */
  def getSectionByName(name: String): Section = nameToSection.getOrElse(name, throw new SectionNotFoundException)

  /**
   * Получить объект секции по идентификатору секции.
   *
   * @param id идентификатор секции
   * @return объект секции
   * @throws SectionNotFoundException если секция не найдена
   */
  def getSection(id: Int): Section = idToSection.getOrElse(id, throw new SectionNotFoundException)

  /**
   * Получить тип "листания" между страницами.
   *
   * @param sectionId  идентификатор секции
   * @return тип "листания" между страницами
   * @throws SectionNotFoundException
   */
  def getScrollMode(sectionId: Int): SectionScrollModeEnum = getSection(sectionId).getScrollMode
}
