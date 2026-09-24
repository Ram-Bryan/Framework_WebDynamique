Web API framework :
peut retourner du json 

methode qui retourne du json (pas dans View):

Ajouter une annotation qui ne va pas dans la vue mais retourne json, niveau methode.
@WebAPI
Tester l existance de l'annotation, avant dispatch if else --> dispatch ou json: content type application/json, printwriter. Avec body:

Methode 1:
return Object, librairie en json

Methode 2:
return String, pas de transformation.

If other than String (peut importe, listes, object,...)  Tout Json

Bonus:
Dans l'annotation: attribut true false (deja json, ou non ?)