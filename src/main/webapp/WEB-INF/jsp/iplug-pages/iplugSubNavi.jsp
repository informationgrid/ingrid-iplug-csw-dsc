<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<c:choose>
    <c:when test="${plugdescriptionExists == 'false'}">
        <li
        <c:if test="${active == 'extras'}">
            class="active"
        </c:if>
        >Weitere Einstellungen</li>
    </c:when>
    <c:when test="${active != 'extras'}">
        <li><a href="../base/extras.html">Weitere Einstellungen</a></li>
    </c:when>
    <c:otherwise>
        <li class="active">Weitere Einstellungen</li>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${plugdescriptionExists == 'false'}">
        <li
        <c:if test="${active == 'cswParams'}">
            class="active"
        </c:if>
        >CSW Parameter</li>
    </c:when>
    <c:when test="${active != 'cswParams'}">
        <li><a href="../iplug-pages/cswParams.html">CSW Parameter</a></li>
    </c:when>
    <c:otherwise>
        <li class="active">CSW Parameter</li>
    </c:otherwise>
</c:choose>



