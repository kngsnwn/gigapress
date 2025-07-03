"""Monitoring configuration schemas"""
from typing import Dict, List, Optional, Any
from pydantic import BaseModel

class PrometheusConfig(BaseModel):
    global_config: Dict[str, Any] = {
        "scrape_interval": "15s",
        "evaluation_interval": "15s"
    }
    scrape_configs: List[Dict[str, Any]] = []
    alerting: Optional[Dict[str, Any]] = None
    rule_files: List[str] = []

class GrafanaDashboard(BaseModel):
    title: str
    panels: List[Dict[str, Any]]
    variables: List[Dict[str, Any]] = []
    time: Dict[str, str] = {"from": "now-6h", "to": "now"}

class AlertRule(BaseModel):
    name: str
    expression: str
    duration: str = "5m"
    labels: Dict[str, str] = {}
    annotations: Dict[str, str] = {}

class MonitoringRequest(BaseModel):
    project_id: str
    monitoring_stack: List[str] = ["prometheus", "grafana"]
    metrics_endpoints: List[str] = []
    log_aggregation: bool = True
    tracing: bool = True
    alerting_channels: List[str] = ["email", "slack"]
