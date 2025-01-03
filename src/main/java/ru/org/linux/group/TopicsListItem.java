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

package ru.org.linux.group;

import javax.annotation.Nullable;
import java.sql.Timestamp;

public class TopicsListItem {
  private final int author; // topic author
  private final int msgid; // topic id
  private final Timestamp lastmod; // topic lastmod
  private final int stat1; // comment count
  private final int groupId;
  private final String groupTitle;
  private final String title;
  private final int cid; // tracker only!

  @Nullable
  private final Integer lastCommentBy;

  private final boolean resolved;
  private final int section;
  private final String groupUrlName;
  private final Timestamp postdate; // date of last comment or topic postdate if none
  private final boolean uncommited; // awaits for approve
  private final boolean deleted;
  private final boolean sticky;
  private final int topicPostscore;

  public TopicsListItem(int author, int msgid, Timestamp lastmod, int stat1,
                        int groupId, String groupTitle, String title,
                        int cid, Integer lastCommentBy, boolean resolved,
                        int section, String groupUrlName,
                        Timestamp postdate, boolean uncommited, boolean deleted,
                        boolean sticky, int topicPostscore) {
    this.author = author;
    this.msgid = msgid;
    this.lastmod = lastmod;
    this.stat1 = stat1;
    this.groupId = groupId;
    this.groupTitle = groupTitle;
    this.title = title;
    this.cid = cid;
    this.lastCommentBy = lastCommentBy;
    this.resolved = resolved;
    this.section = section;
    this.groupUrlName = groupUrlName;
    this.postdate = postdate;
    this.uncommited = uncommited;
    this.deleted = deleted;
    this.sticky = sticky;
    this.topicPostscore = topicPostscore;
  }

  public int getMsgid() {
    return msgid;
  }

  public Timestamp getLastmod() {
    return lastmod;
  }

  public int getGroupId() {
    return groupId;
  }

  public String getGroupTitle() {
    return groupTitle;
  }

  public String getTitle() {
    return title;
  }

  public int getTopicAuthor() {
    return author;
  }

  public boolean isResolved() {
    return resolved;
  }

  public int getSection() {
    return section;
  }

  public Timestamp getPostdate() {
    return postdate;
  }

  public boolean isUncommited() {
    return uncommited;
  }

  public int getCommentId() {
    return cid;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public boolean isSticky() {
    return sticky;
  }

  public int getStat1() {
    return stat1;
  }

  @Nullable
  public Integer getLastCommentBy() {
    return lastCommentBy;
  }

  public String getGroupUrlName() {
    return groupUrlName;
  }

  public int getTopicPostscore() {
    return topicPostscore;
  }
}
