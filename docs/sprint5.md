Dans projet test:

DB ---> 

Gerer par Spring:
Classe Entity
Classe Repository

Sans service 

Repository --> controller 

creer une page liste -->

Step1:
- faire data static de liste --> call function --> render dans une vue

Comment envoyer les donnees ?
Quels vue utiliser ?

ModelView public (ModelAndView)
attrbut Map<String, Object> set Attribute: put de Map
attribut String vue
setUrl();
return MOdelAndView 

Recurperer model And View:
Map et Url.
Concater Url -- chemin vue. dispatcher forward --> boucler les donnees dans Map et requets.tsetAttributes --> request.getAttributes --> loop dans jsp
web.xml: chemin enlever prefix et suffix
Prendre parametre de la methode, si y a un argument type model ---> attribut Map