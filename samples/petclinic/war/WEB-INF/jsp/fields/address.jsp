<%@ include file="/WEB-INF/jsp/includes.jsp" %>

<B>Address:</B>
<i21:bind path="command.address">
    <FONT color="red">
        <B><c:out value="${status.errorMessage}"/></B>
    </FONT>
    <BR><INPUT type="text" maxlength="255" size="30" name="address" value="<c:out value="${status.value}"/>">
</i21:bind>
<P>
