@@ -0,0 +1,31 @@
Upload fichier:

Modif 1:
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


Modif 2:
Creer une classe modele:
MapFile: nom du fichier, extension, bytes[]
Instancer dans le framework: si match input + rectoCIN + field de l'objet, avec un type MapFile dans le filed --> instance MapFile(nom, extension, bytes[])
