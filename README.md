# An example service built on Tagless Final

A small example demonstrating how to make use of *Tagless Final* in order to delay the decision
of choosing a concrete effect (like `Future` or `Task`) till the end and in order to provide easily
swappable implementation of effects. 

Here, we delay the implementation of `OrderRepository` to the "end of the world". The `OrderService` that depends on the 
abstract `OrderRepository` makes a requirement that `OrderRepository` needs to have an effect that can support 
sequencing via the `Monad` typeclass constraint (`F[_]: Monad`) since it needs to sequence actions (i.e. in order to 
perform an update, it needs to sequence `get` and `put`).

There are two interpreters:
- An in-memory implementation backed by the `Id` monad
- A Postgres implementation backed by Slick and Monix `Task`

## Setup instructions for PostgreSQL
Use `docker-compose up` to bring up a Dockerized PostgreSQL.

Log in to the default `postgres` database (username = `docker`, password = `docker`)
```sql
CREATE DATABASE calvin;
GRANT ALL PRIVILEGES ON DATABASE calvin TO docker;
```

Log into the `calvin` database (username = `docker`, password = `docker`). 
Make sure you use `calvin.<no-schema>`
```sql
CREATE SCHEMA IF NOT EXISTS example;
```

The `PostgresApp` will automatically take care of creating the table in the provided
database and schema. 
