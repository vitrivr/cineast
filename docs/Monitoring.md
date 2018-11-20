# Prometheus
## Starting a prometheus instance

* Install docker

### Ubuntu
* ```docker run --name prometheus-cineast -d -v /abs/path/to/cineast/docs/prometheus.yml:/etc/prometheus/prometheus.yml --network host prom/prometheus```

Small note: we could also set up a bridge network with a fixed IP for the host machine, but just using the host NW for this container is easier.

### Mac
* ```docker run --name prometheus-cineast -d -v /abs/path/to/cineast/docs/prometheus_mac.yml:/etc/prometheus/prometheus.yml -p 9090:9090 prom/prometheus```
We're not using host mode since it doesn't work on mac.
## Lifecycle
Since the .yml-file is now linked within the docker-container, you can simply modify it and then use ```docker restart prometheus-cineast``` to load the new config.

# Grafana
## Starting an instance

* Install docker
* ```docker run -d -p 3000:3000 grafana/grafana```
* Default login is admin/admin

## Setup
* Use the saved grafana-dashboard for an overview of metrics
* Add a Data Source named `prom-localhost` of Type `Prometheus` with URL `http://localhost:9090` and Access `Browser`

# Netdata
Netdata is optional - but it can be neatly integrated into prometheus/grafana.

* Install Netdata from [Github](https://github.com/firehol/netdata/)
* That's it.