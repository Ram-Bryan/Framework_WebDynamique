# Spec Explained: Adding a Spring-like IoC Container to the Framework

## What this is

This spec describes how to evolve your mini-ORM/mini-framework from a
collection of annotated classes into something that behaves like an actual
**dependency-injection framework** (the way Spring does it). It's not a new
feature bolted on the side — it's a change to *how the whole application
starts up and how its pieces find each other*.

Right now you have two building blocks already in place:

- **Annotations** to mark classes (Controllers, and presumably models/DB
  entities).
- **View resolution**, so a Controller can return a view name and something
  renders it.
- **A startup Listener**, which currently does *some* setup work when the
  app boots.

What's missing is the piece that ties everything together: a single,
central place that knows about every object (bean) in the application,
builds them in the right order, and hands them out wherever they're needed.
That's what this spec is asking for.

## The core idea: one container per application

Spring's whole philosophy is: **don't create your objects yourself, let the
container create them for you and hand them to you already wired up.**

Instead of a Controller doing `new UserService(new UserRepository())`
somewhere inline, the framework:

1. Scans the codebase (or reads an XML file) at startup to find every class
   that should be managed.
2. Creates one instance of each of those classes.
3. Figures out which of those instances depend on which others, and injects
   them automatically.
4. Keeps all of these instances in one place — "the container" — for the
   lifetime of the application.

This container is created **exactly once**, when the app starts, not per
request and not per test. That's the "Spring = 1 conteneur" line in your
notes: there is conceptually a single source of truth for every object in
the app.

## What this improves in the current framework

**1. Introduces real layering (Controller → Service → Repository)**

Today, DB access presumably happens close to wherever it's needed. This
spec introduces a proper separation:

- **Repository** — the only layer allowed to talk to the database.
- **Service** — business logic, depends on Repositories.
- **Controller** — handles requests/views, depends on Services.

This is a structural improvement, not just a naming convention: it enforces
that no layer skips over the one below it.

**2. Moves object creation out of application code**

Currently, if a Controller needs a Service, something has to construct
that Service (and whatever *that* depends on). This spec removes that
responsibility from your code entirely and hands it to the container. Your
existing Listener becomes the trigger that builds this container, rather
than doing ad-hoc setup itself.

**3. Gives the framework a real startup lifecycle**

Instead of objects being created lazily/on-demand, the app now has a
defined boot sequence: *listener fires → container is built → beans are
instantiated → dependencies are injected → app is ready to serve
requests.* This is the same lifecycle real web frameworks use, and it
makes app startup predictable and centralized in one spot.

**4. Makes the container reachable from anywhere in the app**

Once built, any part of the application (any Controller, any Servlet)
needs a way to ask "give me the instance of X" without knowing how X was
constructed. This spec adds that lookup mechanism, so components stay
decoupled from each other and only depend on the container.

**5. Supports two ways of declaring what's managed**

The container can be told what to manage either through **XML
configuration** (explicit list of beans) or through **annotation/package
scanning** (automatic discovery). The spec asks for both paths to be
supported, with a sensible fallback: if no XML is given, scan packages
instead.

**6. Makes the framework testable the same way it runs in production**

Right now, testing probably means manually instantiating whatever class
you want to test. This spec asks for tests to boot the *same* container
logic used in production (just without necessarily needing a real web
server around it), so that what you're testing includes the wiring itself,
not just isolated classes.

## Why this matters overall

Without this, your framework is a set of annotated classes with no real
runtime behind them. With this, it becomes an actual **framework with a
lifecycle**: something starts it, something manages the objects inside it,
and everything else in the app (including tests) asks that one thing for
what it needs — instead of constructing things by hand. This is the
conceptual leap from "annotations that describe things" to "a container
that acts on those annotations."