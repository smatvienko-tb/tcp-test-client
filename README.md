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

To create a **simple cluster** use a single command:

```bash
eksctl create cluster \
  --version=1.25 \
  --name performance-test \
  --region eu-west-1 \
  --nodegroup-name linux-arm64 \
  --node-volume-type gp3 \
  --node-type c7g.medium \
  --nodes 6 \
  --ssh-access \
  --ssh-public-key smatvienko \
  --tags environment=performance-test,owner=smatvienko
```
{: .copy-code}

Finally, let's config the `kube-proxy` to track one million connections.
```bash
kubectl edit -n kube-system configmap/kube-proxy-config
# conntrack:
#   min: 16777216
# portRange: "1025-65535"
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
echo "net.ipv4.ip_local_port_range = 1025 65535" | sudo tee -a /etc/sysctl.conf
sudo -s sysctl -p
