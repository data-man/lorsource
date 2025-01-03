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
package ru.org.linux.comment

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import ru.org.linux.auth.AuthUtil.ModeratorOnly
import ru.org.linux.user.{UserNotFoundException, UserService}

@Controller
class ShowCommentsController(userService: UserService, commentService: CommentReadService) {
  @RequestMapping(Array("/show-comments.jsp"))
  def showComments(@RequestParam nick: String): RedirectView = {
    val user = userService.getUserCached(nick)
    new RedirectView(s"search.jsp?range=COMMENTS&user=${user.getNick}&sort=DATE")
  }

  @RequestMapping(value = Array("/people/{nick}/deleted-comments"))
  def showDeletedComments(@PathVariable nick: String): ModelAndView = ModeratorOnly { _ =>
    val user = userService.getUserCached(nick)

    val mv = new ModelAndView("deleted-comments")

    mv.getModel.put("user", user)
    mv.getModel.put("deletedList", commentService.getDeletedComments(user))

    mv
  }

  @ExceptionHandler(Array(classOf[UserNotFoundException]))
  @ResponseStatus(HttpStatus.NOT_FOUND)
  def handleUserNotFound: ModelAndView = {
    val mav = new ModelAndView("errors/good-penguin")

    mav.addObject("msgTitle", "Ошибка: пользователя не существует")
    mav.addObject("msgHeader", "Пользователя не существует")
    mav.addObject("msgMessage", "")

    mav
  }
}