<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Reset Password</title>
</head>
<body>
    <h2>Reset Password</h2>
    <form action="resetPasswordServlet" method="post">
        <label for="userUuid">User UUID:</label>
        <input type="text" id="userUuid" name="userUuid" required><br><br>
        
        <label for="customPassword">New Password:</label>
        <input type="password" id="customPassword" name="customPassword" required><br><br>
        
        <input type="submit" value="Reset Password">
    </form>
</body>
</html>
