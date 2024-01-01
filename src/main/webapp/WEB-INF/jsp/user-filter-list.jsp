<%@ page contentType="text/html; charset=utf-8"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="lor" %>
<%--
  ~ Copyright 1998-2024 Linux.org.ru
  ~    Licensed under the Apache License, Version 2.0 (the "License");
  ~    you may not use this file except in compliance with the License.
  ~    You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~    Unless required by applicable law or agreed to in writing, software
  ~    distributed under the License is distributed on an "AS IS" BASIS,
  ~    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~    See the License for the specific language governing permissions and
  ~    limitations under the License.
  --%>
<%--@elvariable id="ignoreList" type="java.util.Map<Integer, User>"--%>
<%--@elvariable id="ignoreRemarks" type="java.util.Map<Integer, Remark>"--%>
<jsp:include page="/WEB-INF/jsp/head.jsp"/>

<%
  response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate");
  response.addHeader("Pragma", "no-cache");
%>
<title>Фильтрация сообщений</title>

<script type="text/javascript">
  $script.ready("jquery", function() {
    $script("/js/jquery-ui-1.13.2.custom/jquery-ui.min.js", "jqueryui");
  });
  $script.ready("jqueryui", function() {
    $script("/js/tagsAutocomplete.js");
  });
</script>
<link rel="stylesheet" href="/js/jquery-ui-1.13.2.custom/jquery-ui.min.css">
<jsp:include page="/WEB-INF/jsp/header.jsp"/>

<h1>Фильтрация сообщений</h1>

<c:if test="${empty newFavoriteTagName and empty newIgnoreTagName}">

<fieldset>
<legend>Список игнорируемых пользователей</legend>
<form action="<c:url value="/user-filter/ignore-user"/>" method="POST">
  <lor:csrf/>

  <label>Ник: <input type="text" name="nick" size="20" maxlength="80"></label>
    <button type="submit" name="add" class="btn btn-default">Добавить</button>
</form>

<c:if test="${fn:length(ignoreList)>0}">
  <ul>
    <c:forEach var="item" items="${ignoreList}">
      <li>
        <form action="<c:url value="/user-filter/ignore-user"/>" method="POST">
          <lor:csrf/>
          <input type="hidden" name="id" value="${item.key}">
          <span style="white-space: nowrap"><img alt="" src="/img/tuxlor.png"><lor:user user="${item.value}" link="true"/> </span>
          <c:if test="${not empty ignoreRemarks[item.key]}">
            <c:out escapeXml="true" value="${ignoreRemarks[item.key].text}"/>
          </c:if>
          <button type="submit" name="del" class="btn btn-default btn-small">Удалить</button>
        </form>
      </li>
    </c:forEach>
  </ul>
</c:if>
</fieldset>

</c:if>

<br />

<c:if test="${empty newIgnoreTagName}">

<fieldset>
<legend>Список избранных тегов</legend>
<form action="<c:url value="/user-filter/favorite-tag"/>" method="POST">
  <lor:csrf/>
  <label>Тег: <input autocapitalize="off" data-tags-autocomplete="data-tags-autocomplete" type="text" name="tagName" id="newFavoriteTagName" size="20" maxlength="80" value="${fn:escapeXml(newFavoriteTagName)}"></label>
  <button type="submit" name="add" class="btn btn-default">Добавить</button>
  <c:if test="${favoriteTagAddError != null}"><div class="error">
  <c:forEach var="tagAddError" items="${favoriteTagAddError}">
    ${tagAddError}<br />
  </c:forEach>
  </div></c:if>
</form>

<c:if test="${fn:length(favoriteTags)>0}">
  <ul>
    <c:forEach var="tagName" items="${favoriteTags}">
      <li>
        <form action="<c:url value="/user-filter/favorite-tag"/>" method="POST">
          <lor:csrf/>
          <input type="hidden" name="tagName" value="${tagName}">
          <span style="white-space: nowrap">${tagName}</span>
          <button type="submit" name="del" class="btn btn-default btn-small">Удалить</button>
        </form>
      </li>
    </c:forEach>
  </ul>
</c:if>
</fieldset>
</c:if>

<br>

<c:if test="${empty newFavoriteTagName}">
<fieldset>
<legend>Список игнорируемых тегов</legend>
<c:choose>
<c:when test="${isModerator}">
Модераторам нельзя игнорировать теги
</c:when>

<c:otherwise>
<form action="<c:url value="/user-filter/ignore-tag"/>" method="POST">
  <lor:csrf/>
  <label>Тег: <input type="text" name="tagName" autocapitalize="off" data-tags-autocomplete="data-tags-autocomplete" id="newIgnoreTagName" size="20" maxlength="80" value="${fn:escapeXml(newIgnoreTagName)}"></label>
  <button type="submit" name="add" class="btn btn-default">Добавить</button>
  <c:if test="${ignoreTagAddError != null}"><div class="error">
  <c:forEach var="tagAddError" items="${ignoreTagAddError}">
    ${tagAddError}<br />
  </c:forEach>
  </div></c:if>
</form>

<c:if test="${fn:length(ignoreTags)>0}">
  <ul>
    <c:forEach var="tagName" items="${ignoreTags}">
      <li>
        <form action="<c:url value="/user-filter/ignore-tag"/>" method="POST">
          <lor:csrf/>
          <input type="hidden" name="tagName" value="${tagName}">
          <span style="white-space: nowrap">${tagName}</span>
          <button type="submit" name="del" class="btn btn-default btn-small">Удалить</button>
        </form>
      </li>
    </c:forEach>
  </ul>
</c:if>
</c:otherwise>
</c:choose>
</fieldset>
</c:if>

<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
