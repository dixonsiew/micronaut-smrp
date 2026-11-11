kubectl delete service smrp-service
kubectl delete deployment smrp-micronaut

mvnw clean package jib:dockerBuild "-Dimage=smrp-micronaut:1.6" "-DskipTests" "-Dpackaging=docker"

docker tag smrp-micronaut:1.6 docker.io/wfsiew888/smrp-micronaut:1.6
docker push docker.io/wfsiew888/smrp-micronaut:1.6

kubectl apply -f deployment.yaml