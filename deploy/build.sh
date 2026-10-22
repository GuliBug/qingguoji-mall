#!/usr/bin/env bash
# ============================================================
#  青果纪商城 - 生产包构建脚本（Linux / macOS / Git Bash）
#  产物输出到 dist/：app.jar + web/ + h5/
# ============================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

NPM_REGISTRY="${NPM_REGISTRY:-https://registry.npmmirror.com}"

echo "[1/5] 检查构建环境"
command -v java >/dev/null || { echo "[ERROR] 未找到 java"; exit 1; }
java -version 2>&1 | head -1
if command -v mvn >/dev/null; then
  MVN=mvn
elif [[ -x backend/mvnw ]]; then
  MVN=backend/mvnw
else
  echo "[ERROR] 未找到 mvn 或 backend/mvnw"; exit 1
fi

echo "[2/5] 构建后端 jar"
(cd backend && "$MVN" -B -q clean package -DskipTests -Dspotless.check.skip=true)

echo "[3/5] 构建 Web 管理端"
(cd frontend-web && { [[ -d node_modules ]] || npm install --registry="$NPM_REGISTRY" --no-audit --no-fund; } && npm run build:prod)

echo "[4/5] 构建 H5 客户端"
(cd frontend-h5/mall-client && { [[ -d node_modules ]] || npm install --registry="$NPM_REGISTRY" --no-audit --no-fund; } && npm run build:h5)

echo "[5/5] 汇总产物"
rm -rf dist && mkdir -p dist/web dist/h5
cp backend/target/mall-backend-0.0.1-SNAPSHOT.jar dist/app.jar
cp -r frontend-web/dist/. dist/web/
cp -r frontend-h5/mall-client/dist/. dist/h5/

cat <<'EOF'

============================================================
 构建完成：dist/
   dist/app.jar   后端可执行 jar
   dist/web/      Web 管理端静态文件
   dist/h5/       H5 客户端静态文件

 上传到服务器：
   rsync -avz dist/ user@your-server:/opt/qingguoji/
============================================================
EOF
