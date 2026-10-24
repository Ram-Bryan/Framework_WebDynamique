Session:
Comment recupere la session dans le Servlet ?

- instancer controller a chaque fois ? Doit avoir 1 seule instanciation de controleur. Singelton e controller. If instance not exist --> mettre dans contexte (cle, valeur) (nmo controller, objet) --> if not existe --> mettre dans ce contexte. 

- classe Session: Map, fonction pour supprimer sesiion, creer session,...toutes lesa citons necesaire de session, dans el contrlleur

- Si vois dans les parmaetres la session et -> injecter. Recree tojours un objet Session a chque requete/action.
Prendre les valeurs des sessions --> envoye dans le tableau de session.

# Resume:
Framework
Creer classe MySession: Map<String, Object> avec methode utilaire de session (getsession, addSession, supprimerSession,...)

Dans le controller:
Si dans les parametres on a MySession: new Instance of MySession: Map<> est rempli a partir de HttpSession pour setter MySession --> invoke.
lors de l'invoke, mettre a jour la map de MySession --> HttpSession deviendra la Map de MySession.

Test:
Dans la methode d'action d'un controller, avoir MySession en parametre.