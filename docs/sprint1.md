Creer une annotation sur la classe de Controller (pas de variable) @Controller 

Dans Framework:
pkg mg.itu.anaranaaprojet.annotation.Controller 

Dans Test:

@Controller 
TestController   

Toutes classe avec @Controller --> stocker les Controllers:

1- soit au demarrage de l'app:
Utiliser Listener. Event: au demarrage --> code execute

2- soit au premier appel FrontServlet:
utiliser fonction init()


Le code:
Connaitre tous les Controller de l'app
Attribut LIst<String> listController:
Si init():
    - initialiser listeController
    - prendre la valeur dans web.xml
    - parcourir toutes les classes/Ou Seulement dans un package specifique:
 Tous tester si y a @Controller 
    - prendre le name et add dans listeController.
  - processRequest:
    - boucler les classes et print classes 
- par convention
- par configuration:
    annotation
    fichier de config

    dans web.xml: key, value (nom variable, package).

    class Utils dans frameworks:
        - getListClass(package)
        - getClassTHatHasthisAnnotatoin(package, annotation, attribut/classe/methode) 
        - methode annotationExist (package, annotation, attribut/classe/methode) --> does this annotation exist ?
        - methode parcourir le package

Si Listener:
    - declarer dans web.xml dans le test.        