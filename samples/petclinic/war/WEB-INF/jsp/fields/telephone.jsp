<%@ include file="/WEB-INF/jsp/includes.jsp" %>

<B>Telephone:</B>
<i21:bind path="command.telephone">
    <FONT color="red">
        <B><c:out value="${status.errorMessage}"/></B>
    </FONT>
    <BR><INPUT type="text" maxlength="20" size="20" name="telephone" value="<c:out value="${status.value}"/>">
</i21:bind>
<P>
