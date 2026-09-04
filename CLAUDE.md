# Project rules for java-taf-template

## Test isolation: one fresh application container per test

Every test starts its own OpenL Studio or Rule Services container and disposes of it afterwards. Reusing an
application container between tests, between test classes, or between shards is strictly forbidden. This
applies to the framework (`BaseTest`, `AppContainer`, `DockerDriverPool`), to any CI optimization on Jenkins or
GitHub Actions, and to local runs. Do not propose or implement container pooling, warm containers shared by a
class, or "start once per suite" schemes: the cost of container start-up is an accepted price for isolation.
Speed-ups must come from parallelism, sharding and balancing, never from sharing application state.
