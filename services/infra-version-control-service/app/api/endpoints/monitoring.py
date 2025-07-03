"""Monitoring configuration API endpoints"""
from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any
from app.schemas.base import BaseResponse
from app.schemas.monitoring import MonitoringRequest, PrometheusConfig, GrafanaDashboard, AlertRule
from app.services.monitoring_service import monitoring_service
from app.services.redis_service import redis_service

router = APIRouter()

@router.post("/setup", response_model=BaseResponse)
async def setup_monitoring(request: MonitoringRequest):
    """Generate complete monitoring setup"""
    try:
        monitoring_files = []
        
        # Generate Prometheus configuration
        if "prometheus" in request.monitoring_stack:
            scrape_configs = monitoring_service.generate_default_scrape_configs(
                request.metrics_endpoints
            )
            
            prometheus_config = PrometheusConfig(
                scrape_configs=scrape_configs
            )
            
            prometheus_yaml = monitoring_service.generate_prometheus_config(prometheus_config)
            monitoring_files.append({
                "name": "prometheus.yml",
                "content": prometheus_yaml
            })
            
            # Generate alert rules
            alert_rules = [
                AlertRule(
                    name="HighCPUUsage",
                    expression='rate(process_cpu_seconds_total[5m]) > 0.8',
                    duration="5m",
                    labels={"severity": "warning"},
                    annotations={
                        "summary": "High CPU usage detected",
                        "description": "CPU usage is above 80% for more than 5 minutes"
                    }
                ),
                AlertRule(
                    name="HighMemoryUsage",
                    expression='(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) > 0.9',
                    duration="5m",
                    labels={"severity": "critical"},
                    annotations={
                        "summary": "High memory usage detected",
                        "description": "Memory usage is above 90% for more than 5 minutes"
                    }
                )
            ]
            
            alert_rules_yaml = monitoring_service.generate_alert_rules(alert_rules)
            monitoring_files.append({
                "name": "alerts.yml",
                "content": alert_rules_yaml
            })
            
        # Generate Grafana dashboards
        if "grafana" in request.monitoring_stack:
            dashboard = GrafanaDashboard(
                title=f"{request.project_id} Overview",
                panels=[
                    {
                        "title": "CPU Usage",
                        "type": "graph",
                        "targets": [{"expr": "rate(process_cpu_seconds_total[5m])"}]
                    },
                    {
                        "title": "Memory Usage",
                        "type": "graph",
                        "targets": [{"expr": "process_resident_memory_bytes"}]
                    },
                    {
                        "title": "HTTP Request Rate",
                        "type": "graph",
                        "targets": [{"expr": "rate(http_requests_total[5m])"}]
                    }
                ]
            )
            
            dashboard_json = monitoring_service.generate_grafana_dashboard(dashboard)
            monitoring_files.append({
                "name": "dashboard.json",
                "content": dashboard_json
            })
            
        # Generate logging configuration
        if request.log_aggregation:
            logging_config = monitoring_service.generate_logging_config("fluentd")
            monitoring_files.append({
                "name": "fluentd.conf",
                "content": logging_config
            })
            
        # Cache the result
        cache_key = f"monitoring:{request.project_id}"
        await redis_service.set(cache_key, monitoring_files)
        
        return BaseResponse(
            success=True,
            message="Monitoring configuration generated successfully",
            data={
                "files": monitoring_files,
                "stack": request.monitoring_stack,
                "features": {
                    "metrics": True,
                    "logging": request.log_aggregation,
                    "tracing": request.tracing,
                    "alerting": len(request.alerting_channels) > 0
                }
            }
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/metrics/endpoints", response_model=BaseResponse)
async def get_metrics_endpoints():
    """Get recommended metrics endpoints for services"""
    try:
        endpoints = {
            "spring-boot": "/actuator/prometheus",
            "express": "/metrics",
            "django": "/metrics",
            "go": "/metrics",
            "default": "/metrics"
        }
        
        return BaseResponse(
            success=True,
            message="Metrics endpoints retrieved successfully",
            data={"endpoints": endpoints}
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
