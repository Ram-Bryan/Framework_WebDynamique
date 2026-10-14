@@ -0,0 +1,31 @@
Upload fichier:

Formulaire:
Map<nom, tableau bytes>

Dans erveur:
- avoir pour que l'info enoye est un File
- File doit etre dnas la methode d'action

action(Map<String/ou extension, tableau de bytes>)

Ex:
classe modele Employe: nom, 

RectCIN tableua de bytes[]
ReCIN tableua de bytes[]

Binding:
si mitovy anarana anle attribut --> binding
avec Map

Creer objets: nom, extension

Comment avoir l'extension ?


conclusion
Creer une classe modele:
MapFile: nom du fichier, extension, bytes[]
Instancer dans le framework: si match input + rectoCIN + field de l'objet, avec un type MapFile dans le filed --> instance MapFile(nom, extension, bytes[])



Dans le web.xml on met;
<context-param>
    <param-name>upload-dir</param-name>
    <param-value>/chemin/absolu/vers/uploads</param-value>
</context-param>

<servlet>
    <servlet-name>FrontControllerServlet</servlet-name>
    <servlet-class>mg.itu.controller.FrontControllerServlet</servlet-class>
    <multipart-config>
        <max-file-size>10485760</max-file-size>        <!-- 10MB -->
        <max-request-size>52428800</max-request-size>  <!-- 50MB -->
        <file-size-threshold>1048576</file-size-threshold>
    </multipart-config>
</servlet>
