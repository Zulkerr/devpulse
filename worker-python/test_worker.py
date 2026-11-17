"""Tests for the DevPulse Worker.

All HTTP calls are mocked so the suite runs green without a live
Java backend or reachable target URLs.
"""

from unittest.mock import patch

import httpx
import pytest
from fastapi.testclient import TestClient

import main
from main import app


def _resp(json_data=None, status_code=200):
    """Build a minimal httpx.Response-like object."""
    return httpx.Response(status_code, json=json_data or {})


@pytest.fixture
def client():
    return TestClient(app)


# --------------------------------------------------------------------------- #
# Health
# --------------------------------------------------------------------------- #
def test_health(client):
    resp = client.get("/health")
    assert resp.status_code == 200
    assert resp.json()["status"] == "ok"


# --------------------------------------------------------------------------- #
# check-all: everything UP
# --------------------------------------------------------------------------- #
def test_check_all_services_up(client):
    services = [{"id": 1, "url": "http://example.com"}, {"id": 2, "url": "http://other.com"}]

    with patch("main.httpx.get") as mock_get, patch("main.httpx.patch") as mock_patch:
        # First call -> backend list, subsequent calls -> URL pings
        mock_get.side_effect = [_resp(services), _resp({}, 200), _resp({}, 200)]
        mock_patch.return_value = _resp()

        resp = client.post("/check-all")

    assert resp.status_code == 200
    body = resp.json()
    assert len(body) == 2
    assert all(item["status"] == "UP" for item in body)
    assert mock_patch.call_count == 2


# --------------------------------------------------------------------------- #
# check-all: service DOWN -> incident created
# --------------------------------------------------------------------------- #
def test_check_all_service_down_creates_incident(client):
    services = [{"id": 5, "url": "http://down.example.com"}]

    with patch("main.httpx.get") as mock_get, \
         patch("main.httpx.patch") as mock_patch, \
         patch("main.httpx.post") as mock_post:
        mock_get.side_effect = [_resp(services), httpx.ConnectError("connection refused")]
        mock_patch.return_value = _resp()
        mock_post.return_value = _resp()

        resp = client.post("/check-all")

    assert resp.status_code == 200
    body = resp.json()
    assert body[0]["status"] == "DOWN"
    assert body[0]["error"] is not None
    mock_post.assert_called_once()  # incident created
    args, _ = mock_post.call_args
    assert args[0].endswith("/api/incidents")


# --------------------------------------------------------------------------- #
# check-all: backend unreachable -> 502
# --------------------------------------------------------------------------- #
def test_check_all_backend_unreachable(client):
    with patch("main.httpx.get", side_effect=httpx.ConnectError("boom")):
        resp = client.post("/check-all")
    assert resp.status_code == 502