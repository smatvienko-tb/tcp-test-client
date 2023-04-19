apt update
apt install -y git maven htop
git clone https://github.com/smatvienko-tb/tcp-test-client.git
cd tcp-test-client
export TARGET_HOST=52.50.5.45
export TARGET_PORT=1883
export TARGET_CLIENTS=1234
mvn spring-boot:run


docker buildx create --use --bootstrap \
--name mybuilderarm \
--driver docker-container

docker buildx build --platform linux/amd64,linux/arm64 -t sevlamat/tcp-test-client --push .

docker build -t sevlamat/tcp-test-client .
docker push sevlamat/tcp-test-client


Finally, let's config the `kube-proxy` to track one million connections.
```bash
kubectl edit -n kube-system configmap/kube-proxy-config
# conntrack:
#   min: 1048576
kubectl rollout restart -n kube-system daemonset kube-proxy
```

Wait for a `kube-proxy` restart on all nodes and check the max connections adjusted.

```bash
kubectl get pods -w -n kube-system
# Ctrl + C when all in Running state
kubectl exec -it tb-mqtt-transport-0 -- sysctl -a | grep conntrack_max
# net.netfilter.nf_conntrack_max = 1048576
```

cat /proc/sys/net/ipv4/ip_local_port_range
#32768	60999
echo "net.ipv4.ip_local_port_range = 1024 65535" | sudo tee -a /etc/sysctl.conf
sudo -s sysctl -p
