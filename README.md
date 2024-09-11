# rate-limiter-tests-distributed

Tests rate limiting a distributed service.

### Why

The essence is to test that specified rate limits apply across multiple instances of a 
single service, when they share the same `BandwidthsStore`.

There are multiple services:

- `message-server` - A server for messages _multiple instances_
- `message-client` - client for message-server
- `redis-cache` - shared cache for message-server instances
- `nginx` - load balancer for message-server instances
- `user-serivce` - A JAX RS web application. 

### How

To build, run the script `shell/build.sh`.

To run the application, run the script `shell/run.docker.sh`.

Some endpoints in `message-server` are rate limited. These endpoints are called by 
`message-client` during the tests. To run the tests open the `message-client` home
and click the link provided. A detailed log is returned after the tests are run.

To shut down all, run `docker-compose down`

### Supported environment variables

See [Supported environment variables](docs/env.md)

