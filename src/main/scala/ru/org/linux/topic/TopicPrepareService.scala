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
package ru.org.linux.topic

import org.springframework.stereotype.Service
import ru.org.linux.auth.AnySession
import ru.org.linux.edithistory.EditInfoSummary
import ru.org.linux.gallery.{Image, ImageService}
import ru.org.linux.group.{GroupDao, GroupPermissionService}
import ru.org.linux.markup.MessageTextService
import ru.org.linux.poll.{Poll, PollNotFoundException, PollPrepareService, PreparedPoll}
import ru.org.linux.reaction.ReactionService
import ru.org.linux.section.SectionService
import ru.org.linux.spring.SiteConfig
import ru.org.linux.spring.dao.{DeleteInfoDao, MessageText, MsgbaseDao, UserAgentDao}
import ru.org.linux.tag.TagRef
import ru.org.linux.user.*
import ru.org.linux.util.StringUtil
import ru.org.linux.warning.{Warning, WarningService}

import javax.annotation.Nullable
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOptional

@Service
class TopicPrepareService(sectionService: SectionService, groupDao: GroupDao, deleteInfoDao: DeleteInfoDao,
                          pollPrepareService: PollPrepareService, remarkDao: RemarkDao, textService: MessageTextService,
                          siteConfig: SiteConfig, userService: UserService,
                          topicPermissionService: TopicPermissionService,
                          groupPermissionService: GroupPermissionService, topicTagService: TopicTagService,
                          msgbaseDao: MsgbaseDao, imageService: ImageService, userAgentDao: UserAgentDao,
                          reactionPrepareService: ReactionService, ignoreListDao: IgnoreListDao,
                          warningService: WarningService) {
  def prepareTopic(message: Topic, @Nullable user: User): PreparedTopic =
    prepareTopic(message, topicTagService.getTagRefs(message).asScala, minimizeCut = false, None, user,
      msgbaseDao.getMessageText(message.id), None)

  def prepareTopic(message: Topic, tags: java.util.List[TagRef], user: Option[User], text: MessageText,
                   warnings: Seq[Warning]): PreparedTopic =
    prepareTopic(message, tags.asScala, minimizeCut = false, None, user.orNull, text, None, warnings)

  def prepareTopicPreview(message: Topic, tags: Seq[TagRef], newPoll: Option[Poll], text: MessageText,
                          image: Option[Image]): PreparedTopic =
    prepareTopic(message, tags, minimizeCut = false, newPoll.map(pollPrepareService.preparePollPreview),
      null, text, image)

  def prepareEditInfo(editInfo: EditInfoSummary, topic: Topic, currentUserOpt: AnySession): PreparedEditInfoSummary = {
    val lastEditor = userService.getUserCached(editInfo.editor).getNick
    val editCount = editInfo.editCount
    val lastEditDate = editInfo.editdate

    PreparedEditInfoSummary(lastEditor, editCount, lastEditDate,
      showHistory = topicPermissionService.canViewHistory(topic)(currentUserOpt))
  }

  /**
   * Функция подготовки топика
   *
   * @param topic       топик
   * @param tags        список тэгов
   * @param minimizeCut сворачивать ли cut
   * @param poll        опрос к топику
   * @param currentUser        пользователь
   * @return подготовленный топик
   */
  private def prepareTopic(topic: Topic, tags: collection.Seq[TagRef], minimizeCut: Boolean, poll: Option[PreparedPoll],
                           @Nullable currentUser: User, text: MessageText, image: Option[Image],
                           warnings: Seq[Warning] = Seq.empty): PreparedTopic = try {
    val group = groupDao.getGroup(topic.groupId)
    val author = userService.getUserCached(topic.authorUserId)
    val section = sectionService.getSection(topic.sectionId)

    val deleteInfo = if (topic.deleted) {
      deleteInfoDao.getDeleteInfo(topic.id).toScala
    } else {
      None
    }

    val deleteUser = deleteInfo.map(_.userid).map(userService.getUserCached)

    val preparedPoll = if (section.isPollPostAllowed) {
      Some(poll.getOrElse(pollPrepareService.preparePoll(topic, currentUser)))
    } else {
      None
    }

    val commiter = if (topic.commitby != 0) {
      Some(userService.getUserCached(topic.commitby))
    } else {
      None
    }

    val url = s"${siteConfig.getSecureUrlWithoutSlash}${topic.getLink}"
    val processedMessage = textService.renderTopic(text, minimizeCut, !topicPermissionService.followInTopic(topic, author), url)

    val preparedImage = if (section.isImagepost || section.isImageAllowed) {
      val loadedImage = image.orElse {
        if (topic.id != 0) {
          imageService.imageForTopic(topic).toScala
        } else {
          None
        }
      }

      loadedImage.flatMap(imageService.prepareImage)
    } else {
      None
    }

    val remark = if (currentUser != null) {
      remarkDao.getRemark(currentUser, author)
    } else {
      None
    }

    val ignoreList = if (currentUser != null) {
      ignoreListDao.get(currentUser.getId)
    } else {
      Set.empty[Int]
    }

    val postscore = topicPermissionService.getPostscore(group, topic)

    val showRegisterInvite = currentUser == null &&
      (postscore <= 45 &&
        postscore != TopicPermissionService.POSTSCORE_UNRESTRICTED ||
        userService.getAnonymous.isFrozen)

    val userAgent = if (currentUser != null && currentUser.isModerator) {
      userAgentDao.getUserAgentById(topic.userAgentId).toScala
    } else {
      None
    }

    PreparedTopic(topic, author, deleteInfo.orNull, deleteUser.orNull, processedMessage, preparedPoll.orNull,
      commiter.orNull, tags.asJava, group, section, text.markup, preparedImage.orNull,
      TopicPermissionService.getPostScoreInfo(postscore), remark.orNull, showRegisterInvite, userAgent.orNull,
      reactionPrepareService.prepare(topic.reactions, ignoreList, Option(currentUser), topic, None),
      warningService.prepareWarning(warnings).asJava)
  } catch {
    case e: PollNotFoundException =>
      throw new RuntimeException(e)
  }

  /**
   * Подготовка ленты топиков для пользователя
   * сообщения рендерятся со свернутым cut
   *
   * @param messages     список топиков
   * @param user         пользователь
   * @param profile      профиль пользователя
   * @param loadUserpics флаг загрузки аватар
   * @return список подготовленных топиков
   */
  def prepareTopicsForUser(messages: collection.Seq[Topic], profile: Profile,
                           loadUserpics: Boolean)(implicit user: AnySession): java.util.List[PersonalizedPreparedTopic] = {
    val textMap = loadTexts(messages)
    val tags = topicTagService.tagRefs(messages.map(_.id))

    messages.map { message =>
      val preparedMessage = prepareTopic(message, tags.getOrElse(message.id, Seq.empty), minimizeCut = true, None,
        user.userOpt.orNull, textMap(message.id), None)

      val topicMenu = getTopicMenu(preparedMessage, profile, loadUserpics)
      new PersonalizedPreparedTopic(preparedMessage, topicMenu)
    }.asJava
  }

  private def loadTexts(messages: collection.Seq[Topic]) =
    msgbaseDao.getMessageText(messages.map(_.id))

  /**
   * Подготовка ленты топиков, используется в TopicListController например
   * сообщения рендерятся со свернутым cut
   *
   * @param messages список топиков
   * @return список подготовленных топиков
   */
  def prepareTopics(messages: collection.Seq[Topic]): Seq[PreparedTopic] = {
    val textMap = loadTexts(messages)
    val tags = topicTagService.tagRefs(messages.map(_.id))

    messages.view.map { message =>
      prepareTopic(message, tags.getOrElse(message.id, Seq.empty), minimizeCut = true, None, null,
        textMap(message.id), None)
    }.toSeq
  }

  def getTopicMenu(topic: PreparedTopic, profile: Profile, loadUserpics: Boolean)
                  (implicit currentUserOpt: AnySession): TopicMenu = {
    val topicEditable = groupPermissionService.isEditable(topic)
    val tagsEditable = groupPermissionService.isTagsEditable(topic)

    val (resolvable, deletable, undeletable) = currentUserOpt.opt.map  { implicit currentUser =>
      val resolvable = (currentUser.moderator || (topic.author.getId == currentUser.user.getId)) &&
        topic.group.resolvable

      val deletable = groupPermissionService.isDeletable(topic.message)
      val undeletable = groupPermissionService.isUndeletable(topic.message)

      (resolvable, deletable, undeletable)
    }.getOrElse((false, false, false))

    val userpic = if (loadUserpics && profile.isShowPhotos) {
      Some(userService.getUserpic(topic.author, profile.getAvatarMode, misteryMan = true))
    } else {
      None
    }

    val showComments = !topic.message.isCommentsHidden

    TopicMenu(topicEditable = topicEditable, tagsEditable = tagsEditable, resolvable = resolvable,
      commentsAllowed = topicPermissionService.isCommentsAllowed(topic.group, topic.message), deletable = deletable,
      undeletable = undeletable, commitable = groupPermissionService.canCommit(topic.message), userpic = userpic.orNull,
      showComments = showComments, warningsAllowed = topicPermissionService.canPostWarning(topic.message, comment = None))
  }

  def prepareBrief(topic: Topic, groupInTitle: Boolean): BriefTopicRef = {
    val group = groupDao.getGroup(topic.groupId)

    val showComments = !topic.isCommentsHidden

    val commentCount = if (showComments) topic.commentCount else 0

    val groupTitle = if (groupInTitle) {
      Some(group.title)
    } else {
      None
    }

    BriefTopicRef(topic.getLink, StringUtil.processTitle(topic.title), commentCount, groupTitle)
  }
}