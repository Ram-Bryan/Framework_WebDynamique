Doit avoir des roles (etudiant, profs) pour pouvoir executer CETTE methode. peut avoir plusieurs roles une seule methode.

regarder dans session l'utilisateur connecte


Mettre dans la session quel est son profil
Variable pour roles
Regarder dans session si existe
Comparer valeur (avec Annotation)

Resume:
avant: variable pour stocker la session de l'utilisateur
matinenant: 
ajouter variable pour role de l'utilisateru conencte
@Auth avec String[] roles comme attribut
methode d'action avec plusieur roles ==> est ce que la variable role existe, prendr el nom de la varibale, estce que ca exits dans session => recuperer valeur et regarder les param de l'annotation, si y a alors excute sinon non