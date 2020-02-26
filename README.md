# org.kittrellcodeworks.db
This project is intended to be an exercise in creating a reusable
database abstraction layer. Our goals are:

1. Use the Factory pattern to allow injection of concrete instances
at runtime.
2. Define a Transaction Monad to encapsulate database functions
that can be atomically chained and rolled back upon failure.
3. Define a generic Query domain specific language.
4. Explore the Repository pattern as a layer above Database abstraction.

To this end, there are 4 sub-projects at this time:

1. *db* - Defines traits, monads and the Query DSL.
2. *db-mem* - Provides in-memory implementations.
3. *db-mongo* - Provides reactive-mongo implementations.
4. *db-solr* - Provides solrCloud implementations.
