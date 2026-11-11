kubectl delete service smrp-service
kubectl delete deployment smrp-micronaut

mvnw clean package jib:dockerBuild "-Dimage=smrp-micronaut:1.5" "-DskipTests" "-Dpackaging=docker"

docker tag smrp-micronaut:1.5 docker.io/wfsiew888/smrp-micronaut:1.5
docker push docker.io/wfsiew888/smrp-micronaut:1.5

kubectl apply -f deployment.yaml