# 部署到服务器

后端 Spring Boot 打成可执行 jar，两个前端打成静态文件，Nginx 统一对外。下面给两种方式，**推荐先看完「部署前必须知道」再选**。

---

## 一、部署前必须知道的 4 件事

| 事项 | 现状 | 上线前要做什么 |
|---|---|---|
| **数据库** | 默认 H2 内存库，重启数据全丢 | 必须切 MySQL：`SPRING_PROFILES_ACTIVE=mysql`，并建库（见下） |
| **登录态** | `TokenService` 用进程内存存 token | 重启后所有人需重新登录；多实例部署会串号。单机够用，要扩机器得换 JWT/Redis |
| **H5 接口地址** | 已改为「localhost 直连 8080，其他域名走同域相对路径」 | 只要 H5 和接口同域（Nginx 反代），无需改代码 |
| **演示数据** | 默认灌 10 个商品 + 30 个防伪码 | 生产设 `DEMO_DATA=false`，商品在管理端自行维护 |

端口规划：

```
80     -> Web 管理端（静态 + /admin-api 反代）
8081   -> H5 客户端（静态 + /api 反代）
8080   -> 后端，只监听 127.0.0.1，不对外
```

---

## 二、方式一：传统部署（systemd + Nginx + MySQL）

适用于 1C2G~2C4G 的普通云服务器，Ubuntu 22.04 / Debian 12 为例。

### 1. 服务器装环境

```bash
sudo apt update
sudo apt install -y openjdk-21-jre-headless mysql-server nginx curl

# 确认版本
java -version          # 需要 21.x
mysql --version        # 8.0.x
nginx -v
```

> CentOS / Rocky：`dnf install java-21-openjdk-headless mysql-server nginx`。

### 2. 本地打包（Windows 开发机）

```bash
deploy\build.cmd
```

产物在 `dist\`：

```
dist/app.jar    后端可执行 jar
dist/web/       Web 管理端静态文件
dist/h5/        H5 客户端静态文件
```

Linux/macOS 用 `bash deploy/build.sh`，产物一致。

### 3. 上传

```bash
ssh user@your-server "sudo mkdir -p /opt/qingguoji /var/log/qingguoji && sudo chown -R \$USER /opt/qingguoji /var/log/qingguoji"
scp -r dist/* user@your-server:/opt/qingguoji/
```

最终目录：

```
/opt/qingguoji/
├── app.jar
├── web/          # 管理端
└── h5/           # H5
```

### 4. 建库

```bash
mysql -uroot -p < deploy/mysql/init.sql
# 记得把脚本里的 change-me 换成强口令
```

表结构由 JPA 首次启动自动创建（`ddl-auto=update`）。稳定后可改成 `validate`，后续变更写 SQL 迁移。

### 5. 创建运行账号 + systemd

```bash
sudo useradd -r -s /sbin/nologin qingguoji
sudo chown -R qingguoji:qingguoji /opt/qingguoji /var/log/qingguoji

sudo cp deploy/qingguoji-mall.service /etc/systemd/system/
sudo vi /etc/systemd/system/qingguoji-mall.service   # 改 DB_PASSWORD
sudo systemctl daemon-reload
sudo systemctl enable --now qingguoji-mall

# 看状态与日志
systemctl status qingguoji-mall
tail -f /var/log/qingguoji/backend.log
```

健康检查：

```bash
curl -s http://127.0.0.1:8080/actuator/health
# {"status":"UP"}
```

### 6. Nginx

```bash
sudo cp deploy/nginx/qingguoji.conf /etc/nginx/conf.d/
sudo vi /etc/nginx/conf.d/qingguoji.conf   # 把 your-domain.com 换成你的域名或公网 IP
sudo nginx -t && sudo systemctl reload nginx
```

访问：

- 管理端 `http://你的域名/`（admin / admin123，**上线第一件事就是改密码**）
- H5 `http://你的域名:8081/`

> 只放行 80 / 8081 / 22，8080 不要开公网安全组。

### 7. HTTPS（有域名就做）

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
sudo certbot renew --dry-run
```

### 8. 升级与回滚

```bash
# 升级
deploy\build.cmd
scp dist/app.jar user@server:/opt/qingguoji/app.jar
scp -r dist/web/* user@server:/opt/qingguoji/web/
sudo systemctl restart qingguoji-mall   # 只重启后端；纯前端改动只需 reload nginx

# 回滚：保留上一版 jar，换回来再 restart
cp /opt/qingguoji/app.jar /opt/qingguoji/app.jar.bak   # 升级前先备份
```

---

## 三、方式二：Docker Compose（一条命令）

服务器只需装 Docker + Compose 插件，MySQL、后端、Nginx 全部由 compose 管理。

```bash
cd deploy
cp .env.example .env
vi .env                 # 改 MYSQL_ROOT_PASSWORD / DB_PASSWORD

docker compose up -d --build
docker compose ps
docker compose logs -f backend
```

访问同上：`http://服务器IP/` 和 `http://服务器IP:8081/`。

要点：

- `mysql` 数据在 `mysql-data` 卷里，`docker compose down` 不会删，`down -v` 才会。
- 后端 `8080` 只绑 `127.0.0.1`，外网进不来。
- 镜像构建会拉 `maven` / `node` / `nginx` 基础镜像，国内建议先配加速器。
- 首次构建要下载全部依赖，5~15 分钟属正常。

常用运维：

```bash
docker compose restart backend        # 重启后端
docker compose up -d --build backend  # 只重建后端
docker compose down                   # 停全部（保留数据）
```

---

## 四、上线验收清单

按顺序打勾，全部通过再交付：

- [ ] `curl http://127.0.0.1:8080/actuator/health` 返回 `UP`
- [ ] 管理端能登录，看板 5 个指标有数字
- [ ] 商品列表有数据，上下架按钮生效（刷新 H5 首页应立即看不到该商品）
- [ ] 库存调整后，`可售库存` 数字变化正确
- [ ] H5 首页能加载商品（说明 `/api` 反代通了）
- [ ] H5 用任意手机号 + `123456` 能登录
- [ ] 下单 → 支付 → 积分增加，三步都成功
- [ ] 管理端发货 → H5 确认收货，状态流转正确
- [ ] 申请售后 → 管理端审核通过 → 确认收货，退款与积分扣回正确
- [ ] 重启服务后数据仍在（证明走的是 MySQL 不是 H2）
- [ ] 管理端密码已改（`Constants.ADMIN_PASSWORD` 或后续接入数据库账号表）
- [ ] 公网无法直接访问 8080

---

## 五、常见问题

**Q：启动报 `Access denied for user`**
`.env` / systemd 里的 `DB_PASSWORD` 与 `deploy/mysql/init.sql` 建的用户口令不一致；或 MySQL 用户 host 不是 `%`。

**Q：管理端能打开但接口 502**
后端没起来。看 `journalctl -u qingguoji-mall -n 100` 或 `docker compose logs backend`。最常见是 JDK 版本低于 21。

**Q：H5 打开白屏**
Nginx `root` 指错目录，或 `try_files` 没配 `index.html`。检查 `/opt/qingguoji/h5/index.html` 是否存在。

**Q：H5 能打开但请求失败**
`src/api.ts` 在非 localhost 域名下走相对路径，必须由 Nginx 把 `/api/` 反代到后端。检查 `location /api/` 是否存在。

**Q：上传图片/大文件 413**
调 `client_max_body_size`（默认配置已设 20m）。

**Q：内存不够，容器被杀**
把 `JAVA_OPTS` 的 `-Xmx` 降到 384m，或加 swap：

```bash
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile
sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

**Q：`mvn verify` 报 spotless 格式错误**
本地跑 `mvn spotless:apply` 后再提交。CI 里可用 `-Dspotless.check.skip=true` 跳过。

---

## 六、上线前的安全整改（这个演示项目默认不安全）

| 风险 | 位置 | 建议 |
|---|---|---|
| 管理员密码硬编码 `admin/admin123` | `common/constant/Constants.java` | 改为数据库账号表 + BCrypt，或至少用环境变量注入 |
| 短信验证码固定 `123456` | `Constants.DEMO_SMS_CODE` | 接真实短信服务商，验证码存 Redis 带 5 分钟过期 |
| token 存内存无过期 | `framework/web/service/TokenService.java` | 换 JWT（HS512 + 7 天）或 Redis 存储 |
| 支付为模拟 | `OrderServiceImpl#payOrder` | 接微信支付/支付宝，回调校验签名 |
| H2 控制台 | `H2_CONSOLE` | 生产必须 `false`（mysql profile 已默认关闭） |
| 全量 CORS `*` | `framework/config/WebMvcConfig.java` | 生产改为白名单域名 |
