#### Sprint0:
**Framwork**
Creer servlet FrontConrollerServlet:
Toutes les requetes de l'application passe par FrontConrollerServlet--> doGet/doPost --> processRequest --> println(request.getRawUrl())

--> faire jar et mettre dans calsspath.

**Test**
Prendre .jar et mettre dans classpath du projet.
Dans web.xml, declarer le FrontController. Donner le full name (path)
/ ou /* --> Url param vont tous vers FrontController