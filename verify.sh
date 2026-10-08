#!/usr/bin/env bash
# Quick health check for all services

set -e

echo "=== Docker Infrastructure ==="
docker compose ps

echo ""
echo "=== Ports in use ==="
for port in 8761 8888 8080 8081 5432; do
  pid=$(lsof -t -i:$port 2>/dev/null || true)
  if [ -n "$pid" ]; then
    echo "✅ Port $port: PID $pid"
  else
    echo "❌ Port $port: FREE (nothing running)"
  fi
done

echo ""
echo "=== Health Endpoints ==="
check() {
  local name=$1 url=$2
  local status=$(curl -s -o /dev/null -w "%{http_code}" "$url" || echo "000")
  if [ "$status" = "200" ]; then
    echo "✅ $name → HTTP $status"
  else
    echo "❌ $name → HTTP $status  ($url)"
  fi
}

check "discovery-server" "http://localhost:8761/actuator/health"
check "config-server"    "http://localhost:8888/actuator/health"
check "api-gateway"      "http://localhost:8080/actuator/health"
check "user-service"     "http://localhost:8081/actuator/health"

echo ""
echo "=== Eureka Registered Instances ==="
curl -s http://localhost:8761/eureka/apps -H "Accept: application/json" \
  | grep -oE '"name":"[^"]*"' | sort -u

echo ""
echo "=== Eureka Registered Instances (expected: 3) ==="
curl -s http://localhost:8761/eureka/apps -H "Accept: application/json" \
  | grep -oE '"name":"[^"]*"' | sort -u