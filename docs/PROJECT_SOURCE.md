FINANCE TRACKER - LIVING PROJECT SOURCE AND PROGRESS LOG
========================================================

Compiled: July 21, 2026
Updated through: August 1, 2026
Project owner: Nicolas Hernandez
Repository: https://github.com/BioNick97/finance-tracker
Base package: com.nicolashernandez.financetracker


CODEX CONTINUITY
----------------

This file is the canonical project source of truth for future Codex work.
Before changing the project, read the current checkpoint and latest dated
update. After completing and verifying meaningful work, append a dated update
that records:

- What changed.
- Important implementation or architecture decisions.
- How the change was verified.
- Relevant commit IDs, when available.
- The current verified state.
- The next recommended step.

Keep historical entries intact. If plans change, add a new dated update rather
than rewriting old decisions without explanation. Update the "Updated through"
date above whenever a progress entry is appended.


PURPOSE OF THIS FILE
--------------------

This is a consolidated source document covering the Finance Tracker work and
concepts discussed throughout the project conversations so far. It combines the
earlier setup notes with the later CRUD, validation, PostgreSQL, Docker, and
architecture discussions.

Important distinction:

- The ArrayList implementation was a temporary learning version.
- The intended/current architecture uses PostgreSQL through Spring Data JPA.
- Code examples for the ArrayList are retained because they explain Java and
  Spring concepts learned along the way, but they are not the final storage
  design.


1. PROJECT GOAL
---------------

Build a full-stack personal finance tracker that serves two purposes:

1. Learn the modern Java/Spring ecosystem by building a real application.
2. Create a portfolio-quality project that can eventually be discussed on a
   resume and shown to employers.

The project is being built one layer and feature at a time, with an emphasis on
understanding why the architecture works instead of only copying tutorial code.

The backend is being built first. The frontend and more advanced financial
logic will be added after the API and data model are stable.


2. TECHNOLOGY STACK
-------------------

Current and planned technologies:

- Java 21 (Eclipse Temurin)
- Spring Boot 4.1.0
- Maven / Maven Wrapper
- Spring Web MVC
- Spring Data JPA
- Hibernate
- PostgreSQL
- Docker and Docker Compose
- Lombok
- Jakarta Bean Validation
- IntelliJ IDEA
- DBeaver for viewing the database
- Git and GitHub
- React for the future frontend
- Bootstrap or another UI library for initial styling
- Possible later deployment to a cloud platform such as AWS

Verified local tooling included:

- Java 21.0.11
- Maven Wrapper 3.9.16
- Apple Silicon / aarch64 Mac
- Spring Boot running with embedded Tomcat on port 8080


3. BUILD AND PACKAGING CONCEPTS
-------------------------------

Maven
~~~~~

Maven is the build and dependency-management tool. Its configuration lives in
pom.xml. Maven downloads libraries, compiles the application, runs tests, and
packages the finished application.

The Maven Wrapper allows the project to use a known Maven version without
requiring every developer to install Maven globally:

    ./mvnw -v
    ./mvnw spring-boot:run

JAR, WAR, and EAR
~~~~~~~~~~~~~~~~~

- JAR: Common for modern Spring Boot applications. It can contain the app and
  embedded Tomcat, allowing the app to run independently.
- WAR: Normally deployed into an external application server.
- EAR: Enterprise archive that can contain multiple application modules. This
  is more common in older enterprise Java environments, including systems like
  those used at work.

The Finance Tracker should normally be built as a standalone Spring Boot JAR.


4. SPRING BOOT STARTUP AND FIRST ENDPOINTS
------------------------------------------

The main application class is the entry point:

    @SpringBootApplication
    public class FinanceTrackerApplication {
        public static void main(String[] args) {
            SpringApplication.run(FinanceTrackerApplication.class, args);
        }
    }

SpringApplication.run starts Spring Boot, performs component scanning, creates
the application context, configures the embedded server, and starts Tomcat.

Successful startup includes messages similar to:

    Tomcat started on port 8080
    Started FinanceTrackerApplication

The base server address is:

    http://localhost:8080

Initial 404
~~~~~~~~~~~

The first visit to the root URL returned a Whitelabel 404 page. That did not
mean the server was broken. It meant:

- Spring Boot was running.
- The browser successfully reached it.
- No controller method was mapped to `/`.

The first practice endpoint was:

    @RestController
    public class HelloController {
        @GetMapping("/hello")
        public String hello() {
            return "Hello, Finance Tracker!";
        }
    }

URL:

    http://localhost:8080/hello


5. CORE SPRING ARCHITECTURE
---------------------------

The target request flow is:

    Client / React UI
        -> Controller
        -> Service
        -> Repository
        -> PostgreSQL

Responses travel back through those layers and Spring converts returned Java
objects to JSON.

Layer responsibilities
~~~~~~~~~~~~~~~~~~~~~~

Model / Entity:

- Represents application data as a Java object.
- Maps to a database table when annotated as a JPA entity.
- Example: Transaction.

Controller:

- Receives HTTP requests.
- Reads URL variables and JSON bodies.
- Calls the service.
- Determines normal HTTP response behavior.
- Should not contain most business logic or direct database code.

Service:

- Contains application and business logic.
- Coordinates repository operations.
- Decides what to do when a record is missing.
- Keeps the controller thin.

Repository:

- Communicates with PostgreSQL through Spring Data JPA.
- Provides common database operations such as findAll, findById, save, existsById,
  and deleteById.

Not every model automatically needs a completely separate copy of every layer.
For major database-backed concepts, a model/entity, repository, service, and
controller are a useful general pattern. Smaller embedded value objects or
request/response DTOs may not need their own repository or controller.


6. IMPORTANT SPRING AND JAVA ANNOTATIONS
----------------------------------------

@SpringBootApplication
~~~~~~~~~~~~~~~~~~~~~~

Marks the main Spring Boot class. It combines configuration, auto-configuration,
and component scanning behavior.

@RestController
~~~~~~~~~~~~~~~

Marks a class as an HTTP controller whose returned values are written directly
to the response body. Spring/Jackson can convert returned Java objects to JSON.
It effectively combines @Controller and @ResponseBody.

@Service
~~~~~~~~

Marks a class as containing service/business logic. Spring creates and manages
an instance of it as a bean.

@RequestMapping("/api/transactions")
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Defines the base URL for every endpoint in TransactionController.

HTTP mappings
~~~~~~~~~~~~~

- @GetMapping: read data
- @PostMapping: create data
- @PutMapping: update or replace a resource
- @PatchMapping: partially update a resource
- @DeleteMapping: delete a resource

The Java method name does not determine the HTTP method. The annotation does.

@RequestBody
~~~~~~~~~~~~

Tells Spring to deserialize the incoming JSON request body into a Java object.
For example:

    @RequestBody Transaction transaction

@PathVariable
~~~~~~~~~~~~~

Reads a placeholder from the URL:

    @PutMapping("/{id}")
    public Transaction updateTransaction(
            @PathVariable("id") Long transactionId,
            @RequestBody Transaction transaction) {
        ...
    }

For this request:

    PUT /api/transactions/3

Spring assigns:

    transactionId = 3L

The explicit form @PathVariable("id") is useful because the Java variable can
have a different name. Multiple path variables are also valid:

    /transactions/{transactionId}/notes/{noteId}

Each @PathVariable matches a named placeholder. Path variables come from the
URL, while @RequestBody values come from JSON.

@ResponseStatus
~~~~~~~~~~~~~~~

Overrides Spring's default successful HTTP status when needed.

Examples:

- POST creation: @ResponseStatus(HttpStatus.CREATED) -> 201 Created
- DELETE returning no body: @ResponseStatus(HttpStatus.NO_CONTENT) -> 204 No Content
- PUT returning the updated object: no annotation required -> 200 OK by default

@Valid
~~~~~~

@Valid tells Spring to run Bean Validation rules on the deserialized request
object before the controller continues:

    import jakarta.validation.Valid;

    public Transaction createTransaction(
            @Valid @RequestBody Transaction transaction) {
        ...
    }

Without @Valid, annotations such as @NotBlank may exist on the entity but will
not automatically validate that controller request.

JPA annotations
~~~~~~~~~~~~~~~

- @Entity: marks a class as a JPA entity.
- @Table(name = "transactions"): maps it to the transactions table.
- @Id: marks the primary key.
- @GeneratedValue(strategy = GenerationType.IDENTITY): lets PostgreSQL generate
  IDs.
- @Column: controls database column constraints and shape.

Lombok annotations
~~~~~~~~~~~~~~~~~~

- @Getter: generates getter methods.
- @Setter: generates setter methods.
- @NoArgsConstructor: generates an empty constructor, also required by JPA.
- @AllArgsConstructor: generates a constructor containing every field.
- @RequiredArgsConstructor: generates a constructor for final and @NonNull fields.

The controller uses @RequiredArgsConstructor with:

    private final TransactionService transactionService;

This gives Spring a constructor through which it can inject TransactionService.


7. DEPENDENCY INJECTION
-----------------------

Spring creates the TransactionService because the class has @Service. It then
passes that service object into TransactionController's constructor.

With Lombok, this:

    @RequiredArgsConstructor
    public class TransactionController {
        private final TransactionService transactionService;
    }

effectively generates:

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

Naming distinction:

- TransactionService is the class/type.
- transactionService is the injected object/instance.

Incorrect static-style call:

    TransactionService.createTransaction(transaction);

Correct instance call:

    transactionService.createTransaction(transaction);


8. TRANSACTION MODEL AND MONEY DATA
-----------------------------------

The initial Transaction model contained:

    private Long id;
    private String description;
    private BigDecimal amount;

BigDecimal is used for money because floating-point types such as double can
introduce binary rounding errors. Decimal money values need predictable decimal
precision.

Current entity structure discussed:

    @Entity
    @Table(name = "transactions")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class Transaction {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank(message = "Description is required")
        @Size(max = 100,
              message = "Description cannot exceed 100 characters")
        @Column(nullable = false, length = 100)
        private String description;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01",
                    message = "Amount must be greater than zero")
        @Column(nullable = false, precision = 12, scale = 2)
        private BigDecimal amount;
    }

The annotations have different jobs:

- @NotBlank, @Size, @NotNull, and @DecimalMin validate Java input.
- @Column controls the database schema.
- @GeneratedValue delegates ID generation to the database.

Entity imports discussed:

    import jakarta.persistence.Column;
    import jakarta.persistence.Entity;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
    import jakarta.persistence.Table;
    import jakarta.validation.constraints.DecimalMin;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import java.math.BigDecimal;

Controller import for @Valid:

    import jakarta.validation.Valid;


9. THE TEMPORARY IN-MEMORY IMPLEMENTATION
-----------------------------------------

Before PostgreSQL was connected, TransactionService stored data in an
ArrayList:

    private final List<Transaction> transactions = new ArrayList<>(List.of(
            new Transaction(1L, "Groceries", new BigDecimal("75.50")),
            new Transaction(2L, "Gas", new BigDecimal("42.25"))
    ));

    private long nextId = 3L;

Creating a transaction used:

    transaction.setId(nextId++);
    transactions.add(transaction);
    return transaction;

nextId++ uses the current number for the new transaction and then increments it
for the next create operation.

This data only lived in application memory. Restarting Spring erased all new
transactions and restored the hard-coded seed data. That limitation motivated
the PostgreSQL migration.


10. LIST.COPYOF, SHALLOW COPIES, REFERENCES, AND MUTABILITY
----------------------------------------------------------

The in-memory get-all method returned:

    return List.copyOf(transactions);

This creates a new, unmodifiable list. Code receiving that returned list cannot
add, remove, or clear list elements through it.

However, this is a shallow copy. The new list contains references to the same
Transaction objects as the original list.

Conceptually:

    original list[0] -> Transaction object A
    copied list[0]   -> Transaction object A

The list containers are different objects, but both contain a reference to the
same mutable Transaction object. Therefore:

    copy.add(transaction);                    // not allowed
    copy.remove(0);                           // not allowed
    copy.get(0).setDescription("New name");  // allowed

Changing the object's description is visible through the original list because
there is only one Transaction object involved.

This sharing has nothing to do with the database ID. An ID is just data stored
inside an object. Two separate Java objects can both contain id = 1 and still be
different objects. A deep copy would explicitly create new Transaction objects.

Transaction is mutable because setters can change its fields after creation.


11. FULL CRUD API
-----------------

CRUD means:

- Create -> POST
- Read -> GET all and GET one
- Update -> PUT
- Delete -> DELETE

Endpoint design:

    GET    /api/transactions
    GET    /api/transactions/{id}
    POST   /api/transactions
    PUT    /api/transactions/{id}
    DELETE /api/transactions/{id}

Controller shape:

    @RestController
    @RequestMapping("/api/transactions")
    @RequiredArgsConstructor
    public class TransactionController {

        private final TransactionService transactionService;

        @GetMapping
        public List<Transaction> getAllTransactions() {
            return transactionService.getAllTransactions();
        }

        @GetMapping("/{id}")
        public Transaction getTransaction(
                @PathVariable("id") Long id) {
            return transactionService.getTransaction(id);
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public Transaction createTransaction(
                @Valid @RequestBody Transaction transaction) {
            return transactionService.createTransaction(transaction);
        }

        @PutMapping("/{id}")
        public Transaction updateTransaction(
                @PathVariable("id") Long id,
                @Valid @RequestBody Transaction transaction) {
            return transactionService.updateTransaction(id, transaction);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void deleteTransaction(
                @PathVariable("id") Long id) {
            transactionService.deleteTransaction(id);
        }
    }

The in-memory service implementation used loops to locate a matching ID. The
database-backed version replaces those loops with repository calls.


12. GET ONE
-----------

The GET-one route and service were added so the API could retrieve an individual
transaction:

    GET /api/transactions/1

The in-memory method looped over the list and returned immediately when it found
the ID. An initially created variable such as this was unnecessary:

    Transaction foundTransaction = new Transaction();

The simplified form was:

    public Transaction getTransaction(Long id) {
        for (Transaction existingTransaction : transactions) {
            if (existingTransaction.getId().equals(id)) {
                return existingTransaction;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Transaction not found"
        );
    }

If a matching object is returned, the method ends immediately. The exception is
only reached after the entire loop finishes without finding the ID.

In the repository version, findById returns Optional<Transaction>. A common
service pattern is:

    return transactionRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Transaction not found"
            ));

Optional represents the possibility that a value may or may not exist.


13. UPDATE / PUT
----------------

The first PUT attempt called createTransaction, which would create a new record
instead of updating an existing one. The server also needed an ID to know which
record to change.

Correct route structure:

    @PutMapping("/{id}")

The request identifies the record in the URL:

    PUT /api/transactions/3

The replacement values arrive in the JSON body.

In-memory update logic:

    public Transaction updateTransaction(
            Long id,
            Transaction updatedTransaction) {

        for (Transaction existingTransaction : transactions) {
            if (existingTransaction.getId().equals(id)) {
                existingTransaction.setDescription(
                        updatedTransaction.getDescription()
                );
                existingTransaction.setAmount(
                        updatedTransaction.getAmount()
                );
                return existingTransaction;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Transaction not found"
        );
    }

This also demonstrated the earlier reference concept: existingTransaction points
to the same mutable object stored in the list. Calling setters changes that
stored object.

Database-backed update logic generally:

1. Calls getTransaction(id) or repository.findById(id).
2. Throws 404 if it does not exist.
3. Updates fields on the found entity.
4. Calls repository.save(existingTransaction).
5. Returns the saved entity.

PUT successfully returns 200 OK by default when it returns the updated object.
@ResponseStatus(HttpStatus.OK) would be redundant.


14. DELETE
----------

DELETE only needs the ID from the URL. It does not need a Transaction request
body.

Controller:

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable("id") Long id) {
        transactionService.deleteTransaction(id);
    }

The return type is void because a successful 204 response intentionally has no
response body. A void method may use:

    return;

That means stop executing the method. It cannot return a value such as a String
or Transaction.

The initial attempt tried to return ResponseStatus from the service. That is not
how the split works:

- The service performs the deletion or throws an exception.
- The controller's @ResponseStatus controls the 204 HTTP response.

In-memory delete logic used an index-based loop:

    public void deleteTransaction(Long id) {
        for (int i = 0; i < transactions.size(); i++) {
            Transaction existingTransaction = transactions.get(i);

            if (existingTransaction.getId().equals(id)) {
                transactions.remove(i);
                return;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Transaction not found"
        );
    }

Removing directly from an ArrayList inside an enhanced for-each loop can cause a
ConcurrentModificationException. Removing by index avoided that issue.

In a repository-based service, deletion generally checks that the ID exists and
then calls deleteById, or retrieves the entity and deletes it. A missing ID
should return 404 rather than falsely claiming a successful deletion.


15. HTTP STATUS CODES LEARNED
-----------------------------

- 200 OK: successful GET or PUT returning data.
- 201 Created: successful POST that created a transaction.
- 204 No Content: successful DELETE with no response body.
- 400 Bad Request: Spring could not parse or validate the request.
- 404 Not Found: the request was valid, but the transaction ID did not exist.

Important control-flow point:

    ID found -> update/delete -> return -> method ends
    ID absent -> loop/repository lookup fails -> throw 404

The throw statement does not run every time. A preceding return exits the entire
method.


16. CURL AND API TESTING
------------------------

The browser address bar is useful for GET requests. POST, PUT, and DELETE were
tested with curl.

GET all:

    curl -i http://localhost:8080/api/transactions

GET one:

    curl -i http://localhost:8080/api/transactions/1

POST:

    curl -i -X POST http://localhost:8080/api/transactions \
      -H "Content-Type: application/json" \
      -d '{"description":"Internet","amount":89.99}'

PUT:

    curl -i -X PUT http://localhost:8080/api/transactions/3 \
      -H "Content-Type: application/json" \
      -d '{"description":"Internet Bill","amount":95.00}'

DELETE:

    curl -i -X DELETE http://localhost:8080/api/transactions/3

Verified complete in-memory flow:

- GET all -> 200
- GET ID 1 -> 200
- POST Internet -> 201, created ID 3
- PUT ID 3 -> 200, changed description and amount
- DELETE ID 3 -> 204
- GET deleted ID 3 -> 404

Testing confirmed the controller and service methods were wired correctly.


17. TERMINAL AND JSON ERRORS ENCOUNTERED
----------------------------------------

Invalid JSON number
~~~~~~~~~~~~~~~~~~~

This body caused 400 Bad Request:

    {"description":"WifiNOMORE","amount":00.00}

JSON numbers cannot contain multiple leading zeros. Correct:

    {"description":"WifiNOMORE","amount":0.00}

This error happened while Spring was deserializing the request, before the
controller update method ran.

Missing curl command
~~~~~~~~~~~~~~~~~~~~

Typing only:

    -i -X DELETE ...

caused:

    zsh: command not found: -i

The command must begin with curl:

    curl -i -X DELETE ...

Broken quotes and dquote prompt
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

An unclosed quote caused zsh to display:

    dquote>

That means the shell is waiting for the closing double quote. Press Control+C to
cancel the unfinished command and re-enter it carefully.

Pasted shell comments
~~~~~~~~~~~~~~~~~~~~~

Lines beginning with # were intended as labels, but interactive zsh tried to
execute them and printed:

    zsh: command not found: #

The API calls still worked. Paste only the actual commands, or enable interactive
comments in that shell:

    setopt interactivecomments

compdef error
~~~~~~~~~~~~~

The separate message:

    compdef: command not found

was a zsh configuration/completion issue, not a Spring Boot or Finance Tracker
error.


18. VALIDATION
--------------

Validation was added to prevent bad data from entering the application.

Example rules:

- Description is required and cannot be blank.
- Description has a maximum length.
- Amount is required.
- Amount must be above a chosen minimum such as 0.01.

The validation annotations live on Transaction, while @Valid is added to POST
and PUT controller parameters.

Required dependency:

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

There was a compilation failure where Java could not find NotBlank and Size.
The fix was to add/confirm the validation dependency and use the jakarta
validation imports. After that, validation tests passed.

The uploaded pom.xml was an earlier snapshot that did not show the validation
dependency, but the active project was later confirmed to already contain it.

Annotations beginning with @ are metadata. Modern Spring relies heavily on them
for configuration, routing, validation, persistence, and dependency injection.
Older workplace Java frameworks may rely more on XML configuration, manual
wiring, or older conventions, explaining why @ annotations may be less common
in that codebase.


19. POSTGRESQL AND JPA MIGRATION
-------------------------------

Why move to PostgreSQL
~~~~~~~~~~~~~~~~~~~~~~

The ArrayList was useful for learning CRUD and Java references, but it did not
persist data. PostgreSQL allows transactions to survive application restarts.

The database-backed flow is:

    Controller -> Service -> TransactionRepository -> PostgreSQL

Initial datasource failure
~~~~~~~~~~~~~~~~~~~~~~~~~~

The project initially had JPA and the PostgreSQL driver but no datasource URL or
credentials. Spring failed startup with a message similar to:

    Failed to configure a DataSource: 'url' attribute is not specified

A temporary property disabled datasource auto-configuration while the early API
was being built:

    spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration

That property had to be removed once PostgreSQL was ready. Leaving it in place
disables the DataSource and prevents Hibernate/JPA from connecting.

Datasource configuration
~~~~~~~~~~~~~~~~~~~~~~~~

The discussed Docker database connection uses:

    spring.datasource.url=jdbc:postgresql://localhost:5432/finance_tracker
    spring.datasource.username=finance_user
    spring.datasource.password=finance_password

    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true

ddl-auto=update tells Hibernate to compare the entity mapping with the database
and update/create the schema during development. show-sql prints generated SQL
for learning and debugging.

Credentials shown here are local development credentials. Production secrets
must not be hard-coded or committed.

TransactionRepository
~~~~~~~~~~~~~~~~~~~~~

The repository pattern discussed/implemented is:

    public interface TransactionRepository
            extends JpaRepository<Transaction, Long> {
    }

Spring Data JPA automatically provides standard database methods, so no manual
SQL is required for basic CRUD.

Common methods:

- findAll()
- findById(id)
- save(transaction)
- existsById(id)
- deleteById(id)

Moving to the repository removes:

- The in-memory List<Transaction>
- Hard-coded Groceries and Gas seed records, unless explicit seeding is added
- The manual nextId field
- Manual loops used to find records

PostgreSQL generates IDs because of @GeneratedValue.

Database integration was confirmed working in the project: Hibernate/Hikari
started, tables were created, DBeaver could inspect the database, and stored
transactions persisted after restarting the Spring application.


20. DOCKER POSTGRESQL MIGRATION
-------------------------------

The project originally used PostgreSQL 16 installed locally through Homebrew.
The database connection was:

    jdbc:postgresql://localhost:5432/finance_tracker

Docker was initially unavailable:

    zsh: command not found: docker

Docker Desktop for Mac with Apple silicon was installed. Docker and Docker
Compose were then available.

Why switch to Docker
~~~~~~~~~~~~~~~~~~~~

- Practice useful Docker/DevOps skills.
- Give the project a reproducible PostgreSQL setup.
- Make it easier for someone cloning the repository to start the database.
- Avoid requiring a manual PostgreSQL installation on every computer.

Local PostgreSQL and Docker PostgreSQL are separate servers. Even if both
databases are called finance_tracker, they do not share or synchronize data.

Port decision
~~~~~~~~~~~~~

If local PostgreSQL runs on port 5432, Docker can temporarily map Mac port 5433
to container port 5432:

    "5433:5432"

The user decided to stop the local Homebrew service and use Docker exclusively.
Homebrew status showed:

    postgresql@16 none

"none" meant the service was stopped, not yet uninstalled. With port 5432 free,
Docker could use:

    "5432:5432"

Compose configuration discussed
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    services:
      postgres:
        image: postgres:18
        environment:
          POSTGRES_DB: finance_tracker
          POSTGRES_USER: finance_user
          POSTGRES_PASSWORD: finance_password
        ports:
          - "5432:5432"
        volumes:
          - postgres_data:/var/lib/postgresql
        healthcheck:
          test: ["CMD-SHELL",
                 "pg_isready -U finance_user -d finance_tracker"]
          interval: 5s
          timeout: 5s
          retries: 5

    volumes:
      postgres_data:

compose.yaml belongs in the project root next to pom.xml and should be committed
to Git because it describes the reproducible development environment. Only safe
local-development credentials belong in that committed file.

Starting and inspecting PostgreSQL:

    docker compose up -d
    docker compose ps

Successful output showed:

- Container: finance-tracker-postgres-1
- Image: postgres:18
- Status: healthy
- Port: 0.0.0.0:5432 -> 5432/tcp

Connecting with psql inside the container:

    docker compose exec postgres \
      psql -U finance_user -d finance_tracker

Useful psql commands:

    \conninfo
    \dt
    \q

Important: psql backslash commands such as \dt must be entered by themselves.
This was incorrect:

    \dt SELECT * FROM transaction;

psql treated the SQL words as extra arguments to \dt. Run these separately:

    \dt

    SELECT * FROM transactions;

Use the exact table name shown by \dt. Because the entity uses
@Table(name = "transactions"), the expected name is transactions.

When \dt initially reported no tables, that was expected. Docker PostgreSQL was
healthy, but Spring/Hibernate had not yet connected and created the table.

The startup chain is:

    Docker starts PostgreSQL
        -> Spring connects using application.properties
        -> Hibernate detects @Entity
        -> Hibernate creates/updates transactions table

Docker volume behavior
~~~~~~~~~~~~~~~~~~~~~~

The named postgres_data volume stores the database separately from the container.
Therefore:

    docker compose down

removes/stops the container while preserving the named volume.

This intentionally deletes the volume and database data:

    docker compose down -v

Do not use -v unless a completely fresh database is wanted.

Restart persistence can be tested with:

    docker compose restart

Then start/check Spring and call GET /api/transactions again.

Local Homebrew cleanup
~~~~~~~~~~~~~~~~~~~~~~

The old local data directory was confirmed at:

    /opt/homebrew/var/postgresql@16

The safe order discussed was:

1. Get Docker PostgreSQL working.
2. Confirm Spring connects and data persists.
3. Only then uninstall Homebrew PostgreSQL.

Commands discussed:

    brew uninstall postgresql@16
    brew cleanup

If the old data directory remains and is no longer needed, it can be moved to
Trash rather than immediately deleted:

    mv /opt/homebrew/var/postgresql@16 ~/.Trash/postgresql@16-old

Do not remove neighboring Homebrew or log directories.


21. DATABASE VIEWING TOOLS
--------------------------

IntelliJ's built-in database viewer required a paid edition/subscription in the
current setup, so DBeaver was used instead. DBeaver successfully connected to
PostgreSQL and confirmed stored records persisted.

psql remains useful for direct checks, especially inside the Docker container.


22. GIT AND GITHUB
------------------

Repository:

    https://github.com/BioNick97/finance-tracker

The first git commands failed with:

    fatal: not a git repository

That meant the finance-tracker directory had not yet been initialized or linked
as a Git repository. The repository was later created and connected.

compose.yaml should be tracked because it is part of the reproducible project
setup. Real passwords, tokens, and production configuration should never be
committed.


23. HOW THE PROJECT CAN BECOME A FULL WEBSITE
---------------------------------------------

Spring Boot and PostgreSQL are appropriate for the backend of a complete
website, including something like the restaurant site previously discussed.
They handle:

- REST endpoints
- Business calculations
- Validation
- Database persistence
- Authentication and authorization later

The UI is a separate frontend concern. The planned Finance Tracker approach is
to use React, likely with Bootstrap or a similar component/style system at first.

The frontend will call the Spring Boot API. For example:

    React form -> POST /api/transactions -> Service -> PostgreSQL

Backend logic should be developed and tested first for core rules and
calculations. The UI can then present that logic and gather user input without
becoming the only place where important financial rules live.


24. CURRENT PROJECT CHECKPOINT
------------------------------

Completed or established:

- Java 21, Maven, and IntelliJ environment configured.
- Spring Boot application runs on port 8080.
- Spring controller/service architecture understood.
- Transaction model/entity created.
- Full CRUD endpoints built and tested.
- HTTP statuses verified: 200, 201, 204, 400, and 404.
- Validation annotations and @Valid added/tested.
- PostgreSQL connection configured.
- DataSourceAutoConfiguration exclusion removed for database use.
- TransactionRepository / Spring Data JPA pattern introduced and used.
- Database persistence confirmed across application restarts.
- Docker Desktop installed.
- PostgreSQL 18 Docker container created and reported healthy on port 5432.
- compose.yaml added to the project.
- Local Homebrew PostgreSQL service stopped while Docker becomes the main DB.
- GitHub repository created and connected.
- TransactionType enum created with INCOME and EXPENSE values.
- Transaction date added using LocalDate.
- Category promoted from transaction text to its own JPA entity.
- CategoryRepository, CategoryService, and CategoryController created.
- Complete category CRUD endpoints created and tested.
- Transaction linked to Category with a required @ManyToOne relationship.
- Transaction creation now verifies that the submitted category ID exists.
- Transaction updates now include description, amount, transaction date, and
  category.
- The duplicated Transaction.type field was removed; type now comes from the
  required Category.
- The development transactions table was safely rebuilt after its schema became
  incompatible with the new required fields.
- Creating a transaction with an existing category ID was verified successfully.

Important configuration check:

- Docker must be running.
- The postgres container must be healthy.
- application.properties must use the Docker database credentials.
- The old DataSourceAutoConfiguration exclusion must not be present.


25. RECOMMENDED NEXT STEPS
--------------------------

Immediate verification
~~~~~~~~~~~~~~~~~~~~~~

1. Start Docker Desktop.
2. Run docker compose up -d.
3. Confirm docker compose ps reports PostgreSQL healthy.
4. Start Spring Boot.
5. Confirm Hibernate creates/sees the transactions table.
6. Run the complete CRUD curl flow against the Docker-backed database.
7. Restart Spring and Docker, then confirm records remain.

Backend improvements
~~~~~~~~~~~~~~~~~~~~

1. Add automated tests for controller, service, repository, validation, 404s,
   and successful CRUD behavior.
2. Add a consistent global error response using @ControllerAdvice.
3. Introduce DTOs so API request/response objects are separated from database
   entities.
4. Refactor repeated category lookup logic in TransactionService.
5. Prevent category type changes or deletion when existing transactions use the
   category.
6. Design Account, Budget, and User entities carefully.
7. Add database migrations with Flyway rather than relying permanently on
   ddl-auto=update.
8. Move configuration secrets to environment variables.

Financial/business logic
~~~~~~~~~~~~~~~~~~~~~~~~

Potential calculations and rules:

- Monthly income total
- Monthly expense total
- Net cash flow
- Spending totals by category
- Budget versus actual spending
- Recurring transactions
- Account balances
- Savings progress
- Debt payoff tracking
- Date-based filtering
- CSV/spreadsheet import

These calculations belong primarily in the backend service/domain layer so they
can be tested independently and reused by any frontend.

Frontend
~~~~~~~~

After the backend is stable:

1. Create the React application.
2. Build a transaction list/table.
3. Build add/edit forms.
4. Add delete confirmation.
5. Add dashboard totals and charts.
6. Improve styling and responsive design.

Later portfolio features
~~~~~~~~~~~~~~~~~~~~~~~~

- User accounts and authentication
- JWT or session security
- Multiple accounts and budgets
- Charts and reporting
- Recurring transaction automation
- CSV import/export
- Dockerized backend and frontend
- Cloud deployment
- CI/CD pipeline
- AI-assisted spending summaries or anomaly detection


26. KEY LESSONS AND TAKEAWAYS
-----------------------------

- Build the backend in small, testable steps.
- Controllers handle HTTP; services handle decisions; repositories handle data.
- An annotation configures framework behavior; it is not just decoration.
- return exits the entire Java method, even when used inside a loop.
- IDs identify records but do not create Java object-reference sharing.
- An unmodifiable list is not the same as an immutable object graph.
- Bad JSON produces 400 before normal controller logic runs.
- A valid request for a missing ID produces 404.
- POST creation should communicate 201.
- DELETE with no body should communicate 204.
- PostgreSQL provides persistence; Docker makes the database reproducible.
- A Docker container and a Docker volume are different: containers can be
  recreated while volumes retain data.
- Local and Docker PostgreSQL servers are independent even when their database
  names match.
- Database migration tools, DTOs, tests, and environment-based secrets are
  important future steps for turning the learning project into a professional
  portfolio application.


27. JULY 22 UPDATE: REAL FINANCE DATA MODEL
-------------------------------------------

The generic transaction CRUD API was extended toward a real finance tracker.
The key additions were:

- Transaction dates
- Income and expense classification
- Database-backed categories
- A relationship from Transaction to Category

TransactionType
~~~~~~~~~~~~~~~

The transaction classification is represented by an enum:

    package com.nicolashernandez.financetracker.model;

    public enum TransactionType {
        INCOME,
        EXPENSE
    }

An enum is appropriate because the allowed values are small and fixed.
TransactionType does not need its own controller, service, repository, or
database table.

The value is stored on Category using:

    @NotNull(message = "Category type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

EnumType.STRING stores readable values such as INCOME and EXPENSE instead of
numeric enum positions. This is safer because rearranging the Java enum does not
change the meaning of existing database values.

Category entity
~~~~~~~~~~~~~~~

Category became a database-backed entity rather than a String stored directly
on every transaction:

    @Entity
    @Table(
            name = "categories",
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_category_name_type",
                    columnNames = {"name", "type"}
            )
    )
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class Category {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank(message = "Category name is required")
        @Column(nullable = false, length = 50)
        private String name;

        @NotNull(message = "Category type is required")
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private TransactionType type;
    }

The categories table currently has three columns:

    id | name | type

Example:

    1 | Groceries     | EXPENSE
    2 | Rent          | EXPENSE
    3 | Car Insurance | EXPENSE

The combined unique constraint means that the pair of name and type cannot be
duplicated. It does not require each column to be individually unique.

For example:

    Groceries | EXPENSE  -> allowed once
    Groceries | EXPENSE  -> duplicate, rejected
    Groceries | INCOME   -> a different pair

The database constraint is named uk_category_name_type so database errors and
migrations can identify it clearly.

Category CRUD layers
~~~~~~~~~~~~~~~~~~~~

The project continues to use a layer-based package structure:

    controller/
        TransactionController
        CategoryController

    model/
        Transaction
        Category
        TransactionType

    repository/
        TransactionRepository
        CategoryRepository

    service/
        TransactionService
        CategoryService

CategoryRepository:

    public interface CategoryRepository
            extends JpaRepository<Category, Long> {
    }

Spring Data automatically supplies findAll, findById, save, delete, and other
standard repository operations.

Category endpoints:

    GET    /api/categories
    GET    /api/categories/{id}
    POST   /api/categories
    PUT    /api/categories/{id}
    DELETE /api/categories/{id}

CategoryController follows the same pattern as TransactionController. @Valid is
used on POST and PUT request bodies. It is not currently repeated in the
service because HTTP request validation occurs at the controller boundary.

CategoryService:

- Returns all categories with categoryRepository.findAll().
- Returns one category or throws 404 Not Found.
- Sets an incoming category ID to null during creation so PostgreSQL generates
  it.
- Updates the existing entity's name and type.
- Verifies a category exists before deleting it.

An accidental static declaration on createCategory caused IntelliJ to warn:

    Static member accessed via instance reference

Service methods should be instance methods because Spring injects and manages a
CategoryService object. The correct controller call is:

    categoryService.createCategory(category);

not:

    CategoryService.createCategory(category);

Category requests tested
~~~~~~~~~~~~~~~~~~~~~~~~

Create:

    curl -i -X POST http://localhost:8080/api/categories \
      -H "Content-Type: application/json" \
      -d '{"name":"Groceries","type":"EXPENSE"}'

Successful creation returns 201 Created.

Get all:

    curl -i http://localhost:8080/api/categories

Verified category response:

    [
      {"id":1,"name":"Groceries","type":"EXPENSE"},
      {"id":2,"name":"Rent","type":"EXPENSE"},
      {"id":3,"name":"Car Insurance","type":"EXPENSE"}
    ]

Transaction date
~~~~~~~~~~~~~~~~

Transaction now includes:

    @NotNull(message = "Transaction date is required")
    @Column(nullable = false)
    private LocalDate transactionDate;

LocalDate represents a calendar date without a time or time zone. JSON uses the
ISO form:

    "transactionDate": "2026-07-22"

Transaction-to-category relationship
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The original Transaction field stored duplicated category text:

    private String category;

It was replaced with a JPA relationship:

    @NotNull(message = "Category is required")
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

@ManyToOne means many Transaction rows can reference the same Category:

    Publix          \
    Walmart          -> Groceries
    Farmers Market  /

The relationship is viewed from the Transaction side:

    many transactions -> one category

@JoinColumn(name = "category_id") says the transactions table stores the
foreign-key value in a column named category_id:

    transactions.category_id -> categories.id

The column name alone does not create the relationship. @ManyToOne and the
Category-typed field tell JPA which entity is related. @JoinColumn tells JPA
where to store that relationship.

JPA knows that category_id references categories.id because Category marks its
id field with @Id. referencedColumnName = "id" could be written explicitly, but
it is unnecessary because JPA references the related entity's @Id by default.

The database stores only the foreign-key number:

    transactions.category_id = 1

Java exposes the full object:

    transaction.getCategory().getName()

This avoids inconsistent repeated text such as Groceries, groceries, and
Grocery. Renaming a category updates one category row while every related
transaction continues pointing to it.

Required category decision
~~~~~~~~~~~~~~~~~~~~~~~~~~

Category is required for manually entered transactions in the MVP:

    @NotNull(message = "Category is required")
    @JoinColumn(name = "category_id", nullable = false)

@NotNull validates the Java/API input. nullable = false enforces the requirement
in PostgreSQL.

Required categories make summaries, filters, budgets, and charts more reliable.
General fallback categories such as Other Income and Other Expense can be added
when a more specific category does not apply.

Transaction type normalization decision
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Transaction initially stored its own TransactionType while Category also stored
a TransactionType. Once Category became required, this duplicated the same
information and could produce contradictions:

    transaction.type = INCOME
    transaction.category.type = EXPENSE

The duplicated type field was therefore removed from Transaction. Category is
now the single source of truth:

    transaction.getCategory().getType()

TransactionType still exists and remains stored on Category.

One consequence is that category type should not be changed freely after
transactions reference it, because doing so would change how historical
transactions are interpreted. A future rule should prevent changing the type
of an in-use category.

Transaction service category lookup
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

TransactionService now receives both repositories:

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

@RequiredArgsConstructor creates a constructor for both final fields, allowing
Spring to inject both repositories.

When creating a transaction, the service:

1. Sets transaction.id to null.
2. Checks that the request contains a category and category ID.
3. Looks up the real Category in PostgreSQL.
4. Throws 400 Bad Request when no category ID was supplied.
5. Throws 404 Not Found when the category ID does not exist.
6. Replaces the partial request Category with the retrieved database entity.
7. Saves the Transaction.

Core logic:

    if (transaction.getCategory() == null ||
            transaction.getCategory().getId() == null) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Category ID is required"
        );
    }

    Category category = categoryRepository
            .findById(transaction.getCategory().getId())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Category Not Found"
            ));

    transaction.setCategory(category);
    return transactionRepository.save(transaction);

Update follows the same lookup process and now changes:

- description
- amount
- transactionDate
- category

The category lookup logic is currently repeated in create and update. It works,
but a future cleanup can extract it into a private helper method.

Transaction JSON with a category relationship
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The client submits the selected category ID as a nested partial object:

    {
      "description": "Publix groceries",
      "amount": 85.42,
      "transactionDate": "2026-07-22",
      "category": {
        "id": 1
      }
    }

Spring creates a Transaction containing a partial Category with id = 1. The
service looks up category 1 and replaces the partial object with the complete
managed Category before saving.

The database stores:

    transactions.category_id = 1

The response contains the complete related category:

    {
      "id": 1,
      "description": "Publix groceries",
      "amount": 85.42,
      "transactionDate": "2026-07-22",
      "category": {
        "id": 1,
        "name": "Groceries",
        "type": "EXPENSE"
      }
    }

DTOs can later simplify the request to categoryId: 1, but the nested entity
format is acceptable for the current learning-stage API.

Database schema failure and resolution
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

After adding required transaction_date and category_id fields, Hibernate tried:

    alter table transactions
    add column transaction_date date not null

and:

    alter table transactions
    add column category_id bigint not null

PostgreSQL rejected both operations because existing transaction rows would
contain null values in the new columns:

    column "transaction_date" of relation "transactions" contains null values
    column "category_id" of relation "transactions" contains null values

Hibernate continued starting, but category_id was never created. A later POST
therefore failed with:

    column "category_id" of relation "transactions" does not exist

This was a schema mismatch, not an error in the controller or service.

Because the existing transactions were disposable development test data, the
transactions table was dropped and allowed to be recreated from the current
entity:

    docker exec -it finance-tracker-postgres-1 \
      psql -U finance_user -d finance_tracker \
      -c "DROP TABLE transactions;"

The command returned:

    DROP TABLE

The categories table was not dropped, so its records remained.

The direct SQL statement could not be entered into zsh by itself:

    DROP TABLE transactions;

That produced:

    zsh: command not found: DROP

SQL must be sent through a database client such as psql. Since psql was not
installed on the Mac, it was run inside the PostgreSQL Docker container.

After restarting Spring Boot, Hibernate recreated the transactions table. The
transaction POST using category ID 1 then worked successfully.

Production migration lesson
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Dropping the table was appropriate only because this was disposable development
data. A production migration would preserve records using this sequence:

1. Back up and verify the database backup.
2. Add new columns as nullable.
3. Backfill transaction dates and category IDs.
4. Investigate any rows that could not be mapped.
5. Confirm that no new-column values remain null.
6. Add NOT NULL constraints.
7. Add the foreign-key constraint.
8. Deploy and verify the new application.
9. Remove obsolete columns in a later cleanup release.

Production schema changes should be versioned with Flyway or Liquibase.
Hibernate should normally validate rather than automatically modify the
production schema:

    spring.jpa.hibernate.ddl-auto=validate

Git checkpoint
~~~~~~~~~~~~~~

The category and transaction-type work was committed and pushed using:

    git status
    git add src/main/java
    git status
    git commit -m "Add category CRUD and transaction type"
    git push

git add selects changes, git commit creates a local checkpoint, and git push
uploads commits to GitHub.

Current data-model flow
~~~~~~~~~~~~~~~~~~~~~~~

    HTTP transaction request
        -> TransactionController
        -> TransactionService
        -> CategoryRepository verifies category
        -> TransactionRepository saves transaction
        -> transactions.category_id references categories.id
        -> PostgreSQL

Current next steps
~~~~~~~~~~~~~~~~~~

1. Add monthly transaction filtering.
2. Add summary calculations for income, expenses, and balance.
3. Add automated tests.
4. Add consistent error-response details with @ControllerAdvice.
5. Introduce request/response DTOs after the entity-based API is understood.
6. Begin the React UI after the backend MVP is stable.


28. JULY 23 UPDATE: RELATIONSHIP TESTING AND CATEGORY SAFETY
------------------------------------------------------------

This session verified the complete Transaction CRUD flow after introducing the
required Category relationship. It then removed repeated service logic and
added rules that protect categories referenced by transactions.

Transaction CRUD verification
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

A transaction was created with the nested category-ID request format:

    curl -i -X POST http://localhost:8080/api/transactions \
      -H "Content-Type: application/json" \
      -d '{
        "description": "Publix groceries",
        "amount": 85.42,
        "transactionDate": "2026-07-22",
        "category": {
          "id": 1
        }
      }'

The relationship was updated with:

    curl -i -X PUT http://localhost:8080/api/transactions/1 \
      -H "Content-Type: application/json" \
      -d '{
        "description": "Monthly car insurance",
        "amount": 142.50,
        "transactionDate": "2026-07-23",
        "category": {
          "id": 3
        }
      }'

The response confirmed transaction 1 referenced category 3, Car Insurance.
DELETE returned 204 No Content, and GET all then returned an empty JSON list:

    []

The trailing percent sign sometimes displayed after curl output, such as []%,
was the zsh prompt. It was not part of the JSON. It appeared on the same line
because the response body did not end with a newline.

curl and HTTP methods
~~~~~~~~~~~~~~~~~~~~~

curl is the terminal program that sends an HTTP request. GET, POST, PUT, and
DELETE tell the server which operation to perform.

- curl by itself uses GET by default.
- -X explicitly selects a method.
- -H adds a request header.
- -d supplies request-body data.
- -i includes the response status and headers.

CRUD was reviewed as:

- Create -> POST
- Read -> GET
- Update -> PUT
- Delete -> DELETE

CRUD describes general stored-data operations. Summary calculations are
business logic built on top of CRUD data.

TransactionService refactoring
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

createTransaction and updateTransaction originally repeated the same steps:

1. Check that a category and category ID were supplied.
2. Find the Category in PostgreSQL.
3. Return 400 when the ID was missing.
4. Return 404 when the Category did not exist.

This logic was moved into one private helper:

    private Category getRequiredCategory(Transaction transaction) {
        if (transaction.getCategory() == null ||
                transaction.getCategory().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category ID is required"
            );
        }

        Optional<Category> result =
                categoryRepository.findById(
                        transaction.getCategory().getId()
                );

        if (result.isPresent()) {
            return result.get();
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Category Not Found"
        );
    }

createTransaction calls getRequiredCategory(transaction), while
updateTransaction calls getRequiredCategory(updatedTransaction). This
refactoring did not change behavior. It put one rule in one place.

Learning-style decision
~~~~~~~~~~~~~~~~~~~~~~~

Compact chained expressions using findById(...).orElseThrow(...) combine method
chaining, Optional, a lambda, exception construction, and return behavior.

The preferred learning style moving forward is:

- Explicit local variables
- Regular if statements
- Visible control flow
- Minimal method chaining
- No lambdas unless lambdas are intentionally being studied

Existing compact expressions do not have to be rewritten solely for style, but
new examples should favor expanded code that makes each step visible.

Spring Data derived queries
~~~~~~~~~~~~~~~~~~~~~~~~~~~

To determine whether a category is used by a transaction, TransactionRepository
added:

    boolean existsByCategory_Id(Long id);

Spring Data parses repository method names according to known conventions:

    exists      -> return whether a matching record exists
    By          -> start of the search criteria
    Category_Id -> Transaction.category.id

The underscore explicitly marks traversal from category to id. Repository
method property names follow Java entity properties, not arbitrary database
column names. @JoinColumn maps the relationship to category_id in PostgreSQL.

Camel case also helps Spring split derived-query names. For example:

    findByTransactionDateBetween

is interpreted as:

    findBy | TransactionDate | Between

Protecting categories that are in use
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

CategoryService now receives TransactionRepository:

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

deleteCategory first retrieves the category so a nonexistent ID returns 404.
It then checks:

    boolean isCategoryInUse =
            transactionRepository.existsByCategory_Id(id);

If the category is in use, the service throws 409 Conflict with:

    "Category cannot be deleted because it is used by transactions"

The rule was tested in both directions:

- Deleting used category 1 returned 409 Conflict.
- GET category 1 still returned 200, proving it was preserved.
- Deleting unused category 2 returned 204 No Content.
- GET category 2 then returned 404 Not Found.

Protecting category type
~~~~~~~~~~~~~~~~~~~~~~~~

Because Transaction type is derived from its required Category, changing an
in-use category from EXPENSE to INCOME would change historical transactions.

CategoryService.updateCategory now evaluates:

    boolean categoryIsInUse =
            transactionRepository.existsByCategory_Id(id);

    boolean typeIsChanging =
            existingCategory.getType() != updatedCategory.getType();

If both are true, the service returns 409 Conflict. The use of != is appropriate
because TransactionType is an enum.

The resulting rules are:

    Unused category -> name and type may change
    Used category   -> name may change, type may not change
    Used category   -> may not be deleted

Tests confirmed:

- Changing used category 1 from EXPENSE to INCOME returned 409.
- A subsequent GET showed that its name and type were unchanged.
- Renaming category 1 from Groceries to Food and Groceries while keeping
  EXPENSE returned 200.

Git checkpoints
~~~~~~~~~~~~~~~

The transaction-category relationship was committed and pushed:

    47c4c21 - Link transactions to categories

The refactoring and category protections were committed and pushed:

    4865f8a - Protect in-use categories and refactor category lookup

GitHub main was inspected after each push and confirmed to contain the expected
files and behavior.

Current verified state
~~~~~~~~~~~~~~~~~~~~~~

- Category CRUD works.
- Transaction CRUD works with transactionDate and required Category.
- Transaction type comes from Category.
- Transaction creation and update verify the category ID.
- Repeated category lookup is centralized in getRequiredCategory.
- Used categories cannot be deleted.
- Used categories cannot change type.
- Used categories can still be renamed.
- Unused categories can be deleted.
- All related changes are committed and pushed to GitHub main.

Next feature: monthly filtering
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The next planned repository method is:

    List<Transaction> findByTransactionDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

Required imports:

    import java.time.LocalDate;
    import java.util.List;

The intended endpoint is:

    GET /api/transactions?month=2026-07

The service will convert the requested month into:

    startDate = 2026-07-01
    endDate   = 2026-07-31

Monthly filtering was the planned next step at this checkpoint. It has since
been implemented and verified, as documented in the following update.


28. JULY 24 UPDATE - MONTH AND CATEGORY FILTERING
-------------------------------------------------

Transaction filtering was expanded so the same GET endpoint can optionally
filter by month, category, or both.

Supported requests
~~~~~~~~~~~~~~~~~~

The transaction endpoint now supports all four combinations:

    GET /api/transactions
    GET /api/transactions?month=2026-07
    GET /api/transactions?categoryId=1
    GET /api/transactions?month=2026-07&categoryId=1

The request parameters are optional. If neither parameter is supplied, the
endpoint retains its original behavior and returns all transactions.

Controller changes
~~~~~~~~~~~~~~~~~~

TransactionController now receives optional query parameters using
@RequestParam:

    @GetMapping
    public List<Transaction> getAllTransactions(
            @RequestParam(name = "month", required = false)
            YearMonth month,

            @RequestParam(name = "categoryId", required = false)
            Long categoryId) {

        if (month != null && categoryId != null) {
            return transactionService.getTransactionsByMonthAndCategory(
                    month,
                    categoryId
            );
        }

        if (month != null) {
            return transactionService.getTransactionsByMonth(month);
        }

        if (categoryId != null) {
            return transactionService.getTransactionsByCategory(categoryId);
        }

        return transactionService.getAllTransactions();
    }

@RequestParam reads values from the URL query string. For example:

    ?month=2026-07

is converted by Spring from text into a YearMonth object. Setting
required = false means the parameter may be omitted; its Java value is then
null.

The explicit name attribute separates the public URL parameter name from the
Java variable name:

    @RequestParam(name = "month", required = false) YearMonth month

Query-parameter names should be treated as exact and case-sensitive. "color",
"Color", and "colour" are different names. Java variable names conventionally
use camelCase.

The combined month-and-category condition must be checked first. Otherwise, a
request containing both parameters could return early through one of the
single-filter branches and ignore the other filter.

Month date-range conversion
~~~~~~~~~~~~~~~~~~~~~~~~~~~

YearMonth contains a year and month but no day. TransactionService converts it
into an inclusive LocalDate range:

    LocalDate startDate = month.atDay(1);
    LocalDate endDate = month.atEndOfMonth();

For July 2026:

    month     = 2026-07
    startDate = 2026-07-01
    endDate   = 2026-07-31

atEndOfMonth automatically handles months with 28, 29, 30, or 31 days,
including leap years.

The month-only service method is:

    public List<Transaction> getTransactionsByMonth(YearMonth month) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        return transactionRepository.findByTransactionDateBetween(
                startDate,
                endDate
        );
    }

Category and combined filtering
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The service also supports category-only filtering and combined filtering:

    public List<Transaction> getTransactionsByCategory(Long categoryId) {
        return transactionRepository.findByCategory_Id(categoryId);
    }

    public List<Transaction> getTransactionsByMonthAndCategory(
            YearMonth month,
            Long categoryId) {

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        return transactionRepository
                .findByTransactionDateBetweenAndCategory_Id(
                        startDate,
                        endDate,
                        categoryId
                );
    }

Repository derived queries
~~~~~~~~~~~~~~~~~~~~~~~~~~

TransactionRepository now contains:

    List<Transaction> findByTransactionDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    List<Transaction> findByCategory_Id(Long categoryId);

    List<Transaction> findByTransactionDateBetweenAndCategory_Id(
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId
    );

The combined method name tells Spring Data to query where:

    transactionDate is between startDate and endDate
    AND
    category.id equals categoryId

Category_Id is used because the request supplies a Long category ID. A method
ending in AndCategory would instead expect a complete Category object.

Verified behavior
~~~~~~~~~~~~~~~~~

The application was launched successfully from the new workspace copy. The
following request was tested:

    GET /api/transactions?month=2026-07

It returned the July transaction:

    id: 2
    description: Weekly groceries
    amount: 96.35
    transactionDate: 2026-07-23
    category id: 1
    category name: Food and Groceries
    category type: EXPENSE

The combined request was also tested:

    GET /api/transactions?month=2026-07&categoryId=1

It returned the same transaction, confirming that both the month and category
conditions were applied successfully through the complete flow:

    Controller -> Service -> Repository -> PostgreSQL

A nonmatching request was prepared as the empty-result test:

    GET /api/transactions?month=2025-01&categoryId=1

Git and workspace update
~~~~~~~~~~~~~~~~~~~~~~~~

The filtering work was committed and pushed successfully:

    fe218d7 - Add month and category transaction filtering

The repository was cloned into the ChatGPT project workspace and the new commit
was pulled. IntelliJ was then switched to that workspace copy so future code
can be inspected and tested more directly.

Workspace project path:

    /Users/nickh/.codex/.chatgpt-projects/
    g-p-6a559954196081919e96b6fe999ba3d6/finance-tracker

Opening a specific macOS path from a file chooser can be done with:

    Command + Shift + G

Current verified state
~~~~~~~~~~~~~~~~~~~~~~

- Category CRUD works.
- Transaction CRUD works.
- Transactions require valid categories.
- Used categories are protected from deletion and type changes.
- Transactions can be filtered by month.
- Transactions can be filtered by category ID.
- Month and category filters can be combined.
- Omitting both filters still returns all transactions.
- Filtering works against PostgreSQL.
- The changes are committed and pushed to GitHub main.

Next feature: monthly summary
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The immediate next feature is:

    GET /api/summary?month=2026-07

It will return monthly totals for:

    income
    expenses
    balance = income - expenses

The planned first step is to create a MonthlySummary response model, followed
by summary calculation logic and a summary controller endpoint. The completed
month-filtering logic can be reused to retrieve the transactions included in
the calculation.


29. AUGUST 1 UPDATE - REACT FRONTEND API CONNECTION
---------------------------------------------------

The first React frontend checkpoint was added using Vite. The frontend now
includes a small API client that requests transactions from:

    GET /api/transactions

Vite proxies `/api` requests to the Spring Boot backend at
`http://localhost:8080`, allowing the React development server to call the
backend without hard-coding a second browser-facing origin. The initial App
component provides loading, error, and success states and displays the returned
transaction JSON as a temporary integration checkpoint.

Project continuity instructions were added in `AGENTS.md`. They require future
agent work to remain read-only until explicitly authorized and require this
project source to be reviewed before meaningful project work.

Repository cleanup removed an accidentally tracked local worktree Gitlink and
added `.worktrees/` to `.gitignore`. An invisible non-breaking space in the API
client was replaced so the frontend lint check succeeds.

Verification completed on August 1, 2026:

- `npm run lint` passed.
- `npm run build` passed with 18 modules transformed.
- `./mvnw test` passed with 1 test, 0 failures, and 0 errors.
- The React production bundle built successfully.
- The Spring application context connected to PostgreSQL successfully.

Current verified state:

- The existing backend CRUD and filtering implementation remains intact.
- The React/Vite frontend scaffold is present.
- The frontend can request transactions through the Vite development proxy.
- Frontend linting and production build pass.
- Local worktree metadata is excluded from Git.

Next recommended step:

Replace the temporary JSON transaction display with the first finance dashboard
view, beginning with monthly summary cards and a structured transaction list.


END OF MASTER NOTES
===================
