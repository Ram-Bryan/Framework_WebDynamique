Doit avoir des roles (etudiant, profs) pour pouvoir executer CETTE methode. peut avoir plusieurs roles une seule methode.


regarder dans session l'utilisateur connect

Annotation: 
Classe User: liste des roles, String[]
instancer dans session

Mettre dans la session quel est son profil
Variable pour rol
Regarder dans session si existe
Comparer valeur (avec Annotation)

Resume:
avant: variable pour stocker la session de l'utilisateur
mtn: 
ajouter variable pour role de l'utilisateru conencte
@Role
On peut reutiliser @Auth 
methode d'action avec plusieur roles ==> est ce que la variable role existe, prendr el eno de la varibale, estce que ca exits dans session => recuperer valeur et regarder les param de l'annotation, si y a alors excute sinon, no

On peut ne pas mettre de @Auth, mais implicitement il doti tujours etre conencte avant d appeler une methode d action