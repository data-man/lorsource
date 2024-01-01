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
package ru.org.linux.boxlets

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import ru.org.linux.site.Template
import ru.org.linux.topic.BoxletTopicDao

import javax.servlet.http.HttpServletRequest
import scala.jdk.CollectionConverters.*

@Controller
class TopTenBoxlet(topTenDao: BoxletTopicDao) extends AbstractBoxlet {
  @RequestMapping(path = Array("/top10.boxlet"))
  override protected def getData(request: HttpServletRequest): ModelAndView = {
    val profile = Template.getTemplate.getProf
    val list = topTenDao.top10(profile.getMessages)

    new ModelAndView(
      "boxlets/topiclist",
      Map(
        "messages" -> list.asJava,
        "name" -> "Top 10",
        "link" -> null,
        "title" -> "Наиболее обсуждаемые темы этого месяца").asJava)
  }
}