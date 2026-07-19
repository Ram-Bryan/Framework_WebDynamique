# Objectif : gestion centralisée des beans contrôleurs

## Contexte
Le framework maison (front controller + annotation `@Controller`) instanciait
un contrôleur **à chaque requête** via `newInstance()`. Cela empêchait toute
injection de dépendances Spring (services, repositories) dans les contrôleurs.

## Objectif
Faire cohabiter le framework maison avec Spring, pour avoir :
- un **seul bean par contrôleur**, créé une fois au démarrage de l'application
- l'**injection Spring** (`@Autowired`) fonctionnelle dans ces contrôleurs
- le front controller qui **réutilise** ces instances à chaque requête,
  au lieu d'en recréer une nouvelle

## Mise en œuvre
1. `ContextLoaderListener` (Spring) démarre en premier et charge `application.xml`
   (datasource, `component-scan` sur `service`/`repository`).
2. `FrameworkContextListener` (custom) démarre ensuite :
   - récupère le `WebApplicationContext` Spring déjà prêt
   - scanne le package des contrôleurs (`@Controller` maison)
   - pour chaque contrôleur : `newInstance()` + `autowireBean()` via
     `AutowireCapableBeanFactory` → injection manuelle car `@Controller`
     n'est pas une annotation Spring, donc pas de component-scan possible dessus
   - stocke les instances dans une `Map<Class<?>, Object> beans`, en attribut
     du `ServletContext`
3. `FrontControllerServlet` ne fait plus `newInstance()` : il récupère
   l'instance déjà créée via `beans.get(mapping.getController())`.

## Point clé à retenir
`@Controller` (maison) ≠ `@Component`/`@Controller` (Spring) → le
component-scan Spring ne voit jamais les contrôleurs. L'injection doit donc
être faite "à la main" avec `autowireBean()`, juste après l'instanciation.