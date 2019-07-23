# Prerequisites 
## Installation requirements
* docker 
* docker-compose

## Monitoring Docker
You can check out the official documentation on [Docker docs](https://docs.docker.com/config/thirdparty/prometheus/)

# Start Monitoring stack
* ```docker-compose up -d```, where `-d` is responsible for launching it in the background.

# Prometheus Lifecycle
Since the .yml-file is now linked within the docker-container, you can simply modify it and then use ```docker restart prometheus-cineast``` to load the new config.

# Grafana

## Setup
* Default login is admin/admin
* Add a Data Source named `prom-localhost` of Type `Prometheus` with URL `http://$prometheusURL:9090` and Access `Browser`. Be aware that `localhost` here would mean the node accessing grafana, not the one grafana is running on.
* Use the saved grafana-dashboard for an overview of metrics. You can import it as JSON.

# Netdata
Netdata is optional - but it can be neatly integrated into prometheus/grafana.

* Install Netdata from [Github](https://github.com/firehol/netdata/)
* That's it.