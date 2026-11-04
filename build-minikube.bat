mvnw package jib:dockerBuild "-Dimage=smrp:1.0"
kubectl delete deployment smrp
kubectl apply -f deployment.yaml