This project is test task from Brdo company.

## Description

This project is a complex app with spring boot backend and react frontend.
For Security and auth I used JWT tokens.

As template I use one of my previous projects that is still not finished: https://github.com/DaNaRim/monal-money-analyzer
So, there maybe some redundant code.

## Running

Before run locally you need to add 'secrets-dev.properties' and 'secrets.properties' file to src/main/resources folder.
This file should contain next properties:

* secrets.database-url - database url
* secrets.database-username - database username
* secrets.database-password - database password
* secrets.jwtSecret - secret for JWT tokens

I also recommend to use 'dev' profile. You can do it by adding '-Dspring.profiles.active=dev' to VM options.

To run in Docker you also need to add .env file to root folder. This file should contain next properties:

* DATABASE_URL - database url
* DATABASE_USERNAME - database username
* DATABASE_PASSWORD - database password

Make sure that credentials in secrets.properties and .env are the same.

## Testing 

There are too types of tests: unit and integration. Unit tests are located in src/test/java folder.
Integration tests are located in src/it folder.

I prepared a configuration to run tests. There are in .run folder. You can use it to run tests in IntelliJ IDEA.

Attention! Before run integration tests you need to run docker in your machine.
That`s because integration tests use docker to run database.
Also, you need to set an active profile to 'test'. You can do it by adding '-Dspring.profiles.active=test' to VM options.
