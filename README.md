## k8-spark-connector-test

### Prerequisites:
* Access to an existing K8 cluster (K8 master API URI)
* Access to an existing DSE cluster (IP address of a DSE node)
* sbt installed in your machine (https://www.scala-sbt.org)
* IntelliJ IDE (optional)

### Build, Run and Monitor the spark job:

* Download and install Apache Spark (2.4.0) - https://spark.apache.org/downloads.html
* Download the sample Scala spark application using spark-cassandra-connector
```
$ git clone https://github.com/gmflau/k8-spark-connector-test 
$ cd k8-spark-connector-test
```

### Build the Uber jar (a.k.a. fat jar) as it is required to use with spark-submit command
```
$ sbt assembly (will generate a fat jar under here: ./target/scala-2.11)
```
Upload the the fat jar to some HTTP server accessible from the K8 cluster env.
For example, I uploaded my fat jar to an Azure’s storage bucket:
https://glau.blob.core.windows.net/spark-job/k8-spark-connector-test-assembly-0.1.jar


### Modify existing scala source or expand the application
Scala source is located in this folder -> ./src/main/scala/datastax/
```
Run $ sbt assembly 
```
Upload the fat jar to some HTTP server accessible from the K8 cluster env.

### Collect the K8 master API
```
$ kubectl cluster-info
Kubernetes master is running at https://k8-1117-ak-gml-aks-rg-f26e06-05524b44.hcp.westus2.azmk8s.io:443
```


### Collect IP address of one DSE node
```
$ kubectl get pod dse-0 -owide
NAME      READY     STATUS    RESTARTS   AGE       IP            NODE                       NOMINATED NODE
dse-0     1/1       Running   0          1d        172.19.1.73   aks-nodepool1-14246748-0   <none>

```

### Build the required Spark docker image
Inside Apache spark installation folder (my case: /Users/gilbertlau/spark-2.4.0-bin-hadoop2.7)
```
$ ./bin/docker-image-tool.sh -r gcr.io/datastax-public/spark -t 2.4.0 build
$ ./bin/docker-image-tool.sh -r gcr.io/datastax-public/spark -t 2.4.0 push
```


### Set up the K8 service account and clusterrolebinding for the Spark cluster to create K8 resources
```
$ kubectl create serviceaccount spark
$ kubectl create clusterrolebinding spark-role --clusterrole=edit --serviceaccount=default:spark --namespace=default
```


### Submitting our application as Spark application to the K8 cluster
For example, Apache Spark is installed at /Users/gilbertlau/spark-2.4.0-bin-hadoop2.7:
```
$ /Users/gilbertlau/spark-2.4.0-bin-hadoop2.7/bin/spark-submit \
   --master k8s://https://k8-1117-ak-gml-aks-rg-f26e06-05524b44.hcp.westus2.azmk8s.io:443 \
   --conf spark.cassandra.connection.host=172.19.1.73 \
   --class datastax.HelloWorld \
   --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark  \
   --deploy-mode cluster \
   --name spark-dse \
   --conf spark.executor.instances=20 \
   --conf spark.kubernetes.container.image=gcr.io/datastax-public/spark/spark:2.4.0 \
   https://glau.blob.core.windows.net/spark-job/k8-spark-connector-test-assembly-0.1.jar
```


### Monitor the spark jobs as K8 resources
```
$ kubectl get pods (to local the driver) 
$ kubectl logs -f spark-dse-xxxxxxxx-driver
```
The spark-dse-xxxxxxxx-driver pod’s STATUS should come to “Completed” if the run is successful

