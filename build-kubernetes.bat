kubectl delete service smrp-service
kubectl delete deployment smrp-micronaut

mvnw clean package jib:dockerBuild "-Dimage=docker.io/wfsiew888/smrp-micronaut:1.21" "-DskipTests" "-Dpackaging=docker"

docker tag smrp-micronaut:1.21 docker.io/wfsiew888/smrp-micronaut:1.21
docker push docker.io/wfsiew888/smrp-micronaut:1.21

kubectl apply -f deployment.yaml