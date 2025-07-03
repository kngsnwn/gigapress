"""Monitoring service for observability setup"""
from typing import Dict, Any, List
import yaml
from app.services.template_service import template_service
from app.schemas.monitoring import PrometheusConfig, GrafanaDashboard, AlertRule
from app.core.logging import setup_logging

logger = setup_logging()

class MonitoringService:
    def generate_prometheus_config(self, config: PrometheusConfig) -> str:
        """Generate prometheus.yml configuration"""
        prometheus_config = {
            "global": config.global_config,
            "scrape_configs": config.scrape_configs
        }
        
        if config.alerting:
            prometheus_config["alerting"] = config.alerting
            
        if config.rule_files:
            prometheus_config["rule_files"] = config.rule_files
            
        return yaml.dump(prometheus_config, default_flow_style=False)
        
    def generate_grafana_dashboard(self, dashboard: GrafanaDashboard) -> Dict[str, Any]:
        """Generate Grafana dashboard JSON"""
        return {
            "dashboard": {
                "title": dashboard.title,
                "panels": dashboard.panels,
                "templating": {"list": dashboard.variables},
                "time": dashboard.time,
                "timepicker": {},
                "timezone": "browser",
                "version": 1
            },
            "overwrite": True
        }
        
    def generate_alert_rules(self, rules: List[AlertRule]) -> str:
        """Generate Prometheus alert rules"""
        groups = [{
            "name": "gigapress_alerts",
            "interval": "30s",
            "rules": [
                {
                    "alert": rule.name,
                    "expr": rule.expression,
                    "for": rule.duration,
                    "labels": rule.labels,
                    "annotations": rule.annotations
                }
                for rule in rules
            ]
        }]
        
        return yaml.dump({"groups": groups}, default_flow_style=False)
        
    def generate_default_scrape_configs(self, services: List[str]) -> List[Dict[str, Any]]:
        """Generate default scrape configurations for services"""
        configs = []
        
        for service in services:
            configs.append({
                "job_name": service,
                "static_configs": [{
                    "targets": [f"{service}:9090"]
                }],
                "metrics_path": "/metrics"
            })
            
        return configs
        
    def generate_logging_config(self, log_aggregator: str = "fluentd") -> str:
        """Generate logging configuration"""
        if log_aggregator == "fluentd":
            return template_service.render_template(
                "monitoring/fluentd.conf.j2",
                {}
            )
        elif log_aggregator == "fluent-bit":
            return template_service.render_template(
                "monitoring/fluent-bit.conf.j2",
                {}
            )
        else:
            return ""

monitoring_service = MonitoringService()
