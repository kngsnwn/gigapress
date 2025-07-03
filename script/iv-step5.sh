#!/bin/bash

# Step 5: Templates Creation
# This script creates all template files

SERVICE_DIR="services/infra-version-control-service"

echo "📝 Step 5: Creating templates..."

# Create Docker templates directory structure
mkdir -p ${SERVICE_DIR}/app/templates/docker/dockerignore
mkdir -p ${SERVICE_DIR}/app/templates/kubernetes
mkdir -p ${SERVICE_DIR}/app/templates/cicd
mkdir -p ${SERVICE_DIR}/app/templates/git/gitignore
mkdir -p ${SERVICE_DIR}/app/templates/terraform
mkdir -p ${SERVICE_DIR}/app/templates/monitoring

# Docker Dockerfile template
cat > ${SERVICE_DIR}/app/templates/docker/Dockerfile.j2 << 'EOF'
FROM {{ base_image }}

WORKDIR {{ workdir }}

{% if labels %}
# Labels
{% for key, value in labels.items() %}
LABEL {{ key }}="{{ value }}"
{% endfor %}
{% endif %}

{% if env_vars %}
# Environment variables
{% for key, value in env_vars.items() %}
ENV {{ key }}={{ value }}
{% endfor %}
{% endif %}

# Install dependencies and copy application
{% for command in commands %}
{{ command }}
{% endfor %}

{% if ports %}
# Expose ports
{% for port in ports %}
EXPOSE {{ port }}
{% endfor %}
{% endif %}

{% if entrypoint %}
# Set entrypoint
ENTRYPOINT {{ entrypoint | tojson }}
{% else %}
# Default command
CMD ["node", "server.js"]
{% endif %}
EOF

# Docker dockerignore templates
cat > ${SERVICE_DIR}/app/templates/docker/dockerignore/node.dockerignore.j2 << 'EOF'
node_modules
npm-debug.log
.env
.env.local
.env.*.local
.git
.gitignore
README.md
.DS_Store
coverage
.nyc_output
.vscode
.idea
*.swp
*.swo
*~
EOF

cat > ${SERVICE_DIR}/app/templates/docker/dockerignore/python.dockerignore.j2 << 'EOF'
__pycache__
*.pyc
*.pyo
*.pyd
.Python
pip-log.txt
pip-delete-this-directory.txt
.tox/
.coverage
.coverage.*
.cache
coverage.xml
*.cover
*.log
.git
.gitignore
.mypy_cache
.pytest_cache
.hypothesis
.env
venv/
ENV/
.vscode
.idea
EOF

cat > ${SERVICE_DIR}/app/templates/docker/dockerignore/java.dockerignore.j2 << 'EOF'
.gradle
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/build/
!**/src/test/**/build/
.idea
*.iws
*.iml
*.ipr
out/
!**/src/main/**/out/
!**/src/test/**/out/
.vscode
.git
.gitignore
EOF

# Kubernetes templates
cat > ${SERVICE_DIR}/app/templates/kubernetes/deployment.yaml.j2 << 'EOF'
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ name }}
  namespace: {{ namespace }}
  labels:
    app: {{ name }}
    {% for key, value in labels.items() %}
    {{ key }}: {{ value }}
    {% endfor %}
  {% if annotations %}
  annotations:
    {% for key, value in annotations.items() %}
    {{ key }}: {{ value }}
    {% endfor %}
  {% endif %}
spec:
  replicas: {{ replicas }}
  selector:
    matchLabels:
      app: {{ name }}
  template:
    metadata:
      labels:
        app: {{ name }}
        {% for key, value in labels.items() %}
        {{ key }}: {{ value }}
        {% endfor %}
    spec:
      containers:
      - name: {{ name }}
        image: {{ image }}
        {% if ports %}
        ports:
        {% for port in ports %}
        - containerPort: {{ port }}
          protocol: TCP
        {% endfor %}
        {% endif %}
        {% if env_vars %}
        env:
        {% for key, value in env_vars.items() %}
        - name: {{ key }}
          value: "{{ value }}"
        {% endfor %}
        {% endif %}
        {% if resources %}
        resources:
          {% if resources.limits %}
          limits:
            {% for key, value in resources.limits.items() %}
            {{ key }}: {{ value }}
            {% endfor %}
          {% endif %}
          {% if resources.requests %}
          requests:
            {% for key, value in resources.requests.items() %}
            {{ key }}: {{ value }}
            {% endfor %}
          {% endif %}
        {% endif %}
        livenessProbe:
          httpGet:
            path: /health
            port: {{ ports[0] if ports else 8080 }}
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /ready
            port: {{ ports[0] if ports else 8080 }}
          initialDelaySeconds: 5
          periodSeconds: 5
EOF

cat > ${SERVICE_DIR}/app/templates/kubernetes/service.yaml.j2 << 'EOF'
apiVersion: v1
kind: Service
metadata:
  name: {{ name }}
  namespace: {{ namespace }}
spec:
  type: {{ type }}
  selector:
    {% for key, value in selector.items() %}
    {{ key }}: {{ value }}
    {% endfor %}
  ports:
  {% for port in ports %}
  - port: {{ port.port }}
    targetPort: {{ port.targetPort }}
    protocol: {{ port.protocol | default('TCP') }}
    {% if port.name %}
    name: {{ port.name }}
    {% endif %}
  {% endfor %}
EOF

cat > ${SERVICE_DIR}/app/templates/kubernetes/ingress.yaml.j2 << 'EOF'
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: {{ name }}
  namespace: {{ namespace }}
  {% if annotations %}
  annotations:
    {% for key, value in annotations.items() %}
    {{ key }}: "{{ value }}"
    {% endfor %}
  {% endif %}
spec:
  {% if tls %}
  tls:
  - hosts:
    - {{ host }}
    secretName: {{ tls.secret_name | default(name + '-tls') }}
  {% endif %}
  rules:
  - host: {{ host }}
    http:
      paths:
      {% for path in paths %}
      - path: {{ path.path }}
        pathType: {{ path.pathType | default('Prefix') }}
        backend:
          service:
            name: {{ path.backend.service.name }}
            port:
              number: {{ path.backend.service.port.number }}
      {% endfor %}
EOF

cat > ${SERVICE_DIR}/app/templates/kubernetes/configmap.yaml.j2 << 'EOF'
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ name }}
  namespace: {{ namespace }}
data:
  {% for key, value in data.items() %}
  {{ key }}: |
{{ value | indent(4) }}
  {% endfor %}
EOF

cat > ${SERVICE_DIR}/app/templates/kubernetes/secret.yaml.j2 << 'EOF'
apiVersion: v1
kind: Secret
metadata:
  name: {{ name }}
  namespace: {{ namespace }}
type: Opaque
data:
  {% for key, value in data.items() %}
  {{ key }}: {{ value | b64encode }}
  {% endfor %}
EOF

cat > ${SERVICE_DIR}/app/templates/kubernetes/namespace.yaml.j2 << 'EOF'
apiVersion: v1
kind: Namespace
metadata:
  name: {{ name }}
EOF

cat > ${SERVICE_DIR}/app/templates/kubernetes/hpa.yaml.j2 << 'EOF'
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: {{ name }}
  namespace: {{ namespace }}
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: {{ deployment }}
  minReplicas: {{ min_replicas }}
  maxReplicas: {{ max_replicas }}
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: {{ target_cpu }}
EOF

cat > ${SERVICE_DIR}/app/templates/kubernetes/pvc.yaml.j2 << 'EOF'
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: {{ name }}
  namespace: {{ namespace }}
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: {{ size }}
  storageClassName: {{ storage_class }}
EOF

# CI/CD templates
cat > ${SERVICE_DIR}/app/templates/cicd/github-actions.yml.j2 << 'EOF'
name: {{ name }}

on:
  {% for trigger, config in triggers.items() %}
  {{ trigger }}:
    {% for key, value in config.items() %}
    {{ key }}: {{ value }}
    {% endfor %}
  {% endfor %}

{% if env %}
env:
  {% for key, value in env.items() %}
  {{ key }}: {{ value }}
  {% endfor %}
{% endif %}

jobs:
  {% for job_name, job_config in jobs.items() %}
  {{ job_name }}:
    runs-on: {{ job_config.get('runs-on', 'ubuntu-latest') }}
    {% if job_config.get('needs') %}
    needs: {{ job_config.needs }}
    {% endif %}
    steps:
    {% for step in job_config.steps %}
    - {% if step.get('name') %}name: {{ step.name }}
      {% endif %}{% if step.get('uses') %}uses: {{ step.uses }}{% endif %}{% if step.get('run') %}run: {{ step.run }}{% endif %}
      {% if step.get('with') %}
      with:
        {% for key, value in step.with.items() %}
        {{ key }}: {{ value }}
        {% endfor %}
      {% endif %}
    {% endfor %}
  {% endfor %}
EOF

cat > ${SERVICE_DIR}/app/templates/cicd/Jenkinsfile.j2 << 'EOF'
pipeline {
    agent {{ agent }}
    
    {% if environment %}
    environment {
        {% for key, value in environment.items() %}
        {{ key }} = '{{ value }}'
        {% endfor %}
    }
    {% endif %}
    
    {% if options %}
    options {
        {% for option in options %}
        {{ option }}
        {% endfor %}
    }
    {% endif %}
    
    stages {
        {% for stage in stages %}
        stage('{{ stage.name }}') {
            steps {
                {% for step in stage.steps %}
                {{ step }}
                {% endfor %}
            }
        }
        {% endfor %}
    }
    
    post {
        always {
            echo 'Pipeline completed'
        }
        success {
            echo 'Pipeline succeeded'
        }
        failure {
            echo 'Pipeline failed'
        }
    }
}
EOF

cat > ${SERVICE_DIR}/app/templates/cicd/gitlab-ci.yml.j2 << 'EOF'
stages:
  {% for stage in stages %}
  - {{ stage }}
  {% endfor %}

{% if variables %}
variables:
  {% for key, value in variables.items() %}
  {{ key }}: "{{ value }}"
  {% endfor %}
{% endif %}

{% for job_name, job_config in jobs.items() %}
{{ job_name }}:
  stage: {{ job_config.stage }}
  {% if job_config.get('image') %}
  image: {{ job_config.image }}
  {% endif %}
  {% if job_config.get('before_script') %}
  before_script:
    {% for cmd in job_config.before_script %}
    - {{ cmd }}
    {% endfor %}
  {% endif %}
  script:
    {% for cmd in job_config.script %}
    - {{ cmd }}
    {% endfor %}
  {% if job_config.get('artifacts') %}
  artifacts:
    {% for key, value in job_config.artifacts.items() %}
    {{ key }}: {{ value }}
    {% endfor %}
  {% endif %}
  {% if job_config.get('only') %}
  only:
    {% for item in job_config.only %}
    - {{ item }}
    {% endfor %}
  {% endif %}
{% endfor %}
EOF

# Git templates
cat > ${SERVICE_DIR}/app/templates/git/README.md.j2 << 'EOF'
# {{ project_name }}

{{ description }}

## Getting Started

### Prerequisites

- Node.js 18+
- Docker
- Kubernetes (optional)

### Installation

```bash
# Clone the repository
git clone <repository-url>

# Install dependencies
npm install

# Run locally
npm start
```

### Development

```bash
# Run in development mode
npm run dev

# Run tests
npm test

# Build for production
npm run build
```

### Deployment

```bash
# Build Docker image
docker build -t {{ project_name }} .

# Run with Docker
docker run -p 8080:8080 {{ project_name }}

# Deploy to Kubernetes
kubectl apply -f k8s/
```

## Project Structure

```
.
├── src/              # Source code
├── tests/            # Test files
├── docker/           # Docker configuration
├── k8s/              # Kubernetes manifests
├── .github/          # GitHub Actions workflows
└── README.md         # This file
```

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the {{ license }} License - see the LICENSE file for details.
EOF

# Git gitignore templates
cat > ${SERVICE_DIR}/app/templates/git/gitignore/node.gitignore.j2 << 'EOF'
# Dependencies
node_modules/
jspm_packages/

# Testing
coverage/
.nyc_output

# Production
build/
dist/

# Logs
logs
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
lerna-debug.log*

# Environment variables
.env
.env.local
.env.development.local
.env.test.local
.env.production.local

# IDE
.idea
.vscode
*.swp
*.swo
*~
.project
.classpath
.c9/

# OS
.DS_Store
.DS_Store?
._*
.Spotlight-V100
.Trashes
ehthumbs.db
Thumbs.db

# Optional npm cache directory
.npm

# Optional eslint cache
.eslintcache

# Optional REPL history
.node_repl_history

# Output of 'npm pack'
*.tgz

# Yarn Integrity file
.yarn-integrity

# dotenv environment variables file
.env
.env.test

# parcel-bundler cache
.cache

# Next.js build output
.next

# Nuxt.js build / generate output
.nuxt
dist

# Gatsby files
.cache/
public

# vuepress build output
.vuepress/dist

# Serverless directories
.serverless/

# FuseBox cache
.fusebox/

# DynamoDB Local files
.dynamodb/

# TernJS port file
.tern-port
EOF

cat > ${SERVICE_DIR}/app/templates/git/gitignore/python.gitignore.j2 << 'EOF'
# Byte-compiled / optimized / DLL files
__pycache__/
*.py[cod]
*$py.class

# C extensions
*.so

# Distribution / packaging
.Python
build/
develop-eggs/
dist/
downloads/
eggs/
.eggs/
lib/
lib64/
parts/
sdist/
var/
wheels/
pip-wheel-metadata/
share/python-wheels/
*.egg-info/
.installed.cfg
*.egg
MANIFEST

# PyInstaller
*.manifest
*.spec

# Installer logs
pip-log.txt
pip-delete-this-directory.txt

# Unit test / coverage reports
htmlcov/
.tox/
.nox/
.coverage
.coverage.*
.cache
nosetests.xml
coverage.xml
*.cover
*.py,cover
.hypothesis/
.pytest_cache/

# Translations
*.mo
*.pot

# Django stuff:
*.log
local_settings.py
db.sqlite3
db.sqlite3-journal

# Flask stuff:
instance/
.webassets-cache

# Scrapy stuff:
.scrapy

# Sphinx documentation
docs/_build/

# PyBuilder
target/

# Jupyter Notebook
.ipynb_checkpoints

# IPython
profile_default/
ipython_config.py

# pyenv
.python-version

# pipenv
Pipfile.lock

# PEP 582
__pypackages__/

# Celery stuff
celerybeat-schedule
celerybeat.pid

# SageMath parsed files
*.sage.py

# Environments
.env
.venv
env/
venv/
ENV/
env.bak/
venv.bak/

# Spyder project settings
.spyderproject
.spyproject

# Rope project settings
.ropeproject

# mkdocs documentation
/site

# mypy
.mypy_cache/
.dmypy.json
dmypy.json

# Pyre type checker
.pyre/

# IDE
.idea/
.vscode/
*.swp
*.swo
*~
EOF

cat > ${SERVICE_DIR}/app/templates/git/gitignore/java.gitignore.j2 << 'EOF'
# Compiled class file
*.class

# Log file
*.log

# BlueJ files
*.ctxt

# Mobile Tools for Java (J2ME)
.mtj.tmp/

# Package Files #
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# virtual machine crash logs
hs_err_pid*

# Gradle
.gradle
**/build/
!src/**/build/
gradle-app.setting
!gradle-wrapper.jar
.gradletasknamecache

# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties
.mvn/wrapper/maven-wrapper.jar

# IntelliJ IDEA
.idea
*.iws
*.iml
*.ipr
out/
!**/src/main/**/out/
!**/src/test/**/out/

# Eclipse
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache
bin/
!**/src/main/**/bin/
!**/src/test/**/bin/

# NetBeans
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/

# VS Code
.vscode/

# OS
.DS_Store
EOF

# Terraform templates
cat > ${SERVICE_DIR}/app/templates/terraform/main.tf.j2 << 'EOF'
# Terraform configuration
terraform {
  required_version = ">= 1.0"
  
  required_providers {
    {% for provider in providers %}
    {{ provider.name }} = {
      source  = "hashicorp/{{ provider.name }}"
      version = "{{ provider.version }}"
    }
    {% endfor %}
  }
}

# Provider configuration
{% for provider in providers %}
provider "{{ provider.name }}" {
  {% for key, value in provider.configuration.items() %}
  {{ key }} = "{{ value }}"
  {% endfor %}
}
{% endfor %}

# Resources
{% for resource in resources %}
resource "{{ resource.type }}" "{{ resource.name }}" {
  {% for key, value in resource.properties.items() %}
  {{ key }} = {{ value | tojson }}
  {% endfor %}
}
{% endfor %}
EOF

cat > ${SERVICE_DIR}/app/templates/terraform/variables.tf.j2 << 'EOF'
# Variables definition
{% for var in variables %}
variable "{{ var.name }}" {
  type        = {{ var.type }}
  {% if var.default is defined %}
  default     = {{ var.default | tojson }}
  {% endif %}
  {% if var.description %}
  description = "{{ var.description }}"
  {% endif %}
}
{% endfor %}
EOF

cat > ${SERVICE_DIR}/app/templates/terraform/outputs.tf.j2 << 'EOF'
# Outputs definition
{% for output in outputs %}
output "{{ output.name }}" {
  value       = {{ output.value }}
  {% if output.description %}
  description = "{{ output.description }}"
  {% endif %}
  {% if output.sensitive %}
  sensitive   = {{ output.sensitive | lower }}
  {% endif %}
}
{% endfor %}
EOF

# Monitoring templates
cat > ${SERVICE_DIR}/app/templates/monitoring/fluentd.conf.j2 << 'EOF'
# Fluentd configuration
<source>
  @type forward
  port 24224
  bind 0.0.0.0
</source>

<source>
  @type tail
  path /var/log/containers/*.log
  pos_file /var/log/fluentd-containers.log.pos
  tag kubernetes.*
  read_from_head true
  <parse>
    @type json
    time_format %Y-%m-%dT%H:%M:%S.%NZ
  </parse>
</source>

<filter kubernetes.**>
  @type kubernetes_metadata
  @id filter_kube_metadata
</filter>

<match **>
  @type elasticsearch
  host elasticsearch
  port 9200
  logstash_format true
  logstash_prefix kubernetes
  <buffer>
    @type file
    path /var/log/fluentd-buffers/kubernetes.system.buffer
    flush_mode interval
    retry_type exponential_backoff
    flush_thread_count 2
    flush_interval 5s
    retry_forever
    retry_max_interval 30
    chunk_limit_size 2M
    queue_limit_length 8
    overflow_action block
  </buffer>
</match>
EOF

cat > ${SERVICE_DIR}/app/templates/monitoring/fluent-bit.conf.j2 << 'EOF'
[SERVICE]
    Flush        5
    Daemon       Off
    Log_Level    info
    Parsers_File parsers.conf

[INPUT]
    Name              tail
    Path              /var/log/containers/*.log
    Parser            docker
    Tag               kube.*
    Refresh_Interval  5
    Mem_Buf_Limit     5MB
    Skip_Long_Lines   On

[FILTER]
    Name                kubernetes
    Match               kube.*
    Kube_URL            https://kubernetes.default.svc:443
    Kube_CA_File        /var/run/secrets/kubernetes.io/serviceaccount/ca.crt
    Kube_Token_File     /var/run/secrets/kubernetes.io/serviceaccount/token
    Merge_Log           On
    K8S-Logging.Parser  On
    K8S-Logging.Exclude On

[OUTPUT]
    Name            es
    Match           *
    Host            elasticsearch
    Port            9200
    Logstash_Format On
    Logstash_Prefix kubernetes
    Retry_Limit     False
    Type            _doc
EOF

echo "✅ Step 5 Complete: Templates created"
echo "📝 Created templates for:"
echo "   - Docker (Dockerfile, dockerignore)"
echo "   - Kubernetes (YAML manifests)"
echo "   - CI/CD (GitHub Actions, Jenkins, GitLab CI)"
echo "   - Git (README, gitignore)"
echo "   - Terraform (HCL files)"
echo "   - Monitoring (Fluentd, Fluent-bit)"