<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Error</title>
    <asset:stylesheet src="bootstrap.css"/>
    <asset:javascript src="jquery.js"/>
    <asset:javascript src="bootstrap.js"/>
  </head>
  <body>
    <div class="body">
      <br/><br/>
      <div class="container">
      <g:each in="${errors}" var="error">
        <div class="alert alert-danger">
          ${error[1]}
        </div>
      </g:each>
      </div>
    </div>
    <br/><br/>
  </body>
</html>